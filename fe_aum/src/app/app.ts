import { Component, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { Navbar } from './components/navbar/navbar';
import { DashboardHeader } from './components/dashboard-header/dashboard-header';
import { KpiCards } from './components/kpi-cards/kpi-cards';
import { FilterModal } from './components/filter-modal/filter-modal';
import { UploadModal } from './components/upload-modal/upload-modal';
import { ActiveChips } from './components/active-chips/active-chips';
import { DataTable } from './components/data-table/data-table';
import { AumDataService } from './services/aum-data';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [CommonModule, Navbar, DashboardHeader, KpiCards, FilterModal, UploadModal, ActiveChips, DataTable],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App implements OnInit {
  private svc = inject(AumDataService);
  private isDataLoaded = false;

  ngOnInit(): void {
    // Load data after a short delay to allow initial render
    setTimeout(() => {
      this.svc.loadData();
      this.isDataLoaded = true;
    }, 50);
  }

  get isLoading(): boolean {
    return !this.isDataLoaded;
  }
}
