import {
  Component,
  inject,
  ElementRef,
  ViewChild,
  AfterViewInit,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AumDataService } from '../../services/aum-data';

@Component({
  selector: 'app-dashboard-header',
  imports: [CommonModule],
  templateUrl: './dashboard-header.html',
  styleUrl: './dashboard-header.css',
})
export class DashboardHeader implements AfterViewInit {
  protected svc = inject(AumDataService);
  protected showColumnPicker = signal(false);

  @ViewChild('dashboardTab') dashboardTab!: ElementRef<HTMLButtonElement>;
  @ViewChild('dataTab') dataTab!: ElementRef<HTMLButtonElement>;
  @ViewChild('tabIndicator') tabIndicator!: ElementRef<HTMLDivElement>;
  @ViewChild('tabSwitcher') tabSwitcher!: ElementRef<HTMLDivElement>;

  ngAfterViewInit() {
    const indicator = this.tabIndicator?.nativeElement;
    if (!indicator) return;

    // Set initial position without animating
    indicator.style.transition = 'none';
    this.updateTabIndicator();

    // Enable smooth animation after first frame
    requestAnimationFrame(() => {
      indicator.style.transition =
        'transform 0.45s cubic-bezier(0.22, 1, 0.36, 1), width 0.45s cubic-bezier(0.22, 1, 0.36, 1)';
    });
  }

  private updateTabIndicator() {
    if (!this.dashboardTab || !this.dataTab || !this.tabIndicator || !this.tabSwitcher) {
      return;
    }

    const isData = this.svc.viewMode() === 'data';
    const activeEl = (isData ? this.dataTab : this.dashboardTab).nativeElement;

    // Get the text span inside the button for precise positioning
    const textSpan = activeEl.querySelector('.tab-text') as HTMLElement;
    if (!textSpan) return;

    const container = this.tabSwitcher.nativeElement;

    // Try using offsetLeft for more reliable positioning
    let currentEl = textSpan;
    let offsetLeft = 0;

    while (currentEl && currentEl !== container) {
      offsetLeft += currentEl.offsetLeft;
      currentEl = currentEl.offsetParent as HTMLElement;
    }

    const textWidth = textSpan.offsetWidth;

    const indicator = this.tabIndicator.nativeElement;
    indicator.style.width = `${textWidth}px`;
    indicator.style.transform = `translateX(${offsetLeft}px)`;

    console.log('Final styles:', {
      width: `${textWidth}px`,
      transform: `translateX(${offsetLeft}px)`
    });
  }

  protected onTabClick(tab: 'summary' | 'data') {
    if (this.svc.viewMode() === tab) return;

    this.svc.setViewMode(tab);
    this.svc.clearProviderFilter();
    this.showColumnPicker.set(false);

    requestAnimationFrame(() => this.updateTabIndicator());
  }

  protected toggleColumnPicker() {
    this.showColumnPicker.update((v: boolean) => !v);
  }

  protected isAllColumnsSelected(): boolean {
    return this.svc.visibleDataColumns().size === this.svc.dataColumns().length;
  }

  protected onToggleAll(event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    this.svc.toggleAllColumns(checked);
  }
}
