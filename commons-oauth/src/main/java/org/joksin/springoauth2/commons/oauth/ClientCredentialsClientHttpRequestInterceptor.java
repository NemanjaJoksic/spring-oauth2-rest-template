package org.joksin.springoauth2.commons.oauth;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.joksin.springoauth2.commons.oauth.exception.InvalidOAuthServerResponseStatusException;
import org.joksin.springoauth2.commons.oauth.exception.UnsupportedAlgorithmException;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
class ClientCredentialsClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String RS256_ALGORITHM = "RS256";

    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String ACCESS_TOKEN = "access_token";

    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, JWTVerifier> jwtVerifiers = new ConcurrentHashMap<>();
    private final JwkProvider jwkProvider;

    private DecodedJWT jwt;

    @SneakyThrows
    public ClientCredentialsClientHttpRequestInterceptor(String tokenEndpoint, String jwksUri, String clientId, String clientSecret) {
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        this.jwkProvider = new UrlJwkProvider(new URL(jwksUri));
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        var headers = request.getHeaders();

        headers.set(AUTHORIZATION_HEADER, "Bearer " + getToken());

        return execution.execute(request, body);
    }

    private String getToken() {
        if (Objects.isNull(jwt)) {
            log.debug("There is no JWT cached");
            refreshJWT();
        } else if (LocalDateTime.ofInstant(jwt.getExpiresAtAsInstant(), ZoneOffset.UTC).isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            log.debug("Cached JWT has expired");
            refreshJWT();
        } else {
            log.debug("Use already cached JWT value");
        }

        return jwt.getToken();
    }

    @SneakyThrows
    private void refreshJWT() {
        var requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add(CLIENT_ID, clientId);
        requestBody.add(CLIENT_SECRET, clientSecret);
        requestBody.add(GRANT_TYPE, CLIENT_CREDENTIALS);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var httpEntity = new HttpEntity<>(requestBody, headers);

        var responseEntity = restTemplate.postForEntity(tokenEndpoint, httpEntity, String.class);
        var statusCode = responseEntity.getStatusCode();

        if (statusCode.isSameCodeAs(HttpStatus.OK)) {
            var responseBody = objectMapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, Object>>() {
            });

            var nonValidatedJwt = JWT.decode((String) responseBody.get(ACCESS_TOKEN));

            var jwtVerifier = jwtVerifiers.computeIfAbsent(nonValidatedJwt.getKeyId(),
                                                           keyId -> {
                                                               log.debug("JWTVerifier for key ID {} does not exists. Adding it", keyId);
                                                               return JWT.require(getAlgorithm(nonValidatedJwt)).build();
                                                           });

            jwt = jwtVerifier.verify(nonValidatedJwt);

            log.info("Token has been successfully fetched, validated and decoded");
        } else {
            throw new InvalidOAuthServerResponseStatusException(statusCode);
        }
    }

    @SneakyThrows
    private Algorithm getAlgorithm(DecodedJWT jwt) {
        var jwk = jwkProvider.get(jwt.getKeyId());
        var algorithm = jwk.getAlgorithm();

        if (algorithm.equals(RS256_ALGORITHM)) {
            return Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey());
        } else {
            throw new UnsupportedAlgorithmException(algorithm);
        }
    }

}
