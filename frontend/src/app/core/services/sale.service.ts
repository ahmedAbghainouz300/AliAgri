import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SaleRequest {
  customerId: number;
  items: {
    productId: number;
    quantity: number;
    price: number;
  }[];
}

export interface SaleResponse {
  id: number;
  saleDate: string;
  totalAmount: number;
  customer: {
    id: number;
    name: string;
    idCardNumber: string;
    address: string;
  };
  items: {
    id: number;
    productName: string;
    licenseNumber: string;
    quantity: number;
    price: number;
  }[];
}

@Injectable({
  providedIn: 'root',
})
export class SaleService {
  private http = inject(HttpClient);
  private apiUrl = '/api/sales';

  getSales(): Observable<SaleResponse[]> {
    return this.http.get<SaleResponse[]>(this.apiUrl);
  }

  createSale(sale: SaleRequest): Observable<SaleResponse> {
    return this.http.post<SaleResponse>(this.apiUrl, sale);
  }

  deleteSale(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
