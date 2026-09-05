import { Filter, CheckCircle2, ChevronRight, Zap, AlertCircle } from 'lucide-react';
import clsx from 'clsx';
import { useState, useEffect, useRef } from 'react';
import { api, type RecoveryCase } from '../../api';

const POLL_INTERVAL_MS = 5000;

function aiProviderLabel(provider: string | null | undefined) {
  if (!provider) return null;
  if (provider === 'NEAR_AI') return { label: 'NEAR AI', color: 'text-purple-400', bg: 'bg-purple-900/30 border-purple-700/50' };
  if (provider === 'GROQ_FALLBACK') return { label: 'Groq Fallback', color: 'text-amber-400', bg: 'bg-amber-900/30 border-amber-700/50' };
  return { label: provider, color: 'text-blue-400', bg: 'bg-blue-900/30 border-blue-700/50' };
}

function diagnosisToTitle(diagnosis: string | null): string {
  if (!diagnosis) return 'Diagnosis Pending...';
  // Map known diagnosis codes to human-readable titles
  const map: Record<string, string> = {
    INSUFFICIENT_FUNDS: 'Insufficient Funds — Retry with lower amount',
    CARD_DECLINED: 'Card Declined — Offer alternative payment method',
    TECHNICAL_ERROR: 'Technical Error — Auto-retry initiated',
    EXPIRED_CARD: 'Expired Card — Request card update',
    FRAUD_SUSPICION: 'Fraud Suspicion — Escalate to human review',
    NETWORK_ERROR: 'Network Error — Schedule retry',
  };
  return map[diagnosis] ?? diagnosis.replace(/_/g, ' ');
}

function statusToProposalStatus(status: string): 'Pending' | 'Approved' | 'Stopped' {
  if (status === 'RECOVERED') return 'Approved';
  if (status === 'STOPPED') return 'Stopped';
  return 'Pending';
}

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins} min ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs} hr ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export function AiProposalFeed() {
  const [filter, setFilter] = useState('All');
  const [cases, setCases] = useState<RecoveryCase[]>([]);
  const [loading, setLoading] = useState(true);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  async function loadCases() {
    try {
      const data = await api.getRecentCases();
      setCases(data.slice(0, 10)); // show latest 10
    } catch (err) {
      console.error('AiProposalFeed: failed to load cases', err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadCases();
    intervalRef.current = setInterval(loadCases, POLL_INTERVAL_MS);
    return () => { if (intervalRef.current) clearInterval(intervalRef.current); };
  }, []);

  const proposals = cases.map((c, i) => ({
    id: `LED-${c.id.slice(0, 6).toUpperCase()}`,
    rawId: c.id,
    status: statusToProposalStatus(c.status),
    title: diagnosisToTitle(c.diagnosis),
    subtitle: `Case ${c.id.slice(0, 8).toUpperCase()} · action: ${c.chosen_action ?? 'pending'}`,
    timeAgo: timeAgo(c.created_at),
    expectedValue: c.expected_recovery_value,
  }));

  const filtered = proposals.filter(p =>
    filter === 'All' || p.status === filter
  );

  return (
    <div className="bg-[#111827] text-white rounded-xl shadow-lg overflow-hidden flex flex-col h-full">
      <div className="px-6 py-5 border-b border-[#374151] flex items-start justify-between">
        <div>
          <div className="flex items-center gap-3 mb-1">
            <h2 className="font-display font-semibold text-lg tracking-tight text-white">AI Proposal Feed</h2>
            <div className="flex items-center gap-1.5 bg-[#1E3A8A] border border-[#2563EB] text-[#60A5FA] px-2 py-0.5 rounded-full text-[9px] font-mono font-bold tracking-wider uppercase shadow-[0_0_8px_rgba(37,99,235,0.4)]">
              <CheckCircle2 size={10} />
              TEE Verified
            </div>
          </div>
          <div className="text-[13px] text-[#9CA3AF]">
            Live recovery decisions — autonomous AI pipeline.
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex bg-[#1F2937] border border-[#374151] rounded-md p-1">
            {['All', 'Pending', 'Approved'].map(f => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={clsx(
                  'px-4 py-1.5 text-[13px] font-medium rounded transition-colors',
                  filter === f ? 'bg-[#374151] text-white shadow-sm' : 'text-[#9CA3AF] hover:text-white'
                )}
              >
                {f}
              </button>
            ))}
          </div>
          <button className="p-1.5 border border-[#374151] bg-[#1F2937] rounded-md text-[#9CA3AF] hover:text-white transition-colors">
            <Filter size={16} />
          </button>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto">
        {loading ? (
          <div className="p-6 text-center text-[#9CA3AF] text-sm animate-pulse">Loading live cases...</div>
        ) : filtered.length === 0 ? (
          <div className="p-6 text-center text-[#9CA3AF] text-sm flex flex-col items-center gap-2">
            <AlertCircle size={20} className="text-[#4B5563]" />
            No cases yet. Trigger a payment failure to see the pipeline in action.
          </div>
        ) : (
          filtered.map(proposal => (
            <div key={proposal.rawId} className="group border-b border-[#1F2937] px-6 py-4 flex items-center justify-between hover:bg-[#1F2937] transition-colors cursor-pointer">
              <div className="flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-[#1E3A8A]/30 border border-[#1E3A8A] text-[#60A5FA] flex items-center justify-center mt-0.5">
                  <Zap size={18} fill="currentColor" className="opacity-80" />
                </div>
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-[11px] font-mono text-[#9CA3AF]">{proposal.id}</span>
                    <span className={clsx(
                      'text-[10px] font-medium px-2 py-0.5 rounded-sm border',
                      proposal.status === 'Pending'
                        ? 'bg-[#92400E]/30 text-[#FBBF24] border-[#92400E]/50'
                        : proposal.status === 'Approved'
                        ? 'bg-[#065F46]/30 text-[#34D399] border-[#065F46]/50'
                        : 'bg-[#374151]/30 text-[#9CA3AF] border-[#374151]/50'
                    )}>
                      {proposal.status}
                    </span>
                  </div>
                  <h3 className="font-sans font-semibold text-[15px] text-white mb-0.5">{proposal.title}</h3>
                  <p className="text-[13px] text-[#9CA3AF]">{proposal.subtitle}</p>
                </div>
              </div>

              <div className="flex items-center gap-8 text-right">
                {proposal.expectedValue ? (
                  <div>
                    <div className="font-display font-bold text-[15px] text-white">
                      ₹{Math.round(proposal.expectedValue).toLocaleString('en-IN')}
                    </div>
                    <div className="text-[9px] font-mono tracking-wider uppercase text-[#6B7280]">Expected</div>
                  </div>
                ) : null}
                <div className="w-[80px]">
                  <div className="font-sans font-medium text-[13px] text-white">{proposal.timeAgo}</div>
                  <div className="text-[9px] font-mono tracking-wider uppercase text-[#6B7280]">Generated</div>
                </div>
                <div className="text-[#4B5563] group-hover:text-white transition-colors">
                  <ChevronRight size={20} />
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      <div className="px-6 py-3 bg-[#030712] border-t border-[#1F2937] flex items-center justify-between">
        <div className="flex items-center gap-2 text-[11px] text-[#6B7280]">
          <span className="inline-flex relative">
            <span className="animate-ping absolute inline-flex h-1.5 w-1.5 rounded-full bg-green-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-400"></span>
          </span>
          Live pipeline · polling every 5s
        </div>
        <button className="text-[13px] font-medium text-[#60A5FA] hover:text-white transition-colors flex items-center gap-1">
          View all in Recovery Log <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
}
