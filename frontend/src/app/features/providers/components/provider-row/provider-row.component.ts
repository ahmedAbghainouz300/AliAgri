import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'tr[app-provider-row]',
  standalone: true,
  templateUrl: './provider-row.component.html',
  styleUrl: './provider-row.component.css',
})
export class ProviderRowComponent {
  @Input() provider: any;
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();

  onEdit() {
    this.edit.emit(this.provider);
  }
  onDelete() {
    this.delete.emit(this.provider);
  }
}
