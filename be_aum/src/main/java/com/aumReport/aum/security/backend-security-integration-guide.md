# Backend Auth Security Layer — Integration Guide

This guide explains how to integrate the `security/` package into any
Spring Boot 3 application to enforce JWT Bearer token validation on all API calls.

---

## What This Module Does

1. **Intercepts every request** — the Spring Security filter chain runs before
   any controller is reached.
2. **Multi-issuer JWT validation** — accepts tokens from multiple Cognito User
   Pools (one per environment) simultaneously. The issuer is detected from the
   JWT `iss` claim and matched against `auth.security.allowed-issuers`.
3. **Cryptographic signature verification** — fetches the Cognito JWKS endpoint
   and verifies the RS256 signature. This is server-side, unlike the frontend
   which only decodes without signature check.
4. **Structured error responses** — returns RFC 7807 ProblemDetail JSON with an
   `errorCode` field on every auth failure, matching the app's existing error format.
5. **Stateless** — no sessions, no cookies. Each request is authenticated independently.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Spring Boot | 3.x |
| Java | 17 or later |
| Network access from backend to Cognito JWKS endpoints | Required on startup / first token |

---

## File Inventory

Copy the entire `security/` package into your application:

```
src/main/java/com/yourapp/security/
├── AuthSecurityProperties.java    ← @ConfigurationProperties — the requirements file
├── SecurityConfig.java            ← Spring Security filter chain configuration
├── BearerTokenEntryPoint.java     ← 401 handler → ProblemDetail with errorCode
├── AuthAccessDeniedHandler.java   ← 403 handler → ProblemDetail with errorCode
├── AuthErrorCode.java             ← Error code enum (matches frontend AuthError)
└── backend-security-integration-guide.md ← this file
```

---

## Integration Steps

### Step 1 — Add dependencies to pom.xml

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Bearer token / OAuth2 Resource Server -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

---

### Step 2 — Register the configuration properties

In your main application class:

```java
@SpringBootApplication
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

---

### Step 3 — Configure allowed issuers in application.yml

```yaml
auth:
  security:
    allowed-issuers:
      # One entry per Cognito User Pool.
      # Format: https://cognito-idp.{region}.amazonaws.com/{userPoolId}
      # The value must match the `iss` claim in the JWT exactly.
      - "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_YourQAPoolId"
      - "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_YourProdPoolId"
```

**How to find the issuer URL:**
1. Decode any Cognito token (base64 the middle segment)
2. The `iss` claim value IS the issuer URL
3. Or: AWS Console → Cognito → User Pools → select pool → "User pool overview"
   → the Pool ID is the last segment of the issuer URL

---

### Step 4 — Expose a CorsConfigurationSource bean in WebConfig

Spring Security's filter chain processes requests before Spring MVC.
Without a `CorsConfigurationSource` bean, CORS preflight OPTIONS requests
will be rejected by Spring Security before reaching the MVC CORS config.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final List<String> ALLOWED_ORIGINS = List.of(
        "http://localhost:4200",
        "https://your-frontend.example.com"
    );

    // Spring MVC CORS (for controllers)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(ALLOWED_ORIGINS.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);
    }

    // Spring Security CORS (must mirror addCorsMappings above)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

---

## Error Response Reference

All auth errors return HTTP 401 or 403 with this JSON body (RFC 7807):

```json
{
  "type":      "/errors/auth/token-missing",
  "title":     "Authentication Required",
  "status":    401,
  "detail":    "No Bearer token was provided. Include an Authorization: Bearer <token> header.",
  "errorCode": "TOKEN_MISSING",
  "timestamp": "2026-03-06T10:00:00Z"
}
```

### Error Code Reference

| HTTP | errorCode | Trigger |
|---|---|---|
| 401 | `TOKEN_MISSING` | No `Authorization: Bearer` header |
| 401 | `TOKEN_EXPIRED` | JWT `exp` claim is in the past |
| 401 | `TOKEN_INVALID_ISSUER` | `iss` claim not in `allowed-issuers` |
| 401 | `TOKEN_INVALID_SIGNATURE` | RS256 signature verification failed |
| 401 | `TOKEN_MALFORMED` | JWT cannot be parsed (not 3 base64 segments) |
| 401 | `TOKEN_INVALID` | Other JWT validation failure |
| 403 | `ACCESS_DENIED` | Valid token but insufficient permissions |

---

## How to Access User Info in Controllers

Once a request passes authentication, the validated JWT is available as a
Spring Security `Jwt` principal:

```java
// Option 1 — method parameter injection
@GetMapping("/me")
public ResponseEntity<?> getMe(@AuthenticationPrincipal Jwt jwt) {
    String sub      = jwt.getSubject();
    String email    = jwt.getClaimAsString("email");
    String username = jwt.getClaimAsString("user_name");   // display name
    String issuer   = jwt.getIssuer().toString();          // which env
    return ResponseEntity.ok(Map.of("email", email, "sub", sub));
}

