import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Sun, Moon, Menu, X } from 'lucide-react';

export default function Header() {
  const [isDark, setIsDark] = useState(true);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const hasDarkClass = document.documentElement.classList.contains('dark');
    setIsDark(hasDarkClass);
  }, []);

  const toggleTheme = () => {
    if (document.documentElement.classList.contains('dark')) {
      document.documentElement.classList.remove('dark');
      localStorage.setItem('theme', 'light');
      setIsDark(false);
    } else {
      document.documentElement.classList.add('dark');
      localStorage.setItem('theme', 'dark');
      setIsDark(true);
    }
  };

  return (
    <header className="sticky top-0 z-50 backdrop-blur-md bg-white/80 dark:bg-[#0B0F19]/80 border-b border-black/5 dark:border-white/10 transition-colors duration-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-10 h-10 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center p-1.5 transition-transform duration-300 group-hover:scale-105">
            <img src="/ic_phone_logo.png" alt="DebtFreeIn Logo" className="w-full h-full object-contain" />
          </div>
          <div className="flex flex-col">
            <span className="font-extrabold text-xl text-brand-headingLight dark:text-brand-headingDark tracking-tight">DebtFreeIn</span>
            <span className="text-[10px] font-mono font-semibold tracking-widest text-brand-accent uppercase">Quant Engine</span>
          </div>
        </Link>

        {/* Center Nav Links */}
        <nav className="hidden md:flex items-center space-x-8 text-sm font-medium">
          <Link
            to="/"
            className={`transition-colors duration-200 ${
              location.pathname === '/'
                ? 'text-brand-headingLight dark:text-brand-headingDark font-semibold'
                : 'hover:text-brand-accent'
            }`}
          >
            Home
          </Link>
          <Link
            to="/engine"
            className={`transition-colors duration-200 ${
              location.pathname === '/engine'
                ? 'text-brand-headingLight dark:text-brand-headingDark font-semibold'
                : 'hover:text-brand-accent'
            }`}
          >
            Engine
          </Link>
          <Link
            to="/privacy"
            className={`transition-colors duration-200 ${
              location.pathname === '/privacy'
                ? 'text-brand-headingLight dark:text-brand-headingDark font-semibold'
                : 'hover:text-brand-accent'
            }`}
          >
            Privacy
          </Link>
          <Link
            to="/terms"
            className={`transition-colors duration-200 ${
              location.pathname === '/terms'
                ? 'text-brand-headingLight dark:text-brand-headingDark font-semibold'
                : 'hover:text-brand-accent'
            }`}
          >
            Terms
          </Link>
        </nav>

        {/* Actions: Theme Toggle + CTA */}
        <div className="flex items-center space-x-4">
          <button
            onClick={toggleTheme}
            className="p-2.5 rounded-xl border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark hover:bg-black/5 dark:hover:bg-white/5 transition-all duration-300"
            aria-label="Toggle Theme"
          >
            {isDark ? (
              <Sun className="w-5 h-5 text-brand-accent" />
            ) : (
              <Moon className="w-5 h-5 text-brand-headingLight" />
            )}
          </button>

          <Link
            to="/#access"
            className="hidden sm:inline-block px-5 py-2.5 rounded-xl font-bold text-sm bg-brand-accent text-black hover:shadow-[0_0_20px_rgba(0,229,255,0.4)] transition-all duration-300 hover:-translate-y-0.5"
          >
            Get Access
          </Link>

          {/* Mobile Menu Toggle */}
          <button
            onClick={() => setMobileOpen(!mobileOpen)}
            className="md:hidden p-2.5 rounded-xl border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark"
            aria-label="Toggle Mobile Navigation"
          >
            {mobileOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer */}
      {mobileOpen && (
        <div className="md:hidden px-4 pt-2 pb-6 border-b border-black/10 dark:border-white/10 bg-brand-light dark:bg-brand-dark flex flex-col space-y-4 text-sm font-medium">
          <Link to="/" onClick={() => setMobileOpen(false)} className="hover:text-brand-accent">Home</Link>
          <Link to="/engine" onClick={() => setMobileOpen(false)} className="hover:text-brand-accent">Engine</Link>
          <Link to="/privacy" onClick={() => setMobileOpen(false)} className="hover:text-brand-accent">Privacy</Link>
          <Link to="/terms" onClick={() => setMobileOpen(false)} className="hover:text-brand-accent">Terms</Link>
        </div>
      )}
    </header>
  );
}
