import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="border-t border-black/5 dark:border-white/10 bg-black/5 dark:bg-black/40 py-16 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 md:grid-cols-4 gap-10">
        {/* Col 1: Brand */}
        <div className="space-y-4">
          <div className="flex items-center gap-3">
            <img src="/ic_phone_logo.png" alt="DebtFreeIn Logo" className="w-8 h-8 object-contain" />
            <span className="font-extrabold text-lg text-brand-headingLight dark:text-brand-headingDark">DebtFreeIn</span>
          </div>
          <p className="text-xs text-brand-textLight dark:text-brand-textDark leading-relaxed">
            Autonomous algorithmic trading, cognitive paper execution, and corporate quantitative finance platform.
          </p>
        </div>

        {/* Col 2: Product */}
        <div>
          <h5 className="text-xs font-mono font-bold text-brand-headingLight dark:text-brand-headingDark uppercase tracking-wider mb-4">Product</h5>
          <ul className="space-y-2.5 text-sm">
            <li><Link to="/engine" className="hover:text-brand-accent transition-colors">Quant Engine</Link></li>
            <li><Link to="/#performance" className="hover:text-brand-accent transition-colors">Risk Controls</Link></li>
            <li><Link to="/#access" className="hover:text-brand-accent transition-colors">Beta Access</Link></li>
          </ul>
        </div>

        {/* Col 3: Legal */}
        <div>
          <h5 className="text-xs font-mono font-bold text-brand-headingLight dark:text-brand-headingDark uppercase tracking-wider mb-4">Legal</h5>
          <ul className="space-y-2.5 text-sm">
            <li><Link to="/privacy" className="hover:text-brand-accent transition-colors">Privacy Policy</Link></li>
            <li><Link to="/terms" className="hover:text-brand-accent transition-colors">Terms of Use</Link></li>
          </ul>
        </div>

        {/* Col 4: Resources */}
        <div>
          <h5 className="text-xs font-mono font-bold text-brand-headingLight dark:text-brand-headingDark uppercase tracking-wider mb-4">Resources</h5>
          <ul className="space-y-2.5 text-sm">
            <li><a href="https://api.upstox.com" target="_blank" rel="noopener noreferrer" className="hover:text-brand-accent transition-colors">Upstox API v2</a></li>
            <li><a href="https://modal.com" target="_blank" rel="noopener noreferrer" className="hover:text-brand-accent transition-colors">Modal Cloud</a></li>
          </ul>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12 pt-8 border-t border-black/5 dark:border-white/5 text-center text-xs text-brand-textLight dark:text-brand-textDark">
        © 2026 DebtFreeIn Platform. All rights reserved. SEBI / Stock Broker Disclaimer: Quantitative paper trading & automated signals are for educational purposes.
      </div>
    </footer>
  );
}
