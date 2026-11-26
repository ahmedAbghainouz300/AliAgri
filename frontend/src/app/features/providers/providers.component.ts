import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProviderRowComponent } from './components/provider-row/provider-row.component';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import {
  ProviderService,
  Provider,
} from '../../core/services/provider.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-providers',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProviderRowComponent,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  templateUrl: './providers.component.html',
  styleUrl: './providers.component.css',
})
export class ProvidersComponent implements OnInit {
  private providerService = inject(ProviderService);
  private confirmationService = inject(ConfirmationService);
  private http = inject(HttpClient);

  searchText: string = '';
  providers: Provider[] = [];
  loading = true;

  displayModal: boolean = false;
  isEditing: boolean = false;
  newProvider: Provider = { name: '', phone: '' };
  editingId: number | null = null;

  ngOnInit() {
    this.loadProviders();
  }

  loadProviders() {
    this.providerService.getProviders().subscribe({
      next: (data) => {
        this.providers = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching providers:', err);
        this.loading = false;
      },
    });
  }

  showAddModal() {
    this.isEditing = false;
    this.editingId = null;
    this.newProvider = { name: '', phone: '' };
    this.displayModal = true;
  }

  showEditModal(provider: Provider) {
    this.isEditing = true;
    this.editingId = provider.id!;
    this.newProvider = { ...provider };
    this.displayModal = true;
  }

  saveProvider() {
    if (this.isEditing && this.editingId) {
      // Update
      this.http
        .put<Provider>(
          `http://localhost:8080/api/providers/${this.editingId}`,
          this.newProvider,
        )
        .subscribe({
          next: (updated) => {
            const index = this.providers.findIndex(
              (p) => p.id === this.editingId,
            );
            if (index !== -1) this.providers[index] = updated;
            this.displayModal = false;
          },
        });
    } else {
      // Create
      this.providerService.createProvider(this.newProvider).subscribe({
        next: (saved) => {
          this.providers.push(saved);
          this.displayModal = false;
        },
      });
    }
  }

  confirmDelete(provider: Provider) {
    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer le fournisseur "${provider.name}" ?`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Annuler',
      accept: () => {
        this.http
          .delete<void>(`http://localhost:8080/api/providers/${provider.id}`)
          .subscribe(() => {
            this.providers = this.providers.filter((p) => p.id !== provider.id);
          });
      },
    });
  }

  get filteredProviders() {
    if (!this.searchText) return this.providers;
    return this.providers.filter(
      (p) =>
        p.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
        (p.phone && p.phone.includes(this.searchText)),
    );
  }
}
