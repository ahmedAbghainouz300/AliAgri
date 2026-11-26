import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { SaleService, SaleResponse } from '../../core/services/sale.service';
import { ProductService, Product } from '../../core/services/product.service';
import {
  CustomerService,
  Customer,
} from '../../core/services/customer.service';

@Component({
  selector: 'app-sales',
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
  templateUrl: './sales.component.html',
  styleUrl: './sales.component.css',
})
export class SalesComponent implements OnInit {
  private saleService = inject(SaleService);
  private productService = inject(ProductService);
  private customerService = inject(CustomerService);
  private confirmationService = inject(ConfirmationService);

  sales: SaleResponse[] = [];
  loading = true;

  // Modal Data
  displayModal = false;
  customers: Customer[] = [];
  products: Product[] = [];

  selectedCustomerId: number | null = null;
  currentItems: any[] = [];

  // Add item form
  selectedProductId: number | null = null;
  selectedQuantity: number = 1;
  selectedPrice: number = 0;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.saleService.getSales().subscribe((data) => {
      this.sales = data;
      this.loading = false;
    });
  }

  showModal() {
    this.displayModal = true;
    this.selectedCustomerId = null;
    this.currentItems = [];

    // Fetch dropdowns
    this.customerService
      .getCustomers()
      .subscribe((data) => (this.customers = data));
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

  saveSale() {
    if (!this.selectedCustomerId || this.currentItems.length === 0) return;

    const request = {
      customerId: this.selectedCustomerId,
      items: this.currentItems.map((i) => ({
        productId: i.productId,
        quantity: i.quantity,
        price: i.price,
      })),
    };

    this.saleService.createSale(request).subscribe({
      next: (newSale) => {
        this.sales.unshift(newSale);
        this.displayModal = false;
      },
      error: (err) => {
        console.error(err);
        alert('Erreur: Stock insuffisant ou problème serveur.');
      },
    });
  }

  deleteSale(sale: SaleResponse) {
    this.confirmationService.confirm({
      message: `Voulez-vous supprimer la vente #${sale.id} ?`,
      header: 'Confirmation',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Annuler',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.saleService.deleteSale(sale.id).subscribe(() => {
          this.sales = this.sales.filter((s) => s.id !== sale.id);
        });
      },
    });
  }
}
