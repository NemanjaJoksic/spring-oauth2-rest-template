package org.joksin.springoauth2.webapp;

import org.joksin.springoauth2.commons.oauth.GrantType;
import org.joksin.springoauth2.commons.oauth.OAuth2RestTemplate;
import org.joksin.springoauth2.commons.oauth.OAuth2RestTemplateConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class OAuth2RestTemplateTest implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        var configuration = OAuth2RestTemplateConfiguration.builder()
                                                           .wellKnownOpenIdConfigurationEndpoint("http://localhost:8000/realms/master/.well-known/openid-configuration")
                                                           .clientId("test_client")
                                                           .clientSecret("YoBJEcoZ1E6TNE3SDVVpGN2cpqO4hIPE")
                                                           .grantType(GrantType.CLIENT_CREDENTIALS)
                                                           .build();

        var restTemplate = new OAuth2RestTemplate(configuration);

        var responseEntity = restTemplate.getForEntity("http://localhost:8080/api/users/token", String.class);

        if (responseEntity.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
            System.out.println(responseEntity.getBody());
        } else {
            System.out.println("Invalid response code " + responseEntity.getStatusCode().value());
        }
    }

}
