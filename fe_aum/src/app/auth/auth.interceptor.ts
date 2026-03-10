import { HttpInterceptorFn } from '@angular/common/http';
import { TOKEN_STORAGE_KEY } from './auth-requirements';

/**
 * authInterceptor
 * ---------------
 * Attaches the stored Cognito JWT as a Bearer token to every outgoing
 * HTTP request. Passes through silently if no token is in localStorage
 * (the auth guard handles that case before any API call is made).
 *
 * Register in app.config.ts:
 *   provideHttpClient(withInterceptors([authInterceptor]))
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);

  // Log for debugging
  console.log('[Auth Interceptor] Request URL:', req.url);
  console.log('[Auth Interceptor] Token present:', !!token);
  console.log('[Auth Interceptor] Token preview:', token ? `${token.substring(0, 20)}...` : 'null');

  if (!token) {
    console.warn('[Auth Interceptor] No token found, proceeding without auth header');
    return next(req);
  }

  const authenticatedReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });

  console.log('[Auth Interceptor] Added Bearer token to request');
  return next(authenticatedReq);
};
