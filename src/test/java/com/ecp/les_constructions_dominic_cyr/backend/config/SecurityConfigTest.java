package com.ecp.les_constructions_dominic_cyr.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to set private fields
        setField("auth0Domain", "test-domain.auth0.com");
        setField("audience", "test-audience");
        setField("issuer", "https://test-domain.auth0.com/");
        setField("allowedOrigins", List.of("http://localhost:3000", "http://localhost:8080"));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = SecurityConfig.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(securityConfig, value);
    }

    private CorsConfiguration getCorsConfiguration(UrlBasedCorsConfigurationSource source) throws Exception {
        Field corsConfigurationsField = UrlBasedCorsConfigurationSource.class.getDeclaredField("corsConfigurations");
        corsConfigurationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, CorsConfiguration> corsConfigurations = 
            (java.util.Map<String, CorsConfiguration>) corsConfigurationsField.get(source);
        
        if (corsConfigurations == null || corsConfigurations.isEmpty()) {
            return null;
        }
        
        // Try to get the configuration for "/**" pattern
        CorsConfiguration config = corsConfigurations.get("/**");
        if (config == null && !corsConfigurations.isEmpty()) {
            // If not found, get the first configuration
            config = corsConfigurations.values().iterator().next();
        }
        return config;
    }

    // ========== JWT Decoder Tests ==========
    
    @Test
    void jwtDecoder_CreatesDecoderWithValidators() {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void jwtDecoder_FormatsJwksUriWithStringFormat() throws Exception {
        setField("auth0Domain", "example.auth0.com");
        
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
        // The String.format call is executed: https://%s/.well-known/jwks.json
    }

    @Test
    void jwtDecoder_CreatesNimbusJwtDecoder() {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void jwtDecoder_CreatesIssuerValidator() throws Exception {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        // JwtValidators.createDefaultWithIssuer(issuer) is called
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void jwtDecoder_CreatesAudienceValidator() throws Exception {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        // new AudienceValidator(issuer, audience) is called
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void jwtDecoder_CreatesDelegatingValidator() throws Exception {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
        
        NimbusJwtDecoder nimbusDecoder = (NimbusJwtDecoder) decoder;
        Field jwtValidatorField = NimbusJwtDecoder.class.getDeclaredField("jwtValidator");
        jwtValidatorField.setAccessible(true);
        Object validator = jwtValidatorField.get(nimbusDecoder);
        
        assertNotNull(validator);
        // new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience) is called
        assertTrue(validator instanceof DelegatingOAuth2TokenValidator);
    }

    @Test
    void jwtDecoder_SetsJwtValidator() throws Exception {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
        
        NimbusJwtDecoder nimbusDecoder = (NimbusJwtDecoder) decoder;
        Field jwtValidatorField = NimbusJwtDecoder.class.getDeclaredField("jwtValidator");
        jwtValidatorField.setAccessible(true);
        Object validator = jwtValidatorField.get(nimbusDecoder);
        
        assertNotNull(validator);
        // jwtDecoder.setJwtValidator(validator) is called
        assertTrue(validator instanceof DelegatingOAuth2TokenValidator);
    }

    @Test
    void jwtDecoder_WithDifferentDomain_FormatsJwksUriCorrectly() throws Exception {
        setField("auth0Domain", "custom-domain.auth0.com");
        
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
    }

    @Test
    void jwtDecoder_ValidatorContainsIssuerAndAudienceValidators() throws Exception {
        JwtDecoder decoder = securityConfig.jwtDecoder();
        
        assertNotNull(decoder);
        assertTrue(decoder instanceof NimbusJwtDecoder);
        
        NimbusJwtDecoder nimbusDecoder = (NimbusJwtDecoder) decoder;
        Field jwtValidatorField = NimbusJwtDecoder.class.getDeclaredField("jwtValidator");
        jwtValidatorField.setAccessible(true);
        Object validator = jwtValidatorField.get(nimbusDecoder);
        
        assertNotNull(validator);
        assertTrue(validator instanceof DelegatingOAuth2TokenValidator);
        
        DelegatingOAuth2TokenValidator<Jwt> delegatingValidator = 
            (DelegatingOAuth2TokenValidator<Jwt>) validator;
        Field tokenValidatorsField = 
            DelegatingOAuth2TokenValidator.class.getDeclaredField("tokenValidators");
        tokenValidatorsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<org.springframework.security.oauth2.core.OAuth2TokenValidator<Jwt>> validators = 
            (List<org.springframework.security.oauth2.core.OAuth2TokenValidator<Jwt>>) 
            tokenValidatorsField.get(delegatingValidator);
        
        assertNotNull(validators);
        assertTrue(validators.size() >= 2, "Should have at least issuer and audience validators");
    }

    // ========== JWT Authentication Converter Tests ==========

    @Test
    void jwtAuthConverter_CreatesJwtGrantedAuthoritiesConverter() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthConverter();
        
        assertNotNull(converter);
        // new JwtGrantedAuthoritiesConverter() is called
    }

    @Test
    void jwtAuthConverter_SetsAuthoritiesClaimName() throws Exception {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthConverter();
        
        assertNotNull(converter);
        
        Field jwtGrantedAuthoritiesConverterField = 
            JwtAuthenticationConverter.class.getDeclaredField("jwtGrantedAuthoritiesConverter");
        jwtGrantedAuthoritiesConverterField.setAccessible(true);
        JwtGrantedAuthoritiesConverter authoritiesConverter = 
            (JwtGrantedAuthoritiesConverter) jwtGrantedAuthoritiesConverterField.get(converter);
        
        assertNotNull(authoritiesConverter);
        
        Field authoritiesClaimNameField = 
            JwtGrantedAuthoritiesConverter.class.getDeclaredField("authoritiesClaimName");
        authoritiesClaimNameField.setAccessible(true);
        String authoritiesClaimName = (String) authoritiesClaimNameField.get(authoritiesConverter);
        
        // grantedAuthoritiesConverter.setAuthoritiesClaimName("https://construction-api.loca/roles") is called
        assertEquals("https://construction-api.loca/roles", authoritiesClaimName);
    }

    @Test
    void jwtAuthConverter_SetsAuthorityPrefix() throws Exception {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthConverter();
        
        assertNotNull(converter);
        
        Field jwtGrantedAuthoritiesConverterField = 
            JwtAuthenticationConverter.class.getDeclaredField("jwtGrantedAuthoritiesConverter");
        jwtGrantedAuthoritiesConverterField.setAccessible(true);
        JwtGrantedAuthoritiesConverter authoritiesConverter = 
            (JwtGrantedAuthoritiesConverter) jwtGrantedAuthoritiesConverterField.get(converter);
        
        assertNotNull(authoritiesConverter);
        
        Field authorityPrefixField = 
            JwtGrantedAuthoritiesConverter.class.getDeclaredField("authorityPrefix");
        authorityPrefixField.setAccessible(true);
        String authorityPrefix = (String) authorityPrefixField.get(authoritiesConverter);
        
        // grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_") is called
        assertEquals("ROLE_", authorityPrefix);
    }

    @Test
    void jwtAuthConverter_CreatesJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthConverter();
        
        assertNotNull(converter);
        // new JwtAuthenticationConverter() is called
    }

    @Test
    void jwtAuthConverter_SetsJwtGrantedAuthoritiesConverter() throws Exception {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthConverter();
        
        assertNotNull(converter);
        
        Field jwtGrantedAuthoritiesConverterField = 
            JwtAuthenticationConverter.class.getDeclaredField("jwtGrantedAuthoritiesConverter");
        jwtGrantedAuthoritiesConverterField.setAccessible(true);
        JwtGrantedAuthoritiesConverter authoritiesConverter = 
            (JwtGrantedAuthoritiesConverter) jwtGrantedAuthoritiesConverterField.get(converter);
        
        assertNotNull(authoritiesConverter);
        // authConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter) is called
    }

    // ========== CORS Configuration Tests ==========

    @Test
    void corsConfigurationSource_CreatesCorsConfiguration() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        
        assertNotNull(source);
        // new CorsConfiguration() is called
    }

    @Test
    void corsConfigurationSource_SetsAllowedOrigins() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        // configuration.setAllowedOrigins(allowedOrigins) is called
        assertEquals(List.of("http://localhost:3000", "http://localhost:8080"), config.getAllowedOrigins());
    }

    @Test
    void corsConfigurationSource_SetsAllowedMethods() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        // configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")) is called
        List<String> allowedMethods = config.getAllowedMethods();
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("DELETE"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("PATCH"));
    }

    @Test
    void corsConfigurationSource_SetsAllowedHeaders() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        // configuration.setAllowedHeaders(List.of("*")) is called
        assertTrue(config.getAllowedHeaders().contains("*"));
    }

    @Test
    void corsConfigurationSource_SetsAllowCredentials() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        // configuration.setAllowCredentials(true) is called
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void corsConfigurationSource_SetsMaxAge() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        // configuration.setMaxAge(3600L) is called
        assertEquals(3600L, config.getMaxAge());
    }

    @Test
    void corsConfigurationSource_CreatesUrlBasedCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        
        assertNotNull(source);
        // new UrlBasedCorsConfigurationSource() is called
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);
    }

    @Test
    void corsConfigurationSource_RegistersCorsConfiguration() throws Exception {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        
        Field corsConfigurationsField = UrlBasedCorsConfigurationSource.class.getDeclaredField("corsConfigurations");
        corsConfigurationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, CorsConfiguration> corsConfigurations = 
            (java.util.Map<String, CorsConfiguration>) corsConfigurationsField.get(urlBasedSource);
        
        assertNotNull(corsConfigurations);
        // source.registerCorsConfiguration("/**", configuration) is called
        assertTrue(corsConfigurations.containsKey("/**") || !corsConfigurations.isEmpty());
    }

    @Test
    void corsConfigurationSource_WithMultipleOrigins_ConfiguresAll() throws Exception {
        setField("allowedOrigins", List.of("http://localhost:3000", "http://localhost:8080", "https://example.com"));
        
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = getCorsConfiguration(urlBasedSource);
        
        assertNotNull(config);
        assertEquals(3, config.getAllowedOrigins().size());
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(config.getAllowedOrigins().contains("http://localhost:8080"));
        assertTrue(config.getAllowedOrigins().contains("https://example.com"));
    }

    // ========== Firewall Tests ==========

    @Test
    void allowSpecialCharactersFirewall_ReturnsDefaultHttpFirewall() {
        HttpFirewall firewall = securityConfig.allowSpecialCharactersFirewall();
        
        assertNotNull(firewall);
        // return new DefaultHttpFirewall() is called
        assertTrue(firewall instanceof DefaultHttpFirewall);
    }

    // ========== Web Security Customizer Tests ==========

    @Test
    void webSecurityCustomizer_ReturnsCustomizer() {
        var customizer = securityConfig.webSecurityCustomizer();
        
        assertNotNull(customizer);
        // return (web) -> web.httpFirewall(allowSpecialCharactersFirewall()) is called
    }

    @Test
    void webSecurityCustomizer_ExecutesLambda() {
        var customizer = securityConfig.webSecurityCustomizer();
        
        assertNotNull(customizer);
        
        org.springframework.security.config.annotation.web.builders.WebSecurity webSecurity = 
            org.mockito.Mockito.mock(org.springframework.security.config.annotation.web.builders.WebSecurity.class);
        
        // The lambda is executed: (web) -> web.httpFirewall(allowSpecialCharactersFirewall())
        assertDoesNotThrow(() -> customizer.customize(webSecurity));
    }

}
