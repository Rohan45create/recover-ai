import { Filter, CheckCircle2, ChevronRight, Zap } from 'lucide-react';
import clsx from 'clsx';
import { useState } from 'react';

export function AiProposalFeed() {
  const [filter, setFilter] = useState('All');
  
  const proposals = [
    {
      id: 'RA-2048',
      status: 'Pending',
      title: 'Re-route stalled SME accounts to regional queue',
      subtitle: '1,284 accounts - expected lift +6.8%',
      confidence: 92.4,
      timeAgo: '2 min ago'
    },
    {
      id: 'RA-2047',
      status: 'Approved',
      title: 'Pause SMS cadence for disputed mandates',
      subtitle: 'Policy 04.12 - reduces repeat contacts by 14%',
      confidence: 98.1,
      timeAgo: '18 min ago'
    },
    {
      id: 'RA-2046',
      status: 'Approved',
      title: 'Offer calibrated settlement band to Tier 2',
      subtitle: '₹18.4L exposure - guardrail within policy',
      confidence: 87.6,
      timeAgo: '41 min ago'
    },
    {
      id: 'RA-2044',
      status: 'Pending',
      title: 'Escalate 63 high-value cases to human review',
      subtitle: 'Anomaly cluster - median balance ₹1.8L',
      confidence: 94.7,
      timeAgo: '1 hr ago'
    }
  ];

  return (
    <div className="bg-[#111827] text-white rounded-xl shadow-lg overflow-hidden flex flex-col">
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
            Auditable recovery decisions, protected by trusted execution.
          </div>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex bg-[#1F2937] border border-[#374151] rounded-md p-1">
            {['All', 'Pending', 'Approved'].map(f => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={clsx(
                  "px-4 py-1.5 text-[13px] font-medium rounded transition-colors",
                  filter === f ? "bg-[#374151] text-white shadow-sm" : "text-[#9CA3AF] hover:text-white"
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
        {proposals.filter(p => filter === 'All' || p.status === filter).map(proposal => (
          <div key={proposal.id} className="group border-b border-[#1F2937] px-6 py-4 flex items-center justify-between hover:bg-[#1F2937] transition-colors cursor-pointer">
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-lg bg-[#1E3A8A]/30 border border-[#1E3A8A] text-[#60A5FA] flex items-center justify-center mt-0.5">
                <Zap size={18} fill="currentColor" className="opacity-80" />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-[11px] font-mono text-[#9CA3AF]">{proposal.id}</span>
                  <span className={clsx(
                    "text-[10px] font-medium px-2 py-0.5 rounded-sm",
                    proposal.status === 'Pending' ? "bg-[#92400E]/30 text-[#FBBF24] border border-[#92400E]/50" :
                    "bg-[#065F46]/30 text-[#34D399] border border-[#065F46]/50"
                  )}>
                    {proposal.status}
                  </span>
                </div>
                <h3 className="font-sans font-semibold text-[15px] text-white mb-0.5">{proposal.title}</h3>
                <p className="text-[13px] text-[#9CA3AF]">{proposal.subtitle}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-8 text-right">
              <div>
                <div className="font-display font-bold text-[15px] text-white">{proposal.confidence}%</div>
                <div className="text-[9px] font-mono tracking-wider uppercase text-[#6B7280]">Confidence</div>
              </div>
              <div className="w-[80px]">
                <div className="font-sans font-medium text-[13px] text-white">{proposal.timeAgo}</div>
                <div className="text-[9px] font-mono tracking-wider uppercase text-[#6B7280]">Generated</div>
              </div>
              <div className="text-[#4B5563] group-hover:text-white transition-colors">
                <ChevronRight size={20} />
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="px-6 py-3 bg-[#030712] border-t border-[#1F2937] flex items-center justify-between">
        <div className="flex items-center gap-2 text-[11px] text-[#6B7280]">
          <div className="w-1.5 h-1.5 rounded-full bg-semantic-green"></div>
          TEE enclave healthy - 146ms median decision time
        </div>
        <button className="text-[13px] font-medium text-[#60A5FA] hover:text-white transition-colors flex items-center gap-1">
          View all proposals <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
}
