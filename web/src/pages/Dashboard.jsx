import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';
import { LayoutDashboard, TrendingUp, ShieldCheck, Cpu, Key, ArrowUpRight } from 'lucide-react';

export default function Dashboard() {
  const { currentUser } = useAuth();

  return (
    <main className="py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        {/* Welcome Header */}
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-black/10 dark:border-white/10 pb-8">
          <div>
            <div className="flex items-center gap-2 text-brand-accent text-xs font-mono font-semibold mb-1">
              <span className="w-2 h-2 rounded-full bg-brand-accent animate-pulse"></span>
              <span>QUANT SAAS PORTAL</span>
            </div>
            <h1 className="text-3xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
              Welcome back, <span className="text-brand-accent">{currentUser?.email?.split('@')[0]}</span>
            </h1>
            <p className="text-xs text-brand-textLight dark:text-brand-textDark mt-1">
              Logged in as <span className="font-mono text-brand-headingLight dark:text-brand-headingDark">{currentUser?.email}</span>
            </p>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/settings"
              className="px-4 py-2.5 rounded-xl font-bold text-xs bg-brand-cardLight dark:bg-brand-cardDark border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark hover:border-brand-accent/50 transition-all flex items-center gap-2"
            >
              <Key className="w-4 h-4 text-brand-accent" />
              <span>BYOK Vault</span>
            </Link>
            <Link
              to="/engine"
              className="px-4 py-2.5 rounded-xl font-bold text-xs bg-brand-accent text-black hover:shadow-[0_0_15px_rgba(0,229,255,0.4)] transition-all flex items-center gap-2"
            >
              <Cpu className="w-4 h-4" />
              <span>Live Engine</span>
            </Link>
          </div>
        </div>

        {/* Portfolio Overview Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm">
            <div className="flex justify-between items-center text-xs font-mono text-brand-textLight dark:text-brand-textDark mb-2">
              <span>Paper Equity</span>
              <TrendingUp className="w-4 h-4 text-emerald-400" />
            </div>
            <div className="text-2xl font-extrabold text-brand-headingLight dark:text-brand-headingDark">
              ₹1,000,000.00
            </div>
            <div className="text-xs text-emerald-400 font-semibold mt-2 flex items-center gap-1">
              <span>+₹12,450.00 (+1.25%)</span>
            </div>
          </div>

          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm">
            <div className="flex justify-between items-center text-xs font-mono text-brand-textLight dark:text-brand-textDark mb-2">
              <span>Verified Win Rate</span>
              <ShieldCheck className="w-4 h-4 text-brand-accent" />
            </div>
            <div className="text-2xl font-extrabold text-brand-headingLight dark:text-brand-headingDark">
              68.4%
            </div>
            <div className="text-xs text-brand-textLight dark:text-brand-textDark mt-2">
              Across 42 Backtested Candles
            </div>
          </div>

          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm">
            <div className="flex justify-between items-center text-xs font-mono text-brand-textLight dark:text-brand-textDark mb-2">
              <span>Risk Sizing Cap</span>
              <span className="text-brand-accent font-bold">1.0%</span>
            </div>
            <div className="text-2xl font-extrabold text-brand-headingLight dark:text-brand-headingDark">
              ₹10,000.00
            </div>
            <div className="text-xs text-brand-textLight dark:text-brand-textDark mt-2">
              Max Risk Per Position
            </div>
          </div>

          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm">
            <div className="flex justify-between items-center text-xs font-mono text-brand-textLight dark:text-brand-textDark mb-2">
              <span>Broker Sync</span>
              <span className="px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 text-[10px] font-bold">ACTIVE</span>
            </div>
            <div className="text-2xl font-extrabold text-brand-headingLight dark:text-brand-headingDark">
              Upstox API v2
            </div>
            <div className="text-xs text-brand-textLight dark:text-brand-textDark mt-2">
              Paper Mode (BYOK Available)
            </div>
          </div>
        </div>

        {/* Simulated Active Positions Table */}
        <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
              <LayoutDashboard className="w-5 h-5 text-brand-accent" />
              <span>Active Quantitative Signals</span>
            </h3>
            <span className="text-xs font-mono text-brand-accent">3 OPEN POSITIONS</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs font-mono">
              <thead>
                <tr className="border-b border-black/10 dark:border-white/10 text-brand-textLight dark:text-brand-textDark uppercase">
                  <th className="pb-3">Symbol</th>
                  <th className="pb-3">Action</th>
                  <th className="pb-3">Entry Price</th>
                  <th className="pb-3">Target Price</th>
                  <th className="pb-3">Stop Loss</th>
                  <th className="pb-3">Conviction</th>
                  <th className="pb-3 text-right">PnL</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5 dark:divide-white/5">
                <tr>
                  <td className="py-4 font-bold text-brand-headingLight dark:text-brand-headingDark">RELIANCE</td>
                  <td className="py-4 text-emerald-400 font-bold">BUY</td>
                  <td className="py-4">₹2,920.00</td>
                  <td className="py-4">₹2,990.00</td>
                  <td className="py-4 text-brand-accent">₹2,930.00 (Locked)</td>
                  <td className="py-4 text-purple-400 font-bold">85% (Groq+CRO)</td>
                  <td className="py-4 text-right text-emerald-400 font-bold">+₹2,450.00</td>
                </tr>
                <tr>
                  <td className="py-4 font-bold text-brand-headingLight dark:text-brand-headingDark">HDFCBANK</td>
                  <td className="py-4 text-emerald-400 font-bold">BUY</td>
                  <td className="py-4">₹1,640.00</td>
                  <td className="py-4">₹1,695.00</td>
                  <td className="py-4">₹1,620.00</td>
                  <td className="py-4 text-purple-400 font-bold">78% (Groq+CRO)</td>
                  <td className="py-4 text-right text-emerald-400 font-bold">+₹1,800.00</td>
                </tr>
                <tr>
                  <td className="py-4 font-bold text-brand-headingLight dark:text-brand-headingDark">INFY</td>
                  <td className="py-4 text-emerald-400 font-bold">BUY</td>
                  <td className="py-4">₹1,810.00</td>
                  <td className="py-4">₹1,870.00</td>
                  <td className="py-4 text-brand-accent">₹1,810.00 (Locked)</td>
                  <td className="py-4 text-purple-400 font-bold">82% (Vision)</td>
                  <td className="py-4 text-right text-emerald-400 font-bold">+₹950.00</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  );
}
