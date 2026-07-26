import React from 'react';
import TradingViewChart from '../components/TradingViewChart';

export default function Engine() {
  return (
    <main className="py-20">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
        <div className="space-y-4 text-center">
          <span className="px-3.5 py-1.5 rounded-full border border-brand-accent/30 bg-brand-accent/10 text-brand-accent text-xs font-mono font-semibold">
            ALGORITHMIC ARCHITECTURE
          </span>
          <h1 className="text-4xl sm:text-5xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
            The Dual-Brain AI & Murphy Confluence Pipeline
          </h1>
          <p className="text-base text-brand-textLight dark:text-brand-textDark max-w-2xl mx-auto">
            An in-depth breakdown of how candidate stocks flow through vector screening, Groq Llama momentum scoring, DeepSeek CRO risk vetting, and Multimodal Vision verification.
          </p>
        </div>

        {/* Pipeline Steps */}
        <div className="space-y-8">
          {/* Step 1 */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 space-y-4 shadow-sm">
            <div className="flex items-center space-x-3">
              <span className="w-8 h-8 rounded-lg bg-brand-accent text-black font-mono font-bold flex items-center justify-center">01</span>
              <h3 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Upstox API v2 Ingestion & Technical Confluence</h3>
            </div>
            <p className="text-sm leading-relaxed">
              The engine ingests 15-minute historical candles and live market quotes across 20 target NSE bluechips. It computes Volume Weighted Average Price (VWAP), 20 & 50 period Simple Moving Averages, RSI (14), and Fibonacci 0.500 retracements.
            </p>
          </div>

          {/* Step 2 */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 space-y-4 shadow-sm">
            <div className="flex items-center space-x-3">
              <span className="w-8 h-8 rounded-lg bg-brand-accent text-black font-mono font-bold flex items-center justify-center">02</span>
              <h3 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Stage 1: Groq Llama 3.3 Momentum Scoring</h3>
            </div>
            <p className="text-sm leading-relaxed">
              Screened candidates meeting liquidity thresholds (&gt;500,000 shares) are analyzed against the core rules of Technical Confluence. Groq outputs a structured JSON trade ticket containing proposed Entry, Target, Stop-Loss, and a Conviction Score (0-100).
            </p>
          </div>

          {/* Step 3 */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 space-y-4 shadow-sm">
            <div className="flex items-center space-x-3">
              <span className="w-8 h-8 rounded-lg bg-brand-accent text-black font-mono font-bold flex items-center justify-center">03</span>
              <h3 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Stage 2: DeepSeek R1 CRO Risk Veto</h3>
            </div>
            <p className="text-sm leading-relaxed">
              Every BUY signal with conviction &gt;= 70 is sent to DeepSeek R1 acting as Chief Risk Officer. DeepSeek evaluates risk-to-reward metrics (minimum 1:2 R:R requirement) and market context, approving or vetoing the trade ticket.
            </p>
          </div>

          {/* Step 4 */}
          <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 space-y-4 shadow-sm">
            <div className="flex items-center space-x-3">
              <span className="w-8 h-8 rounded-lg bg-brand-accent text-black font-mono font-bold flex items-center justify-center">04</span>
              <h3 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Stage 3: Multimodal Vision Pattern Recognition</h3>
            </div>
            <p className="text-sm leading-relaxed">
              The engine plots an in-memory candlestick chart and converts it to a base64 image payload. OpenRouter Vision inspects the chart structure to confirm bullish candlestick patterns with at least 75% confidence before final order routing.
            </p>
          </div>
        </div>

        {/* Live Quantitative Breakout Visualization */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Simulated Confluence Breakout Execution</h2>
            <span className="text-xs font-mono text-brand-accent">REAL-TIME CANDLESTICK STREAM</span>
          </div>
          <TradingViewChart />
        </div>
      </div>
    </main>
  );
}
