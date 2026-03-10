package com.aumReport.aum.security;

/**
 * Standardised error codes returned by the auth security layer.
 *
 * These codes appear in the ProblemDetail response body under the
 * "errorCode" property, allowing clients to distinguish failure reasons
 * and display specific messages to users.
 *
 * Mirrors the AuthError enum in the Angular frontend (auth-requirements.ts).
 */
public enum AuthErrorCode {

    /** No Authorization header or Bearer token was present in the request. */
    TOKEN_MISSING,

    /** The JWT could not be parsed — not a valid three-segment base64 structure. */
    TOKEN_MALFORMED,

    /** The JWT exp claim is in the past. */
    TOKEN_EXPIRED,

    /**
     * The JWT iss claim does not match any entry in auth.security.allowed-issuers.
     * This means the token was issued by an untrusted Cognito User Pool.
     */
    TOKEN_INVALID_ISSUER,

    /** The JWT cryptographic signature could not be verified against the JWKS. */
    TOKEN_INVALID_SIGNATURE,

    /** Token is structurally valid but does not meet access requirements (403). */
    ACCESS_DENIED,

    /** Catch-all for unexpected token validation failures. */
    TOKEN_INVALID
}
