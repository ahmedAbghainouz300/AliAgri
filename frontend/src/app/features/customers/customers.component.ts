import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CustomerRowComponent } from './components/customer-row/customer-row.component';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import {
  CustomerService,
  Customer,
} from '../../core/services/customer.service';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    CustomerRowComponent,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.css',
})
export class CustomersComponent implements OnInit {
  private customerService = inject(CustomerService);
  private confirmationService = inject(ConfirmationService);

  searchText: string = '';
  customers: Customer[] = [];
  loading = true;

  displayModal: boolean = false;
  isEditing: boolean = false;
  newCustomer: Customer = { name: '', idCardNumber: '', address: '' };
  editingId: number | null = null;

  ngOnInit() {
    this.loadCustomers();
  }

  loadCustomers() {
    this.customerService.getCustomers().subscribe({
      next: (data) => {
        this.customers = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching customers:', err);
        this.loading = false;
      },
    });
  }

  showAddModal() {
    this.isEditing = false;
    this.editingId = null;
    this.newCustomer = { name: '', idCardNumber: '', address: '' };
    this.displayModal = true;
  }

  showEditModal(customer: Customer) {
    this.isEditing = true;
    this.editingId = customer.id!;
    this.newCustomer = { ...customer };
    this.displayModal = true;
  }

  saveCustomer() {
    if (this.isEditing && this.editingId) {
      this.customerService
        .updateCustomer(this.editingId, this.newCustomer)
        .subscribe({
          next: (updated) => {
            const index = this.customers.findIndex(
              (c) => c.id === this.editingId,
            );
            if (index !== -1) this.customers[index] = updated;
            this.displayModal = false;
          },
        });
    } else {
      this.customerService.createCustomer(this.newCustomer).subscribe({
        next: (saved) => {
          this.customers.push(saved);
          this.displayModal = false;
        },
      });
    }
  }

  confirmDelete(customer: Customer) {
    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer le client "${customer.name}" ?`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Annuler',
      accept: () => {
        this.customerService.deleteCustomer(customer.id!).subscribe(() => {
          this.customers = this.customers.filter((c) => c.id !== customer.id);
        });
      },
    });
  }

  get filteredCustomers() {
    if (!this.searchText) return this.customers;
    return this.customers.filter(
      (c) =>
        c.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
        c.idCardNumber.toLowerCase().includes(this.searchText.toLowerCase()),
    );
  }
}
