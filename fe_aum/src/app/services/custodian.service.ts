import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { ApiEndpoints } from '../config/api.endpoints';

export interface Custodian {
  id?: string;
  name: string;
  email?: string;
  phone?: string;
  address?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CustodianListResponse {
  data: Custodian[];
  total: number;
  page: number;
  limit: number;
}

@Injectable({
  providedIn: 'root'
})
export class CustodianService {
  constructor(private apiService: ApiService) {}

  /**
   * Get all custodians
   * @param params Optional query parameters (page, limit, search, etc.)
   */
  getCustodians(params?: any): Observable<CustodianListResponse> {
    return this.apiService.get<CustodianListResponse>(
      ApiEndpoints.custodians.list,
      params
    );
  }

  /**
   * Get custodian by ID
   * @param id Custodian ID
   */
  getCustodianById(id: string): Observable<Custodian> {
    const endpoint = ApiEndpoints.replacePathParams(
      ApiEndpoints.custodians.details,
      { id }
    );
    return this.apiService.get<Custodian>(endpoint);
  }

  /**
   * Create a new custodian
   * @param custodian Custodian data
   */
  createCustodian(custodian: Omit<Custodian, 'id'>): Observable<Custodian> {
    return this.apiService.post<Custodian>(
      ApiEndpoints.custodians.create,
      custodian
    );
  }

  /**
   * Update an existing custodian
   * @param id Custodian ID
   * @param custodian Updated custodian data
   */
  updateCustodian(id: string, custodian: Partial<Custodian>): Observable<Custodian> {
    const endpoint = ApiEndpoints.replacePathParams(
      ApiEndpoints.custodians.update,
      { id }
    );
    return this.apiService.put<Custodian>(endpoint, custodian);
  }

  /**
   * Delete a custodian
   * @param id Custodian ID
   */
  deleteCustodian(id: string): Observable<void> {
    const endpoint = ApiEndpoints.replacePathParams(
      ApiEndpoints.custodians.delete,
      { id }
    );
    return this.apiService.delete<void>(endpoint);
  }
}
