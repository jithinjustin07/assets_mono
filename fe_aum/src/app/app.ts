import { Component, OnInit, inject, ChangeDetectionStrategy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { AuthService } from './auth/auth.service';

// Import app components
import { Navbar } from './components/navbar/navbar';
import { DashboardHeader } from './components/dashboard-header/dashboard-header';
import { KpiCards } from './components/kpi-cards/kpi-cards';
import { FilterModal } from './components/filter-modal/filter-modal';
import { UploadModal } from './components/upload-modal/upload-modal';
import { ActiveChips } from './components/active-chips/active-chips';
import { DataTable } from './components/data-table/data-table';
import { AumDataService } from './services/aum-data';

@Component({
  selector: 'app-root',
  imports: [
    CommonModule, 
    RouterOutlet,
    Navbar, 
    DashboardHeader, 
    KpiCards, 
    FilterModal, 
    UploadModal, 
    ActiveChips, 
    DataTable
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App implements OnInit {
  private authService = inject(AuthService);
  protected svc = inject(AumDataService);
  private isDataLoaded = false;

  public isAuthenticated = computed(() => this.authService.authState() === 'authenticated');

  ngOnInit(): void {
    // Only load data - auth is handled by routing
    setTimeout(() => {
      this.svc.loadData();
      this.isDataLoaded = true;
    }, 50);
  }

  get isLoading(): boolean {
    return !this.isDataLoaded;
  }
}
