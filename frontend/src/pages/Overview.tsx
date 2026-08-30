import { useEffect, useState } from 'react';
import { AlertCircle } from 'lucide-react';
import { api, type DashboardOverviewResponse } from '../api';
import { KpiGrid } from '../components/overview/KpiGrid';
import { PerformanceChart } from '../components/overview/PerformanceChart';
import { RecoveryEngine } from '../components/overview/RecoveryEngine';
import { EfficiencyScore } from '../components/overview/EfficiencyScore';
import { CaseStatus } from '../components/overview/CaseStatus';
import { AiProposalFeed } from '../components/overview/AiProposalFeed';

export function Overview() {
  const [overview, setOverview] = useState<DashboardOverviewResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadData() {
      try {
        const overviewData = await api.getOverview();
        setOverview(overviewData);
        setError(null);
      } catch (err) {
        console.error("Failed to load dashboard data", err);
        setError("Failed to load dashboard data. Please try again.");
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  if (loading) {
    return (
      <div className="space-y-8 max-w-[1200px] mx-auto pb-12 animate-pulse">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[1, 2, 3, 4].map(i => (
            <div key={i} className="bg-surface p-6 rounded-xl border border-border-default shadow-sm h-[104px]">
              <div className="h-4 bg-page rounded w-24 mb-4"></div>
              <div className="h-8 bg-page rounded w-32"></div>
            </div>
          ))}
        </div>
        <div className="bg-surface rounded-xl border border-border-default shadow-sm h-[360px]"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center h-[60vh] text-center">
        <div className="w-16 h-16 bg-semantic-red/10 rounded-full flex items-center justify-center text-semantic-red mb-4">
          <AlertCircle size={24} />
        </div>
        <h2 className="text-xl font-display font-medium text-text-primary mb-2">Error Loading Dashboard</h2>
        <p className="text-text-secondary max-w-md">{error}</p>
        <button onClick={() => window.location.reload()} className="mt-6 px-4 py-2 bg-accent-blue text-white rounded-md font-medium text-sm">Retry</button>
      </div>
    );
  }

  if (!overview) return null;

  return (
    <div className="space-y-8 max-w-[1100px] mx-auto pb-12">
      {/* KPI Row */}
      <KpiGrid overview={overview} />

      {/* Chart Row */}
      <div className="h-[360px]">
        <PerformanceChart trajectory={overview.trajectory || []} />
      </div>

      {/* Engine and Efficiency Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[260px]">
        <div className="lg:col-span-2 h-full">
          <RecoveryEngine />
        </div>
        <div className="h-full">
          <EfficiencyScore />
        </div>
      </div>

      {/* Status Row */}
      <CaseStatus overview={overview} />

      {/* AI Proposal Feed */}
      <div className="h-[400px]">
        <AiProposalFeed />
      </div>
    </div>
  );
}
