import { Component, inject } from '@angular/core';
import { AumDataService } from '../../services/aum-data';
import { FilterConfig, RangeValue } from '../../models/aum.models';

@Component({
  selector: 'app-active-chips',
  imports: [],
  templateUrl: './active-chips.html',
  styleUrl: './active-chips.css',
})
export class ActiveChips {
  protected svc = inject(AumDataService);

  get activeFilters(): Array<{ config: FilterConfig; label: string }> {
    const filters = this.svc.activeFilters();
    const config = this.svc.activeFilterConfig();
    const result: Array<{ config: FilterConfig; label: string }> = [];
    config.forEach(f => {
      if (!f.matchable) return;
      const v = filters[f.key];
      let text = '';
      if (v instanceof Set) {
        if (v.size === 0) return;
        text = [...v].join(', ');
      } else if (v && typeof v === 'object') {
        const rv = v as RangeValue;
        const parts: string[] = [];
        if (rv.min !== '') parts.push('≥ $' + Number(rv.min).toLocaleString());
        if (rv.max !== '') parts.push('≤ $' + Number(rv.max).toLocaleString());
        if (!parts.length) return;
        text = parts.join(' & ');
      } else {
        if (!v) return;
        text = v as string;
      }
      result.push({ config: f, label: text });
    });
    return result;
  }

  removeChip(key: string): void { this.svc.removeFilter(key); }
}
