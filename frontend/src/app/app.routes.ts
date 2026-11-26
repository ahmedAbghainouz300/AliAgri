import { Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { ProductsComponent } from './features/products/products.component';
import { DashboardComponent } from './features/dashboard/dashboard.component'; // Add this
import { SalesComponent } from './features/sales/sales.component';
import { CustomersComponent } from './features/customers/customers.component';
import { PurchasesComponent } from './features/purchases/purchases.component';
import { ProvidersComponent } from './features/providers/providers.component';
import { ReportsComponent } from './features/reports/reports.component';
import { MaintenanceComponent } from './features/maintenance/maintenance.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'sales', component: SalesComponent },
      { path: 'customers', component: CustomersComponent },
      { path: 'purchases', component: PurchasesComponent },
      { path: 'providers', component: ProvidersComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'maintenance', component: MaintenanceComponent },
    ],
  },
];
