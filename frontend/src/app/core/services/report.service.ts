import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ReportRow {
  id: number;
  type: 'VENTE' | 'ACHAT';
  date: string;
  partyName: string;
  partyId: string;
  productName: string;
  licenseNumber: string;
  unit: string;
  quantity: number;
  price: number;
  totalAmount: number;
}

export interface ReportFilter {
  startDate?: string;
  endDate?: string;
  customerId?: number;
  providerId?: number;
  productId?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private http = inject(HttpClient);
  private apiUrl = '/api/reports';

  downloadRegistre(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/registre`, { responseType: 'blob' });
  }

  getReportData(filter: ReportFilter): Observable<ReportRow[]> {
    let params = new HttpParams();

    // Only add parameters if they have a real value
    if (filter.startDate) params = params.set('startDate', filter.startDate);
    if (filter.endDate) params = params.set('endDate', filter.endDate);
    if (filter.customerId) params = params.set('customerId', filter.customerId);
    if (filter.providerId) params = params.set('providerId', filter.providerId);
    if (filter.productId) params = params.set('productId', filter.productId);

    return this.http.get<ReportRow[]>(`${this.apiUrl}/data`, { params });
  }
}
