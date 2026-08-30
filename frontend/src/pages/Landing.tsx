import { useNavigate } from 'react-router-dom';
import { Shield, Brain, Activity, Lock, ArrowRight, CheckCircle2, ChevronRight, XCircle } from 'lucide-react';

export function Landing() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[#0A0D14] text-white selection:bg-accent-blue/30 overflow-x-hidden font-sans">
      
      {/* 1. Header */}
      <header className="fixed top-0 w-full z-50 bg-[#0A0D14]/80 backdrop-blur-md border-b border-white/5">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded bg-accent-blue flex items-center justify-center text-white font-bold text-sm shadow-[0_0_15px_rgba(46,90,234,0.5)]">R</div>
            <span className="font-display font-semibold text-xl tracking-tight">RecoverAI</span>
          </div>
          <button 
            onClick={() => navigate('/app')}
            className="px-5 py-2.5 bg-white/10 hover:bg-white/15 border border-white/10 rounded-md text-sm font-medium transition-all flex items-center gap-2"
          >
            Launch Dashboard <ChevronRight size={16} />
          </button>
        </div>
      </header>

      {/* 2. Hero Section with Particle Motif (Simulated with CSS for now) */}
      <section className="relative pt-40 pb-32 px-6 overflow-hidden">
        {/* Background "Recovery Engine" abstract visualization */}
        <div className="absolute inset-0 z-0 opacity-30 pointer-events-none flex items-center justify-center">
           <div className="w-[800px] h-[800px] border border-accent-blue/20 rounded-full animate-[spin_60s_linear_infinite]" />
           <div className="absolute w-[600px] h-[600px] border border-semantic-green/20 rounded-full animate-[spin_40s_linear_infinite_reverse]" />
           <div className="absolute w-2 h-2 bg-accent-blue rounded-full shadow-[0_0_20px_rgba(46,90,234,1)]" />
        </div>

        <div className="relative z-10 max-w-4xl mx-auto text-center">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-accent-blue/10 border border-accent-blue/20 text-accent-blue text-xs font-semibold tracking-widest uppercase mb-8">
            <span className="w-2 h-2 rounded-full bg-accent-blue animate-pulse" />
            Built for Razorpay Buildathon 2026
          </div>
          <h1 className="font-display font-semibold text-5xl md:text-7xl leading-[1.1] mb-8 tracking-tight">
            Autonomous revenue recovery <br className="hidden md:block"/>
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-blue to-semantic-green">
              governed by strict policy.
            </span>
          </h1>
          <p className="text-lg md:text-xl text-text-secondary max-w-2xl mx-auto mb-10 leading-relaxed font-light">
            Don't rely on blind retries. RecoverAI diagnoses failed payments using Gemma 4 31B, scores actions for maximum expected value, and executes strictly within your compliance guardrails.
          </p>
          <div className="flex items-center justify-center gap-4">
            <button 
              onClick={() => navigate('/app')}
              className="px-8 py-4 bg-accent-blue hover:bg-accent-blue/90 text-white rounded-md text-base font-medium transition-all shadow-[0_4px_20px_rgba(46,90,234,0.4)] flex items-center gap-2 hover:scale-105"
            >
              Enter Dashboard <ArrowRight size={18} />
            </button>
            <button className="px-8 py-4 bg-surface-dark-panel hover:bg-white/5 border border-white/10 rounded-md text-base font-medium transition-all">
              View Architecture
            </button>
          </div>
        </div>
      </section>

      {/* 3. The Problem */}
      <section className="py-24 px-6 bg-[#0E111A] border-t border-b border-white/5">
        <div className="max-w-7xl mx-auto">
          <div className="grid md:grid-cols-2 gap-16 items-center">
            <div>
              <h2 className="font-display font-semibold text-3xl mb-6">Blind retries are costing you money and trust.</h2>
              <p className="text-text-secondary mb-6 leading-relaxed">
                When a payment fails, existing systems fall back to generic, time-based retries. They don't know if a card expired, if funds were insufficient, or if the network timed out. 
              </p>
              <p className="text-text-secondary leading-relaxed">
                This causes unoptimized recovery rates, mandate window violations, and frustrated customers. You need intelligence that adapts to the failure reason, bounded by rules that protect your brand.
              </p>
            </div>
            <div className="bg-surface-dark-panel border border-white/10 rounded-2xl p-8 relative overflow-hidden">
               <div className="absolute top-0 right-0 w-64 h-64 bg-semantic-red/10 blur-3xl rounded-full" />
               <div className="font-mono text-sm text-semantic-red mb-2 uppercase tracking-wider">Traditional approach</div>
               <div className="space-y-4 relative z-10">
                 <div className="p-4 bg-white/5 rounded-lg border border-white/5 text-text-secondary flex items-center line-through">
                   Wait 24 hours → Retry Transaction
                 </div>
                 <div className="p-4 bg-white/5 rounded-lg border border-white/5 text-text-secondary flex items-center line-through">
                   Wait 48 hours → Send Email
                 </div>
                 <div className="p-4 bg-semantic-red/10 border border-semantic-red/20 rounded-lg text-white flex items-center gap-3">
                   <XCircle className="text-semantic-red" />
                   Customer blocked due to spam. Mandate window expired.
                 </div>
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* 4. Recovery Numbers */}
      <section className="py-24 px-6 max-w-7xl mx-auto border-b border-white/5">
        <div className="text-center mb-16">
          <h2 className="font-display font-semibold text-3xl mb-4">The numbers speak for themselves</h2>
          <p className="text-text-secondary">Based on a 1,000-case synthetic dataset evaluation using live transaction patterns.</p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-8 mb-12">
          <div className="text-center p-8 bg-surface-dark-panel rounded-2xl border border-white/5 relative overflow-hidden group">
            <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-transparent via-semantic-gray to-transparent opacity-50" />
            <div className="text-sm font-semibold text-text-secondary mb-2 uppercase tracking-widest">Baseline (Standard Retries)</div>
            <div className="font-display font-medium text-4xl mb-1 text-white">2.4%</div>
            <div className="text-sm text-text-secondary">Recovery Rate</div>
          </div>
          
          <div className="text-center p-8 bg-accent-blue/10 rounded-2xl border border-accent-blue/30 relative overflow-hidden shadow-[0_0_30px_rgba(46,90,234,0.15)] transform hover:scale-105 transition-transform">
            <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-transparent via-accent-blue to-transparent" />
            <div className="text-sm font-bold text-accent-blue mb-2 uppercase tracking-widest flex items-center justify-center gap-2">
              <span className="w-2 h-2 rounded-full bg-accent-blue animate-pulse" />
              RecoverAI
            </div>
            <div className="font-display font-bold text-5xl mb-1 text-white shadow-sm">12.8%</div>
            <div className="text-sm text-accent-blue">Recovery Rate (+433%)</div>
          </div>
          
          <div className="text-center p-8 bg-surface-dark-panel rounded-2xl border border-white/5 relative overflow-hidden">
            <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-transparent via-semantic-green to-transparent opacity-50" />
            <div className="text-sm font-semibold text-text-secondary mb-2 uppercase tracking-widest">Compliance</div>
            <div className="font-display font-medium text-4xl mb-1 text-semantic-green">0</div>
            <div className="text-sm text-text-secondary">Policy Violations</div>
          </div>
        </div>
      </section>

      {/* 5. 3-Card Grid */}
      <section className="py-24 px-6 max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="font-display font-semibold text-3xl mb-4">A deterministic pipeline for AI recovery</h2>
          <p className="text-text-secondary">AI PROPOSES → POLICY DECIDES → TOOLS EXECUTE → AUDIT PROVES</p>
        </div>
        
        <div className="grid md:grid-cols-3 gap-8">
          <div className="bg-surface-dark-panel p-8 rounded-2xl border border-white/5 hover:border-accent-blue/30 transition-colors group">
            <div className="w-12 h-12 rounded-xl bg-accent-blue/10 flex items-center justify-center text-accent-blue mb-6 group-hover:scale-110 transition-transform">
              <Brain size={24} />
            </div>
            <h3 className="font-display font-medium text-xl mb-3">AI Diagnosis</h3>
            <p className="text-text-secondary text-sm leading-relaxed">
              Gemma 4 31B analyzes anonymized transaction features to diagnose the true root cause of failure and suggests a set of high-probability recovery actions.
            </p>
          </div>
          
          <div className="bg-surface-dark-panel p-8 rounded-2xl border border-white/5 hover:border-semantic-green/30 transition-colors group">
            <div className="w-12 h-12 rounded-xl bg-semantic-green/10 flex items-center justify-center text-semantic-green mb-6 group-hover:scale-110 transition-transform">
              <Shield size={24} />
            </div>
            <h3 className="font-display font-medium text-xl mb-3">Deterministic Policy</h3>
            <p className="text-text-secondary text-sm leading-relaxed">
              The AI cannot act directly. A hard-coded policy engine intercepts all proposals, stripping out actions that violate DND hours, mandate windows, or cost caps.
            </p>
          </div>
          
          <div className="bg-surface-dark-panel p-8 rounded-2xl border border-white/5 hover:border-accent-blue/30 transition-colors group">
            <div className="w-12 h-12 rounded-xl bg-accent-blue/10 flex items-center justify-center text-accent-blue mb-6 group-hover:scale-110 transition-transform">
              <Activity size={24} />
            </div>
            <h3 className="font-display font-medium text-xl mb-3">Expected Value Execution</h3>
            <p className="text-text-secondary text-sm leading-relaxed">
              The decision engine calculates `P(recovery) × Amount` minus intervention costs and friction penalties to execute the most profitable Razorpay integration.
            </p>
          </div>
        </div>
      </section>

      {/* 5. TEE / Privacy */}
      <section className="py-24 px-6 bg-[#0E111A] border-t border-white/5">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row gap-16 items-center">
          <div className="flex-1">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-white/5 border border-white/10 mb-6">
              <Lock className="text-white" size={28} />
            </div>
            <h2 className="font-display font-semibold text-3xl mb-6">Zero PII Leakage. Secure Enclave Inference.</h2>
            <p className="text-text-secondary mb-4 leading-relaxed">
              Institutional recovery requires institutional security. RecoverAI executes AI inference inside NEAR AI Cloud's Trusted Execution Environment (TEE).
            </p>
            <ul className="space-y-4 mt-8">
              <li className="flex items-start gap-3 text-sm text-text-secondary">
                <CheckCircle2 className="text-semantic-green shrink-0 mt-0.5" size={18} />
                <span>Payloads are mathematically stripped of PII before leaving your infrastructure.</span>
              </li>
              <li className="flex items-start gap-3 text-sm text-text-secondary">
                <CheckCircle2 className="text-semantic-green shrink-0 mt-0.5" size={18} />
                <span>Cryptographic attestation proves the exact model weights used for diagnosis.</span>
              </li>
              <li className="flex items-start gap-3 text-sm text-text-secondary">
                <CheckCircle2 className="text-semantic-green shrink-0 mt-0.5" size={18} />
                <span>Tamper-evident, append-only PostgreSQL trigger secures the audit log from mutation.</span>
              </li>
            </ul>
          </div>
          <div className="flex-1 bg-surface-dark-panel p-1 rounded-2xl border border-white/10 shadow-2xl">
            <div className="bg-[#0A0D14] rounded-xl p-6 font-mono text-xs text-text-secondary overflow-hidden">
              <div className="text-semantic-green mb-2">// Payload Anonymization Example</div>
              <div className="text-white">{"{"}</div>
              <div className="pl-4">
                <span className="text-accent-blue">"case_id"</span>: <span className="text-[#D97706]">"rc_9Xk2Lp..."</span>,<br/>
                <span className="text-accent-blue">"amount"</span>: <span className="text-[#D97706]">1200.00</span>,<br/>
                <span className="text-semantic-red line-through">"customer_name": "Jane Doe",</span><br/>
                <span className="text-accent-blue">"failure_code"</span>: <span className="text-[#D97706]">"INSUFFICIENT_FUNDS"</span>,<br/>
                <span className="text-accent-blue">"historical_success_rate"</span>: <span className="text-[#D97706]">0.85</span>
              </div>
              <div className="text-white">{"}"}</div>
            </div>
          </div>
        </div>
      </section>

      {/* 6. Differentiation Table */}
      <section className="py-24 px-6 max-w-5xl mx-auto">
        <h2 className="font-display font-semibold text-3xl mb-12 text-center">Why RecoverAI is Different</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-white/10">
                <th className="py-4 px-6 font-sans font-medium text-text-secondary w-1/3">Feature</th>
                <th className="py-4 px-6 font-sans font-medium text-text-secondary w-1/3">Standard Retry Systems</th>
                <th className="py-4 px-6 font-sans font-semibold text-accent-blue bg-accent-blue/5 rounded-t-xl w-1/3">RecoverAI</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5 text-sm">
              <tr>
                <td className="py-4 px-6 font-medium">Diagnosis</td>
                <td className="py-4 px-6 text-text-secondary">None (Blind retries)</td>
                <td className="py-4 px-6 text-white bg-accent-blue/5">LLM-driven root cause analysis</td>
              </tr>
              <tr>
                <td className="py-4 px-6 font-medium">Action Selection</td>
                <td className="py-4 px-6 text-text-secondary">Hardcoded static paths</td>
                <td className="py-4 px-6 text-white bg-accent-blue/5">Expected Value (EV) optimization</td>
              </tr>
              <tr>
                <td className="py-4 px-6 font-medium">Compliance Guardrails</td>
                <td className="py-4 px-6 text-text-secondary">Manual configuration prone to error</td>
                <td className="py-4 px-6 text-white bg-accent-blue/5">Deterministic Policy Engine</td>
              </tr>
              <tr>
                <td className="py-4 px-6 font-medium">Auditability</td>
                <td className="py-4 px-6 text-text-secondary">Standard application logs</td>
                <td className="py-4 px-6 text-white bg-accent-blue/5">Append-only, tamper-evident ledger</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      {/* 7. Final CTA */}
      <section className="py-32 px-6 bg-gradient-to-b from-[#0A0D14] to-[#0E111A] border-t border-white/5 text-center">
        <div className="max-w-3xl mx-auto">
          <h2 className="font-display font-semibold text-4xl mb-6">Stop leaving revenue on the table.</h2>
          <p className="text-lg text-text-secondary mb-10 font-light">
            Deploy autonomous, compliant, and auditable revenue recovery with Razorpay today.
          </p>
          <button 
            onClick={() => navigate('/app')}
            className="px-10 py-4 bg-accent-blue hover:bg-accent-blue/90 text-white rounded-md text-lg font-medium transition-all shadow-[0_4px_20px_rgba(46,90,234,0.4)]"
          >
            Launch the Dashboard
          </button>
        </div>
      </section>

      <footer className="py-8 px-6 border-t border-white/5 text-center text-sm text-text-secondary">
        <p>© 2026 RecoverAI. Built for Razorpay Buildathon.</p>
      </footer>
    </div>
  );
}
