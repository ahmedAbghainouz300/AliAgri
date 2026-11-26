import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import {
  PurchaseService,
  PurchaseResponse,
} from '../../core/services/purchase.service';
import { ProductService, Product } from '../../core/services/product.service';
import {
  ProviderService,
  Provider,
} from '../../core/services/provider.service';

@Component({
  selector: 'app-purchases',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputNumberModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  templateUrl: './purchases.component.html',
  styleUrl: './purchases.component.css',
})
export class PurchasesComponent implements OnInit {
  private purchaseService = inject(PurchaseService);
  private productService = inject(ProductService);
  private providerService = inject(ProviderService);
  private confirmationService = inject(ConfirmationService);

  purchases: PurchaseResponse[] = [];
  loading = true;

  // Modal Data
  displayModal = false;
  providers: Provider[] = [];
  products: Product[] = [];

  selectedProviderId: number | null = null;
  currentItems: any[] = [];

  // Add item form
  selectedProductId: number | null = null;
  selectedQuantity: number = 1;
  selectedPrice: number = 0;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.purchaseService.getPurchases().subscribe((data) => {
      this.purchases = data;
      this.loading = false;
    });
  }

  showModal() {
    this.displayModal = true;
    this.selectedProviderId = null;
    this.currentItems = [];

    this.providerService
      .getProviders()
      .subscribe((data) => (this.providers = data));
    this.productService
      .getProducts()
      .subscribe((data) => (this.products = data));
  }

  addItem() {
    if (!this.selectedProductId || this.selectedQuantity <= 0) return;

    const prod = this.products.find((p) => p.id === this.selectedProductId);
    if (!prod) return;

    this.currentItems.push({
      productId: prod.id,
      productName: prod.name,
      quantity: this.selectedQuantity,
      price: this.selectedPrice,
    });

    this.selectedProductId = null;
    this.selectedQuantity = 1;
    this.selectedPrice = 0;
  }

  removeItem(index: number) {
    this.currentItems.splice(index, 1);
  }

  getTotal() {
    return this.currentItems.reduce(
      (acc, item) => acc + item.quantity * item.price,
      0,
    );
  }

  savePurchase() {
    if (!this.selectedProviderId || this.currentItems.length === 0) return;

    const request = {
      providerId: this.selectedProviderId,
      items: this.currentItems.map((i) => ({
        productId: i.productId,
        quantity: i.quantity,
        price: i.price,
      })),
    };

    this.purchaseService.createPurchase(request).subscribe({
      next: (newPurchase) => {
        this.purchases.unshift(newPurchase);
        this.displayModal = false;
      },
      error: (err) => {
        console.error(err);
        alert("Erreur lors de l'enregistrement de l'achat.");
      },
    });
  }

  deletePurchase(purchase: PurchaseResponse) {
    this.confirmationService.confirm({
      message: `Voulez-vous supprimer l'achat #${purchase.id} ?`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.purchaseService.deletePurchase(purchase.id).subscribe(() => {
          this.purchases = this.purchases.filter((p) => p.id !== purchase.id);
        });
      },
    });
  }
}
