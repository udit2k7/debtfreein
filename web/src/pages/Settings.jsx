import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Key, Shield, CheckCircle, Lock, Server, AlertCircle, RefreshCw } from 'lucide-react';

export default function Settings() {
  const { currentUser } = useAuth();
  const [broker, setBroker] = useState('upstox');
  const [accessToken, setAccessToken] = useState('');
  const [apiKey, setApiKey] = useState(localStorage.getItem('byok_api_key') || '');
  const [apiSecret, setApiSecret] = useState(localStorage.getItem('byok_api_secret') || '');
  const [tierMode, setTierMode] = useState('byok');
  
  const [loading, setLoading] = useState(false);
  const [fetchingVault, setFetchingVault] = useState(true);
  const [vaultStatus, setVaultStatus] = useState(null);
  const [statusMessage, setStatusMessage] = useState(null);

  // Fetch current vault status from API Gateway
  const fetchVaultStatus = async () => {
    setFetchingVault(true);
    try {
      const res = await api.get('/vault/upstox');
      setVaultStatus(res);
    } catch (err) {
      console.error('Error fetching vault status:', err);
    } finally {
      setFetchingVault(false);
    }
  };

  useEffect(() => {
    if (currentUser) {
      fetchVaultStatus();
    }
  }, [currentUser]);

  const handleSaveKeys = async (e) => {
    e.preventDefault();
    setStatusMessage(null);

    const tokenToSave = accessToken.trim() || apiKey.trim();
    if (!tokenToSave) {
      setStatusMessage({
        type: 'error',
        text: 'Please provide an Upstox Access Token or API Key.'
      });
      return;
    }

    setLoading(true);
    try {
      // Hit POST /vault/upstox via central API utility
      await api.post('/vault/upstox', {
        accessToken: tokenToSave,
        apiKey: apiKey.trim() || null,
        apiSecret: apiSecret.trim() || null,
        broker
      });

      // Save local preferences
      localStorage.setItem('byok_api_key', apiKey);
      localStorage.setItem('byok_broker', broker);
      localStorage.setItem('byok_tier', tierMode);

      setStatusMessage({
        type: 'success',
        text: 'Upstox API Access Token saved securely to cloud vault! Live proxy active.'
      });

      setAccessToken('');
      await fetchVaultStatus();
    } catch (error) {
      console.error('Error saving credentials to vault:', error);
      setStatusMessage({
        type: 'error',
        text: error.message || 'Failed to save credentials to vault.'
      });
    } finally {
      setLoading(false);
    }
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
            Broker API Credentials &amp; Vault Sync
          </h1>
          <p className="text-xs text-brand-textLight dark:text-brand-textDark">
            Manage your Upstox API v2 Access Tokens. Credentials are stored securely in your private cloud vault.
          </p>
        </div>

        {/* Feedback Banner */}
        {statusMessage && (
          <div
            className={`p-4 rounded-xl border text-sm flex items-center gap-2 font-semibold ${
              statusMessage.type === 'success'
                ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                : 'bg-rose-500/10 border-rose-500/30 text-rose-400'
            }`}
          >
            {statusMessage.type === 'success' ? (
              <CheckCircle className="w-5 h-5 shrink-0" />
            ) : (
              <AlertCircle className="w-5 h-5 shrink-0" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        {/* Current Vault Status Indicator */}
        <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-bold text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
              <Shield className="w-4 h-4 text-brand-accent" />
              <span>Cloud Vault Status</span>
            </h3>
            <button
              onClick={fetchVaultStatus}
              disabled={fetchingVault}
              className="text-xs text-brand-accent hover:underline flex items-center gap-1 font-mono"
            >
              <RefreshCw className={`w-3 h-3 ${fetchingVault ? 'animate-spin' : ''}`} />
              <span>Refresh Vault</span>
            </button>
          </div>

          {fetchingVault ? (
            <div className="text-xs font-mono text-brand-textLight dark:text-brand-textDark animate-pulse">
              Reading cloud vault status...
            </div>
          ) : vaultStatus?.hasToken ? (
            <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
                <span className="text-xs font-mono font-bold text-emerald-400">UPSTOX API TOKEN STORED &amp; ACTIVE</span>
              </div>
              <span className="text-xs font-mono text-brand-textLight dark:text-brand-textDark">
                Token Preview: <code className="text-brand-headingLight dark:text-brand-headingDark">{vaultStatus.maskedToken}</code>
              </span>
            </div>
          ) : (
            <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-mono">
              No live Upstox token in cloud vault. Paper-trading fallback mode active.
            </div>
          )}
        </div>

        <form onSubmit={handleSaveKeys} className="space-y-8">
          {/* Execution Architecture Tier */}
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
                  <span className="text-[10px] font-mono font-bold text-brand-accent">DIRECT API</span>
                </div>
                <p className="text-xs text-brand-textLight dark:text-brand-textDark">
                  Connect your personal Upstox API v2 access token directly for execution with zero markup.
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

          {/* Broker Credentials Form */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-6">
            <div className="space-y-1">
              <h3 className="text-base font-bold text-brand-headingLight dark:text-brand-headingDark flex items-center gap-2">
                <Key className="w-5 h-5 text-brand-accent" />
                <span>Upstox API v2 Credentials</span>
              </h3>
              <p className="text-xs text-brand-textLight dark:text-brand-textDark">
                Your access token is submitted securely over HTTPS to the backend vault.
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
                Upstox Access Token (Bearer Token)
              </label>
              <input
                type="password"
                value={accessToken}
                onChange={(e) => setAccessToken(e.target.value)}
                placeholder="Paste your Upstox OAuth Access Token (e.g. eyJhbGciOi...)"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm font-mono focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Upstox API Key / Client ID (Optional)
              </label>
              <input
                type="text"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="e.g. 5b98a3e0-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm font-mono focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="px-8 py-3.5 rounded-xl font-bold text-sm bg-brand-accent text-black hover:shadow-[0_0_20px_rgba(0,229,255,0.4)] transition-all duration-300 disabled:opacity-50 flex items-center gap-2"
            >
              <Key className="w-4 h-4" />
              <span>{loading ? 'Saving to Vault...' : 'Save Credentials to Vault'}</span>
            </button>
          </div>
        </form>
      </div>
    </main>
  );
}
