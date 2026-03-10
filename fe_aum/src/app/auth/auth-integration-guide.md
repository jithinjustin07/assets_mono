# Auth Landing Module — Integration Guide

This guide explains how to integrate the `auth/` module into any Angular 17+
standalone-component application. Follow every numbered step in order.

---

## What This Module Does

1. **Landing gate** — the user arrives from the main application via a link
   carrying a Cognito JWT (`?token=<JWT>`).
2. **Token validation** — decodes and validates the JWT client-side:
   - Structural integrity (3 base64 segments)
   - Not expired (`exp` claim)
   - Issuer matches a configured Cognito user pool (`iss` claim)
3. **Session storage** — stores the validated token + user info in
   `localStorage` (persists across tabs and browser restarts until the token expires).
4. **Session monitoring** — checks for expiry every 60 seconds and on
   browser-tab focus. Redirects to the error screen on expiry.
5. **Route guard** — blocks all protected routes unless a valid session exists.
6. **HTTP interceptor** — attaches `Authorization: Bearer <token>` to every
   outgoing API request automatically.
7. **Error screen** — shows a user-friendly error with the error code when
   validation fails or the session expires.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Angular | 17 or later (standalone component support required) |
| `@angular/router` | included with Angular |
| `@angular/common/http` | included with Angular |
| AWS Cognito | Any tier — you need Pool IDs and App Client IDs |

No additional npm packages are required.

---

## File Inventory

Copy the entire `auth/` folder into your application's `src/app/` directory.
The folder contains exactly these files — do not rename them:

```
src/app/auth/
├── auth-requirements.ts        ← contracts, DI token, config interface, constants
├── auth.service.ts             ← validation, session storage, session monitoring
├── landing-page.component.ts   ← entry gate component (shown on /landing)
├── auth-error.component.ts     ← error screen component (shown on /auth-error)
├── auth.guard.ts               ← functional route guard
├── auth.interceptor.ts         ← HTTP interceptor (adds Bearer token)
└── auth-integration-guide.md  ← this file
```

---

## Integration Steps

### Step 1 — Copy the auth/ folder

Copy `src/app/auth/` verbatim into your target application.
No changes to any file inside `auth/` are required for normal use.

---

### Step 2 — Provide the configuration in app.config.ts

This is the **only file you touch** in your application code.

```typescript
// src/app/app.config.ts
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './auth/auth.interceptor';
import { AUTH_LANDING_CONFIG } from './auth/auth-requirements';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),

    {
      provide: AUTH_LANDING_CONFIG,
      useValue: {
        environments: {
          // One entry per Cognito User Pool your main app uses.
          // KEY = exact userPoolId string (e.g. 'us-east-1_AbCdEf123')
          'us-east-1_YourProdPoolId': {
            region: 'us-east-1',
            userPoolId: 'us-east-1_YourProdPoolId',
            clientId: 'yourProdClientId',
            env: 'prod',
          },
          'us-east-1_YourQaPoolId': {
            region: 'us-east-1',
            userPoolId: 'us-east-1_YourQaPoolId',
            clientId: 'yourQaClientId',
            env: 'qa',
          },
          'us-east-1_YourStgPoolId': {
            region: 'us-east-1',
            userPoolId: 'us-east-1_YourStgPoolId',
            clientId: 'yourStgClientId',
            env: 'stg',
          },
          // Add dev, local as needed
        },

        // Optional — these are the defaults, omit to keep them
        tokenQueryParam:        'token',       // URL param carrying the JWT
        sessionCheckIntervalMs: 60_000,        // expiry check frequency (ms)
        errorRoute:             'auth-error',  // path for the error screen
        postLoginRoute:         'analytics',   // path after successful login
      },
    },
  ],
};
```

**Where to find Cognito values:**
- AWS Console → Cognito → User Pools → select pool
- **Pool ID**: shown on the "User pool overview" page (e.g. `us-east-1_AbCdEf123`)
- **Client ID**: App Integration tab → App clients section

