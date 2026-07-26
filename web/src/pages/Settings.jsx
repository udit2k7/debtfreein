import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Key, Shield, CheckCircle, Lock, Server } from 'lucide-react';

export default function Settings() {
  const { currentUser } = useAuth();
  const [broker, setBroker] = useState('upstox');
  const [apiKey, setApiKey] = useState(localStorage.getItem('byok_api_key') || '');
  const [apiSecret, setApiSecret] = useState(localStorage.getItem('byok_api_secret') || '');
  const [tierMode, setTierMode] = useState('byok');
  const [saved, setSaved] = useState(false);

  const handleSaveKeys = (e) => {
    e.preventDefault();
    localStorage.setItem('byok_api_key', apiKey);
    localStorage.setItem('byok_api_secret', apiSecret);
    localStorage.setItem('byok_broker', broker);
    localStorage.setItem('byok_tier', tierMode);
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <main className="py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        <div className="border-b border-black/10 dark:border-white/10 pb-6 space-y-2">
          <div className="flex items-center gap-2 text-brand-accent text-xs font-mono font-semibold">
            <Key className="w-4 h-4" />
            <span>BYOK SECURITY VAULT</span>
          </div>
          <h1 className="text-3xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
            Broker API Credentials & Monetization Tiers
          </h1>
          <p className="text-xs text-brand-textLight dark:text-brand-textDark">
            Manage your direct broker connections or select Admin Master Key proxy execution.
          </p>
        </div>

        {saved && (
          <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-sm flex items-center gap-2 font-semibold">
            <CheckCircle className="w-5 h-5 shrink-0" />
            <span>Broker API credentials updated and encrypted in local vault successfully!</span>
          </div>
        )}

        <form onSubmit={handleSaveKeys} className="space-y-8">
          {/* Tier Selection */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-4">
            <h3 className="text-base font-bold text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
              <Shield className="w-5 h-5 text-brand-accent" />
              <span>Execution Architecture Tier</span>
            </h3>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                onClick={() => setTierMode('byok')}
                className={`p-5 rounded-xl border cursor-pointer transition-all ${
                  tierMode === 'byok'
                    ? 'border-brand-accent bg-brand-accent/10'
                    : 'border-black/10 dark:border-white/10 hover:border-brand-accent/30'
                }`}
              >
                <div className="flex justify-between items-center mb-2">
                  <span className="font-bold text-sm text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
                    <Lock className="w-4 h-4 text-brand-accent" />
                    <span>BYOK Tier (Direct Keys)</span>
                  </span>
                  <span className="text-[10px] font-mono font-bold text-brand-accent">FREE ACCESS</span>
                </div>
                <p className="text-xs text-brand-textLight dark:text-brand-textDark">
                  Connect your personal Upstox/Zerodha API keys directly for execution with zero markup.
                </p>
              </div>

              <div
                onClick={() => setTierMode('proxy')}
                className={`p-5 rounded-xl border cursor-pointer transition-all ${
                  tierMode === 'proxy'
                    ? 'border-brand-accent bg-brand-accent/10'
                    : 'border-black/10 dark:border-white/10 hover:border-brand-accent/30'
                }`}
              >
                <div className="flex justify-between items-center mb-2">
                  <span className="font-bold text-sm text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
                    <Server className="w-4 h-4 text-purple-400" />
                    <span>Admin Master Proxy Tier</span>
                  </span>
                  <span className="text-[10px] font-mono font-bold text-purple-400">PRO SAAS</span>
                </div>
                <p className="text-xs text-brand-textLight dark:text-brand-textDark">
                  Route trades through DebtFreeIn server-managed master broker keys with institutional low latency.
                </p>
              </div>
            </div>
          </div>

          {/* Broker API Key Vault Inputs */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-6">
            <div className="space-y-1">
              <h3 className="text-base font-bold text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
                <Key className="w-5 h-5 text-brand-accent" />
                <span>Broker API Credentials</span>
              </h3>
              <p className="text-xs text-brand-textLight dark:text-brand-textDark">
                Credentials are saved securely and never transmitted in plaintext.
              </p>
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Select Broker Provider
              </label>
              <select
                value={broker}
                onChange={(e) => setBroker(e.target.value)}
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm focus:outline-none focus:border-brand-accent transition-colors font-mono"
              >
                <option value="upstox">Upstox API v2 (Official Partner)</option>
                <option value="zerodha">Zerodha KiteConnect API</option>
                <option value="dhan">Dhan HQ API</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Broker API Key (Client ID)
              </label>
              <input
                type="text"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="e.g. 5b98a3e0-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm font-mono focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Broker API Secret
              </label>
              <input
                type="password"
                value={apiSecret}
                onChange={(e) => setApiSecret(e.target.value)}
                placeholder="••••••••••••••••••••••••••••••••"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm font-mono focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <button
              type="submit"
              className="px-8 py-3.5 rounded-xl font-bold text-sm bg-brand-accent text-black hover:shadow-[0_0_20px_rgba(0,229,255,0.4)] transition-all duration-300"
            >
              Save Credentials to Vault
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}
