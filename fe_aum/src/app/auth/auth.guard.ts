import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * authGuard
 * ---------
 * Functional route guard that protects all application routes.
 * Redirects to /auth-error if no valid session exists.
 *
 * Apply to every route that should be protected:
 *   { path: 'analytics', component: EmptyRouteComponent, canActivate: [authGuard] }
 */
export const authGuard: CanActivateFn = (_route, _state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.restoreSessionIfValid()) {
    return true;
  }

  authService.clearSession();
  return router.createUrlTree(['/landing']);
};