---

### Step 3 — Add auth routes to app.routes.ts

```typescript
// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { authGuard }            from './auth/auth.guard';
import { LandingPageComponent } from './auth/landing-page.component';
import { AuthErrorComponent }   from './auth/auth-error.component';

export const routes: Routes = [
  // ── Public auth routes (no guard) ──────────────────────────────────────
  { path: '',           redirectTo: 'landing', pathMatch: 'full' },
  { path: 'landing',    component: LandingPageComponent },
  { path: 'auth-error', component: AuthErrorComponent },

  // ── Protected app routes (add canActivate: [authGuard] to every route) ─
  { path: 'dashboard',  component: DashboardComponent, canActivate: [authGuard] },
  { path: 'settings',   component: SettingsComponent,  canActivate: [authGuard] },

  // Catch-all — redirect unknown paths back to landing
  { path: '**', redirectTo: 'landing' },
];
```

**Rules:**
- `/landing` and `/auth-error` must NOT have `canActivate: [authGuard]`.
- Every other route that should be protected MUST have `canActivate: [authGuard]`.
- The default redirect `''` should point to `'landing'`.

---

### Step 4 — Wrap the app shell in app.html

The dashboard/shell must only render after authentication succeeds.
Add an `@if` block wrapping the entire shell content.

**In your root component (app.ts)**, add:

```typescript
import { RouterOutlet } from '@angular/router';
import { AuthService }  from './auth/auth.service';
import { computed }     from '@angular/core';

export class App {
  private authService = inject(AuthService);
  public isAuthenticated = computed(() => this.authService.authState() === 'authenticated');

  // ... rest of your component
}

// Also add RouterOutlet to the @Component imports array:
@Component({
  imports: [ ..., RouterOutlet ],
  ...
})
```

**In your root template (app.html)**, wrap the shell:

```html
@if (isAuthenticated()) {
  <div class="your-shell-container">
    <!-- navbar, sidebar, main content, modals, etc. -->
  </div>
} @else {
  <router-outlet />
}
```

This ensures:
- `/landing` → shows the validation spinner or error screen (no shell)
- After valid login → shell appears, `<router-outlet />` is unused
- On session expiry → shell hides, router navigates to `/auth-error`

---

### Step 5 — Update backend CORS (if applicable)

If your backend is a Spring Boot API, update `WebConfig.java` to accept
the `Authorization` header and the origins of all environments:

```java
registry.addMapping("/api/**")
        .allowedOrigins(
            "http://localhost:4200",
            "https://your-poc-app.prod.example.com",
            "https://your-poc-app.stg.example.com"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin")
        .allowCredentials(false)
        .maxAge(3600);
```

---

## Configuration Reference

All fields of `AuthLandingConfig` (defined in `auth-requirements.ts`):

| Field | Type | Required | Default | Description |
|---|---|---|---|---|
| `environments` | `Record<string, CognitoPoolConfig>` | **Yes** | — | Map of userPoolId → pool config |
| `tokenQueryParam` | `string` | No | `'token'` | URL param name carrying the JWT |
| `sessionCheckIntervalMs` | `number` | No | `60000` | Expiry check interval in ms |
| `errorRoute` | `string` | No | `'auth-error'` | Route path for the error screen |
| `postLoginRoute` | `string` | No | `'analytics'` | Route path after successful login |

`CognitoPoolConfig` fields:

| Field | Type | Required | Description |
|---|---|---|---|
| `region` | `string` | **Yes** | AWS region (e.g. `'us-east-1'`) |
| `userPoolId` | `string` | **Yes** | Full pool ID (e.g. `'us-east-1_AbCdEf'`) |
| `clientId` | `string` | **Yes** | App client ID from Cognito |
| `env` | `'prod' \| 'qa' \| 'stg' \| 'dev' \| 'local'` | No | Human label; inferred from poolId if omitted |

---

