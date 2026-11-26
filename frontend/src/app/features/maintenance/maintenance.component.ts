import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { CategoryService } from '../../core/services/category.service';
import { UnitService } from '../../core/services/unit.service';
import { Category, Unit } from '../../core/services/product.service';

type MaintenanceTab = 'categories' | 'units';

@Component({
  selector: 'app-maintenance',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    DialogModule,
    InputTextModule,
    ConfirmDialogModule,
  ],
  providers: [ConfirmationService],
  templateUrl: './maintenance.component.html',
  styleUrl: './maintenance.component.css',
})
export class MaintenanceComponent implements OnInit {
  private categoryService = inject(CategoryService);
  private unitService = inject(UnitService);
  private confirmationService = inject(ConfirmationService);

  activeTab: MaintenanceTab = 'categories';

  setTab(tab: MaintenanceTab) {
    this.activeTab = tab;
  }

  // --- Categories ---
  categories: Category[] = [];
  displayCategoryModal = false;
  isEditingCategory = false;
  newCategory: Category = { id: 0, name: '' };
  editingCategoryId: number | null = null;

  // --- Units ---
  units: Unit[] = [];
  displayUnitModal = false;
  isEditingUnit = false;
  newUnit: Unit = { id: 0, name: '' };
  editingUnitId: number | null = null;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.categoryService
      .getCategories()
      .subscribe((data) => (this.categories = data));
    this.unitService.getUnits().subscribe((data) => (this.units = data));
  }

  // --- Category CRUD ---
  showAddCategory() {
    this.isEditingCategory = false;
    this.editingCategoryId = null;
    this.newCategory = { id: 0, name: '' };
    this.displayCategoryModal = true;
  }

  showEditCategory(cat: Category) {
    this.isEditingCategory = true;
    this.editingCategoryId = cat.id;
    this.newCategory = { ...cat };
    this.displayCategoryModal = true;
  }

  saveCategory() {
    if (this.isEditingCategory && this.editingCategoryId) {
      this.categoryService
        .updateCategory(this.editingCategoryId, this.newCategory)
        .subscribe(() => {
          this.loadData();
          this.displayCategoryModal = false;
        });
    } else {
      this.categoryService.createCategory(this.newCategory).subscribe(() => {
        this.loadData();
        this.displayCategoryModal = false;
      });
    }
  }

  deleteCategory(cat: Category) {
    this.confirmationService.confirm({
      message: `Supprimer la catégorie "${cat.name}" ? Cela échouera si des produits l'utilisent.`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.categoryService.deleteCategory(cat.id).subscribe({
          next: () => this.loadData(),
          error: () =>
            alert(
              'Impossible de supprimer : cette catégorie est utilisée par un produit.',
            ),
        });
      },
    });
  }

  // --- Unit CRUD ---
  showAddUnit() {
    this.isEditingUnit = false;
    this.editingUnitId = null;
    this.newUnit = { id: 0, name: '' };
    this.displayUnitModal = true;
  }

  showEditUnit(unit: Unit) {
    this.isEditingUnit = true;
    this.editingUnitId = unit.id;
    this.newUnit = { ...unit };
    this.displayUnitModal = true;
  }

  saveUnit() {
    if (this.isEditingUnit && this.editingUnitId) {
      this.unitService
        .updateUnit(this.editingUnitId, this.newUnit)
        .subscribe(() => {
          this.loadData();
          this.displayUnitModal = false;
        });
    } else {
      this.unitService.createUnit(this.newUnit).subscribe(() => {
        this.loadData();
        this.displayUnitModal = false;
      });
    }
  }

  deleteUnit(unit: Unit) {
    this.confirmationService.confirm({
      message: `Supprimer l'unité "${unit.name}" ? Cela échouera si des produits l'utilisent.`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.unitService.deleteUnit(unit.id).subscribe({
          next: () => this.loadData(),
          error: () =>
            alert(
              'Impossible de supprimer : cette unité est utilisée par un produit.',
            ),
        });
      },
    });
  }
}
