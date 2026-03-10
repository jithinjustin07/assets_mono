import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl: string;

  constructor(private http: HttpClient) {
    this.baseUrl = environment.api.baseUrl;
  }

  /**
   * Generic GET request
   * @param endpoint API endpoint path
   * @param params Optional query parameters
   * @param headers Optional additional headers
   */
  get<T>(endpoint: string, params?: any, headers?: any): Observable<T> {
    const httpParams = this.buildParams(params);
    const httpHeaders = this.buildHeaders(headers);
    
    return this.http.get<T>(`${this.baseUrl}${endpoint}`, {
      params: httpParams,
      headers: httpHeaders
    });
  }

  /**
   * Generic POST request
   * @param endpoint API endpoint path
   * @param body Request body
   * @param headers Optional additional headers
   */
  post<T>(endpoint: string, body?: any, headers?: any): Observable<T> {
    const httpHeaders = this.buildHeaders(headers);
    
    return this.http.post<T>(`${this.baseUrl}${endpoint}`, body, {
      headers: httpHeaders
    });
  }

  /**
   * Generic PUT request
   * @param endpoint API endpoint path
   * @param body Request body
   * @param headers Optional additional headers
   */
  put<T>(endpoint: string, body?: any, headers?: any): Observable<T> {
    const httpHeaders = this.buildHeaders(headers);
    
    return this.http.put<T>(`${this.baseUrl}${endpoint}`, body, {
      headers: httpHeaders
    });
  }

  /**
   * Generic DELETE request
   * @param endpoint API endpoint path
   * @param params Optional query parameters
   * @param headers Optional additional headers
   */
  delete<T>(endpoint: string, params?: any, headers?: any): Observable<T> {
    const httpParams = this.buildParams(params);
    const httpHeaders = this.buildHeaders(headers);
    
    return this.http.delete<T>(`${this.baseUrl}${endpoint}`, {
      params: httpParams,
      headers: httpHeaders
    });
  }

  /**
   * Generic PATCH request
   * @param endpoint API endpoint path
   * @param body Request body
   * @param headers Optional additional headers
   */
  patch<T>(endpoint: string, body?: any, headers?: any): Observable<T> {
    const httpHeaders = this.buildHeaders(headers);
    
    return this.http.patch<T>(`${this.baseUrl}${endpoint}`, body, {
      headers: httpHeaders
    });
  }

  /**
   * Build HTTP parameters from object
   * @param params Object containing query parameters
   */
  private buildParams(params?: any): HttpParams {
    let httpParams = new HttpParams();
    
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key].toString());
        }
      });
    }
    
    return httpParams;
  }

  /**
   * Build HTTP headers combining default headers with custom headers
   * @param customHeaders Optional additional headers
   */
  private buildHeaders(customHeaders?: any): HttpHeaders {
    let headers = new HttpHeaders(environment.api.defaultHeaders);
    
    if (customHeaders) {
      Object.keys(customHeaders).forEach(key => {
        if (customHeaders[key] !== null && customHeaders[key] !== undefined) {
          headers = headers.set(key, customHeaders[key]);
        }
      });
    }
    
    return headers;
  }

  /**
   * Get full URL for an endpoint
   * @param endpoint API endpoint path
   */
  getFullUrl(endpoint: string): string {
    return `${this.baseUrl}${endpoint}`;
  }

  /**
   * Replace path parameters in endpoint URL
   * @param endpoint API endpoint path with placeholders
   * @param params Object containing parameter values
   */
  replacePathParams(endpoint: string, params: any): string {
    let url = endpoint;
    Object.keys(params).forEach(key => {
      url = url.replace(`:${key}`, params[key]);
    });
    return url;
  }
}
