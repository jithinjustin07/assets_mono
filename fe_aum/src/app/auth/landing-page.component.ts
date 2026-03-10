import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthError } from './auth-requirements';
import { AuthErrorComponent } from './auth-error.component';

type LandingState = 'validating' | 'valid' | 'invalid';

/**
 * LandingPageComponent
 * --------------------
 * The single entry gate for this application.
 *
 * Reads the Cognito JWT from the URL query param (configured via
 * AuthLandingConfig.tokenQueryParam, default '?token='), validates it,
 * and either navigates to the post-login route or shows an error screen.
 *
 * REUSE INSTRUCTIONS
 * ──────────────────
 * 1. Copy the auth/ folder into your Angular app.
 * 2. Register this component on the landing route in app.routes.ts.
 * 3. Provide AUTH_LANDING_CONFIG in app.config.ts.
 * 4. Wrap your app shell in app.html with @if (isAuthenticated()) { ... }
 *    and add <router-outlet /> in the @else branch.
 *
 * See auth-integration-guide.md for the full checklist.
 */
@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [AuthErrorComponent],
  template: `
    @if (state() === 'validating') {
      <div class="landing-container">
        <div class="landing-card">
          <div class="spinner" aria-label="Validating your session..."></div>
          <p class="landing-message">Validating your session&hellip;</p>
        </div>
      </div>
    }

    @if (state() === 'invalid') {
      <app-auth-error [errorCode]="errorCode()" />
    }
  `,
  styles: [`
    .landing-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: #f5f6fa;
    }

    .landing-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;
      background: #ffffff;
      border-radius: 12px;
      padding: 48px 64px;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
    }

    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #e2e8f0;
      border-top-color: #6366f1;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .landing-message {
      color: #64748b;
      font-size: 14px;
      margin: 0;
    }
  `],
})
export class LandingPageComponent implements OnInit {
  readonly state = signal<LandingState>('validating');
  readonly errorCode = signal<AuthError>(AuthError.UNKNOWN);

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  ngOnInit(): void {
    // If a valid session already exists (e.g. in-app refresh),
    // skip validation and go straight to the app.
    if (this.authService.isSessionValid()) {
      this.router.navigate([`/${this.authService.postLoginRoute}`], { replaceUrl: true });
      return;
    }

    const params = this.route.snapshot.queryParamMap;
    const token = params.get(this.authService.tokenQueryParam);
    const result = this.authService.validateToken(token);

    if (result.valid && result.userInfo) {
      this.authService.storeSession(token!, result.userInfo);

      // Store the refresh token if the main portal provided one
      const refreshToken = params.get(this.authService.refreshTokenQueryParam);
      if (refreshToken) {
        this.authService.storeRefreshToken(refreshToken);
      }

      this.authService.startSessionMonitor();
      // Navigate and replace URL so the raw ?token= never appears in history
      this.router.navigate([`/${this.authService.postLoginRoute}`], { replaceUrl: true });
    } else {
      this.errorCode.set(result.error ?? AuthError.UNKNOWN);
      this.state.set('invalid');
    }
  }
}
