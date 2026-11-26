import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PurchaseRequest {
  providerId: number;
  items: {
    productId: number;
    quantity: number;
    price: number;
  }[];
}

export interface PurchaseResponse {
  id: number;
  purchaseDate: string;
  totalAmount: number;
  provider: {
    id: number;
    name: string;
    phone: string;
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
export class PurchaseService {
  private http = inject(HttpClient);
  private apiUrl = '/api/purchases'; // Using the Nginx proxy path

  getPurchases(): Observable<PurchaseResponse[]> {
    return this.http.get<PurchaseResponse[]>(this.apiUrl);
  }

  createPurchase(purchase: PurchaseRequest): Observable<PurchaseResponse> {
    return this.http.post<PurchaseResponse>(this.apiUrl, purchase);
  }

  deletePurchase(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
