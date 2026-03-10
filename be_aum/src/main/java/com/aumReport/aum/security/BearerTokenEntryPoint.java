package com.aumReport.aum.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * Handles authentication failures (HTTP 401) with structured ProblemDetail responses.
 *
 * Maps each failure type to a specific {@link AuthErrorCode} so clients can
 * display actionable error messages.
 *
 * Response format (RFC 7807 ProblemDetail):
 * <pre>
 * {
 *   "type":      "/errors/auth/token-missing",
 *   "title":     "Authentication Required",
 *   "status":    401,
 *   "detail":    "No Bearer token was provided.",
 *   "errorCode": "TOKEN_MISSING",
 *   "timestamp": "2026-03-06T10:00:00Z"
 * }
 * </pre>
 */
@Slf4j
public class BearerTokenEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        AuthErrorCode errorCode = resolveErrorCode(authException);
        String detail = resolveDetail(authException, errorCode);

        log.warn("[AUTH] 401 {} — {} — path={} — cause={}",
                errorCode, detail, request.getRequestURI(), authException.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, detail);
        problem.setType(URI.create("/errors/auth/" + errorCode.name().toLowerCase().replace('_', '-')));
        problem.setTitle("Authentication Required");
        problem.setProperty("errorCode", errorCode.name());
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getWriter(), problem);
    }

    // -------------------------------------------------------------------------
    // Error code resolution
    // -------------------------------------------------------------------------

    /**
     * Maps the Spring Security exception to one of our {@link AuthErrorCode} values.
     *
     * Resolution logic:
     *  - No token at all                    → TOKEN_MISSING
     *  - OAuth2 exception (token present):
     *      description contains "expired"   → TOKEN_EXPIRED
     *      description contains "issuer"    → TOKEN_INVALID_ISSUER
     *      description contains "signature" → TOKEN_INVALID_SIGNATURE
     *      description contains "resolve"   → TOKEN_MALFORMED (unparseable iss)
     *      otherwise                        → TOKEN_INVALID
     */
    private AuthErrorCode resolveErrorCode(AuthenticationException ex) {
        if (!(ex instanceof OAuth2AuthenticationException oauthEx)) {
            // No Bearer token was present — Spring sends InsufficientAuthenticationException
            return AuthErrorCode.TOKEN_MISSING;
        }

        String description = safeDescription(oauthEx);

        if (description.contains("expired")) {
            return AuthErrorCode.TOKEN_EXPIRED;
        }
        if (description.contains("issuer") || description.contains("untrusted")) {
            return AuthErrorCode.TOKEN_INVALID_ISSUER;
        }
        if (description.contains("signature") || description.contains("verify")) {
            return AuthErrorCode.TOKEN_INVALID_SIGNATURE;
        }
        if (description.contains("resolve") || description.contains("unable to resolve")
                || description.contains("malformed")) {
            return AuthErrorCode.TOKEN_MALFORMED;
        }
        return AuthErrorCode.TOKEN_INVALID;
    }

    private String resolveDetail(AuthenticationException ex, AuthErrorCode code) {
        return switch (code) {
            case TOKEN_MISSING -> "No Bearer token was provided. Include an Authorization: Bearer <token> header.";
            case TOKEN_EXPIRED -> "The provided token has expired. Obtain a fresh token and retry.";
            case TOKEN_INVALID_ISSUER -> "The token was issued by an untrusted source. Ensure you are using an authorised environment.";
            case TOKEN_INVALID_SIGNATURE -> "The token signature could not be verified.";
            case TOKEN_MALFORMED -> "The token is malformed and cannot be parsed.";
            default -> "Authentication failed. Provide a valid Bearer token.";
        };
    }

    private String safeDescription(OAuth2AuthenticationException ex) {
        if (ex.getError() == null || ex.getError().getDescription() == null) {
            return ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        }
        return ex.getError().getDescription().toLowerCase();
    }
}
