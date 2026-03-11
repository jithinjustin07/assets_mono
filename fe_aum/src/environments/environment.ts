export const environment = {
  production: true,

  // API Configuration
  api: {
    // Base URL for your backend API - Production
    baseUrl: 'http://localhost:8080',

    // API Endpoints
    endpoints: {
      // Authentication endpoints
      auth: {
        login: '/auth/login',
        logout: '/auth/logout',
        refresh: '/auth/refresh',
        verify: '/auth/verify'
      },

      // Custodian endpoints
      custodians: {
        list: '/api/custodians',
        create: '/custodians',
        update: '/custodians/:id',
        delete: '/custodians/:id',
        details: '/custodians/:id'
      },

      // AUM Reports endpoints
      aumReports: {
        list: '/aum-reports',
        create: '/aum-reports',
        update: '/aum-reports/:id',
        delete: '/aum-reports/:id',
        export: '/aum-reports/export',
        details: '/aum-reports/:id'
      },

     aum: {
            list: '/api/aum/data',
            create: '/api/aum',
            update: '/aum-reports/:id',
            delete: '/aum-reports/:id',
            export: '/aum-reports/export',
            details: '/aum-reports/:id'
          },

      // vendors
      vendors: {
        list: '/api/vendors',
        create: '/assets',
        update: '/assets/:id',
        delete: '/assets/:id',
        details: '/assets/:id'
      },

      // Users endpoints
      users: {
        list: '/users',
        create: '/users',
        update: '/users/:id',
        delete: '/users/:id',
        profile: '/users/profile',
        details: '/users/:id'
      },

      // Reports endpoints
      reports: {
        list: '/reports',
        generate: '/reports/generate',
        download: '/reports/:id/download',
        delete: '/reports/:id',
        details: '/reports/:id'
      },

      // Dashboard endpoints
      dashboard: {
        summary: '/dashboard/summary',
        metrics: '/dashboard/metrics',
        charts: '/dashboard/charts'
      },

      // Settings endpoints
      settings: {
        get: '/settings',
        update: '/settings',
        preferences: '/settings/preferences'
      }
    },

    // Request configuration - Production timeout
    timeout: 45000, // 45 seconds

    // Default headers
    defaultHeaders: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  }
};
