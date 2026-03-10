package com.aumReport.aum.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================================
 * AUTH SECURITY REQUIREMENTS  (backend equivalent of auth-requirements.ts)
 * ============================================================================
 * Configuration contract for the reusable JWT Bearer token security layer.
 *
 * WHAT THIS CLASS DEFINES
 * ────────────────────────
 * The set of Cognito User Pools whose tokens this application will accept.
 * Each entry is a full issuer URL matching the `iss` claim in the JWT.
 *
 * WHAT THIS CLASS DOES NOT DEFINE
 * ────────────────────────────────
 * ✗ Real pool URLs — those live in application.yml / environment config.
 * ✗ Any validation logic — that is handled by SecurityConfig + NimbusJwtDecoder.
 *
 * HOW TO CONFIGURE
 * ─────────────────
 * In application.yml (or application-{env}.yml):
 *
 *   auth:
 *     security:
 *       allowed-issuers:
 *         - "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_YourPoolId"
 *
 * Add one entry per Cognito User Pool (prod, qa, stg, dev, local).
 * The order does not matter. Tokens from pools NOT listed here are rejected
 * with TOKEN_INVALID_ISSUER.
 *
 * REUSE
 * ─────
 * Copy the entire com.avestarone.billing.security package into any Spring Boot
 * application. Add the two dependencies to pom.xml (spring-boot-starter-security
 * and spring-boot-starter-oauth2-resource-server) and set the allowed-issuers
 * property. No other changes are required.
 *
 * See backend-security-integration-guide.md for the full setup checklist.
 * ============================================================================
 */
@ConfigurationProperties(prefix = "auth.security")
public class AuthSecurityProperties {

    /**
     * The list of trusted Cognito issuer URLs.
     *
     * Format: https://cognito-idp.{region}.amazonaws.com/{userPoolId}
     *
     * The value must match exactly what Cognito puts in the JWT `iss` claim.
     * You can verify the correct value by decoding any token from that pool.
     *
     * Example:
     *   "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_LY7jo7QDt"
     */
    private List<String> allowedIssuers = new ArrayList<>();

    public List<String> getAllowedIssuers() {
        return allowedIssuers;
    }

    public void setAllowedIssuers(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers;
    }
}
