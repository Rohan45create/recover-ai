import { useEffect, useState } from 'react';
import { CheckCircle2, Activity, XCircle, Clock, X } from 'lucide-react';

export interface ToastMessage {
  id: string;
  caseId: string;
  oldStatus: string;
  newStatus: string;
  timestamp: number;
}

interface ToastProps {
  toasts: ToastMessage[];
  onDismiss: (id: string) => void;
}

function statusLabel(s: string) {
  const map: Record<string, string> = {
    ELIGIBLE: 'Eligible',
    DIAGNOSING: 'Diagnosing',
    ACTION_PENDING: 'Action Pending',
    ACTION_EXECUTING: 'Executing',
    RECOVERED: 'Recovered',
    WAITING: 'Waiting',
    STOPPED: 'Stopped',
  };
  return map[s] ?? s;
}

function statusColor(s: string) {
  if (s === 'RECOVERED') return 'text-emerald-400';
  if (s === 'STOPPED') return 'text-gray-400';
  if (s === 'WAITING') return 'text-amber-400';
  return 'text-blue-400';
}

function StatusIcon({ status }: { status: string }) {
  if (status === 'RECOVERED') return <CheckCircle2 size={16} className="text-emerald-400" />;
  if (status === 'STOPPED') return <XCircle size={16} className="text-gray-400" />;
  if (status === 'WAITING') return <Clock size={16} className="text-amber-400" />;
  return <Activity size={16} className="text-blue-400" />;
}

function Toast({ toast, onDismiss }: { toast: ToastMessage; onDismiss: (id: string) => void }) {
  useEffect(() => {
    const t = setTimeout(() => onDismiss(toast.id), 6000);
    return () => clearTimeout(t);
  }, [toast.id, onDismiss]);

  const shortId = toast.caseId.slice(0, 8).toUpperCase();

  return (
    <div className="flex items-start gap-3 bg-[#111827] border border-[#374151] rounded-xl px-4 py-3 shadow-2xl min-w-[320px] max-w-[400px] animate-slide-in">
      <div className="mt-0.5">
        <StatusIcon status={toast.newStatus} />
      </div>
      <div className="flex-1 min-w-0">
        <div className="text-[13px] font-semibold text-white">
          Case <span className="font-mono text-[#60A5FA]">LED-{shortId}</span> updated
        </div>
        <div className="text-[12px] text-[#9CA3AF] mt-0.5">
          <span className="text-[#6B7280]">{statusLabel(toast.oldStatus)}</span>
          {' → '}
          <span className={statusColor(toast.newStatus)}>{statusLabel(toast.newStatus)}</span>
        </div>
      </div>
      <button
        onClick={() => onDismiss(toast.id)}
        className="text-[#6B7280] hover:text-white transition-colors mt-0.5"
      >
        <X size={14} />
      </button>
    </div>
  );
}

export function ToastContainer({ toasts, onDismiss }: ToastProps) {
  return (
    <div className="fixed bottom-6 right-6 z-50 flex flex-col gap-3 items-end">
      {toasts.map(t => (
        <Toast key={t.id} toast={t} onDismiss={onDismiss} />
      ))}
    </div>
  );
}
