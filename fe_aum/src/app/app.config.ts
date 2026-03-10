// import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
// import { provideHttpClient } from '@angular/common/http';
//
// export const appConfig: ApplicationConfig = {
//   providers: [
//     provideBrowserGlobalErrorListeners(),
//     provideZoneChangeDetection({ eventCoalescing: true }),
//     provideHttpClient(),
//   ]
// };
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './auth/auth.interceptor';
import { AUTH_LANDING_CONFIG } from './auth/auth-requirements';
import { ApiService } from './services/api.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    ApiService,

    // ── Auth Landing Configuration ─────────────────────────────────────────
    // Replace each placeholder pool ID and client ID with the real values
    // from the AWS Cognito console for each environment.
    //
    // How to find them:
    //   AWS Console → Cognito → User Pools → select pool
    //   Pool ID:   shown on the "User pool overview" page
    //   Client ID: App Integration tab → App clients section
    {
      provide: AUTH_LANDING_CONFIG,
      useValue: {
        environments: {
          // ── QA ───────────────────────────────────────────────────────────
          // KEY must be the exact Cognito Pool ID (matches JWT `iss` claim)
          'us-east-1_LY7jo7QDt': {
            region: 'us-east-1',
            userPoolId: 'us-east-1_LY7jo7QDt',
            clientId: '3q76fem6vltvb12kji24ipkh2e',
            env: 'qa',
          },
          // ── Local (both apps running on localhost) ───────────────────────
          'us-east-1_YiGu3qb4W': {
            region: 'us-east-1',
            userPoolId: 'us-east-1_YiGu3qb4W',  // ← replace
            clientId: '3mkk5aefkktk309o472s3b42s1',  // ← replace
            env: 'local',
          },
        },

        // Optional overrides — remove to use defaults shown in parentheses
        tokenQueryParam: 'token',           // default: 'token'
        sessionCheckIntervalMs: 60_000,     // default: 60000 (1 min)
        errorRoute: 'auth-error',           // default: 'auth-error'
        postLoginRoute: 'aum-reports',        // default: 'analytics'
      },
    },
  ],
};
