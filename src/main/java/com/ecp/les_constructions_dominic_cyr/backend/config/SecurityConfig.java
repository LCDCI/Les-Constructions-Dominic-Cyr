package com.ecp.les_constructions_dominic_cyr.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    // Filter chain for public inquiry submission (POST/OPTIONS only) - with rate limiting
    @Bean
    @Order(0)
    public SecurityFilterChain inquiriesSubmitFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher inquiriesPostMatcher = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/inquiries", "POST"),
            new AntPathRequestMatcher("/api/inquiries", "OPTIONS")
        );
        
        http
            .securityMatcher(inquiriesPostMatcher)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.ignoringRequestMatchers(inquiriesPostMatcher))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .addFilterBefore(inquiriesRateLimitFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    // Filter chain for OWNER inquiry viewing (GET only) - requires authentication
    @Bean
    @Order(1)
    public SecurityFilterChain inquiriesOwnerFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher inquiriesGetMatcher = new OrRequestMatcher(
            new AntPathRequestMatcher("/api/inquiries", "GET"),
            new AntPathRequestMatcher("/api/inquiries/**", "GET")
        );
        
        http
            .securityMatcher(inquiriesGetMatcher)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.ignoringRequestMatchers(inquiriesGetMatcher))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().hasAuthority("ROLE_OWNER")
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthConverter())
                )
            );
        return http.build();
    }

    // Main security filter chain for all other endpoints
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // --- 1. PUBLIC ENDPOINTS (Originals + New Public Lots) ---
                        .requestMatchers("/actuator/**", "/api/theme").permitAll()
                        .requestMatchers("/api/v1/translations/**").permitAll()
                        .requestMatchers("/api/v1/residential-projects/**").permitAll()
                        .requestMatchers("/api/v1/renovations/**").permitAll()
                        .requestMatchers("/api/v1/project-management/**").permitAll()
                        .requestMatchers("/api/v1/realizations/**").permitAll()
                        .requestMatchers("/api/v1/contact/**").permitAll()
                        
                        // Public inquiry submission (POST only, handled by inquiriesSubmitFilterChain but fallback here)
                        .requestMatchers(HttpMethod.POST, "/api/inquiries").permitAll()

                        // Project Public Endpoints (From your original list)
                        .requestMatchers(HttpMethod.GET, "/api/v1/projects").permitAll()
                        .requestMatchers("/api/v1/projects/{id}/overview").permitAll()

                        // Lot Public Access (Added to prevent 401)
                        .requestMatchers(HttpMethod.GET, "/api/v1/lots/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/projects/*/lots/**").permitAll()

                        // --- 2. AUTHENTICATED USER ENDPOINTS (From your original list) ---
                        .requestMatchers("/api/v1/users/me").authenticated()
                        .requestMatchers("/api/v1/users/auth0/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").authenticated()

                        // --- 3. OWNER SPECIFIC (From your original list) ---
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/deactivate").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/inactive").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/reactivate").hasAuthority("ROLE_OWNER")
                        .requestMatchers("/api/v1/owners/**").hasAuthority("ROLE_OWNER")
                        .requestMatchers("/api/v1/users/**").hasAuthority("ROLE_OWNER")

                        // Task updates (allow contractor and owner) — placed before the broader owners/** rule
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/owners/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/owners/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")

                        // Lot Management (Original permissions, just moved below public GET)
                        .requestMatchers(HttpMethod.POST, "/api/v1/lots").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/lots/**").hasAuthority("ROLE_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/lots/**").hasAuthority("ROLE_OWNER")

                        // Task Management - Allow viewing, but only owners can delete via /owners/tasks endpoint
                        .requestMatchers(HttpMethod.GET, "/api/v1/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tasks/**").hasAuthority("ROLE_OWNER")

                        // --- 4. OTHER ROLES (From your original list) ---
                        .requestMatchers("/api/v1/projects/**").hasAnyAuthority("ROLE_CONTRACTOR", "ROLE_SALESPERSON", "ROLE_OWNER", "ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/schedules/*/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/schedules/*/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/schedules/*/tasks/**").hasAnyAuthority("ROLE_OWNER", "ROLE_CONTRACTOR")
                        .requestMatchers("/api/v1/contractors/**").hasAuthority("ROLE_CONTRACTOR")
                        .requestMatchers("/api/v1/salesperson/**").hasAuthority("ROLE_SALESPERSON")
                        .requestMatchers("/api/v1/customers/**").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers("/api/v1/reports/**").hasAuthority("ROLE_OWNER")
                        // --- 5. CATCH-ALL ---
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthConverter())
                        )
                );


        return http.build();
    }

    @Bean
    public InquiriesRateLimitFilter inquiriesRateLimitFilter() {
        return new InquiriesRateLimitFilter();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwksUri = String.format("https://%s/.well-known/jwks.json", auth0Domain);

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new AudienceValidator(issuer, audience);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience);

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // THIS MUST MATCH THE NAMESPACE IN YOUR AUTH0 ACTION
        grantedAuthoritiesConverter.setAuthoritiesClaimName("https://construction-api.loca/roles");

        // This adds "ROLE_" to the front, turning "OWNER" into "ROLE_OWNER"
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return authConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public HttpFirewall allowSpecialCharactersFirewall() {
        return new DefaultHttpFirewall();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(allowSpecialCharactersFirewall());
    }
}