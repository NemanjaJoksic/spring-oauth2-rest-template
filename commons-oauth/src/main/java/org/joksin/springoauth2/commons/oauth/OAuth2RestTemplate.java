package org.joksin.springoauth2.commons.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.joksin.springoauth2.commons.oauth.exception.InvalidOAuthServerResponseStatusException;
import org.joksin.springoauth2.commons.oauth.exception.UnsupportedGrantTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedList;
import java.util.Map;

public class OAuth2RestTemplate extends RestTemplate {

    public OAuth2RestTemplate(OAuth2RestTemplateConfiguration configuration) {
        super();

        if (configuration.grantType().equals(GrantType.CLIENT_CREDENTIALS)) {
            var interceptors = this.getInterceptors();
            if (CollectionUtils.isEmpty(interceptors)) {
                interceptors = new LinkedList<>();
            }

            var openIdConfiguration = fetchOpenIdConfiguration(configuration.wellKnownOpenIdConfigurationEndpoint());

            interceptors.add(new ClientCredentialsClientHttpRequestInterceptor((String) openIdConfiguration.get("token_endpoint"),
                                                                               (String) openIdConfiguration.get("jwks_uri"),
                                                                               configuration.clientId(),
                                                                               configuration.clientSecret()));

            this.setInterceptors(interceptors);
        } else {
            throw new UnsupportedGrantTypeException(configuration.grantType());
        }
    }

    @SneakyThrows
    private Map<String, Object> fetchOpenIdConfiguration(String wellKnownOpenIdConfigurationEndpoint) {
        var restTemplate = new RestTemplate();
        var objectMapper = new ObjectMapper();

        var responseEntity = restTemplate.getForEntity(wellKnownOpenIdConfigurationEndpoint, String.class);

        var statusCode = responseEntity.getStatusCode();
        if (statusCode.isSameCodeAs(HttpStatus.OK)) {
            return objectMapper.readValue(responseEntity.getBody(), new TypeReference<>() {});
        } else {
            throw new InvalidOAuthServerResponseStatusException(statusCode);
        }
    }

}