// Option 2 — from SecurityContext (useful in service layer)
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

Jwt jwt = (Jwt) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();
```

---

## How Multi-Issuer Validation Works

```
Incoming request: GET /api/v1/bill-runs
Authorization: Bearer eyJhbGci...

     Spring Security BearerTokenAuthenticationFilter
                         │
                         ▼
      JwtIssuerAuthenticationManagerResolver
      ┌─ peeks at `iss` claim (without signature check)
      ├─ is issuer in allowed-issuers list?
      │    NO  → TOKEN_INVALID_ISSUER (401)
      │    YES ↓
      └─ fetch JWKS from:
         https://cognito-idp.{region}.amazonaws.com/{poolId}/.well-known/jwks.json
         (cached after first fetch)
                         │
                         ▼
           NimbusJwtDecoder (per issuer)
           ├─ verify RS256 signature       → TOKEN_INVALID_SIGNATURE on failure
           ├─ check exp claim              → TOKEN_EXPIRED on failure
           └─ check iss claim              → TOKEN_INVALID_ISSUER on mismatch
                         │
                         ▼
           Authentication stored in SecurityContext
           Controller executes normally
```

---

## Verification Checklist

- [ ] `mvn compile` succeeds (ignore Eclipse JDT false-positives for java.util.List etc.)
- [ ] App starts and logs show Spring Security initialised
- [ ] `GET /api/v1/bill-runs` with no token → `401 TOKEN_MISSING`
- [ ] Same request with expired token → `401 TOKEN_EXPIRED`
- [ ] Same request with token from unknown pool → `401 TOKEN_INVALID_ISSUER`
- [ ] Same request with valid QA token → `200 OK`
- [ ] CORS preflight: `OPTIONS /api/v1/bill-runs` with `Origin: http://localhost:4200` → `200` (no auth required on OPTIONS)
- [ ] `@AuthenticationPrincipal Jwt jwt` in a controller returns the correct claims

---

## Making a Public Endpoint (no auth required)

Add a `requestMatchers(...).permitAll()` rule in `SecurityConfig.java` **before** `.anyRequest().authenticated()`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/health").permitAll()
    .requestMatchers("/api/v1/public/**").permitAll()   // add this
    .anyRequest().authenticated()
)
```

---

## Extending for Role-Based Access Control

Cognito `groups` claims are available in the JWT. To restrict specific
endpoints to specific groups, add a `JwtAuthenticationConverter` that
maps groups to Spring Security `GrantedAuthority`:

```java
// In SecurityConfig.java, replace:
JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(issuers)

// With a custom resolver that configures a JwtAuthenticationConverter:
JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
converter.setJwtGrantedAuthoritiesConverter(jwt -> {
    List<String> groups = jwt.getClaimAsStringList("groups");
    if (groups == null) return List.of();
    return groups.stream()
        .map(g -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + g))
        .toList();
});

// Then in routes:
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/v1/admin/**").hasRole("AvestarCapital_Administrator")
    .anyRequest().authenticated()
)
```
