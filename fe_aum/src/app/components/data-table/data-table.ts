import { Component, inject, ChangeDetectionStrategy } from '@angular/core';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataTable {
  protected svc = inject(AumDataService);
  private readonly VIRTUAL_SCROLL_THRESHOLD = 50;
  private readonly BATCH_SIZE = 20;

  sortIcon(col: string): string {
    if (this.svc.sortCol() !== col) return '⇅';
    return this.svc.sortDir() === 'asc' ? '▲' : '▼';
  }

  onSort(col: string): void { this.svc.setSort(col); }

  onToggle(advisor: string): void { this.svc.toggleAdvisor(advisor); }

  get rowsInfo(): string {
    const groups = this.svc.advisorGroups();
    const filtered = this.svc.filteredRows();
    if (!groups.length) return 'No results';
    return `Showing ${groups.length} advisor${groups.length !== 1 ? 's' : ''} · ${filtered.length} rows`;
  }

  // Performance optimization: limit visible rows for large datasets
  get visibleGroups(): any[] {
    const groups = this.svc.advisorGroups();
    if (groups.length <= this.VIRTUAL_SCROLL_THRESHOLD) {
      return groups;
    }
    // For now, return first batch - in production, implement proper virtual scrolling
    return groups.slice(0, this.BATCH_SIZE);
  }

  get shouldShowVirtualScroll(): boolean {
    return this.svc.advisorGroups().length > this.VIRTUAL_SCROLL_THRESHOLD;
  }

  trackByAdvisor(index: number, group: any): string {
    return group.advisor;
  }

  trackByProvider(index: number, row: any): string {
    return row.provider;
  }
}
