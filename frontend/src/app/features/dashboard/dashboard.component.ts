import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  LucideAngularModule,
  TrendingUp,
  Package,
  ShoppingCart,
  AlertCircle,
} from 'lucide-angular';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  readonly TrendingUp = TrendingUp;
  readonly Package = Package;
  readonly ShoppingCart = ShoppingCart;
  readonly AlertCircle = AlertCircle;
}
