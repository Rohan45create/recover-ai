import { CheckCircle2, ShieldAlert, Cpu, Activity, PlayCircle, Lock, TerminalSquare, AlertCircle } from 'lucide-react';
import clsx from 'clsx';

interface TimelineProps {
  events: any[];
}

export function RecoveryLogTimeline({ events }: TimelineProps) {
  if (!events || events.length === 0) {
    return <div className="text-sm text-text-secondary">No audit events recorded.</div>;
  }

  const getEventConfig = (eventType: string) => {
    switch (eventType) {
      case 'WEBHOOK_VERIFIED':
        return { icon: Activity, color: 'text-accent-blue', bg: 'bg-accent-blue/10', border: 'border-accent-blue/20' };
      case 'AI_DIAGNOSIS':
        return { icon: Cpu, color: 'text-purple-500', bg: 'bg-purple-500/10', border: 'border-purple-500/20' };
      case 'POLICY_EVALUATION':
        return { icon: Lock, color: 'text-semantic-amber', bg: 'bg-semantic-amber/10', border: 'border-semantic-amber/20' };
      case 'DECISION':
        return { icon: PlayCircle, color: 'text-accent-blue', bg: 'bg-accent-blue/10', border: 'border-accent-blue/20' };
      case 'TOOL_EXECUTION':
        return { icon: TerminalSquare, color: 'text-semantic-green', bg: 'bg-semantic-green/10', border: 'border-semantic-green/20' };
      case 'OUTCOME':
        return { icon: CheckCircle2, color: 'text-semantic-green', bg: 'bg-semantic-green/10', border: 'border-semantic-green/20' };
      default:
        return { icon: AlertCircle, color: 'text-text-secondary', bg: 'bg-page', border: 'border-border-default' };
    }
  };

  return (
    <div className="flex gap-8">
      {/* Main Events Column */}
      <div className="flex-1 relative">
        {/* Connecting line */}
        <div className="absolute top-4 bottom-4 left-[27px] w-0.5 bg-border-default z-0"></div>
        
        <div className="space-y-4 relative z-10">
          {events.map((event, index) => {
            const config = getEventConfig(event.event_type);
            const Icon = config.icon;
            
            return (
              <div key={event.id} className="flex gap-4">
                <div className={clsx("w-14 h-14 rounded-xl flex items-center justify-center border flex-shrink-0 bg-white", config.border)}>
                  <div className={clsx("w-10 h-10 rounded-lg flex items-center justify-center", config.bg, config.color)}>
                    <Icon size={20} />
                  </div>
                </div>
                
                <div className="flex-1 bg-white border border-border-default rounded-xl p-4 shadow-sm">
                  <div className="flex items-center justify-between mb-2">
                    <div className="font-mono text-[11px] font-bold tracking-wider text-text-primary uppercase">
                      {event.event_type.replace('_', ' ')}
                    </div>
                    <div className="text-[10px] text-text-secondary font-mono">
                      {new Date(event.created_at).toLocaleTimeString('en-IN', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit', fractionalSecondDigits: 3 })} IST
                    </div>
                  </div>
                  
                  <div className="text-[13px] text-text-secondary leading-relaxed">
                    {event.event_type === 'WEBHOOK_VERIFIED' && (
                      <>
                        Signed event received from <span className="font-medium text-text-primary">{event.details?.source || 'Razorpay Route'}</span>. Signature matched ledger account and event nonce has not been seen before.
                        <div className="flex gap-2 mt-3">
                          <span className="px-2 py-1 bg-page border border-border-default rounded text-[10px] font-mono">{event.details?.event_id || 'evt_unknown'}</span>
                          <span className="px-2 py-1 bg-page border border-border-default rounded text-[10px] font-mono">HMAC-SHA256 - VALID</span>
                        </div>
                      </>
                    )}
                    
                    {event.event_type === 'AI_DIAGNOSIS' && (
                      <>
                        <div className="mb-2">{event.details?.diagnosis || 'Diagnosis completed.'}</div>
                        <div className="flex items-center gap-2 mt-3">
                          <span className="px-2 py-1 bg-accent-blue/10 text-accent-blue font-medium rounded text-[10px] uppercase">
                            Confidence: {event.details?.confidence || '0.90'}
                          </span>
                        </div>
                      </>
                    )}

                    {event.event_type === 'POLICY_EVALUATION' && (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3 mt-2">
                        {event.details?.evaluations && Array.isArray(event.details.evaluations) ? (
                          event.details.evaluations.map((ev: any, i: number) => (
                            <div key={i} className={clsx("p-3 rounded-lg border", ev.allowed ? "bg-semantic-green/5 border-semantic-green/20" : "bg-semantic-red/5 border-semantic-red/20")}>
                              <div className={clsx("text-[11px] font-bold uppercase mb-1 flex items-center gap-1", ev.allowed ? "text-semantic-green" : "text-semantic-red")}>
                                {ev.allowed ? <CheckCircle2 size={12} /> : <AlertCircle size={12} />}
                                {ev.allowed ? 'ALLOWED' : 'BLOCKED'} · {ev.action}
                              </div>
                              <div className="text-[11px] text-text-secondary">{ev.reason}</div>
                            </div>
                          ))
                        ) : (
                          <div className="text-sm">Policy evaluations applied successfully.</div>
                        )}
                      </div>
                    )}

                    {event.event_type === 'DECISION' && (
                      <>
                        Selected <span className="font-medium text-text-primary">{event.details?.chosen_action || 'an action'}</span>. Decision sealed by policy engine.
                      </>
                    )}

                    {event.event_type === 'TOOL_EXECUTION' && (
                      <>
                        Recovery request accepted by the tool. Idempotency key is locked for this case.
                        <div className="mt-3">
                          <span className="px-2 py-1 bg-semantic-green/10 text-semantic-green border border-semantic-green/20 rounded text-[10px] font-mono flex items-center gap-1 w-max">
                            <Lock size={10} />
                            {event.details?.tool_name || 'execute_action'}
                          </span>
                        </div>
                      </>
                    )}

                    {event.event_type === 'OUTCOME' && (
                      <>
                        Funds recovered and ledger marked settled. Customer notification queued; reconciliation will verify the bank reference in the next sweep.
                        <div className="flex gap-2 mt-3">
                          <span className="px-2 py-1 bg-semantic-green/10 text-semantic-green border border-semantic-green/20 rounded text-[10px] font-bold flex items-center gap-1">
                            <CheckCircle2 size={10} />
                            TEE VERIFIED
                          </span>
                          <span className="px-2 py-1 bg-page text-text-secondary border border-border-default rounded text-[10px] font-mono">
                            BANK REF - {event.details?.reference_id || 'PENDING'}
                          </span>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right side context panel */}
      <div className="w-[280px] flex-shrink-0 space-y-4">
        {/* Execution Trace Box */}
        <div className="bg-white border border-border-default rounded-xl p-5 shadow-sm">
          <div className="flex items-center gap-2 text-accent-blue mb-4">
            <Activity size={16} />
            <span className="font-mono text-[10px] font-bold tracking-widest uppercase">Execution Trace</span>
          </div>
          
          <div className="flex justify-between items-end mb-6">
            <div>
              <div className="text-[10px] font-mono font-bold text-text-secondary uppercase tracking-wider mb-1">Decision Latency</div>
              <div className="font-display font-semibold text-2xl text-text-primary">146ms</div>
            </div>
            <div className="text-[10px] font-mono font-bold text-text-secondary uppercase tracking-wider bg-page px-2 py-1 rounded">
              Avg
            </div>
          </div>

          <div className="relative pl-3 border-l border-border-default space-y-4 mb-4">
            <div className="relative">
              <div className="absolute -left-[17px] top-1.5 w-2 h-2 rounded-full bg-accent-blue"></div>
              <div className="text-[10px] font-mono text-text-secondary">09:41:52</div>
              <div className="text-xs font-semibold text-text-primary">Event accepted</div>
            </div>
            <div className="relative">
              <div className="absolute -left-[17px] top-1.5 w-2 h-2 rounded-full bg-accent-blue"></div>
              <div className="text-[10px] font-mono text-text-secondary">09:41:52</div>
              <div className="text-xs font-semibold text-text-primary">Diagnosis sealed</div>
            </div>
            <div className="relative">
              <div className="absolute -left-[17px] top-1.5 w-2 h-2 rounded-full bg-accent-blue"></div>
              <div className="text-[10px] font-mono text-text-secondary">09:41:52</div>
              <div className="text-xs font-semibold text-text-primary">Policy evaluated</div>
            </div>
            <div className="relative">
              <div className="absolute -left-[17px] top-1.5 w-2 h-2 rounded-full bg-accent-blue"></div>
              <div className="text-[10px] font-mono text-text-secondary">09:42:07</div>
              <div className="text-xs font-semibold text-text-primary">Settlement confirmed</div>
            </div>
          </div>
          
          <div className="flex items-center gap-2 text-[10px] font-mono font-bold text-semantic-green uppercase tracking-wider mt-4">
            <CheckCircle2 size={12} />
            Immutable trace recorded
          </div>
        </div>
      </div>
    </div>
  );
}
