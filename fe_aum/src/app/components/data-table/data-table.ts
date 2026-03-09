import { Component, inject } from '@angular/core';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.html',
  styleUrl: './data-table.css',
})
export class DataTable {
  protected svc = inject(AumDataService);

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
}
