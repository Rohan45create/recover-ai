import { useEffect, useState } from 'react';
import { api } from '../api';
import { Shield, ShieldAlert, Lock, CheckCircle2, Edit2, Save, X } from 'lucide-react';

export function Policies() {
  const [policies, setPolicies] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editValue, setEditValue] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadPolicies();
  }, []);

  async function loadPolicies() {
    try {
      const data = await api.getPolicies();
      // Map backend 'enabled' -> frontend 'active', 'value' stays 'value'
      setPolicies(data.map((p: any) => ({ ...p, active: p.enabled ?? p.active })));
    } catch (err) {
      console.error("Failed to load policies", err);
    } finally {
      setLoading(false);
    }
  }

  const handleEdit = (policy: any) => {
    setEditingId(policy.id);
    setEditValue(policy.value);
  };

  const handleSave = async (policy: any) => {
    try {
      setSaving(true);
      await api.updatePolicy(policy.id, { value: editValue });
      setEditingId(null);
      await loadPolicies(); // Re-fetch from server to prove persistence
    } catch (err) {
      console.error("Failed to update policy", err);
      alert("Failed to save policy.");
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (policy: any) => {
    try {
      await api.updatePolicy(policy.id, { enabled: !policy.active });
      await loadPolicies(); // Re-fetch from server to prove persistence
    } catch (err) {
      console.error("Failed to toggle policy", err);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-text-secondary">Loading Policies...</div>;
  }

  return (
    <div className="max-w-[1200px] mx-auto space-y-8">
      
      {/* Intro section */}
      <div className="bg-surface p-6 rounded-xl border border-border-default shadow-sm flex flex-col md:flex-row gap-6 md:items-center">
        <div className="w-16 h-16 bg-semantic-green/10 rounded-full flex items-center justify-center text-semantic-green flex-shrink-0">
          <Shield size={32} />
        </div>
        <div>
          <h2 className="font-display font-semibold text-xl text-text-primary mb-2">Deterministic Guardrails</h2>
          <p className="text-text-secondary text-sm max-w-3xl leading-relaxed">
            The AI engine operates completely independently of action execution. It only proposes recovery strategies. 
            Before any strategy is executed, this deterministic policy engine acts as an irrevocable gate, 
            blocking any proposed action that violates compliance, limits, or regulatory frameworks.
          </p>
        </div>
      </div>

      {/* Authority Callouts */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-page p-5 rounded-lg border-l-4 border-accent-blue">
          <h3 className="font-sans font-semibold text-text-primary flex items-center gap-2 mb-2">
            <Lock size={16} className="text-accent-blue" /> AI Authority Boundary
          </h3>
          <p className="text-sm text-text-secondary">
            AI has <b>Read-Only</b> access to anonymized ledger states to formulate strategy. It cannot directly initiate API requests to payment gateways or messaging services.
          </p>
        </div>
        <div className="bg-page p-5 rounded-lg border-l-4 border-semantic-green">
          <h3 className="font-sans font-semibold text-text-primary flex items-center gap-2 mb-2">
            <CheckCircle2 size={16} className="text-semantic-green" /> Financial & Regulatory Authority
          </h3>
          <p className="text-sm text-text-secondary">
            Deterministic code handles all money movement and message dispatch. It strictly abides by the configured guardrails below.
          </p>
        </div>
      </div>

      {/* Policy Limit Cards */}
      <div>
        <h3 className="font-sans font-semibold text-lg text-text-primary mb-4 flex items-center gap-2">
          <ShieldAlert size={20} className="text-text-secondary" /> Active Guardrails
        </h3>
        
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {policies.map(policy => (
            <div key={policy.id} className="bg-surface border border-border-default rounded-xl p-5 shadow-sm hover:border-accent-blue/30 transition-colors">
              <div className="flex items-start justify-between mb-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-mono text-xs font-semibold text-text-secondary bg-page px-2 py-0.5 rounded">
                      {policy.id}
                    </span>
                    <h4 className="font-medium text-text-primary">{policy.name}</h4>
                  </div>
                  <p className="text-sm text-text-secondary mt-1">{policy.description}</p>
                </div>
                
                <label className="relative inline-flex items-center cursor-pointer ml-4">
                  <input 
                    type="checkbox" 
                    className="sr-only peer" 
                    checked={policy.active}
                    onChange={() => toggleStatus(policy)}
                  />
                  <div className="w-9 h-5 bg-border-default peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border-default after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-semantic-green"></div>
                </label>
              </div>
              
              <div className="bg-page rounded-lg p-3 flex items-center justify-between mt-4">
                <div className="text-sm font-medium text-text-secondary">Enforcement Limit</div>
                
                <div className="flex items-center gap-2">
                  {editingId === policy.id ? (
                    <div className="flex items-center gap-2">
                      <input 
                        type="text"
                        value={editValue}
                        onChange={e => setEditValue(e.target.value)}
                        className="w-24 px-2 py-1 text-sm font-mono border border-accent-blue rounded outline-none"
                      />
                      <span className="text-sm font-semibold text-text-secondary">{policy.unit}</span>
                      <button 
                        onClick={() => handleSave(policy)}
                        disabled={saving}
                        className="p-1 text-semantic-green hover:bg-semantic-green/10 rounded"
                      >
                        <Save size={16} />
                      </button>
                      <button 
                        onClick={() => setEditingId(null)}
                        className="p-1 text-semantic-red hover:bg-semantic-red/10 rounded"
                      >
                        <X size={16} />
                      </button>
                    </div>
                  ) : (
                    <div className="flex items-center gap-3">
                      <div className="font-mono text-text-primary font-semibold text-sm">
                        {policy.value} <span className="text-text-secondary ml-1">{policy.unit}</span>
                      </div>
                      <button 
                        onClick={() => handleEdit(policy)}
                        className="p-1 text-text-secondary hover:text-accent-blue hover:bg-accent-blue/10 rounded transition-colors"
                      >
                        <Edit2 size={14} />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      
    </div>
  );
}
