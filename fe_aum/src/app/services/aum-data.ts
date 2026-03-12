import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  AumRow,
  FilterConfig,
  FilterState,
  RangeValue,
  KpiData,
  AdvisorGroup,
  AumDashboardData,
  BackendDataResponse,
  Custodian,
  Advisor,
} from '../models/aum.models';
import * as XLSX from 'xlsx';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AumDataService {
  // ── Raw data loaded from API ───────────────────────────────────────────
  readonly dashboardData = signal<AumDashboardData | null>(null);
  readonly backendData = signal<BackendDataResponse[]>([]);
  readonly custodians = signal<Custodian[]>([]);
  readonly advisors = signal<Advisor[]>([]);
  readonly loading = signal<boolean>(true);

  // ── Filter & sort state ─────────────────────────────────────────────────
  readonly sortCol = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly searchQuery = signal<string>('');
  readonly collapsed = signal<Record<string, boolean>>({});
  // signal for upload dialog
  readonly uploadModalOpen = signal<boolean>(false);
  // view mode: 'summary' (Dashboard), 'data' (Data Tab)
  readonly viewMode = signal<'summary' | 'data'>('summary');
  // active data-provider drill-down filter (null means no filter)
  readonly dataProviderFilter = signal<{ advisor: string; provider: string } | null>(null);

  // ── Dashboard Filter State ───────────────────────────────────────────────
  readonly filters = signal<FilterState>({});
  readonly draft = signal<FilterState>({});

  // ── Data Tab Filter State ────────────────────────────────────────────────
  readonly dataFilters = signal<FilterState>({});
  readonly dataDraft = signal<FilterState>({});

  // ── Column Visibility State ──────────────────────────────────────────────
  readonly dataColumns = signal<string[]>([
    "Account Number", "Account Name", "Data Provider", "Account Supervised",
    "Market Value", "AUM", "Relationship Manager", "Advisor", "Start Date",
    "As of Date", "Closed Date", "Relationship Name", "AUA", "Platform"
  ]);

  readonly visibleDataColumns = signal<Set<string>>(new Set([
    "Account Number",
    "Account Name",
    "Data Provider",
    "Market Value",
    "AUM",
    "Relationship Manager",
    "Advisor",
    "Start Date",
    "As of Date",
    "Relationship Name",
    "Platform"
  ]));

  // ── Derived: rows with total ─────────────────────────────────────────────
  readonly allRows = computed<AumRow[]>(() => {
    const data = this.dashboardData();
    if (!data) return [];
    return data.rows.map(r => ({ ...r, total: r.addepar + r.blackdiamond }));
  });

  readonly filterConfig = computed<FilterConfig[]>(() => this.dashboardData()?.filterConfig ?? []);

  // ── Data Tab Specific Config ─────────────────────────────────────────────
  readonly dataFilterConfig = computed<FilterConfig[]>(() => [
    { key: 'Data Provider', label: 'Data Provider', type: 'multiselect', icon: 'hub', options: this.getCustodianNames(), defaultValue: [], matchable: true },
    { key: 'Advisor', label: 'Advisor', type: 'multiselect', icon: 'person', options: this.getAdvisorNames(), defaultValue: [], matchable: true },
    { key: 'Account Supervised', label: 'Account Supervised', type: 'multiselect', icon: 'security', options: this.getUnique('Account Supervised'), defaultValue: [], matchable: true },
    { key: 'AUM', label: 'AUM', type: 'multiselect', icon: 'check_circle', options: this.getUnique('AUM'), defaultValue: [], matchable: true },
    { key: 'Platform', label: 'Platform', type: 'multiselect', icon: 'layers', options: this.getUnique('Platform'), defaultValue: [], matchable: true }
  ]);

  // ── View-Aware Accessors ─────────────────────────────────────────────────
  readonly activeFilterConfig = computed(() => this.viewMode() === 'summary' ? this.filterConfig() : this.dataFilterConfig());
  readonly activeFilters = computed(() => this.viewMode() === 'summary' ? this.filters() : this.dataFilters());
  readonly activeDraft = computed(() => this.viewMode() === 'summary' ? this.draft() : this.dataDraft());
  readonly activeFilterCount = computed<number>(() => {
    const filters = this.activeFilters();
    const config = this.activeFilterConfig();
    return config.filter(f => {
      if (!f.matchable) return false;
      const v = filters[f.key];
      if (v instanceof Set) {
        if (f.key === 'aumFilter' && v.size === 1 && v.has('All')) return false;
        return v.size > 0;
      }
      if (v && typeof v === 'object') return (v as RangeValue).min !== '' || (v as RangeValue).max !== '';
      return v !== '' && v !== (f.defaultValue as string);
    }).length;
  });

  // ── All detailed table rows (dynamic from backend) ───────────────────────
  readonly allDetailedRows = computed<Array<Record<string, string>>>(() => {
    return this.backendData().map(item => ({
      "Account Number": item.accountNumber,
      "Account Name": item.accountName,
      "Data Provider": item.dataProvider,
      "Account Supervised": item.isSupervised ? "Supervised" : "Unsupervised",
      "Market Value": item.marketValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }),
      "AUM": item.aum ? "Yes" : "No",
      "Relationship Manager": item.relationshipManager,
      "Advisor": item.advisor,
      "Start Date": item.startDate ? new Date(item.startDate).toLocaleDateString('en-GB') : "",
      "As of Date": item.asOfDate ? new Date(item.asOfDate).toLocaleDateString('en-GB') : "",
      "Closed Date": item.closedDate ? new Date(item.closedDate).toLocaleDateString('en-GB') : "-",
      "Relationship Name": item.relationshipName,
      "AUA": item.aua ? item.marketValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : "",
      "Platform": item.platform
    }));
  });

  readonly detailedRows = computed<Array<Record<string, string>>>(() => {
    let rows = this.allDetailedRows();
    const q = this.searchQuery().toLowerCase();
    const drillFilter = this.dataProviderFilter();
    const filters = this.dataFilters();
    const config = this.dataFilterConfig();

    return rows.filter(row => {
      // 1. Apply drill-down filter
      if (drillFilter) {
        if (row['Data Provider'].toLowerCase() !== drillFilter.provider.toLowerCase() ||
          row['Advisor'].toLowerCase() !== drillFilter.advisor.toLowerCase()) {
          return false;
        }
      }

      // 2. Apply Data filters (Multiselect)
      for (const f of config) {
        if (!f.matchable) continue;
        const val = filters[f.key];
        const rowVal = (row as any)[f.key];
        if (f.type === 'multiselect') {
          const set = val as Set<string>;
          if (set && set.size > 0 && !set.has(rowVal)) return false;
        }
      }

      // 3. Apply Search Filter
      if (q) {
        const matches = Object.values(row).some(val =>
          val !== null && val !== undefined && val.toString().toLowerCase().includes(q)
        );
        if (!matches) return false;
      }

      return true;
    });
  });

  // ── Derived: filtered rows ───────────────────────────────────────────────
  readonly filteredRows = computed<AumRow[]>(() => {
    const rows = this.allRows();
    const filters = this.filters();
    const config = this.filterConfig();
    const q = this.searchQuery().toLowerCase();

    return rows.filter(row => {
      for (const f of config) {
        if (!f.matchable) continue;

        if (f.key === 'platformView') continue; // Handled via visibility, not filtering rows
        if (f.key === 'aumFilter') continue; // Handled via backend API call, not client-side filtering

        if (f.type === 'multiselect') {
          const set = filters[f.key] as Set<string>;
          if (set && set.size > 0 && !set.has((row as any)[f.key])) return false;
        } else if (f.type === 'range') {
          const rv = filters[f.key] as RangeValue;
          if (rv) {
            if (rv.min !== '' && (row as any)[f.key] < Number(rv.min)) return false;
            if (rv.max !== '' && (row as any)[f.key] > Number(rv.max)) return false;
          }
        }
      }
      if (q) {
        const matches = Object.values(row).some(val =>
          val !== null && val !== undefined && val.toString().toLowerCase().includes(q)
        );
        if (!matches) return false;
      }
      return true;
    });
  });

  // ── Derived: sorted rows ─────────────────────────────────────────────────
  readonly sortedRows = computed<AumRow[]>(() => {
    const rows = [...this.filteredRows()];
    const col = this.sortCol();
    const dir = this.sortDir();
    if (!col) return rows;
    return rows.sort((a, b) => {
      let av: any = (a as any)[col];
      let bv: any = (b as any)[col];
      if (typeof av === 'string') { av = av.toLowerCase(); bv = bv.toLowerCase(); }
      return dir === 'asc' ? (av > bv ? 1 : -1) : (av < bv ? 1 : -1);
    });
  });

  // ── Derived: grouped by advisor ──────────────────────────────────────────
  readonly advisorGroups = computed<AdvisorGroup[]>(() => {
    const rows = this.sortedRows();
    const collapsedMap = this.collapsed();
    const map = new Map<string, AumRow[]>();
    rows.forEach(r => {
      const list = map.get(r.advisor) ?? [];
      list.push(r);
      map.set(r.advisor, list);
    });
    return [...map.entries()].map(([advisor, grpRows]) => ({
      advisor,
      rows: grpRows,
      totalAua: grpRows.reduce((s, r) => s + r.aua, 0),
      totalAddepar: grpRows.reduce((s, r) => s + r.addepar, 0),
      totalBD: grpRows.reduce((s, r) => s + r.blackdiamond, 0),
      grandTotal: grpRows.reduce((s, r) => s + (r.total ?? 0), 0),
      collapsed: !!collapsedMap[advisor],
    }));
  });

  // ── Platform Visibility Helper ──────────────────────────────────────────
  readonly platformDisplay = computed(() => {
    const filters = this.filters();
    const view = filters['platformView'];
    const isSet = view instanceof Set;

    // Default to show everything if nothing is selected or size is 0
    const noSelection = !view || (isSet && view.size === 0);

    return {
      all: noSelection,
      addepar: noSelection || (isSet && view.has('Addepar')),
      blackdiamond: noSelection || (isSet && view.has('Black Diamond'))
    };
  });

  // ── Grand KPI (Dashboard Only) ──────────────────────────────────────────
  readonly kpi = computed<KpiData>(() => {
    const groups = this.advisorGroups();
    const grandAua = groups.reduce((s, g) => s + g.totalAua, 0);
    const grandAddepar = groups.reduce((s, g) => s + g.totalAddepar, 0);
    const grandBD = groups.reduce((s, g) => s + g.totalBD, 0);
    return {
      grandAua,
      grandAddepar,
      grandBD,
      grandTotal: grandAddepar + grandBD,
      advisorCount: groups.length,
    };
  });



  constructor(private http: HttpClient) { }

  loadData(): void {
    // Load data from backend APIs
    this.loadBackendData();
    this.loadCustodians();
    this.loadAdvisors();
  }

  private loadBackendData(): void {
    // Get AUM filter state to determine API call
    const aumFilter = this.filters()['aumFilter'] as Set<string>;
    const isReload = this.dashboardData() !== null;
    // Capture current filter/draft state before the HTTP call so we can restore it
    const savedFilters = isReload ? this.cloneState(this.filters()) : null;
    const savedDraft = isReload ? this.cloneState(this.draft()) : null;

    let url = `${environment.api.baseUrl}/api/aum/data`;

    if (aumFilter && aumFilter.size > 0 && !aumFilter.has('All')) {
      if (aumFilter.has('Yes')) {
        url += '?aum=true';
      } else if (aumFilter.has('No')) {
        url += '?aum=false';
      }
    }

    this.loading.set(true);
    this.http.get<BackendDataResponse[]>(url).subscribe(data => {
      this.backendData.set(data);

      // Transform backend data to dashboard format
      const dashboardData = this.transformToDashboardData(data);
      this.dashboardData.set(dashboardData);

      // Initialize Dashboard Filters
      const dashDefaults = this.buildDefaults(dashboardData.filterConfig);
      if (isReload && savedFilters) {
        // Preserve user's current selections; only refresh filter config options
        this.filters.set(savedFilters);
        this.draft.set(savedDraft!);
      } else {
        this.filters.set(dashDefaults);
        this.draft.set(this.cloneState(dashDefaults));
      }

      // Initialize Data Filters
      const dataDefaults = this.buildDefaults(this.dataFilterConfig());
      this.dataFilters.set(dataDefaults);
      this.dataDraft.set(this.cloneState(dataDefaults));

      this.loading.set(false);
    });
  }

  private loadCustodians(): void {
    const url = `${environment.api.baseUrl}/api/custodians`;
    this.http.get<Custodian[]>(url).subscribe(data => {
      this.custodians.set(data);
    });
  }

  private loadAdvisors(): void {
    const url = `${environment.api.baseUrl}/api/advisors`;
    this.http.get<Advisor[]>(url).subscribe(data => {
      this.advisors.set(data);
    });
  }

  private transformToDashboardData(data: BackendDataResponse[]): AumDashboardData {
    // Group data by advisor and data provider to calculate summary rows
    const groupedData = new Map<string, Map<string, { aua: number; addepar: number; blackdiamond: number }>>();

    data.forEach(item => {
      const advisor = item.advisor;
      const provider = item.dataProvider;
      const marketValue = item.marketValue || 0;

      if (!groupedData.has(advisor)) {
        groupedData.set(advisor, new Map());
      }

      const advisorMap = groupedData.get(advisor)!;
      if (!advisorMap.has(provider)) {
        advisorMap.set(provider, { aua: 0, addepar: 0, blackdiamond: 0 });
      }

      const summary = advisorMap.get(provider)!;
      // Calculate AUA from market value when aua is true
      if (item.aua) {
        summary.aua += marketValue;
      }

      // Allocate to platforms based on the platform field
      if (item.platform?.toLowerCase() === 'addepar') {
        summary.addepar += marketValue;
      } else if (item.platform?.toLowerCase().includes('black diamond')) {
        summary.blackdiamond += marketValue;
      }
    });

    // Convert to rows format
    const rows: AumRow[] = [];
    groupedData.forEach((providerMap, advisor) => {
      providerMap.forEach((summary, provider) => {
        rows.push({
          advisor,
          provider,
          aua: summary.aua,
          addepar: summary.addepar,
          blackdiamond: summary.blackdiamond
        });
      });
    });

    // Get unique values for filter options from all backend data
    const allData = this.backendData();
    const uniqueAdvisors = Array.from(new Set(allData.map(item => item.advisor))).sort();
    const uniqueProviders = Array.from(new Set(allData.map(item => item.dataProvider))).sort();

    return {
      reportDate: new Date().toISOString().split('T')[0],
      asOfLabel: `As of ${new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' })}`,
      kpiMeta: {
        totalAumTrend: "+0%",
        newAccounts: 0
      },
      filterConfig: [
        {
          key: "reportDate",
          label: "Report Date",
          type: "date",
          icon: "calendar_today",
          defaultValue: new Date().toISOString().split('T')[0],
          span: "full",
          matchable: false
        },
        {
          key: "advisor",
          label: "Advisor",
          type: "multiselect",
          icon: "person",
          options: uniqueAdvisors,
          defaultValue: [],
          matchable: true
        },
        {
          key: "provider",
          label: "Data Provider",
          type: "multiselect",
          icon: "storage",
          options: uniqueProviders,
          defaultValue: [],
          matchable: true
        },
        {
          key: "addeparRange",
          label: "Addepar AUM Range ($)",
          type: "range",
          icon: "pie_chart",
          defaultValue: { min: "", max: "" },
          matchable: true
        },
        {
          key: "bdRange",
          label: "Black Diamond Range ($)",
          type: "range",
          icon: "diamond",
          defaultValue: { min: "", max: "" },
          matchable: true
        },
        {
          key: "platformView",
          label: "Platform View",
          type: "multiselect",
          icon: "visibility",
          options: ["Addepar", "Black Diamond"],
          defaultValue: [],
          matchable: true
        },
        {
          key: "aumFilter",
          label: "AUM",
          type: "multiselect",
          icon: "check_circle",
          options: ["All", "Yes", "No"],
          defaultValue: ["All"],
          matchable: true
        }
      ],
      rows
    };
  }
  setViewMode(mode: 'summary' | 'data') { this.viewMode.set(mode); }

  /** Drill-down: switch to the Data tab and filter detailedRows by advisor + provider */
  navigateToProvider(advisor: string, provider: string): void {
    this.dataProviderFilter.set({ advisor, provider });
    this.viewMode.set('data');
  }

  /** Clear the provider drill-down filter */
  clearProviderFilter(): void {
    this.dataProviderFilter.set(null);
  }  // ── Actions ──────────────────────────────────────────────────────────────
  applyModal(): void {
    const isData = this.viewMode() === 'data';
    if (isData) {
      this.dataFilters.set(this.cloneState(this.dataDraft()));
    } else {
      this.filters.set(this.cloneState(this.draft()));
      // Reload backend data when AUM filter changes in dashboard mode
      this.loadBackendData();
    }
  }

  // upload dialog helpers
  openUpload(): void {
    this.uploadModalOpen.set(true);
  }

  closeUpload(): void { this.uploadModalOpen.set(false); }

  resetFilters(): void {
    const isData = this.viewMode() === 'data';
    const config = isData ? this.dataFilterConfig() : this.filterConfig();
    const defaults = this.buildDefaults(config);
    if (isData) {
      this.dataFilters.set(defaults);
      this.dataDraft.set(this.cloneState(defaults));
    } else {
      this.filters.set(defaults);
      this.draft.set(this.cloneState(defaults));
    }
  }

  // ── Essential Actions (continued) ───────────────────────────────────
  updateDraftValue(key: string, value: any): void {
    if (this.viewMode() === 'data') {
      this.dataDraft.update(f => ({ ...f, [key]: value }));
    } else {
      this.draft.update(f => ({ ...f, [key]: value }));
    }
    this.applyModal(); // Auto-applysss
  }

  toggleAdvisor(advisor: string): void {
    this.collapsed.update(c => ({ ...c, [advisor]: !c[advisor] }));
  }

  setSort(col: string): void {
    if (this.sortCol() === col) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortCol.set(col);
      this.sortDir.set('asc');
    }
  }

  setSearch(q: string): void { this.searchQuery.set(q); }

  toggleDataColumn(col: string): void {
    this.visibleDataColumns.update(set => {
      const next = new Set(set);
      if (next.has(col)) {
        next.delete(col);
      } else {
        next.add(col);
      }
      return next;
    });
  }

  toggleAllColumns(all: boolean): void {
    if (all) {
      this.visibleDataColumns.set(new Set(this.dataColumns()));
    } else {
      this.visibleDataColumns.set(new Set());
    }
  }

  isColumnVisible(col: string): boolean {
    return this.visibleDataColumns().has(col);
  }


  removeFilter(key: string): void {
    const config = this.activeFilterConfig().find(f => f.key === key);
    if (!config) return;
    const def = this.defaultForConfig(config);
    if (this.viewMode() === 'data') {
      this.dataFilters.update(f => ({ ...f, [key]: def }));
      this.dataDraft.update(f => ({ ...f, [key]: def }));
    } else {
      this.filters.update(f => ({ ...f, [key]: def }));
      this.draft.update(f => ({ ...f, [key]: def }));
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────
  private buildDefaults(config: FilterConfig[]): FilterState {
    const d: FilterState = {};
    for (const f of config) {
      d[f.key] = this.defaultForConfig(f);
    }
    return d;
  }

  private defaultForConfig(f: FilterConfig): any {
    const v = f.defaultValue;
    if (f.type === 'multiselect') {
      if (v instanceof Set) return new Set(v);
      if (Array.isArray(v)) return new Set<string>(v);
      if (typeof v === 'string' && v !== '') return new Set<string>([v]);
      return new Set<string>();
    }
    if (v && typeof v === 'object' && !(v instanceof Set)) return { ...(v as RangeValue) };
    return v ?? '';
  }

  // Format helpers
  fmt(v: number): string {
    return v === 0 ? '-' : '$ ' + v.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  fmtShort(v: number): string {
    return '$' + Math.round(v).toLocaleString('en-US');
  }

  cloneState(state: FilterState): FilterState {
    const d: FilterState = {};
    for (const [k, v] of Object.entries(state)) {
      if (v instanceof Set) d[k] = new Set(v);
      else if (v && typeof v === 'object') d[k] = { ...(v as RangeValue) };
      else d[k] = v;
    }
    return d;
  }

  private getUnique(col: string): string[] {
    const data = this.allDetailedRows();
    const set = new Set<string>();
    for (const r of data) {
      const value = (r as any)[col];
      if (value) set.add(value);
    }
    return Array.from(set).sort();
  }

  private getCustodianNames(): string[] {
    const custodianNames = this.custodians().map(c => c.name);
    const backendProviderNames = new Set(this.backendData().map(item => item.dataProvider));
    const allNames = new Set([...custodianNames, ...backendProviderNames]);
    return Array.from(allNames).sort();
  }

  private getAdvisorNames(): string[] {
    const advisorNames = this.advisors().map(a => a.name);
    const backendAdvisorNames = new Set(this.backendData().map(item => item.advisor));
    const allNames = new Set([...advisorNames, ...backendAdvisorNames]);
    return Array.from(allNames).sort();
  }

  // ── Export functionality ───────────────────────────────────────────────
  exportExcel(): void {
    // 1. Prepare "Report" sheet (Dashboard data)
    const reportData = [];
    // Header
    reportData.push(["Advisor", "Data Provider", "AUA", "Addepar", "Black Diamond", "Total"]);

    // Rows
    const dashRows = this.filteredRows();
    dashRows.forEach(row => {
      reportData.push([
        row.advisor,
        row.provider,
        row.aua,
        row.addepar,
        row.blackdiamond,
        row.total || 0
      ]);
    });

    // Grand Total
    const totals = this.kpi();
    reportData.push([]); // Empty line
    reportData.push(["Grand Total", "", totals.grandAua, totals.grandAddepar, totals.grandBD, totals.grandTotal]);

    // 2. Prepare "Data" sheet
    const dataTabContent = [];
    const cols = this.dataColumns();
    // Header
    dataTabContent.push(cols);
    // Rows
    const dRows = this.detailedRows();
    dRows.forEach(row => {
      dataTabContent.push(cols.map(c => row[c]));
    });

    // 3. Create Workbook
    const wb = XLSX.utils.book_new();

    // Add Report sheet
    const wsReport = XLSX.utils.aoa_to_sheet(reportData);
    XLSX.utils.book_append_sheet(wb, wsReport, "Report");

    // Add Data sheet
    const wsData = XLSX.utils.aoa_to_sheet(dataTabContent);
    XLSX.utils.book_append_sheet(wb, wsData, "Data");

    // 4. Generate file and trigger download
    const dateStr = new Date().toISOString().split('T')[0];
    XLSX.writeFile(wb, `AUM_Report_${dateStr}.xlsx`);
  }
}
