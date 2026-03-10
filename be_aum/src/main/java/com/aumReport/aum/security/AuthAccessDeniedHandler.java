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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;


import java.io.IOException;
import java.net.URI;
import java.time.Instant;

/**
 * Handles authorisation failures (HTTP 403) with structured ProblemDetail responses.
 *
 * A 403 means the request carried a valid, authenticated token but the principal
 * does not have permission to access the requested resource.
 *
 * Response format (RFC 7807 ProblemDetail):
 * <pre>
 * {
 *   "type":      "/errors/auth/access-denied",
 *   "title":     "Access Denied",
 *   "status":    403,
 *   "detail":    "You do not have permission to access this resource.",
 *   "errorCode": "ACCESS_DENIED",
 *   "timestamp": "2026-03-06T10:00:00Z"
 * }
 * </pre>
 */
@Slf4j
public class AuthAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        log.warn("[AUTH] 403 ACCESS_DENIED — path={} — principal={}",
                request.getRequestURI(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "unknown");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource."
        );
        problem.setType(URI.create("/errors/auth/access-denied"));
        problem.setTitle("Access Denied");
        problem.setProperty("errorCode", AuthErrorCode.ACCESS_DENIED.name());
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MAPPER.writeValue(response.getWriter(), problem);
    }
}
