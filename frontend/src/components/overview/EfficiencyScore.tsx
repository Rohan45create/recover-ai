import { ArrowUpRight } from 'lucide-react';

export function EfficiencyScore() {
  const score = 78.6;
  // Circumference = 2 * pi * r = 2 * Math.PI * 40 = 251.2
  const circumference = 251.2;
  const strokeDashoffset = circumference - (score / 100) * circumference;

  return (
    <div className="bg-surface p-6 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)] h-full flex flex-col items-center justify-between">
      <div className="w-full flex items-start justify-between mb-4">
        <div>
          <h2 className="font-display font-semibold text-lg text-text-primary tracking-tight mb-1">Efficiency Score</h2>
          <div className="text-[13px] text-text-secondary w-3/4">System-wide operating signal</div>
        </div>
        <div className="text-accent-blue opacity-50">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <circle cx="12" cy="12" r="4" />
            <circle cx="12" cy="12" r="1" />
          </svg>
        </div>
      </div>

      <div className="relative w-32 h-32 flex items-center justify-center my-4">
        {/* Background circle */}
        <svg className="absolute inset-0 w-full h-full transform -rotate-90">
          <circle 
            cx="64" cy="64" r="40" 
            fill="none" 
            stroke="#F3F4F6" 
            strokeWidth="10" 
          />
          {/* Progress circle */}
          <circle 
            cx="64" cy="64" r="40" 
            fill="none" 
            stroke="#2E5AEA" 
            strokeWidth="10" 
            strokeLinecap="round"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            className="transition-all duration-1000 ease-out"
          />
        </svg>
        <div className="flex flex-col items-center justify-center z-10 pt-2">
          <span className="font-display font-medium text-3xl text-text-primary leading-none">{score}</span>
          <span className="text-[9px] font-mono font-medium tracking-wider text-text-secondary mt-1 uppercase">Out of 100</span>
        </div>
      </div>

      <div className="text-center w-full mt-2">
        <div className="flex items-center justify-center gap-1 text-semantic-green text-[13px] font-semibold mb-2">
          <ArrowUpRight size={16} />
          <span>4.8 pts this month</span>
        </div>
        <p className="text-xs text-text-secondary max-w-[200px] mx-auto leading-relaxed">
          Policy precision and recovery yield are both trending above target.
        </p>
      </div>
    </div>
  );
}
