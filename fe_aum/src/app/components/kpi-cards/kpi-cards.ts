import { Component, inject } from '@angular/core';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-kpi-cards',
  imports: [],
  templateUrl: './kpi-cards.html',
  styleUrl: './kpi-cards.css',
})
export class KpiCards {
  protected svc = inject(AumDataService);

  pct(part: number, total: number): string {
    return total ? Math.round((part / total) * 100) + '%' : '0%';
  }
}
