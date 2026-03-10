package com.aumReport.aum.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Global CORS configuration.
 *
 * Allows the Angular frontend to call the Spring Boot API from any of the
 * environments where the main application is hosted (prod / qa / stg / dev / local).
 *
 * TWO beans are provided:
 *  1. addCorsMappings()          — used by Spring MVC (WebMvcConfigurer)
 *  2. corsConfigurationSource()  — used by Spring Security's filter chain
 *                                  (SecurityConfig calls .cors(Customizer.withDefaults())
 *                                   which looks for this bean by type)
 *
 * Both are kept in sync — only edit the ALLOWED_ORIGINS / ALLOWED_HEADERS lists below.
 *
 * SETUP: Replace the placeholder origin URLs with real deployment URLs.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final List<String> ALLOWED_ORIGINS = List.of(
            // Local development
            "http://localhost:4200",
            // Production — replace with real URL
            "https://app.prod.example.com",
            // Staging — replace with real URL
            "https://app.stg.example.com",
            // Development — replace with real URL
            "https://app.dev.example.com"
    );

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
    );

    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"
    );

    // ── Spring MVC CORS ───────────────────────────────────────────────────────

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new))
                .allowedMethods(ALLOWED_METHODS.toArray(String[]::new))
                .allowedHeaders(ALLOWED_HEADERS.toArray(String[]::new))
                .allowCredentials(false)
                .maxAge(3600);
    }

    // ── Spring Security CORS — must mirror the above ──────────────────────────

    /**
     * Exposes the CORS config as a bean so Spring Security's filter chain
     * (SecurityConfig → .cors(Customizer.withDefaults())) picks it up.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Registers the CorsFilter at HIGHEST_PRECEDENCE so it runs before the entire
     * Spring Security filter chain. Without this, the OAuth2 resource server filter
     * may reject OPTIONS preflight requests with a 401 that carries no CORS headers,
     * causing the browser to report a "missing Access-Control-Allow-Origin" error.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
