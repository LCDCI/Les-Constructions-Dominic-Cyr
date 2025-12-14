package com.ecp.les_constructions_dominic_cyr.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials for authenticated requests
        config.setAllowCredentials(true);
        
        // Allow specific origins - including proxy scenarios
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:80",
                "http://localhost",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:3001",
                "http://127.0.0.1:80",
                "http://127.0.0.1"
        ));
        
        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow all common HTTP methods including OPTIONS for preflight
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
        
        // Expose all response headers
        config.setExposedHeaders(Arrays.asList("*"));
        
        // Apply CORS configuration to all /api/** paths
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}