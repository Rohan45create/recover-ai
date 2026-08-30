import { NavLink } from 'react-router-dom';
import { LayoutDashboard, BarChart3, List, Shield, Settings, Play, BookOpen, LifeBuoy } from 'lucide-react';
import clsx from 'clsx';

const navItems = [
  { name: 'Overview', to: '/app', icon: LayoutDashboard },
  { name: 'Analytics', to: '/app/analytics', icon: BarChart3 },
  { name: 'Recovery Log', to: '/app/log', icon: List },
  { name: 'Policies', to: '/app/policies', icon: Shield },
  { name: 'Settings', to: '/app/settings', icon: Settings },
];

import { useState } from 'react';
import { api } from '../../api';

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

export function Sidebar({ isOpen = false }: SidebarProps) {
  const [isInjecting, setIsInjecting] = useState(false);

  const injectSyntheticCases = async (count: number) => {
    try {
      setIsInjecting(true);
      await api.post(`/demo/inject?count=${count}`);
      alert(`Successfully injected ${count} cases into the real pipeline!`);
    } catch (error) {
      console.error('Failed to inject cases', error);
      alert('Failed to inject cases.');
    } finally {
      setIsInjecting(false);
    }
  };

  return (
    <aside className={clsx(
      "w-[240px] flex-shrink-0 bg-sidebar border-r border-border-default flex flex-col h-screen fixed md:sticky top-0 z-50 transition-transform duration-300",
      isOpen ? "translate-x-0" : "-translate-x-full md:translate-x-0"
    )}>
      {/* Brand */}
      <div className="p-6 pb-8">
        <div className="flex items-center gap-2 mb-1">
          <div className="w-6 h-6 rounded bg-accent-blue flex items-center justify-center text-white font-bold text-xs">R</div>
          <span className="font-display font-semibold text-lg text-text-primary tracking-tight">RecoverAI</span>
        </div>
        <div className="text-[10px] font-sans font-medium text-text-secondary tracking-[0.08em] uppercase">
          Institutional Recovery
        </div>
      </div>

      {/* Main Nav */}
      <nav aria-label="Main Navigation" className="flex-1 px-3 space-y-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.name}
              to={item.to}
              className={({ isActive }) => clsx(
                "flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors duration-200",
                isActive 
                  ? "bg-accent-blue text-white font-medium" 
                  : "text-text-secondary hover:text-text-primary hover:bg-page"
              )}
            >
              <Icon size={18} className="flex-shrink-0" aria-hidden="true" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
        
        {/* Simulation Engine Promo */}
        <div className="mt-8 mx-2 p-4 rounded-xl bg-page border border-border-default shadow-sm" role="region" aria-label="Simulation Engine">
          <div className="flex items-center gap-2 text-accent-blue mb-2">
            <Play size={16} fill="currentColor" aria-hidden="true" />
            <span className="text-xs font-semibold uppercase tracking-wider">Simulation</span>
          </div>
          <p className="text-xs text-text-secondary mb-3 leading-relaxed">
            Run batch evaluations to test policy impact before production.
          </p>
          <div className="flex gap-2">
            <button 
              disabled={isInjecting}
              onClick={() => injectSyntheticCases(50)}
              aria-label="Inject 50 synthetic cases"
              className="flex-1 py-1.5 px-2 bg-white border border-border-default rounded text-xs font-medium text-text-primary hover:bg-page transition-colors disabled:opacity-50"
            >
              {isInjecting ? '...' : '50'}
            </button>
            <button 
              disabled={isInjecting}
              onClick={() => injectSyntheticCases(100)}
              aria-label="Inject 100 synthetic cases"
              className="flex-1 py-1.5 px-2 bg-white border border-border-default rounded text-xs font-medium text-text-primary hover:bg-page transition-colors disabled:opacity-50"
            >
              {isInjecting ? '...' : '100'}
            </button>
            <button 
              disabled={isInjecting}
              onClick={() => injectSyntheticCases(200)}
              aria-label="Inject 200 synthetic cases"
              className="flex-1 py-1.5 px-2 bg-white border border-border-default rounded text-xs font-medium text-text-primary hover:bg-page transition-colors disabled:opacity-50"
            >
              {isInjecting ? '...' : '200'}
            </button>
          </div>
        </div>
      </nav>

      {/* Footer Nav */}
      <nav aria-label="Footer Navigation" className="p-4 border-t border-border-default space-y-1">
        <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-md text-sm text-text-secondary hover:text-text-primary hover:bg-page transition-colors">
          <BookOpen size={18} aria-hidden="true" />
          <span>Documentation</span>
        </a>
        <a href="#" className="flex items-center gap-3 px-3 py-2 rounded-md text-sm text-text-secondary hover:text-text-primary hover:bg-page transition-colors">
          <LifeBuoy size={18} aria-hidden="true" />
          <span>Support</span>
        </a>
      </nav>
    </aside>
  );
}
