import { useEffect, useState } from 'react';
import { api } from '../api';
import { PieChart, Pie, Cell, Tooltip as RechartsTooltip, ResponsiveContainer } from 'recharts';
import { Target, ShieldAlert } from 'lucide-react';

const COLORS = ['#2E5AEA', '#12A150', '#D97706', '#9CA3AF', '#DC2626'];

export function Analytics() {
  const [analytics, setAnalytics] = useState<any>(null);
  const [range, setRange] = useState('Last 30 days');
  const [diagnosis, setDiagnosis] = useState('All diagnoses');
  const [action, setAction] = useState('All actions');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      try {
        setLoading(true);
        const data = await api.getAnalytics({ range, diagnosis, action });
        setAnalytics(data);
      } catch (err) {
        console.error("Failed to load analytics", err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [range, diagnosis, action]);

  if (loading) {
    return <div className="p-8 text-center text-text-secondary">Loading Analytics...</div>;
  }

  if (!analytics) return null;

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="max-w-[1200px] mx-auto space-y-6">
      
      {/* Filter Bar */}
      <div className="flex flex-col md:flex-row items-center justify-between bg-white border border-border-default rounded-xl p-4 shadow-sm mb-6">
        <div className="flex items-center gap-6 w-full md:w-auto">
          <div className="flex items-center gap-2 text-accent-blue font-semibold text-[13px] tracking-wide uppercase px-2">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon></svg>
            Filters
          </div>
          <div className="hidden md:block w-px h-8 bg-border-default"></div>
          
          <div className="flex gap-4">
            <div>
              <div className="text-[9px] font-mono font-bold text-text-secondary uppercase tracking-wider mb-1">Range</div>
              <select 
                className="bg-page border border-border-default text-[13px] text-text-primary rounded-md px-3 py-1.5 outline-none min-w-[140px]"
                value={range}
                onChange={e => setRange(e.target.value)}
              >
                <option>Last 30 days</option>
                <option>Last 7 days</option>
              </select>
            </div>
            
            <div>
              <div className="text-[9px] font-mono font-bold text-text-secondary uppercase tracking-wider mb-1">Diagnosis</div>
              <select 
                className="bg-page border border-border-default text-[13px] text-text-primary rounded-md px-3 py-1.5 outline-none min-w-[140px]"
                value={diagnosis}
                onChange={e => setDiagnosis(e.target.value)}
              >
                <option>All diagnoses</option>
              </select>
            </div>

            <div>
              <div className="text-[9px] font-mono font-bold text-text-secondary uppercase tracking-wider mb-1">Action</div>
              <select 
                className="bg-page border border-border-default text-[13px] text-text-primary rounded-md px-3 py-1.5 outline-none min-w-[140px]"
                value={action}
                onChange={e => setAction(e.target.value)}
              >
                <option>All actions</option>
              </select>
            </div>
          </div>
        </div>

        <button className="flex items-center gap-2 px-4 py-2 border border-border-default rounded bg-page text-[12px] font-mono font-bold tracking-wider text-text-secondary hover:text-text-primary transition-colors uppercase mt-4 md:mt-0">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          Export View
        </button>
      </div>

      {/* Top Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-surface p-6 rounded-xl border border-border-default shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-accent-blue/10 flex items-center justify-center text-accent-blue">
            <Target size={24} />
          </div>
          <div>
            <div className="text-sm font-sans font-semibold text-text-secondary">Decision Accuracy (Eval Baseline)</div>
            {analytics.decision_accuracy == null ? (
              <div className="font-mono text-xl font-medium text-text-secondary mt-1 mt-2">
                Insufficient data
              </div>
            ) : (
              <div className="font-mono text-3xl font-medium text-text-primary mt-1">
                {analytics.decision_accuracy.toFixed(1)}%
              </div>
            )}
          </div>
        </div>

        <div className="bg-surface p-6 rounded-xl border border-border-default shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-full bg-semantic-green/10 flex items-center justify-center text-semantic-green">
            <ShieldAlert size={24} />
          </div>
          <div>
            <div className="text-sm font-sans font-semibold text-text-secondary">Policy Block Rate</div>
            <div className="font-mono text-3xl font-medium text-text-primary mt-1">
              {analytics.policy_block_rate?.toFixed(1) ?? '0.0'}%
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Diagnosis Split */}
        <div className="bg-surface p-6 rounded-xl border border-border-default shadow-sm lg:col-span-1">
          <h2 className="font-sans font-semibold text-lg text-text-primary mb-6">Diagnosis Split</h2>
          <div className="h-[240px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={analytics.diagnosis_split}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="count"
                  nameKey="diagnosis"
                >
                  {analytics.diagnosis_split?.map((_: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <RechartsTooltip 
                  formatter={(value: any, name: any, props: any) => [`${value} cases (${props.payload.percentage.toFixed(1)}%)`, name]}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="mt-4 space-y-2">
            {analytics.diagnosis_split?.map((entry: any, index: number) => (
              <div key={entry.diagnosis} className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                  <span className="text-text-primary">{entry.diagnosis}</span>
                </div>
                <span className="text-text-secondary font-mono">{entry.percentage.toFixed(1)}%</span>
              </div>
            ))}
          </div>
        </div>

        {/* Action Effectiveness */}
        <div className="bg-surface rounded-xl border border-border-default shadow-sm lg:col-span-2 overflow-hidden flex flex-col">
          <div className="px-6 py-5 border-b border-border-default">
            <h2 className="font-sans font-semibold text-lg text-text-primary">Action Effectiveness</h2>
          </div>
          <div className="flex-1 p-6 overflow-x-auto">
            <table className="w-full text-left">
              <thead>
                <tr className="text-xs font-semibold text-text-secondary uppercase tracking-wider border-b border-border-default">
                  <th className="pb-3 pr-4">Action</th>
                  <th className="pb-3 px-4 text-right">Attempts</th>
                  <th className="pb-3 px-4 text-right">Success</th>
                  <th className="pb-3 px-4 text-right">Win Rate</th>
                  <th className="pb-3 pl-4 text-right">Value Recovered</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border-default">
                {analytics.action_effectiveness?.sort((a: any, b: any) => b.total_recovered - a.total_recovered).map((item: any) => (
                  <tr key={item.action} className="hover:bg-page/50">
                    <td className="py-4 pr-4 font-medium text-text-primary text-sm">{item.action}</td>
                    <td className="py-4 px-4 text-right font-mono text-sm text-text-secondary">{item.attempt_count}</td>
                    <td className="py-4 px-4 text-right font-mono text-sm text-text-secondary">{item.success_count}</td>
                    <td className="py-4 px-4 text-right">
                      <div className="inline-flex items-center gap-1.5 px-2 py-1 rounded-md text-xs font-medium bg-page border border-border-default text-text-primary">
                        {item.success_rate?.toFixed(1) ?? '0.0'}%
                      </div>
                    </td>
                    <td className="py-4 pl-4 text-right font-mono text-sm text-text-primary font-medium">
                      {formatCurrency(item.total_recovered)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
    </div>
  );
}
