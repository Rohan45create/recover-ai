import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

axios.defaults.headers.common['X-API-KEY'] = import.meta.env.VITE_API_KEY;

export interface TrajectoryPoint {
  day: string;
  amount: number;
}

export interface DashboardOverviewResponse {
  total_recovered_amount: number;
  recovery_rate_percentage: number;
  active_cases: number;
  total_cases: number;
  policy_violations: number;
  efficiency_score: number;
  trajectory: TrajectoryPoint[];
}

export interface DashboardKpis {
  totalRecoveredAmount: number;
  recoveryRatePercentage: number;
  activeCases: number;
  totalCases: number;
  policyViolations: number;
}

export interface RecoveryCase {
  id: string;
  created_at: string;
  status: string;
  diagnosis: string;
  chosen_action: string;
  expected_recovery_value: number;
  actual_recovered_amount: number | null;
  payment_link_url: string | null;
}

export const api = {
  getOverview: async (): Promise<DashboardOverviewResponse> => {
    const response = await axios.get(`${API_BASE}/dashboard/overview`);
    return response.data;
  },

  getKpis: async (): Promise<DashboardKpis> => {
    const response = await axios.get(`${API_BASE}/dashboard/kpis`);
    return response.data;
  },
  
  getRecentCases: async (): Promise<RecoveryCase[]> => {
    const response = await axios.get(`${API_BASE}/dashboard/cases?size=50`); // Fetch 50 cases for log
    return response.data.content;
  },

  getCaseTimeline: async (id: string): Promise<any[]> => {
    const response = await axios.get(`${API_BASE}/dashboard/cases/${id}/timeline`);
    return response.data;
  },

  getAnalytics: async (params?: { range?: string; diagnosis?: string; action?: string }): Promise<any> => {
    const response = await axios.get(`${API_BASE}/dashboard/analytics`, { params });
    return response.data;
  },

  getPolicies: async (): Promise<any[]> => {
    const response = await axios.get(`${API_BASE}/dashboard/policies`);
    return response.data;
  },

  updatePolicy: async (id: string, patch: { value?: string; enabled?: boolean }): Promise<any> => {
    const response = await axios.put(`${API_BASE}/dashboard/policies/${id}`, patch);
    return response.data;
  },

  updatePolicies: async (policies: any[]): Promise<any[]> => {
    const response = await axios.post(`${API_BASE}/dashboard/policies`, policies);
    return response.data;
  },

  post: async (endpoint: string, data?: any) => {
    const response = await axios.post(`${API_BASE}${endpoint}`, data);
    return response.data;
  }
};
