import { inject, Injectable, OnDestroy, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ParamMap, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {
  AUTH_LANDING_CONFIG,
  AuthError,
  AuthLandingConfig,
  AuthValidationResult,
  CognitoPoolConfig,
  CognitoUserInfo,
  DEFAULT_ERROR_ROUTE,
  DEFAULT_POST_LOGIN_ROUTE,
  DEFAULT_REFRESH_THRESHOLD_SECONDS,
  DEFAULT_REFRESH_TOKEN_QUERY_PARAM,
  DEFAULT_SESSION_CHECK_INTERVAL_MS,
  DEFAULT_TOKEN_QUERY_PARAM,
  REFRESH_TOKEN_STORAGE_KEY,
  resolveEnvFromPoolConfig,
  TOKEN_STORAGE_KEY,
  USER_INFO_STORAGE_KEY,
} from './auth-requirements';

interface CognitoRefreshResponse {
  AuthenticationResult?: {
    IdToken?: string;
    AccessToken?: string;
    ExpiresIn?: number;
  };
}

type AuthState = 'pending' | 'authenticated' | 'invalid';

@Injectable({ providedIn: 'root' })
export class AuthService implements OnDestroy {
  /** Reactive auth state — components and guards observe this signal */
  readonly authState = signal<AuthState>('pending');

  private readonly config: AuthLandingConfig = inject(AUTH_LANDING_CONFIG);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  private monitorInterval: ReturnType<typeof setInterval> | null = null;
  private readonly visibilityHandler = () => this.checkSessionOnFocus();

  // -------------------------------------------------------------------------
  // Resolved config helpers (apply defaults for optional fields)
  // -------------------------------------------------------------------------

  get tokenQueryParam(): string {
    return this.config.tokenQueryParam ?? DEFAULT_TOKEN_QUERY_PARAM;
  }

  get sessionCheckIntervalMs(): number {
    return this.config.sessionCheckIntervalMs ?? DEFAULT_SESSION_CHECK_INTERVAL_MS;
  }

  get errorRoute(): string {
    return this.config.errorRoute ?? DEFAULT_ERROR_ROUTE;
  }

  get postLoginRoute(): string {
    return this.config.postLoginRoute ?? DEFAULT_POST_LOGIN_ROUTE;
  }

  get refreshTokenQueryParam(): string {
    return this.config.refreshTokenQueryParam ?? DEFAULT_REFRESH_TOKEN_QUERY_PARAM;
  }

  get refreshThresholdSeconds(): number {
    return this.config.refreshThresholdSeconds ?? DEFAULT_REFRESH_THRESHOLD_SECONDS;
  }

  // -------------------------------------------------------------------------
  // Token extraction
  // -------------------------------------------------------------------------

  /** Reads the raw JWT string from URL query params using the configured param name. */
  extractTokenFromUrl(params: ParamMap): string | null {
    return params.get(this.tokenQueryParam);
  }

  // -------------------------------------------------------------------------
  // Validation
  // -------------------------------------------------------------------------

  /**
   * Validates a raw Cognito JWT against the configured environments.
   *
   * Checks (all client-side, no network call):
   *   1. Token is present and structurally valid (3 base64 segments)
   *   2. `exp` claim is in the future
   *   3. `iss` claim matches one of the userPoolIds in AUTH_LANDING_CONFIG.environments
   *
   * NOTE: Cryptographic signature verification via JWKS is intentionally
   * omitted in this POC. For production, add an async JWKS fetch step inside
   * this method, or delegate to a backend introspection endpoint.
   */
  validateToken(token: string | null | undefined): AuthValidationResult {
    if (!token) {
      return this.fail(AuthError.MISSING_TOKEN);
    }

    const payload = this.decodeJwtPayload(token);
    if (!payload) {
      return this.fail(AuthError.MALFORMED_TOKEN);
    }

    // Check 1 — expiry
    const exp = payload['exp'] as number | undefined;
    if (!exp || Math.floor(Date.now() / 1000) >= exp) {
      return this.fail(AuthError.EXPIRED);
    }

    // Check 2 — issuer matches a configured pool
    // Cognito iss format: https://cognito-idp.{region}.amazonaws.com/{userPoolId}
    const iss = payload['iss'] as string | undefined;
    if (!iss) {
      return this.fail(AuthError.INVALID_ISSUER);
    }

    const userPoolId = iss.split('/').pop() ?? '';
    const poolConfig = this.config.environments[userPoolId];
    if (!poolConfig) {
      return this.fail(AuthError.INVALID_ISSUER);
    }

    // Build user info from JWT claims
    const userInfo: CognitoUserInfo = {
      sub: (payload['sub'] as string) ?? '',
      email: (payload['email'] as string) ?? '',
      // Claim resolution order:
      //   1. cognito:username  — standard claim in ID tokens
      //   2. user_name         — custom display-name claim used in some access tokens
      //   3. username          — fallback (may be a UUID in access tokens)
      username:
        (payload['cognito:username'] as string) ??
        (payload['user_name'] as string) ??
        (payload['username'] as string) ??
        '',
      env: resolveEnvFromPoolConfig(userPoolId, this.config),
      exp,
    };

    return { valid: true, userInfo };
  }

  // -------------------------------------------------------------------------
  // Session storage
  // -------------------------------------------------------------------------

  storeSession(token: string, userInfo: CognitoUserInfo): void {
    console.log('[AuthService] Storing session token and user info');
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    localStorage.setItem(USER_INFO_STORAGE_KEY, JSON.stringify(userInfo));
    this.authState.set('authenticated');
    console.log('[AuthService] Session stored, authState set to authenticated');
  }

  getStoredToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }

  getStoredUserInfo(): CognitoUserInfo | null {
    const raw = localStorage.getItem(USER_INFO_STORAGE_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as CognitoUserInfo;
    } catch {
      return null;
    }
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_INFO_STORAGE_KEY);
    localStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
    this.authState.set('invalid');
  }

  storeRefreshToken(refreshToken: string): void {
    localStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken);
  }

  getStoredRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
  }

  // -------------------------------------------------------------------------
  // Session validity
  // -------------------------------------------------------------------------

  /**
   * Returns true if a non-expired session exists in localStorage.
   * Pure local check — no network call.
   */
  isSessionValid(): boolean {
    const userInfo = this.getStoredUserInfo();
    if (!userInfo) return false;
    return Math.floor(Date.now() / 1000) < userInfo.exp;
  }

  /**
   * Restores auth state from localStorage when navigating directly to a protected
   * route (bypassing the landing page). Sets authState to 'authenticated' if the
   * stored session is still valid, so the app shell renders correctly.
   * Returns true if the session was valid and restored.
   */
  restoreSessionIfValid(): boolean {
    if (this.authState() === 'authenticated') return true;
    if (this.isSessionValid()) {
      this.authState.set('authenticated');
      return true;
    }
    return false;
  }

  // -------------------------------------------------------------------------
  // Session monitoring
  // -------------------------------------------------------------------------

  /**
   * Starts periodic + visibility-based session monitoring.
   * Must be called once after successful validation (inside LandingPageComponent).
   */
  startSessionMonitor(): void {
    this.stopSessionMonitor();

    this.monitorInterval = setInterval(
      () => this.enforceSessionValidity(),
      this.sessionCheckIntervalMs
    );

    document.addEventListener('visibilitychange', this.visibilityHandler);
  }

  stopSessionMonitor(): void {
    if (this.monitorInterval !== null) {
      clearInterval(this.monitorInterval);
      this.monitorInterval = null;
    }
    document.removeEventListener('visibilitychange', this.visibilityHandler);
  }

  ngOnDestroy(): void {
    this.stopSessionMonitor();
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private enforceSessionValidity(): void {
    const refreshToken = this.getStoredRefreshToken();

    if (!this.isSessionValid()) {
      // Token fully expired — refresh only if a refresh token was provided
      refreshToken ? this.attemptRefresh(refreshToken) : this.expireSession();
      return;
    }

    // Token still valid — proactively refresh only if near expiry AND a refresh token exists
    if (refreshToken && this.isNearExpiry()) {
      this.attemptRefresh(refreshToken);
    }
  }

  /** True if the stored token expires within refreshThresholdSeconds. */
  private isNearExpiry(): boolean {
    const userInfo = this.getStoredUserInfo();
    if (!userInfo) return false;
    const secondsRemaining = userInfo.exp - Math.floor(Date.now() / 1000);
    return secondsRemaining <= this.refreshThresholdSeconds;
  }

  /** Calls Cognito InitiateAuth with REFRESH_TOKEN_AUTH and updates the stored session. */
  private attemptRefresh(refreshToken: string): void {
    this.refreshAccessToken(refreshToken).subscribe(success => {
      if (!success) {
        this.expireSession();
      }
    });
  }

  /** Navigates to the error route with EXPIRED reason and clears the session. */
  private expireSession(): void {
    this.clearSession();
    this.router.navigate([`/${this.errorRoute}`], {
      queryParams: { reason: AuthError.EXPIRED },
      replaceUrl: true,
    });
  }

  /**
   * Exchanges a Cognito refresh token for a new access/ID token.
   * Looks up the pool config from the currently stored token's issuer.
   * Updates localStorage and authState on success.
   * Returns an Observable<boolean> — true on success, false on any failure.
   */
  refreshAccessToken(refreshToken: string): Observable<boolean> {
    const poolConfig = this.getPoolConfigForStoredToken();
    if (!poolConfig) {
      return of(false);
    }

    const url = `https://cognito-idp.${poolConfig.region}.amazonaws.com/`;
    const body = {
      AuthFlow: 'REFRESH_TOKEN_AUTH',
      ClientId: poolConfig.clientId,
      AuthParameters: { REFRESH_TOKEN: refreshToken },
    };

    return this.http.post<CognitoRefreshResponse>(url, body, {
      headers: {
        'Content-Type': 'application/x-amz-json-1.1',
        'X-Amz-Target': 'AWSCognitoIdentityProviderService.InitiateAuth',
      },
    }).pipe(
      map(res => {
        // Prefer IdToken (carries email/username claims); fall back to AccessToken
        const newToken = res.AuthenticationResult?.IdToken
          ?? res.AuthenticationResult?.AccessToken;
        if (!newToken) return false;

        const result = this.validateToken(newToken);
        if (!result.valid || !result.userInfo) return false;

        this.storeSession(newToken, result.userInfo);
        // Refresh token itself does not change unless Cognito rotation is enabled
        return true;
      }),
      catchError(() => of(false))
    );
  }

  /** Resolves the CognitoPoolConfig for the currently stored token's issuer. */
  private getPoolConfigForStoredToken(): CognitoPoolConfig | null {
    const token = this.getStoredToken();
    if (!token) return null;
    const payload = this.decodeJwtPayload(token);
    if (!payload) return null;
    const iss = payload['iss'] as string | undefined;
    if (!iss) return null;
    const poolId = iss.split('/').pop() ?? '';
    return this.config.environments[poolId] ?? null;
  }

  private checkSessionOnFocus(): void {
    if (document.visibilityState === 'visible') {
      this.enforceSessionValidity();
    }
  }

  /**
   * Decodes the payload segment of a JWT without verifying the signature.
   * Returns null if the token is not structurally valid.
   */
  private decodeJwtPayload(token: string): Record<string, unknown> | null {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    try {
      const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64)) as Record<string, unknown>;
    } catch {
      return null;
    }
  }

  private fail(error: AuthError): AuthValidationResult {
    return { valid: false, error };
  }
}
