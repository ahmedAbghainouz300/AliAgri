import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'tr[app-customer-row]',
  standalone: true,
  templateUrl: './customer-row.component.html',
  styleUrl: './customer-row.component.css',
})
export class CustomerRowComponent {
  @Input() customer: any;
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();

  onEdit() {
    this.edit.emit(this.customer);
  }
  onDelete() {
    this.delete.emit(this.customer);
  }
}
