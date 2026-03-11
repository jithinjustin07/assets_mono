import { environment } from '../../environments/environment';

/**
 * API Endpoints Configuration
 * Centralized access to all API endpoints using environment configuration
 */
export class ApiEndpoints {
  private static baseUrl = environment.api.baseUrl;
  private static endpoints = environment.api.endpoints;

  // Authentication endpoints
  static get auth() {
    return this.endpoints.auth;
  }

  // Custodian endpoints
  static get custodians() {
    return this.endpoints.custodians;
  }

  // AUM Reports endpoints
  static get aumReports() {
    return this.endpoints.aumReports;
  }

//   // Assets endpoints
//   static get assets() {
//     return this.endpoints.assets;
//   }

  // Users endpoints
  static get users() {
    return this.endpoints.users;
  }

  // Reports endpoints
  static get reports() {
    return this.endpoints.reports;
  }

  // Dashboard endpoints
  static get dashboard() {
    return this.endpoints.dashboard;
  }

  // Settings endpoints
  static get settings() {
    return this.endpoints.settings;
  }

  /**
   * Get full URL for an endpoint
   * @param endpoint API endpoint path
   */
  static getFullUrl(endpoint: string): string {
    return `${this.baseUrl}${endpoint}`;
  }

  /**
   * Replace path parameters in endpoint URL
   * @param endpoint API endpoint path with placeholders
   * @param params Object containing parameter values
   */
  static replacePathParams(endpoint: string, params: any): string {
    let url = endpoint;
    Object.keys(params).forEach(key => {
      url = url.replace(`:${key}`, params[key]);
    });
    return url;
  }

  /**
   * Get all available endpoints for reference
   */
  static getAllEndpoints() {
    return {
      auth: this.auth,
      custodians: this.custodians,
      aumReports: this.aumReports,
//       assets: this.assets,
      users: this.users,
      reports: this.reports,
      dashboard: this.dashboard,
      settings: this.settings
    };
  }
}
