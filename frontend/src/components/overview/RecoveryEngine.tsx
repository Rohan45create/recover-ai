import { CheckCircle2, AlertCircle, Lock, Cpu, PlayCircle } from 'lucide-react';
import clsx from 'clsx';

export function RecoveryEngine() {
  const steps = [
    { label: 'Failed', icon: AlertCircle, active: false },
    { label: 'AI', icon: Cpu, active: true },
    { label: 'Policy', icon: Lock, active: false, warning: true },
    { label: 'Decision', icon: PlayCircle, active: false },
    { label: 'Recovery', icon: CheckCircle2, active: false }
  ];

  return (
    <div className="bg-surface p-6 rounded-xl border border-border-default shadow-[0_2px_8px_rgba(0,0,0,0.04)] h-full flex flex-col relative overflow-hidden">
      <div className="flex items-start justify-between mb-8 z-10 relative">
        <div>
          <h2 className="font-display font-semibold text-lg text-text-primary tracking-tight mb-1">Recovery Engine</h2>
          <div className="text-[13px] text-text-secondary">Case progression through guarded AI decisions</div>
        </div>
        <div className="flex items-center gap-1.5 text-[10px] font-mono font-bold text-semantic-green bg-semantic-green/10 px-2.5 py-1 rounded-full uppercase tracking-wider">
          <div className="w-1.5 h-1.5 rounded-full bg-semantic-green animate-pulse"></div>
          RUNNING
        </div>
      </div>

      {/* Engine Graphic Box */}
      <div className="flex-1 bg-page border border-border-default rounded-lg relative overflow-hidden flex items-center justify-center p-6">
        {/* Dot grid background */}
        <div 
          className="absolute inset-0 opacity-[0.03]" 
          style={{ backgroundImage: 'radial-gradient(#000 1px, transparent 1px)', backgroundSize: '16px 16px' }}
        ></div>

        <div className="absolute top-4 left-4 flex items-center gap-2 text-text-secondary/60">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
          </svg>
          <span className="text-[10px] font-mono font-bold tracking-widest uppercase">Recovery Engine</span>
        </div>

        <div className="absolute bottom-4 right-4 text-right">
          <div className="text-[10px] font-mono font-bold tracking-widest text-text-secondary/60 uppercase">Live</div>
          <div className="text-[13px] font-mono font-medium text-text-primary">146ms</div>
        </div>

        {/* Nodes */}
        <div className="relative w-full max-w-[400px] flex justify-between items-center z-10 pt-4">
          {/* Connector Line */}
          <div className="absolute top-1/2 left-0 w-full h-[2px] bg-border-default -z-10 -translate-y-1/2"></div>
          {/* Active Line (partial) */}
          <div className="absolute top-1/2 left-0 w-[40%] h-[2px] bg-accent-blue -z-10 -translate-y-1/2"></div>
          
          {steps.map((step, idx) => {
            const Icon = step.icon;
            return (
              <div key={step.label} className="flex flex-col items-center gap-3">
                <div className={clsx(
                  "w-10 h-10 rounded-full flex items-center justify-center border-2 bg-page transition-all",
                  step.active ? "border-accent-blue text-accent-blue shadow-[0_0_0_4px_rgba(46,90,234,0.1)]" : 
                  step.warning ? "border-semantic-amber text-semantic-amber" :
                  idx === steps.length - 1 ? "border-semantic-green text-semantic-green" :
                  "border-border-default text-text-secondary"
                )}>
                  <Icon size={18} strokeWidth={step.active ? 2.5 : 2} />
                </div>
                <span className={clsx(
                  "text-[11px] font-semibold tracking-wide",
                  step.active ? "text-text-primary" : "text-text-secondary"
                )}>{step.label}</span>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  );
}
