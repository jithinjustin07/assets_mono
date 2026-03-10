package com.aumReport.aum.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration — JWT Bearer token resource server.
 *
 * WHAT THIS DOES
 * ──────────────
 * 1. Requires a valid Cognito JWT on every /api/** request.
 * 2. Validates the token against the correct Cognito User Pool JWKS endpoint,
 *    selected dynamically from the JWT `iss` claim (multi-issuer support).
 * 3. Returns RFC 7807 ProblemDetail JSON (with errorCode) on 401 / 403.
 * 4. Stateless — no sessions, no cookies.
 * 5. CSRF disabled (safe for stateless token APIs).
 * 6. CORS delegated to WebConfig (Customizer.withDefaults()).
 *
 * HOW MULTI-ISSUER WORKS
 * ──────────────────────
 * JwtIssuerAuthenticationManagerResolver peeks at the `iss` claim without
 * verifying the signature first. It looks up whether that issuer is in the
 * configured allowed-issuers list. If it is, it fetches the JWKS from:
 *   https://cognito-idp.{region}.amazonaws.com/{poolId}/.well-known/jwks.json
 * and fully verifies the signature + claims. JWKS are cached after first fetch.
 * If the issuer is unknown, it rejects with TOKEN_INVALID_ISSUER.
 *
 * REUSE
 * ─────
 * Copy the security/ package into any Spring Boot app. The only app-specific
 * configuration is the allowed-issuers list in application.yml.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Main security filter chain.
     *
     * All /api/** routes require authentication.
     * The /actuator/health endpoint is public (for load balancer health checks).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    AuthSecurityProperties authProps) throws Exception {

        JwtIssuerAuthenticationManagerResolver multiIssuerResolver =
                JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(
                        authProps.getAllowedIssuers()
                );

        http
                // ── Session & CSRF ────────────────────────────────────────────────
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // ── CORS — delegates to WebConfig.addCorsMappings() ──────────────
                .cors(Customizer.withDefaults())

                // ── Route authorisation ───────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight — must be permitted before auth runs
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public endpoints (add more as needed)
                        .requestMatchers("/actuator/health").permitAll()
                        // All other routes require a valid token
                        .anyRequest().authenticated()
                )

                // ── JWT resource server — multi-issuer ────────────────────────────
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(multiIssuerResolver)
                        .authenticationEntryPoint(new BearerTokenEntryPoint())
                )

                // ── 403 handler ───────────────────────────────────────────────────
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(new AuthAccessDeniedHandler())
                );

        return http.build();
    }
}
