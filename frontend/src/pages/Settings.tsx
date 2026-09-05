import { useState, useEffect } from 'react';
import { Settings as SettingsIcon, Accessibility, Users, Bell, ShieldCheck, Code, FileText, Palette } from 'lucide-react';
import clsx from 'clsx';

export function Settings() {
  const [activeTab, setActiveTab] = useState('Appearance');
  const [reducedMotion, setReducedMotion] = useState(() => {
    return localStorage.getItem('reducedMotion') === 'true';
  });

  useEffect(() => {
    if (reducedMotion) {
      document.documentElement.setAttribute('data-reduced-motion', 'true');
      localStorage.setItem('reducedMotion', 'true');
    } else {
      document.documentElement.removeAttribute('data-reduced-motion');
      localStorage.setItem('reducedMotion', 'false');
    }
  }, [reducedMotion]);

  const tabs = [
    { id: 'Merchant', icon: Users },
    { id: 'Notifications', icon: Bell },
    { id: 'Security', icon: ShieldCheck },
    { id: 'AI Configuration', icon: Code },
    { id: 'Audit', icon: FileText },
    { id: 'Appearance', icon: Palette },
  ];

  return (
    <div className="max-w-4xl space-y-8">
      
      {/* Tab Bar */}
      <div className="flex items-center gap-8 border-b border-border-default px-2 pb-px overflow-x-auto">
        {tabs.map(tab => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={clsx(
                "flex items-center gap-2 pb-3 px-1 text-sm font-medium transition-colors border-b-2 whitespace-nowrap",
                isActive 
                  ? "border-accent-blue text-accent-blue" 
                  : "border-transparent text-text-secondary hover:text-text-primary"
              )}
            >
              <Icon size={16} />
              {tab.id}
            </button>
          )
        })}
      </div>

      {activeTab === 'Appearance' ? (
        <section className="bg-surface border border-border-default rounded-xl overflow-hidden">
          <div className="px-6 py-5 border-b border-border-default">
            <h2 className="text-lg font-display font-semibold text-text-primary">Appearance & Accessibility</h2>
            <p className="text-sm text-text-secondary mt-1">Manage how the dashboard looks and behaves.</p>
          </div>
          <div className="p-6 space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Accessibility size={18} className="text-accent-blue" />
                  <span className="font-medium text-text-primary">Reduced Motion</span>
                </div>
                <p className="text-sm text-text-secondary">Disable animations, transitions, and particle effects.</p>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input 
                  type="checkbox" 
                  className="sr-only peer" 
                  checked={reducedMotion}
                  onChange={(e) => setReducedMotion(e.target.checked)}
                />
                <div className="w-11 h-6 bg-border-default peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-border-default after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-accent-blue"></div>
              </label>
            </div>
          </div>
        </section>
      ) : (
        <section className="bg-surface border border-border-default rounded-xl overflow-hidden">
          <div className="px-6 py-5 border-b border-border-default">
            <h2 className="text-lg font-display font-semibold text-text-primary">{activeTab} Settings</h2>
            <p className="text-sm text-text-secondary mt-1">Configure your workspace preferences.</p>
          </div>
          <div className="p-6 flex flex-col items-center justify-center py-16 text-center">
            <SettingsIcon size={48} className="text-text-secondary mb-4 opacity-50" />
            <h3 className="text-lg font-medium text-text-primary mb-2">Settings Configuration</h3>
            <p className="text-sm text-text-secondary max-w-md">
              Integration configuration, API keys, and team management will be available here.
            </p>
          </div>
        </section>
      )}
    </div>
  );
}
