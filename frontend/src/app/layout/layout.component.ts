import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import {
  LucideAngularModule,
  LayoutDashboard,
  Package,
  Users,
  Truck,
  ShoppingCart,
  FileText,
  Settings,
  Bell,
  Search,
} from 'lucide-angular';

@Component({
  selector: 'app-layout',
  standalone: true,
  // ONLY import the module here
  imports: [RouterLink, RouterLinkActive, RouterOutlet, LucideAngularModule],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css',
})
export class LayoutComponent {
  readonly icons = {
    LayoutDashboard,
    Package,
    Users,
    Truck,
    ShoppingCart,
    FileText,
    Settings,
    Bell,
    Search,
  };
}
