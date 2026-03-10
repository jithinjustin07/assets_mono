/**
 * ============================================================================
 * AUTH REQUIREMENTS
 * ============================================================================
 * Single source of truth for the cross-application auth landing module.
 *
 * WHAT THIS FILE DEFINES
 * ──────────────────────
 * 1. Data contracts (interfaces & types) for the auth flow
 * 2. Error codes and human-readable messages
 * 3. The AuthLandingConfig interface — what every consuming app must supply
 * 4. The AUTH_LANDING_CONFIG InjectionToken — how the config is delivered via DI
 * 5. Internal constants (storage keys, defaults)
 * 6. Utility: resolveEnvFromPoolConfig()
 *
 * WHAT THIS FILE DOES NOT DEFINE
 * ───────────────────────────────
 * ✗ Real Cognito pool IDs or client IDs — those live in the consuming app's
 *   app.config.ts, never in this file.
 * ✗ Any application-specific routing or UI logic.
 *
 * HOW TO CONFIGURE
 * ────────────────
 * In your consuming app's app.config.ts, provide AUTH_LANDING_CONFIG:
 *
 *   import { AUTH_LANDING_CONFIG } from './auth/auth-requirements';
 *
 *   providers: [
 *     { provide: AUTH_LANDING_CONFIG, useValue: { environments: { ... } } }
 *   ]
 *
 * See auth-integration-guide.md for full setup instructions.
 * ============================================================================
 */

import { InjectionToken } from '@angular/core';

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 1 — Data Contracts
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Configuration for a single AWS Cognito User Pool.
 * One entry is required per environment (prod, stg, dev, local).
 */
export interface CognitoPoolConfig {
  /** AWS region, e.g. 'us-east-1' */
  region: string;

  /**
   * Full User Pool ID, e.g. 'us-east-1_AbCdEf123'.
   * This must match the userPoolId portion of the JWT `iss` claim exactly.
   */
  userPoolId: string;

  /** App client ID registered for this pool */
  clientId: string;

  /**
   * Human-readable environment label for this pool.
   * Used in audit logging and UI display.
   * If omitted, the service infers the label from the userPoolId string.
   */
  env?: 'prod' | 'qa' | 'stg' | 'dev' | 'local';
}

/**
 * Parsed user information extracted from a validated Cognito JWT.
 * Available via AuthService.getStoredUserInfo() after login.
 */
export interface CognitoUserInfo {
  /** Cognito subject — the immutable unique user ID */
  sub: string;
  /** User's email address */
  email: string;
  /** Cognito username */
  username: string;
  /** Which environment's user pool issued the token */
  env: 'prod' | 'qa' | 'stg' | 'dev' | 'local';
  /** Token expiry as Unix timestamp (seconds since epoch) */
  exp: number;
}

/** Returned by AuthService.validateToken() */
export interface AuthValidationResult {
  valid: boolean;
  error?: AuthError;
  userInfo?: CognitoUserInfo;
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 2 — Error Codes
// ─────────────────────────────────────────────────────────────────────────────

export enum AuthError {
  /** No ?token= param present in the URL */
  MISSING_TOKEN = 'MISSING_TOKEN',

  /** JWT does not have the expected three base64 segments */
  MALFORMED_TOKEN = 'MALFORMED_TOKEN',

  /** JWT exp claim is in the past */
  EXPIRED = 'EXPIRED',

  /** JWT iss does not match any configured Cognito user pool */
  INVALID_ISSUER = 'INVALID_ISSUER',

  /** Catch-all for unexpected failures */
  UNKNOWN = 'UNKNOWN',
}

/** Maps each AuthError to a user-facing message shown on the error screen. */
export const AUTH_ERROR_MESSAGES: Record<AuthError, string> = {
  [AuthError.MISSING_TOKEN]:
    'No authentication token was provided. Please navigate to this application from the main portal.',
  [AuthError.MALFORMED_TOKEN]:
    'The authentication token is malformed and cannot be read. Please try navigating from the portal again.',
  [AuthError.EXPIRED]:
    'Your session has expired. Please log in again via the main portal.',
  [AuthError.INVALID_ISSUER]:
    'The authentication token was not issued by a recognised environment. Please use the official portal link.',
  [AuthError.UNKNOWN]:
    'An unexpected authentication error occurred. Please contact your system administrator.',
};

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 3 — Consuming App Configuration Contract
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The configuration object every consuming app must supply via DI.
 *
 * REQUIRED
 * ────────
 * environments   Map of userPoolId → CognitoPoolConfig.
 *                Must include one entry for every environment the main
 *                application is deployed in (prod, stg, dev, local).
 *                The key is the exact Cognito User Pool ID.
 *
 * OPTIONAL (sensible defaults apply if omitted)
 * ──────────────────────────────────────────────
 * tokenQueryParam        URL param name carrying the JWT.     Default: 'token'
 * sessionCheckIntervalMs Expiry check frequency in ms.         Default: 60 000
 * errorRoute             Route shown on auth failure.          Default: '/auth-error'
 * postLoginRoute         Route navigated to after success.     Default: '/analytics'
 */
export interface AuthLandingConfig {
  /**
   * Map of Cognito User Pool ID → pool config.
   *
   * The KEY must be the exact userPoolId as it appears in Cognito
   * (e.g. 'us-east-1_AbCdEfGhI') — this is what the auth service matches
   * against the `iss` claim in the incoming JWT.
   *
   * @example
   * environments: {
   *   'us-east-1_ProdPoolABC': { region: 'us-east-1', userPoolId: 'us-east-1_ProdPoolABC', clientId: 'abc123', env: 'prod' },
   *   'us-east-1_StgPoolXYZ':  { region: 'us-east-1', userPoolId: 'us-east-1_StgPoolXYZ',  clientId: 'xyz456', env: 'stg'  },
   * }
   */
  environments: Record<string, CognitoPoolConfig>;

