package com.ecp.les_constructions_dominic_cyr.backend.config;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtValidators;

import java.util.List;

/**
 * Validates that a JWT was issued by the configured issuer and contains the expected audience.
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2TokenValidator<Jwt> defaultValidator;
    private final String audience;

    public AudienceValidator(String issuer, String audience) {
        this.defaultValidator = JwtValidators.createDefaultWithIssuer(issuer);
        this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        OAuth2TokenValidatorResult defaultResult = defaultValidator.validate(jwt);
        if (defaultResult.hasErrors()) {
            return defaultResult;
        }

        List<String> audiences = jwt.getAudience();
        if (audience != null && !audience.isBlank() && (audiences == null || audiences.stream().noneMatch(audience::equals))) {
            OAuth2Error error = new OAuth2Error("invalid_token", "The required audience is missing", JwtClaimNames.AUD);
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
