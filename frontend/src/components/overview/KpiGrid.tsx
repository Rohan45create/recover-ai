import type { DashboardOverviewResponse } from '../../api';

interface KpiGridProps {
  overview: DashboardOverviewResponse;
}

export function KpiGrid({ overview }: KpiGridProps) {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Recovered (Mocked 'At Risk' in design) */}
      <div className="bg-surface p-5 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)]">
        <div className="flex items-center justify-between mb-4">
          <div className="text-[13px] font-sans font-medium text-text-primary">Total Recovered</div>
          <div className="text-[9px] font-mono font-medium tracking-wider text-text-secondary bg-page px-2 py-0.5 rounded uppercase">
            SOURCE: RISK MODEL
          </div>
        </div>
        <div className="font-display font-medium text-3xl text-text-primary mb-3">
          {formatCurrency(overview.total_recovered_amount)}
        </div>
        <div className="inline-flex items-center text-[11px] font-medium text-semantic-red bg-semantic-red/10 px-2 py-0.5 rounded-full">
          ₹84.2K surfaced today
        </div>
      </div>

      {/* Recovery Rate */}
      <div className="bg-surface p-5 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)]">
        <div className="flex items-center justify-between mb-4">
          <div className="text-[13px] font-sans font-medium text-text-primary">Recovery Rate</div>
          <div className="text-[9px] font-mono font-medium tracking-wider text-text-secondary bg-page px-2 py-0.5 rounded uppercase">
            SOURCE: LEDGER
          </div>
        </div>
        <div className="font-display font-medium text-3xl text-text-primary mb-3">
          {overview.recovery_rate_percentage.toFixed(1)}%
        </div>
        <div className="inline-flex items-center text-[11px] font-medium text-semantic-green bg-semantic-green/10 px-2 py-0.5 rounded-full">
          +3.6% vs baseline
        </div>
      </div>

      {/* Total Cases */}
      <div className="bg-surface p-5 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)]">
        <div className="flex items-center justify-between mb-4">
          <div className="text-[13px] font-sans font-medium text-text-primary">Total Cases</div>
          <div className="text-[9px] font-mono font-medium tracking-wider text-text-secondary bg-page px-2 py-0.5 rounded uppercase">
            SOURCE: CASE INDEX
          </div>
        </div>
        <div className="font-display font-medium text-3xl text-text-primary mb-3">
          {new Intl.NumberFormat('en-IN').format(overview.total_cases)}
        </div>
        <div className="inline-flex items-center text-[11px] font-medium text-text-secondary bg-page px-2 py-0.5 rounded-full border border-border-default">
          {overview.active_cases} active queues
        </div>
      </div>

      {/* Policy Violation */}
      <div className="bg-surface p-5 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)]">
        <div className="flex items-center justify-between mb-4">
          <div className="text-[13px] font-sans font-medium text-text-primary">Policy Violation</div>
          <div className="text-[9px] font-mono font-medium tracking-wider text-text-secondary bg-page px-2 py-0.5 rounded uppercase">
            SOURCE: GUARDRAILS
          </div>
        </div>
        <div className="font-display font-medium text-3xl text-text-primary mb-3">
          {overview.policy_violations}
        </div>
        <div className="inline-flex items-center text-[11px] font-medium text-semantic-amber bg-semantic-amber/10 px-2 py-0.5 rounded-full">
          4 require review
        </div>
      </div>
    </div>
  );
}
