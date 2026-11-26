import { Component, Input, Output, EventEmitter } from '@angular/core';
import { StatusBadgeComponent } from '../../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'tr[app-product-row]',
  standalone: true,
  imports: [StatusBadgeComponent],
  templateUrl: './product-row.component.html',
  styleUrl: './product-row.component.css',
})
export class ProductRowComponent {
  @Input() product: any;

  // Send the specific product back to the parent
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();

  getStatus(): 'success' | 'warning' | 'danger' {
    if (this.product.stockQuantity === 0) return 'danger';
    if (this.product.stockQuantity < 10) return 'warning';
    return 'success';
  }

  getStatusText(): string {
    if (this.product.stockQuantity === 0) return 'Épuisé';
    if (this.product.stockQuantity < 10) return 'Stock faible';
    return 'Disponible';
  }

  onEdit() {
    this.edit.emit(this.product);
  }
  onDelete() {
    this.delete.emit(this.product);
  }
}