  /**
   * URL query parameter used to receive the JWT from the main app.
   * @default 'token'
   * @example '?token=eyJhbGci...' ← uses param name 'token'
   */
  tokenQueryParam?: string;

  /**
   * How often (milliseconds) the session monitor checks for token expiry.
   * Runs both on an interval and on browser tab focus/visibility change.
   * @default 60000 (1 minute)
   */
  sessionCheckIntervalMs?: number;

  /**
   * Angular route path to navigate to when auth fails or session expires.
   * Do not include a leading slash in the path segment.
   * @default 'auth-error'
   */
  errorRoute?: string;

  /**
   * Angular route path to navigate to after successful token validation.
   * @default 'analytics'
   */
  postLoginRoute?: string;

  /**
   * URL query parameter used to receive the Cognito refresh token from the main app.
   * If absent from the landing URL, refresh will not be available.
   * @default 'refresh_token'
   */
  refreshTokenQueryParam?: string;

  /**
   * Seconds before the access token expires at which the session monitor
   * proactively calls Cognito to refresh the token.
   * @default 300 (5 minutes)
   */
  refreshThresholdSeconds?: number;
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 4 — InjectionToken
// ─────────────────────────────────────────────────────────────────────────────

/**
 * DI token used to inject AuthLandingConfig into AuthService.
 *
 * Provide this in your app.config.ts:
 *
 *   { provide: AUTH_LANDING_CONFIG, useValue: { environments: { ... } } }
 *
 * Angular will throw "No provider for InjectionToken AUTH_LANDING_CONFIG"
 * if this is missing — that is intentional and serves as a setup reminder.
 */
export const AUTH_LANDING_CONFIG = new InjectionToken<AuthLandingConfig>(
  'AUTH_LANDING_CONFIG'
);

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 5 — Internal Constants (defaults used by AuthService)
// ─────────────────────────────────────────────────────────────────────────────

/** Default URL query param name. Overridden by AuthLandingConfig.tokenQueryParam */
export const DEFAULT_TOKEN_QUERY_PARAM = 'token';

/** Default session monitor interval in ms. Overridden by AuthLandingConfig.sessionCheckIntervalMs */
export const DEFAULT_SESSION_CHECK_INTERVAL_MS = 60_000;

/** Default route shown on auth failure. Overridden by AuthLandingConfig.errorRoute */
export const DEFAULT_ERROR_ROUTE = 'auth-error';

/** Default route navigated to after success. Overridden by AuthLandingConfig.postLoginRoute */
export const DEFAULT_POST_LOGIN_ROUTE = 'analytics';

/** localStorage key for the raw JWT string */
export const TOKEN_STORAGE_KEY = 'auth_session_token';

/** localStorage key for the serialised CognitoUserInfo object */
export const USER_INFO_STORAGE_KEY = 'auth_session_user';

/** localStorage key for the Cognito refresh token */
export const REFRESH_TOKEN_STORAGE_KEY = 'auth_session_refresh_token';

/** Default URL query param name for the refresh token */
export const DEFAULT_REFRESH_TOKEN_QUERY_PARAM = 'refresh_token';

/**
 * Default seconds before expiry at which the session monitor proactively
 * refreshes the access token. 300 = 5 minutes.
 */
export const DEFAULT_REFRESH_THRESHOLD_SECONDS = 300;

// ─────────────────────────────────────────────────────────────────────────────
// SECTION 6 — Utility
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Derives the environment label for a given userPoolId.
 *
 * Resolution order:
 *   1. The explicit `env` field on the matching CognitoPoolConfig (most accurate)
 *   2. String inference from the userPoolId (fallback for legacy configs)
 *   3. 'local' as the final fallback
 */
export function resolveEnvFromPoolConfig(
  userPoolId: string,
  config: AuthLandingConfig
): CognitoUserInfo['env'] {
  const poolCfg = config.environments[userPoolId];

  // Prefer the explicit label set by the consuming app
  if (poolCfg?.env) return poolCfg.env;

  // Fallback: infer from the pool ID string (case-insensitive)
  const id = userPoolId.toLowerCase();
  if (id.includes('prod')) return 'prod';
  if (id.includes('qa')) return 'qa';
  if (id.includes('stg') || id.includes('staging')) return 'stg';
  if (id.includes('dev')) return 'dev';
  return 'local';
}