## How the Main App Should Navigate to This App

The main application must append the user's current Cognito access token
(or ID token) as the `token` query parameter when building the navigation link:

```typescript
// In the main application (pseudocode — adapt to your auth library)
const token = cognitoSession.getIdToken().getJwtToken();
const pocAppUrl = `https://billing-poc.example.com/landing?token=${token}`;
window.open(pocAppUrl, '_blank');
// or: window.location.href = pocAppUrl;
```

> **Important:** Both ID tokens and access tokens are supported.
> The service resolves the display name from these claims in order:
> `cognito:username` → `user_name` → `username`. Access tokens from Cognito
> carry the display name in the `user_name` claim. ID tokens use `cognito:username`.

---

## Token Flow Diagram

```
Main App (any env)
   │
   │  User clicks "Open Billing POC" link
   │
   └─► https://billing-poc.example.com/landing?token=<Cognito JWT>
                │
                ▼
        LandingPageComponent.ngOnInit()
                │
        authService.validateToken(token)
                │
        ┌───────┴─────────┐
        │ valid            │ invalid
        ▼                  ▼
  storeSession()     show AuthErrorComponent
  startMonitor()     (dead end — no navigation)
  navigate to
  /analytics
        │
        ▼
  authGuard on all routes
  authInterceptor adds Bearer header
  SessionMonitor runs every 60s + on tab focus
        │
        ▼ (on expiry)
  clearSession()
  navigate to /auth-error
```

---

## Verification Checklist

Test each scenario after integration:

- [ ] **Happy path**: Navigate to `/landing?token=<valid Cognito JWT>` → reaches post-login route
- [ ] **Missing token**: Navigate to `/landing` (no ?token=) → error screen shows MISSING_TOKEN
- [ ] **Expired token**: Use a JWT with `exp` in the past → error screen shows EXPIRED
- [ ] **Wrong issuer**: Use a JWT from an unconfigured pool → error screen shows INVALID_ISSUER
- [ ] **Direct protected route**: Navigate to `/analytics` with no session → redirected to `/auth-error`
- [ ] **Page refresh**: Valid session in sessionStorage → skips landing, goes straight to app
- [ ] **API calls**: Open DevTools → Network → confirm `Authorization: Bearer ...` header on all API requests
- [ ] **Session expiry (simulated)**: In DevTools console, run
      `localStorage.setItem('auth_session_user', JSON.stringify({...JSON.parse(localStorage.getItem('auth_session_user')), exp: 1}))`,
      then wait 60s or switch tabs → app navigates to `/auth-error`
- [ ] **Persistence across tabs**: Open a new tab to the app with an active session → session is restored (localStorage persists across tabs)

---

## Known Limitations (POC scope)

1. **No cryptographic signature verification** — the JWT signature is not
   verified against Cognito's JWKS endpoint. Tokens are trusted if they are
   structurally valid, not expired, and have a matching issuer. For production,
   add JWKS verification or use backend token introspection.

2. **URL token exposure** — the raw JWT briefly appears in the browser address
   bar before being removed. This is unavoidable with the query-param hand-off
   approach but mitigated by `replaceUrl: true` on navigation.

3. **localStorage persistence** — the token persists across tabs and browser
   restarts until the `exp` claim is reached. Call `authService.clearSession()`
   on explicit logout to remove it early.

---

## Extending This Module

| Need | Change |
|---|---|
| Add JWKS signature verification | Add an async step inside `AuthService.validateToken()` before returning `valid: true` |
| Show the logged-in user's name | Call `authService.getStoredUserInfo()` in any component |
| Custom error screen branding | Edit `auth-error.component.ts` styles/template |
| Different token param per app | Set `tokenQueryParam` in `AuthLandingConfig` |
| Shorter/longer session check | Set `sessionCheckIntervalMs` in `AuthLandingConfig` |
| Navigate somewhere other than analytics | Set `postLoginRoute` in `AuthLandingConfig` |
