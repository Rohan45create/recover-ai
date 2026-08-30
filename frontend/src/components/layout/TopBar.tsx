import { Search, Bell, Menu } from 'lucide-react';

interface TopBarProps {
  title: string;
  subtitle: string;
  primaryActionLabel: string;
  onPrimaryAction?: () => void;
  onMenuToggle?: () => void;
}

export function TopBar({ title, subtitle, primaryActionLabel, onPrimaryAction, onMenuToggle }: TopBarProps) {
  return (
    <div className="flex flex-col bg-surface border-b border-border-default sticky top-0 z-10">
      {/* Utility Bar */}
      <div className="h-14 px-4 md:px-8 flex items-center justify-between border-b border-border-default">
        {/* Left Side: Mobile Menu + Search */}
        <div className="flex items-center gap-3 w-full md:w-auto">
          <button 
            className="md:hidden text-text-secondary hover:text-text-primary p-1 -ml-1"
            onClick={onMenuToggle}
            aria-label="Open Navigation Menu"
          >
            <Menu size={20} aria-hidden="true" />
          </button>
          <div className="flex items-center flex-1 md:w-96 bg-page border border-border-default rounded-md px-3 py-1.5 focus-within:ring-2 focus-within:ring-accent-blue/20 focus-within:border-accent-blue transition-all">
            <Search size={16} className="text-text-secondary mr-2" aria-hidden="true" />
            <input 
              type="text" 
              placeholder="Search..." 
              aria-label="Search Ledgers"
              className="bg-transparent border-none outline-none w-full text-sm text-text-primary placeholder:text-text-secondary"
            />
          </div>
        </div>
        
        {/* Right Actions */}
        <div className="flex items-center gap-2 md:gap-4 ml-4">
          <div className="hidden md:flex px-3 py-1 bg-page border border-border-default rounded-full text-[11px] font-mono font-medium tracking-wide text-text-secondary items-center">
            LAST 30 DAYS
          </div>
          
          <button 
            className="text-text-secondary transition-colors cursor-not-allowed"
            aria-label="Notifications"
            disabled
          >
            <Bell size={20} aria-hidden="true" />
          </button>
          
          <div className="w-8 h-8 rounded-full bg-accent-blue text-white flex items-center justify-center font-medium text-sm cursor-not-allowed">
            JD
          </div>
        </div>
      </div>
      
      {/* Page Header */}
      <div className="px-8 py-6 flex items-start justify-between">
        <div>
          <h1 className="font-display font-semibold text-[30px] text-text-primary leading-tight mb-1">
            {title}
          </h1>
          <p className="text-sm text-text-secondary">
            {subtitle}
          </p>
        </div>
        
        <button 
          onClick={onPrimaryAction}
          className="bg-accent-blue hover:bg-accent-blue/90 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors shadow-sm whitespace-nowrap flex-shrink-0"
        >
          {primaryActionLabel}
        </button>
      </div>
    </div>
  );
}
