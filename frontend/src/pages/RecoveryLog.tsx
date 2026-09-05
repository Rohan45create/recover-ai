import { useEffect, useState, useRef, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, type RecoveryCase } from '../api';
import { Search, ChevronDown, ChevronRight, Activity, Clock, ShieldAlert, CheckCircle2, AlertCircle, XCircle } from 'lucide-react';
import { RecoveryLogTimeline } from '../components/RecoveryLogTimeline';
import { ToastContainer, type ToastMessage } from '../components/Toast';

const POLL_INTERVAL_MS = 5000;

export function RecoveryLog() {
  const [searchParams] = useSearchParams();
  const initialCaseId = searchParams.get('caseId');

  const [cases, setCases] = useState<RecoveryCase[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedCase, setExpandedCase] = useState<string | null>(initialCaseId);
  const [timelines, setTimelines] = useState<Record<string, any[]>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [lastSynced, setLastSynced] = useState<Date | null>(null);
  const [secondsSince, setSecondsSince] = useState(0);
  const [toasts, setToasts] = useState<ToastMessage[]>([]);
  const prevStatusMap = useRef<Record<string, string>>({});
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const tickRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const dismissToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  const loadCases = useCallback(async (isInitial = false) => {
    try {
      const data = await api.getRecentCases();

      // Detect status changes and fire toasts
      if (!isInitial) {
        const newToasts: ToastMessage[] = [];
        data.forEach(c => {
          const prev = prevStatusMap.current[c.id];
          if (prev && prev !== c.status) {
            newToasts.push({
              id: `${c.id}-${Date.now()}`,
              caseId: c.id,
              oldStatus: prev,
              newStatus: c.status,
              timestamp: Date.now(),
            });
          }
        });
        if (newToasts.length > 0) {
          setToasts(prev => [...prev, ...newToasts]);
          // Refresh timelines for cases whose status changed
          newToasts.forEach(t => {
            setTimelines(prev => {
              const next = { ...prev };
              delete next[t.caseId]; // invalidate so it reloads on expand
              return next;
            });
          });
        }
      }

      // Update status map
      const map: Record<string, string> = {};
      data.forEach(c => { map[c.id] = c.status; });
      prevStatusMap.current = map;

      setCases(data);
      setLastSynced(new Date());
      setSecondsSince(0);

      if (isInitial && initialCaseId) {
        loadTimeline(initialCaseId);
      }
    } catch (err) {
      console.error('Failed to load cases', err);
    } finally {
      if (isInitial) setLoading(false);
    }
  }, [initialCaseId]);

  useEffect(() => {
    loadCases(true);
    intervalRef.current = setInterval(() => loadCases(false), POLL_INTERVAL_MS);
    tickRef.current = setInterval(() => setSecondsSince(s => s + 1), 1000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (tickRef.current) clearInterval(tickRef.current);
    };
  }, [loadCases]);

  const loadTimeline = async (caseId: string) => {
    try {
      const timelineData = await api.getCaseTimeline(caseId);
      setTimelines(prev => ({ ...prev, [caseId]: timelineData }));
    } catch (err) {
      console.error('Failed to load timeline', err);
    }
  };

  const toggleExpand = (caseId: string) => {
    if (expandedCase === caseId) {
      setExpandedCase(null);
    } else {
      setExpandedCase(caseId);
      loadTimeline(caseId);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 0
    }).format(amount);
  };

  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'RECOVERED': return { color: 'text-semantic-green', bg: 'bg-semantic-green/10', icon: CheckCircle2, label: 'Recovered' };
      case 'DIAGNOSING': return { color: 'text-accent-blue', bg: 'bg-accent-blue/10', icon: Activity, label: 'Diagnosing' };
      case 'ACTION_EXECUTING': return { color: 'text-accent-blue', bg: 'bg-accent-blue/10', icon: Activity, label: 'Executing' };
      case 'ACTION_PENDING': return { color: 'text-accent-blue', bg: 'bg-accent-blue/10', icon: Activity, label: 'Action Pending' };
      case 'WAITING': return { color: 'text-semantic-amber', bg: 'bg-semantic-amber/10', icon: Clock, label: 'Waiting' };
      case 'STOPPED': return { color: 'text-semantic-gray', bg: 'bg-semantic-gray/10', icon: XCircle, label: 'Stopped' };
      default: return { color: 'text-text-secondary', bg: 'bg-page', icon: AlertCircle, label: status };
    }
  };

  const filteredCases = cases.filter(c => {
    if (filter !== 'ALL' && c.status !== filter) return false;
    if (searchQuery) {
      return c.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
             (c.diagnosis && c.diagnosis.toLowerCase().includes(searchQuery.toLowerCase()));
    }
    return true;
  });

  if (loading) {
    return <div className="p-8 text-center text-text-secondary">Loading Recovery Log...</div>;
  }

  return (
    <>
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />

      <div className="max-w-[1200px] mx-auto space-y-6">

        {/* Header with LIVE indicator */}
        <div className="flex items-center justify-between">
          <div />
          <div className="flex items-center gap-1.5 text-[12px]">
            <span className="inline-flex relative">
              <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-green-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-green-400"></span>
            </span>
            <span className="font-medium text-green-400">LIVE</span>
            {lastSynced && (
              <span className="text-text-secondary ml-1">· synced {secondsSince}s ago</span>
            )}
          </div>
        </div>

        {/* Filter and Search Bar */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-surface p-4 rounded-xl border border-border-default shadow-sm">
          <div className="flex items-center gap-2 overflow-x-auto w-full sm:w-auto">
            {['ALL', 'RECOVERED', 'DIAGNOSING', 'WAITING', 'STOPPED'].map(f => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`px-3 py-1.5 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${filter === f ? 'bg-text-primary text-white' : 'bg-page text-text-secondary hover:bg-border-default'}`}
              >
                {f}
              </button>
            ))}
          </div>

          <div className="relative w-full sm:w-64">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary" />
            <input
              type="text"
              placeholder="Search cases..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="w-full bg-page border border-border-default rounded-md pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-accent-blue/20 focus:border-accent-blue transition-all"
            />
          </div>
        </div>

        {/* Case List */}
        <div className="bg-surface rounded-xl border border-border-default shadow-sm overflow-hidden">
          {filteredCases.length === 0 ? (
            <div className="p-12 text-center text-text-secondary">No cases match your filters.</div>
          ) : (
            <div className="divide-y divide-border-default">
              {filteredCases.map(rc => {
                const status = getStatusConfig(rc.status);
                const StatusIcon = status.icon;
                const isExpanded = expandedCase === rc.id;

                return (
                  <div key={rc.id} className="flex flex-col">
                    {/* Row Header */}
                    <div
                      onClick={() => toggleExpand(rc.id)}
                      className={`flex items-center justify-between p-4 cursor-pointer hover:bg-page/50 transition-colors ${isExpanded ? 'bg-page/50' : ''}`}
                    >
                      <div className="flex items-center gap-4">
                        <button className="text-text-secondary">
                          {isExpanded ? <ChevronDown size={20} /> : <ChevronRight size={20} />}
                        </button>
                        <div>
                          <div className="font-mono text-sm font-medium text-text-primary flex items-center gap-2">
                            {rc.id}
                          </div>
                          <div className="text-xs text-text-secondary mt-1">
                            {new Date(rc.created_at).toLocaleString()}
                          </div>
                        </div>
                      </div>

                      <div className="flex items-center gap-6">
                        <div className="hidden md:block text-right">
                          <div className="text-sm font-medium text-text-primary">{rc.diagnosis || 'Diagnosis Pending'}</div>
                          <div className="text-xs text-text-secondary mt-1">Action: {rc.chosen_action || '-'}</div>
                        </div>

                        <div className="text-right w-24 font-mono text-sm font-medium text-text-primary">
                          {rc.status === 'RECOVERED' && rc.actual_recovered_amount ? (
                            <div className="flex flex-col">
                              <span className="text-semantic-green">{formatCurrency(rc.actual_recovered_amount)}</span>
                              <span className="text-[10px] text-text-secondary uppercase mt-0.5 tracking-wider">Recovered</span>
                            </div>
                          ) : rc.expected_recovery_value ? (
                            <div className="flex flex-col">
                              <span>{formatCurrency(rc.expected_recovery_value)}</span>
                              <span className="text-[10px] text-text-secondary uppercase mt-0.5 tracking-wider">Expected</span>
                            </div>
                          ) : '-'}
                        </div>

                        <div className={`w-32 inline-flex justify-center items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${status.bg} ${status.color}`}>
                          <StatusIcon size={12} />
                          {status.label}
                        </div>
                      </div>
                    </div>

                    {/* Expanded Timeline */}
                    {isExpanded && (
                      <div className="p-6 bg-page/30 border-t border-border-default">
                        <div className="max-w-4xl mx-auto">
                          <h4 className="text-[13px] font-semibold text-text-primary mb-6 flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <ShieldAlert size={16} className="text-text-secondary" />
                              APPEND-ONLY AUDIT TRAIL
                            </div>
                            {rc.payment_link_url && (
                              <a href={rc.payment_link_url} target="_blank" rel="noopener noreferrer" 
                                 className="px-3 py-1 bg-accent-blue/10 hover:bg-accent-blue/20 text-accent-blue border border-accent-blue/20 rounded text-xs font-medium transition-colors">
                                Open Payment Link
                              </a>
                            )}
                          </h4>

                          {!timelines[rc.id] ? (
                            <div className="text-sm text-text-secondary animate-pulse">Loading timeline...</div>
                          ) : (
                            <RecoveryLogTimeline events={timelines[rc.id]} />
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </>
  );
}
