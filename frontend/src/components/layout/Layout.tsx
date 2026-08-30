import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { TopBar } from './TopBar';

export function Layout() {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  
  // Dynamic header based on route
  const getHeaderProps = () => {
    const path = location.pathname.replace(/\/$/, '');
    switch (path) {
      case '/app':
        return {
          title: "Overview",
          subtitle: "Institutional recovery performance and active cases.",
          primaryActionLabel: "Export Report"
        };
      case '/app/analytics':
        return {
          title: "Analytics",
          subtitle: "Deep dive into recovery models and historical trends.",
          primaryActionLabel: "Export Data"
        };
      case '/app/log':
        return {
          title: "Recovery Log",
          subtitle: "Detailed timeline of all automated and escalated interventions.",
          primaryActionLabel: "+ New Recovery Case"
        };
      case '/app/policies':
        return {
          title: "Policies",
          subtitle: "Configure guardrails, rules, and AI parameters.",
          primaryActionLabel: "Update Global Policies"
        };
      case '/app/settings':
        return {
          title: "Settings",
          subtitle: "Manage integrations, team access, and appearance.",
          primaryActionLabel: "Save Changes"
        };
      default:
        return {
          title: "RecoverAI",
          subtitle: "",
          primaryActionLabel: "Action"
        };
    }
  };

  return (
    <div className="flex min-h-screen bg-page w-full overflow-hidden relative">
      <Sidebar isOpen={isMobileMenuOpen} onClose={() => setIsMobileMenuOpen(false)} />
      <main className="flex-1 flex flex-col min-w-0 h-screen overflow-hidden">
        <TopBar {...getHeaderProps()} onMenuToggle={() => setIsMobileMenuOpen(true)} />
        <div className="flex-1 p-4 md:p-8 overflow-y-auto">
          <Outlet />
        </div>
      </main>
      
      {/* Mobile Backdrop */}
      {isMobileMenuOpen && (
        <div 
          className="fixed inset-0 bg-black/50 z-40 md:hidden"
          onClick={() => setIsMobileMenuOpen(false)}
          aria-hidden="true"
        />
      )}
    </div>
  );
}
