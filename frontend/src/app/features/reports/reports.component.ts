import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import {
  ReportService,
  ReportRow,
  ReportFilter,
} from '../../core/services/report.service';
import { ProductService, Product } from '../../core/services/product.service';
import {
  CustomerService,
  Customer,
} from '../../core/services/customer.service';
import {
  ProviderService,
  Provider,
} from '../../core/services/provider.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    DropdownModule,
    CalendarModule,
  ],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css',
})
export class ReportsComponent implements OnInit {
  private reportService = inject(ReportService);
  private productService = inject(ProductService);
  private customerService = inject(CustomerService);
  private providerService = inject(ProviderService);

  rows: ReportRow[] = [];
  loading = true;

  // Filter Data
  filter: ReportFilter = {};
  products: Product[] = [];
  customers: Customer[] = [];
  providers: Provider[] = [];

  // Selected values for dropdowns
  selectedProductId: number | null = null;
  selectedCustomerId: number | null = null;
  selectedProviderId: number | null = null;
  dateRange: Date[] | null = null;

  // Excel Download state
  isDownloading = false;
  downloadSuccess = false;

  ngOnInit() {
    this.loadDropdowns();
    this.loadData();
  }

  loadDropdowns() {
    this.productService
      .getProducts()
      .subscribe((data) => (this.products = data));
    this.customerService
      .getCustomers()
      .subscribe((data) => (this.customers = data));
    this.providerService
      .getProviders()
      .subscribe((data) => (this.providers = data));
  }

  loadData() {
    this.loading = true;

    const filter: ReportFilter = {};

    if (
      this.dateRange &&
      this.dateRange.length === 2 &&
      this.dateRange[0] &&
      this.dateRange[1]
    ) {
      filter.startDate = this.formatDate(this.dateRange[0]);
      filter.endDate = this.formatDate(this.dateRange[1]);
    }

    if (this.selectedCustomerId) filter.customerId = this.selectedCustomerId;
    if (this.selectedProviderId) filter.providerId = this.selectedProviderId;
    if (this.selectedProductId) filter.productId = this.selectedProductId;

    console.log('Sending filter to backend:', filter);

    this.reportService.getReportData(filter).subscribe({
      next: (data) => {
        this.rows = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading report', err);
        this.loading = false;
      },
    });
  }

  clearFilters() {
    this.dateRange = null;
    this.selectedCustomerId = null;
    this.selectedProviderId = null;
    this.selectedProductId = null;
    this.loadData();
  }

  downloadExcel() {
    this.isDownloading = true;
    this.downloadSuccess = false;

    this.reportService.downloadExcel().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `registre_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.isDownloading = false;
        this.downloadSuccess = true;
        setTimeout(() => (this.downloadSuccess = false), 3000);
      },
      error: () => {
        this.isDownloading = false;
        alert('Erreur lors du téléchargement.');
      },
    });
  }

  downloadPdf() {
    this.isDownloading = true;
    this.downloadSuccess = false;

    this.reportService.downloadPdf().subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `registre_${new Date().toISOString().split('T')[0]}.pdf`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
        this.isDownloading = false;
        this.downloadSuccess = true;
        setTimeout(() => (this.downloadSuccess = false), 3000);
      },
      error: () => {
        this.isDownloading = false;
        alert('Erreur lors du téléchargement du PDF.');
      },
    });
  }

  formatDate(date: Date): string {
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
    return [year, month, day].join('-');
  }

  getTotalSales() {
    return this.rows
      .filter((r) => r.type === 'VENTE')
      .reduce((sum, r) => sum + r.totalAmount, 0);
  }

  getTotalPurchases() {
    return this.rows
      .filter((r) => r.type === 'ACHAT')
      .reduce((sum, r) => sum + r.totalAmount, 0);
  }
}
