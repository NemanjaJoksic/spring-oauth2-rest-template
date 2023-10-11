package org.joksin.springoauth2.commons.oauth;

import lombok.Builder;

@Builder
public record OAuth2RestTemplateConfiguration(
        String wellKnownOpenIdConfigurationEndpoint,
        String clientId,
        String clientSecret,
        GrantType grantType
) {}
