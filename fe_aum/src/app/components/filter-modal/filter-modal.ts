import { Component, inject, AfterViewInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AumDataService } from '../../services/aum-data';
import { FilterConfig, RangeValue } from '../../models/aum.models';

@Component({
  selector: 'app-filter-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './filter-modal.html',
  styleUrl: './filter-modal.css',
})
export class FilterModal implements AfterViewInit, OnDestroy {
  protected svc = inject(AumDataService);

  // Update filter width to match the first table column. Store handler so we can remove listener.
  private _resizeHandler = () => this.updateWidthFromTable();
  private _retryTimer: any = null;
  private _retryCount = 0;

  ngAfterViewInit(): void {
    // initial measurement after view has rendered
    setTimeout(() => this.updateWidthFromTable(), 50);
    window.addEventListener('resize', this._resizeHandler);

    // start a short retry loop to catch table render timing (runs up to 10 times)
    this._retryCount = 0;
    this._retryTimer = setInterval(() => {
      this._retryCount++;
      this.updateWidthFromTable();
      if (this._retryCount >= 10) {
        clearInterval(this._retryTimer);
        this._retryTimer = null;
      }
    }, 200);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this._resizeHandler);
    if (this._retryTimer) { clearInterval(this._retryTimer); this._retryTimer = null; }
  }

  private updateWidthFromTable(): void {
    try {
      // Prefer to find the detailed table 'Account Number' header by exact or partial text
      let th: HTMLElement | null = null;
      const ths = Array.from(document.querySelectorAll('table thead th')) as HTMLElement[];
      if (ths.length) {
        th = ths.find(t => (t.textContent || '').trim().toLowerCase().includes('account number')) ?? ths[0];
      }
      // fallback to any first-child header if needed
      if (!th) th = document.querySelector('.min-w-full thead tr th:first-child, table thead tr th:first-child') as HTMLElement | null;
      const width = th ? Math.round(th.getBoundingClientRect().width) : 320;
      document.documentElement.style.setProperty('--filter-width', `${width}px`);
    } catch (e) {
      // ignore
    }
  }

  // typescript
  private setFilterWidthFromTable() {
    const ths = document.querySelectorAll('table thead th');
    if (!ths || ths.length === 0) return;
    // prefer header that contains "Account Number"
    let th = Array.from(ths).find(t => /account\s*number/i.test(t.textContent || '')) as HTMLElement
      || ths[0] as HTMLElement;
    const measured = th.getBoundingClientRect().width;
    // reduce by 20px and clamp minimum
    const reduced = Math.max(12, Math.round(measured - 10));
    document.documentElement.style.setProperty('--filter-width', `${reduced}px`);
  }


  getSet(key: string): Set<string> {
    return (this.svc.activeDraft()[key] as Set<string>) ?? new Set();
  }

  getRange(key: string): RangeValue {
    return (this.svc.activeDraft()[key] as RangeValue) ?? { min: '', max: '' };
  }

  getString(key: string): string {
    return (this.svc.activeDraft()[key] as string) ?? '';
  }

  toggleOption(key: string, opt: string, checked: boolean): void {
    const current = new Set(this.getSet(key));
    if (checked) current.add(opt); else current.delete(opt);
    this.svc.updateDraftValue(key, current);
  }

  clearField(f: FilterConfig): void {
    const v = f.defaultValue;
    if (Array.isArray(v)) this.svc.updateDraftValue(f.key, new Set());
    else if (v && typeof v === 'object') this.svc.updateDraftValue(f.key, { min: '', max: '' });
    else this.svc.updateDraftValue(f.key, v ?? '');
  }

  onDateChange(key: string, e: Event): void {
    this.svc.updateDraftValue(key, (e.target as HTMLInputElement).value);
  }

  onRangeChange(key: string, field: 'min' | 'max', e: Event): void {
    const current = { ...this.getRange(key) };
    current[field] = (e.target as HTMLInputElement).value;
    this.svc.updateDraftValue(key, current);
  }

  onStringChange(key: string, e: Event): void {
    this.svc.updateDraftValue(key, (e.target as HTMLInputElement).value);
  }


}
