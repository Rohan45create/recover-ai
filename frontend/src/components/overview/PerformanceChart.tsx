import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { TrajectoryPoint } from '../../api';
import { ArrowUpRight } from 'lucide-react';
import clsx from 'clsx';

interface PerformanceChartProps {
  trajectory: TrajectoryPoint[];
}

export function PerformanceChart({ trajectory }: PerformanceChartProps) {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  // Add dummy baseline data for illustration
  const chartData = trajectory.map(t => ({
    ...t,
    baseline: t.amount * 0.76 // Mocking baseline as 76% of actual
  }));

  return (
    <div className="bg-surface p-6 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)] h-full flex flex-col">
      <div className="flex items-start justify-between mb-8">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <h2 className="font-display font-semibold text-lg text-text-primary tracking-tight">Recovery Performance</h2>
            <span className="text-[9px] font-mono font-bold text-accent-blue bg-accent-blue/10 px-2 py-0.5 rounded tracking-wider uppercase">LIVE</span>
          </div>
          <div className="text-[13px] text-text-secondary">as a share of eligible cases</div>
        </div>
        <div className="flex bg-page border border-border-default rounded-md p-1">
          {['7D', '30D', '90D'].map((period) => (
            <button
              key={period}
              className={clsx(
                "px-3 py-1 text-xs font-medium rounded transition-colors",
                period === '30D' ? "bg-white text-accent-blue shadow-sm border border-border-default" : "text-text-secondary hover:text-text-primary"
              )}
            >
              {period}
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
            <span className="text-xs font-bold text-text-primary">51.0%</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full border-2 border-border-default bg-page"></div>
            <span className="text-xs font-medium text-text-secondary">Baseline</span>
            <span className="text-xs font-bold text-text-secondary">39.0%</span>
          </div>
        </div>
        <div className="flex items-center gap-1 text-semantic-green text-xs font-semibold">
          <ArrowUpRight size={14} />
          <span>+12.0 pts</span>
        </div>
      </div>

      <div className="flex-1 min-h-0 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="colorRecoverAI" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#2E5AEA" stopOpacity={0.1}/>
                <stop offset="95%" stopColor="#2E5AEA" stopOpacity={0}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E4E7EC" />
            <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#6B7280', fontWeight: 500 }} dy={10} />
            <YAxis 
              axisLine={false} 
              tickLine={false} 
              tick={{ fontSize: 11, fill: '#6B7280', fontWeight: 500 }} 
              tickFormatter={(val) => `${val/1000}%`} // Fake % formatting
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
