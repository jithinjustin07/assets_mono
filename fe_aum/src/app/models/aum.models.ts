export interface AumRow {
  advisor: string;
  provider: string;
  aua: number;
  auaAddepar: number;
  auaBD: number;
  aum: number;
  addepar: number;
  blackdiamond: number;
  total?: number;
}

export interface RangeValue {
  min: string;
  max: string;
}

export interface FilterConfig {
  key: string;
  label: string;
  type: 'multiselect' | 'date' | 'range' | 'text';
  icon: string;
  options?: string[];
  defaultValue: string | string[] | RangeValue;
  span?: string;
  matchable: boolean;
}

export interface FilterState {
  [key: string]: string | Set<string> | RangeValue;
}

export interface KpiData {
  grandAua: number;
  grandAuaAddepar: number;
  grandAuaBD: number;
  grandAum: number;
  grandAddepar: number;
  grandBD: number;
  grandTotal: number;
  advisorCount: number;
}

export interface AdvisorGroup {
  advisor: string;
  rows: AumRow[];
  totalAua: number;
  totalAuaAddepar: number;
  totalAuaBD: number;
  totalAum: number;
  totalAddepar: number;
  totalBD: number;
  grandTotal: number;
  collapsed: boolean;
}

export interface AumDashboardData {
  reportDate: string;
  asOfLabel: string;
  kpiMeta: { totalAumTrend: string; newAccounts: number };
  filterConfig: FilterConfig[];
  rows: AumRow[];
}

// Backend API response interface
export interface BackendDataResponse {
  accountNumber: string;
  accountName: string;
  dataProvider: string;
  isSupervised: boolean;
  marketValue: number;
  aum: boolean;
  relationshipManager: string;
  advisor: string;
  startDate: string;
  asOfDate: string;
  closedDate: string;
  relationshipName: string;
  aua: boolean;
  platform: string;
}

// Custodian entity for filter options
export interface Custodian {
  id: number;
  name: string;
  type: string;
  [key: string]: any;
}

// Advisor entity for filter options
export interface Advisor {
  id: number;
  name: string;
  type: string;
  [key: string]: any;
}
