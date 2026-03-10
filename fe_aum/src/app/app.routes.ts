import { Routes } from '@angular/router';
import { App } from './app';
import { LandingPageComponent } from './auth/landing-page.component';
import { AuthErrorComponent } from './auth/auth-error.component';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  // Default redirect to landing page
  {
    path: '',
    redirectTo: 'landing',
    pathMatch: 'full'
  },

  // Landing page for authentication
  {
    path: 'landing',
    component: LandingPageComponent,
    title: 'Authentication Landing'
  },

  // Protected routes - require authentication
  {
    path: 'aum-reports',
    component: App,
    canActivate: [authGuard],
    title: 'Analytics Dashboard'
  },
//   {
//     path: 'upload',
//     component: App,
//     canActivate: [authGuard],
//     title: 'Data Upload'
//   },

  // Auth error page
  {
    path: 'auth-error',
    component: AuthErrorComponent,
    title: 'Authentication Error'
  },

  // Wildcard - redirect to landing
  {
    path: '**',
    redirectTo: 'landing',
    pathMatch: 'full'
  }
];
