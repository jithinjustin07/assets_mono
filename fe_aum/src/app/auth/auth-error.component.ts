import { Component, Input, OnInit, signal } from '@angular/core';
import { AUTH_ERROR_MESSAGES, AuthError } from './auth-requirements';

/**
 * AuthErrorComponent
 * ------------------
 * Standalone error screen shown when token validation fails or the session
 * expires during use. Dead-end by design — no navigation back into the app.
 *
 * Usage:
 *   <app-auth-error [errorCode]="AuthError.EXPIRED" />
 *
 *   Or as a route:
 *   { path: 'auth-error', component: AuthErrorComponent }
 *   and read ?reason= query param.
 */
@Component({
  selector: 'app-auth-error',
  standalone: true,
  template: `
    <div class="error-container">
      <div class="error-card">
        <div class="error-icon" aria-hidden="true">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="1.5"
               stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>

        <h1 class="error-title">Access Denied</h1>

        <p class="error-message">{{ message() }}</p>

        <div class="error-code-badge">
          Error code: <code>{{ errorCode }}</code>
        </div>

        <p class="error-help">
          If you believe this is a mistake, please contact your system
          administrator or return to the main portal and try navigating
          to this application again.
        </p>
      </div>
    </div>
  `,
  styles: [`
    .error-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: #f5f6fa;
      padding: 24px;
    }

    .error-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      gap: 16px;
      max-width: 480px;
      width: 100%;
      background: #ffffff;
      border-radius: 12px;
      padding: 48px 40px;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
    }

    .error-icon {
      color: #ef4444;
    }

    .error-title {
      font-size: 22px;
      font-weight: 600;
      color: #1e293b;
      margin: 0;
    }

    .error-message {
      font-size: 14px;
      line-height: 1.6;
      color: #475569;
      margin: 0;
    }

    .error-code-badge {
      font-size: 12px;
      color: #94a3b8;
      background: #f1f5f9;
      border-radius: 6px;
      padding: 4px 12px;
    }

    .error-code-badge code {
      font-family: monospace;
      color: #64748b;
    }

    .error-help {
      font-size: 12px;
      line-height: 1.6;
      color: #94a3b8;
      margin: 0;
      border-top: 1px solid #f1f5f9;
      padding-top: 16px;
      width: 100%;
    }
  `],
})
export class AuthErrorComponent implements OnInit {
  @Input() errorCode: AuthError = AuthError.UNKNOWN;

  readonly message = signal<string>('');

  ngOnInit(): void {
    this.message.set(
      AUTH_ERROR_MESSAGES[this.errorCode] ?? AUTH_ERROR_MESSAGES[AuthError.UNKNOWN]
    );
  }
}
