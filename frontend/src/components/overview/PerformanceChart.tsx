import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { TrajectoryPoint } from '../../api';
import { ArrowUpRight } from 'lucide-react';
import clsx from 'clsx';
import { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const PERIODS = ['7D', '30D', '90D'] as const;
type Period = typeof PERIODS[number];

const PERIOD_LABELS: Record<Period, string> = {
  '7D': '7d',
  '30D': '30d',
  '90D': '90d',
};

export function PerformanceChart({ trajectory: initialTrajectory }: { trajectory: TrajectoryPoint[] }) {
  const [period, setPeriod] = useState<Period>('30D');
  const [trajectory, setTrajectory] = useState<TrajectoryPoint[]>(initialTrajectory);
  const [loading, setLoading] = useState(false);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  async function fetchTrajectory(p: Period) {
    setLoading(true);
    try {
      const res = await axios.get(`${API_BASE}/dashboard/overview`, {
        params: { range: PERIOD_LABELS[p] },
      });
      const data = res.data;
      if (data?.trajectory) {
        console.log("PerformanceChart trajectory length:", data.trajectory.length, "last entry:", data.trajectory[data.trajectory.length - 1]);
        setTrajectory(data.trajectory);
      }
    } catch (e) {
      console.error('Failed to fetch trajectory for period', p, e);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchTrajectory(period);
    const interval = setInterval(() => {
      fetchTrajectory(period);
    }, 5000);
    return () => clearInterval(interval);
  }, [period]);

  // Add dummy baseline data for illustration
  const chartData = trajectory.map(t => ({
    ...t,
    amount: typeof t.amount === 'number' ? t.amount : Number(t.amount),
    baseline: (typeof t.amount === 'number' ? t.amount : Number(t.amount)) * 0.76
  }));

  return (
    <div className="bg-surface p-6 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)] h-full flex flex-col">
      <div className="flex items-start justify-between mb-8">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h2 className="font-display font-semibold text-lg text-text-primary tracking-tight">Recovery Performance</h2>
            <span className="text-[9px] font-mono font-bold text-accent-blue bg-accent-blue/10 px-2 py-0.5 rounded tracking-wider uppercase">LIVE</span>
          </div>
          <div className="text-[13px] text-text-secondary">as a share of eligible cases · {period} window</div>
        </div>
        <div className="flex bg-page border border-border-default rounded-md p-1">
          {PERIODS.map((p) => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={clsx(
                'px-3 py-1 text-xs font-medium rounded transition-colors',
                p === period
                  ? 'bg-white text-accent-blue shadow-sm border border-border-default'
                  : 'text-text-secondary hover:text-text-primary'
              )}
            >
              {p}
            </button>
          ))}
        </div>
      </div>

      {/* Legend & Stats */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-accent-blue"></div>
            <span className="text-xs font-medium text-text-primary">RecoverAI</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full border-2 border-border-default bg-page"></div>
            <span className="text-xs font-medium text-text-secondary">Baseline</span>
          </div>
        </div>
        <div className="flex items-center gap-1 text-semantic-green text-xs font-semibold">
          <ArrowUpRight size={14} />
          <span>+12.0 pts</span>
        </div>
      </div>

      <div className={clsx('flex-1 min-h-0 w-full transition-opacity', loading ? 'opacity-40' : 'opacity-100')}>
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="colorRecoverAI" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#2E5AEA" stopOpacity={0.1}/>
                <stop offset="95%" stopColor="#2E5AEA" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E4E7EC" />
            <XAxis 
              dataKey="day" 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 11, fill: '#6B7280', fontWeight: 500 }} 
              dy={10} 
              tickFormatter={(val) => {
                try {
                  const d = new Date(val);
                  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                } catch {
                  return val;
                }
              }}
            />
            <YAxis
              axisLine={false}
              tickLine={false}
              tick={{ fontSize: 11, fill: '#6B7280', fontWeight: 500 }}
              tickFormatter={(val) => `₹${(val/1000).toFixed(0)}k`}
              domain={[0, 'dataMax + 100000']}
            />
            <Tooltip
              contentStyle={{ borderRadius: '8px', border: '1px solid #E4E7EC', boxShadow: '0 2px 8px rgba(0,0,0,0.05)', fontSize: '12px' }}
              formatter={(value: any, name: string) => [formatCurrency(Number(value)), name === 'amount' ? 'RecoverAI' : 'Baseline']}
            />
            {/* Baseline Line */}
            <Area type="monotone" dataKey="baseline" stroke="#9CA3AF" strokeWidth={2} strokeDasharray="5 5" fill="none" activeDot={false} />
            {/* RecoverAI Line */}
            <Area type="monotone" dataKey="amount" stroke="#2E5AEA" strokeWidth={3} fillOpacity={1} fill="url(#colorRecoverAI)" activeDot={{ r: 5, strokeWidth: 2, fill: "#fff", stroke: "#2E5AEA" }} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
