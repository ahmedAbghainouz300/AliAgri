import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductRowComponent } from './components/product-row/product-row.component';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import {
  ProductService,
  Product,
  Category,
  Unit,
} from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { UnitService } from '../../core/services/unit.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ProductRowComponent,
    ButtonModule,
    DialogModule,
    InputTextModule,
    DropdownModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css',
})
export class ProductsComponent implements OnInit {
  private productService = inject(ProductService);
  private categoryService = inject(CategoryService);
  private unitService = inject(UnitService);
  private confirmationService = inject(ConfirmationService);

  searchText: string = '';
  products: Product[] = [];
  categories: Category[] = [];
  units: Unit[] = [];
  loading = true;

  displayModal: boolean = false;
  isEditing: boolean = false;
  newProduct: any = {
    name: '',
    licenseNumber: '',
    unitId: null,
    stockQuantity: 0,
    categoryId: null,
  };
  editingId: number | null = null;

  ngOnInit() {
    this.loadProducts();
    this.loadDropdowns();
  }

  loadProducts() {
    this.productService.getProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching products:', err);
        this.loading = false;
      },
    });
  }

  loadDropdowns() {
    this.categoryService.getCategories().subscribe({
      next: (data) => {
        this.categories = data;
        console.log('Categories loaded:', this.categories);
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      },
    });

    this.unitService.getUnits().subscribe({
      next: (data) => {
        this.units = data;
        console.log('Units loaded:', this.units);
      },
      error: (err) => {
        console.error('Error loading units:', err);
      },
    });
  }

  showAddModal() {
    this.isEditing = false;
    this.editingId = null;
    this.newProduct = {
      name: '',
      licenseNumber: '',
      unitId: null,
      stockQuantity: 0,
      categoryId: null,
    };
    this.displayModal = true;
  }

  showEditModal(product: Product) {
    this.isEditing = true;
    this.editingId = product.id!;
    this.newProduct = {
      name: product.name,
      licenseNumber: product.licenseNumber,
      unitId: product.unitId,
      stockQuantity: product.stockQuantity,
      categoryId: product.categoryId,
    };
    this.displayModal = true;
  }

  saveProduct() {
    if (this.isEditing && this.editingId) {
      this.productService
        .updateProduct(this.editingId, this.newProduct)
        .subscribe({
          next: (updated) => {
            const index = this.products.findIndex(
              (p) => p.id === this.editingId,
            );
            if (index !== -1) this.products[index] = updated;
            this.displayModal = false;
          },
          error: (err) => console.error('Error updating product:', err),
        });
    } else {
      this.productService.createProduct(this.newProduct).subscribe({
        next: (saved) => {
          this.products.push(saved);
          this.displayModal = false;
        },
        error: (err) => console.error('Error creating product:', err),
      });
    }
  }

  confirmDelete(product: Product) {
    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer le produit "${product.name}" ?`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui, supprimer',
      rejectLabel: 'Annuler',
      accept: () => {
        this.productService.deleteProduct(product.id!).subscribe(() => {
          this.products = this.products.filter((p) => p.id !== product.id);
        });
      },
    });
  }

  get filteredProducts() {
    if (!this.searchText) return this.products;
    return this.products.filter(
      (p) =>
        p.name.toLowerCase().includes(this.searchText.toLowerCase()) ||
        p.licenseNumber.toLowerCase().includes(this.searchText.toLowerCase()),
    );
  }
}
