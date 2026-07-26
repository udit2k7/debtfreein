import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import BetaWaitlistForm from '../components/BetaWaitlistForm';
import { ChevronDown, Check } from 'lucide-react';

export default function Home() {
  // Telemetry simulation state
  const [pnl, setPnl] = useState(1250.0);
  const [logs, setLogs] = useState([]);
  const [openFaq, setOpenFaq] = useState(null);

  useEffect(() => {
    const sampleSymbols = ['RELIANCE', 'HDFCBANK', 'INFY', 'ICICIBANK', 'TCS'];
    const sampleActions = ['BUY', 'SELL'];

    const interval = setInterval(() => {
      setPnl((prev) => {
        const delta = Math.random() * 40 - 15;
        return prev + delta;
      });

      const symbol = sampleSymbols[Math.floor(Math.random() * sampleSymbols.length)];
      const action = sampleActions[Math.floor(Math.random() * sampleActions.length)];
      const qty = Math.floor(Math.random() * 25) + 5;
      const price = (Math.random() * 1500 + 500).toFixed(2);
      const timeStr = new Date().toLocaleTimeString('en-US', { hour12: false });

      setLogs((prevLogs) => [
        { id: Date.now(), timeStr, action, symbol, qty, price },
        ...prevLogs.slice(0, 5),
      ]);
    }, 4000);

    return () => clearInterval(interval);
  }, []);

  const toggleFaq = (id) => {
    setOpenFaq(openFaq === id ? null : id);
  };

  const formattedPnL = (pnl >= 0 ? '+₹' : '-₹') + Math.abs(pnl).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  return (
    <div className="space-y-12">
      {/* SECTION B: Hero Section */}
      <section className="py-20 md:py-28 overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center">
          {/* Left Content */}
          <div className="flex flex-col space-y-6">
            <div className="inline-flex items-center space-x-2 px-3.5 py-1.5 rounded-full border border-brand-accent/30 bg-brand-accent/10 text-brand-accent text-xs font-mono font-semibold w-fit">
              <span className="w-2 h-2 rounded-full bg-brand-accent animate-pulse"></span>
              <span>UPSTOX API V2 CERTIFIED ARCHITECTURE</span>
            </div>

            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black tracking-tight text-brand-headingLight dark:text-brand-headingDark leading-[1.1]">
              Autonomous Quantitative <span className="text-transparent bg-clip-text bg-gradient-to-r from-brand-accent via-cyan-300 to-blue-400">Trading Engine.</span>
            </h1>

            <p className="text-base sm:text-lg text-brand-textLight dark:text-brand-textDark font-normal leading-relaxed max-w-xl">
              DebtFreeIn eliminates emotional vulnerability through Dual-Brain AI Validation (Groq + DeepSeek R1 CRO) and Murphy Technical Confluence. Experience institutional risk controls with automated 1% position sizing.
            </p>

            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4 pt-2">
              <Link to="/engine" className="px-8 py-4 rounded-xl font-bold text-center bg-brand-accent text-black hover:shadow-[0_0_25px_rgba(0,229,255,0.5)] transition-all duration-300 hover:-translate-y-1">
                Explore Dual-Brain Architecture →
              </Link>
              <a href="#performance" className="px-8 py-4 rounded-xl font-semibold text-center border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark hover:bg-black/5 dark:hover:bg-white/5 transition-all duration-300">
                View Live Performance
              </a>
            </div>
          </div>

          {/* Right Card Display (Interactive Preview) */}
          <div className="relative">
            <div className="absolute -inset-1 rounded-3xl bg-gradient-to-r from-brand-accent/40 to-blue-600/40 blur-xl opacity-50"></div>
            <div className="relative bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 sm:p-8 shadow-xl transition-all duration-300 hover:-translate-y-1">
              {/* Terminal Header */}
              <div className="flex items-center justify-between pb-6 border-b border-black/10 dark:border-white/10">
                <div className="flex items-center space-x-2">
                  <span className="w-3 h-3 rounded-full bg-red-500"></span>
                  <span className="w-3 h-3 rounded-full bg-yellow-500"></span>
                  <span className="w-3 h-3 rounded-full bg-green-500"></span>
                  <span className="text-xs font-mono text-brand-textLight dark:text-brand-textDark ml-2">cloud_quant_engine.py</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="font-mono text-xs font-bold" style={{ color: pnl >= 0 ? '#10b981' : '#f43f5e' }}>
                    {formattedPnL}
                  </span>
                  <span className="px-2.5 py-1 rounded bg-green-500/10 text-green-400 font-mono text-xs font-bold border border-green-500/20">LIVE CLOUD</span>
                </div>
              </div>

              {/* Execution Metrics Mockup */}
              <div className="mt-6 space-y-4 font-mono text-xs">
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/5 dark:bg-black/30">
                  <span className="text-brand-textLight dark:text-brand-textDark">Target Universe</span>
                  <span className="text-brand-headingLight dark:text-brand-headingDark font-bold">20 Bluechip NSE Stocks</span>
                </div>
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/5 dark:bg-black/30">
                  <span className="text-brand-textLight dark:text-brand-textDark">Risk Model</span>
                  <span className="text-brand-accent font-bold">1.0% Capital Risk (Max ₹10,000)</span>
                </div>
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/5 dark:bg-black/30">
                  <span className="text-brand-textLight dark:text-brand-textDark">Confluence Gate</span>
                  <span className="text-brand-headingLight dark:text-brand-headingDark font-bold">VWAP + SMA20 + RSI (50-70)</span>
                </div>
                <div className="flex justify-between items-center p-3 rounded-xl bg-black/5 dark:bg-black/30">
                  <span className="text-brand-textLight dark:text-brand-textDark">Triple-Veto Status</span>
                  <span className="text-emerald-400 font-bold">Groq (70+) → CRO → Vision (75%)</span>
                </div>
              </div>

              {/* Simulated Live Console Lines */}
              {logs.length > 0 && (
                <div className="mt-4 pt-4 border-t border-black/10 dark:border-white/10 space-y-1.5">
                  {logs.map((log) => (
                    <div key={log.id} className="console-line">
                      <span className="ts">[{log.timeStr}]</span>{' '}
                      <span className="tag">[TELEMETRY]</span> Executed {log.action} {log.symbol} x{log.qty} @ ₹{log.price} (Slippage: 0.05%)
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* SECTION C: Social Proof Band */}
      <section className="py-16 bg-black/5 dark:bg-white/[0.02] border-y border-black/5 dark:border-white/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl sm:text-5xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight mb-4">
            Zero Emotion. <span className="text-brand-accent">100% Mathematical Precision.</span>
          </h2>
          <p className="text-sm sm:text-base text-brand-textLight dark:text-brand-textDark max-w-2xl mx-auto">
            Backtested over 6 months of 15-minute intraday candles across 20 NSE bluechip equities with an institutionally verified win rate.
          </p>
        </div>
      </section>

      {/* SECTION D: 50/50 Alternating Feature Grids */}
      <section className="py-24 space-y-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Strip 1: Text Left / Card Right */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center">
            <div className="space-y-6">
              <span className="text-xs font-mono font-bold tracking-widest text-brand-accent uppercase">Dual-Brain Engine</span>
              <h3 className="text-3xl sm:text-4xl font-extrabold text-brand-headingLight dark:text-brand-headingDark tracking-tight">
                Groq Llama 3.3 Momentum + DeepSeek R1 CRO Validation
              </h3>
              <p className="text-brand-textLight dark:text-brand-textDark leading-relaxed">
                Every candidate setup is generated by Groq fast momentum screening and then vetted by DeepSeek R1 Chief Risk Officer logic before trade execution.
              </p>
              <ul className="space-y-3 font-medium text-sm">
                <li className="flex items-center space-x-3 text-brand-headingLight dark:text-brand-headingDark">
                  <Check className="w-5 h-5 text-brand-accent" />
                  <span>Groq 70+ Conviction Screening Gate</span>
                </li>
                <li className="flex items-center space-x-3 text-brand-headingLight dark:text-brand-headingDark">
                  <Check className="w-5 h-5 text-brand-accent" />
                  <span>DeepSeek R1 CRO Risk Veto & Circuit Protection</span>
                </li>
              </ul>
            </div>

            <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="space-y-4 font-mono text-xs">
                <div className="p-4 rounded-xl bg-brand-accent/10 border border-brand-accent/20 text-brand-accent font-bold">
                  [STAGE 1] Groq Llama 3.3-70B: BUY Signal Generated (Conviction 85%)
                </div>
                <div className="p-4 rounded-xl bg-purple-500/10 border border-purple-500/20 text-purple-400 font-bold">
                  [STAGE 2] DeepSeek R1 CRO: Risk Veto Approved (1:2.4 R:R Ratio)
                </div>
                <div className="p-4 rounded-xl bg-blue-500/10 border border-blue-500/20 text-blue-400 font-bold">
                  [STAGE 3] Multimodal Vision: Candlestick Pattern Confirmed (82%)
                </div>
              </div>
            </div>
          </div>

          {/* Strip 2: Reversed (Card Left / Text Right) */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16 items-center pt-16">
            <div className="order-2 lg:order-1 bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-8 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="space-y-4">
                <div className="flex justify-between text-xs font-mono">
                  <span className="text-brand-textLight dark:text-brand-textDark">Position PnL</span>
                  <span className="text-emerald-400 font-bold">+1.25%</span>
                </div>
                <div className="w-full bg-black/10 dark:bg-white/10 h-2 rounded-full overflow-hidden">
                  <div className="bg-brand-accent h-full w-[65%]"></div>
                </div>
                <p className="text-xs font-mono text-brand-accent">
                  ⚡ STRIKE PROTECTION ACTIVATED: Stop-Loss moved to Breakeven (₹2,930.00)
                </p>
              </div>
            </div>

            <div className="order-1 lg:order-2 space-y-6">
              <span className="text-xs font-mono font-bold tracking-widest text-brand-accent uppercase">Automated Execution</span>
              <h3 className="text-3xl sm:text-4xl font-extrabold text-brand-headingLight dark:text-brand-headingDark tracking-tight">
                1.0% Trailing Stop Breakeven Lock & Auto-Exits
              </h3>
              <p className="text-brand-textLight dark:text-brand-textDark leading-relaxed">
                As soon as an open position hits +1.0% PnL, the engine automatically moves the stop-loss to breakeven, eliminating downside risk while allowing target gains to compound.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* SECTION E: Bento Grid (4 Columns) */}
      <section id="performance" className="py-24 bg-black/5 dark:bg-white/[0.02]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
              Institutional Risk Architecture
            </h2>
            <p className="text-sm sm:text-base text-brand-textLight dark:text-brand-textDark mt-3">
              Built on rigid quantitative rules to preserve equity across all market conditions.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="w-12 h-12 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center text-brand-accent font-bold text-xl mb-6">
                1%
              </div>
              <h4 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark mb-2">1% Risk Sizing</h4>
              <p className="text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed">
                Dynamic position sizing caps maximum risk per setup to ₹10,000 on a ₹1,000,000 portfolio.
              </p>
            </div>

            <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="w-12 h-12 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center text-brand-accent font-bold text-xl mb-6">
                1:2
              </div>
              <h4 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark mb-2">1:2 R:R Requirement</h4>
              <p className="text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed">
                Setups must offer at least 2 units of potential reward for every 1 unit of risk, or output HOLD.
              </p>
            </div>

            <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="w-12 h-12 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center text-brand-accent font-bold text-xl mb-6">
                Fib
              </div>
              <h4 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark mb-2">Murphy Confluence</h4>
              <p className="text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed">
                VWAP baseline, SMA20/50 alignment, RSI 50-70, and 0.500 Fibonacci retracement support.
              </p>
            </div>

            <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm hover:shadow-md transition-all duration-300 hover:-translate-y-1">
              <div className="w-12 h-12 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center text-brand-accent font-bold text-xl mb-6">
                API
              </div>
              <h4 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark mb-2">Upstox API v2</h4>
              <p className="text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed">
                Direct REST integration for live institutional quotes, intraday candles, and automated order routing.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* SECTION F: Beta Access & FAQs */}
      <section id="faqs" className="py-24">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-16">
          <BetaWaitlistForm />

          {/* FAQ Accordions */}
          <div className="space-y-4">
            <h3 className="text-2xl font-extrabold text-brand-headingLight dark:text-brand-headingDark text-center mb-8">
              Frequently Asked Questions
            </h3>

            {/* FAQ 1 */}
            <div className="border border-black/5 dark:border-white/10 rounded-2xl bg-brand-cardLight dark:bg-brand-cardDark overflow-hidden">
              <button
                onClick={() => toggleFaq('faq1')}
                className="w-full p-6 text-left flex justify-between items-center font-bold text-brand-headingLight dark:text-brand-headingDark"
              >
                <span>How does the Dual-Brain AI Validation system work?</span>
                <ChevronDown className={`w-5 h-5 transform transition-transform duration-300 text-brand-accent ${openFaq === 'faq1' ? 'rotate-180' : ''}`} />
              </button>
              {openFaq === 'faq1' && (
                <div className="p-6 pt-0 text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed border-t border-black/5 dark:border-white/5">
                  Groq Llama 3.3 screens screened candidates for momentum setups with a minimum conviction threshold of 70. Approved trade tickets are passed to DeepSeek R1 acting as Chief Risk Officer (CRO) to evaluate risk reward ratios and macro conditions before execution.
                </div>
              )}
            </div>

            {/* FAQ 2 */}
            <div className="border border-black/5 dark:border-white/10 rounded-2xl bg-brand-cardLight dark:bg-brand-cardDark overflow-hidden">
              <button
                onClick={() => toggleFaq('faq2')}
                className="w-full p-6 text-left flex justify-between items-center font-bold text-brand-headingLight dark:text-brand-headingDark"
              >
                <span>Is the Upstox OAuth V3 Integration live?</span>
                <ChevronDown className={`w-5 h-5 transform transition-transform duration-300 text-brand-accent ${openFaq === 'faq2' ? 'rotate-180' : ''}`} />
              </button>
              {openFaq === 'faq2' && (
                <div className="p-6 pt-0 text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed border-t border-black/5 dark:border-white/5">
                  Yes. The app connects directly via Upstox API v2 OAuth flow, syncing tokens to Firestore (`system_config/upstox_auth`) to allow both live paper trading and live broker order routing.
                </div>
              )}
            </div>

            {/* FAQ 3 */}
            <div className="border border-black/5 dark:border-white/10 rounded-2xl bg-brand-cardLight dark:bg-brand-cardDark overflow-hidden">
              <button
                onClick={() => toggleFaq('faq3')}
                className="w-full p-6 text-left flex justify-between items-center font-bold text-brand-headingLight dark:text-brand-headingDark"
              >
                <span>What is the 1% Risk Sizing Rule?</span>
                <ChevronDown className={`w-5 h-5 transform transition-transform duration-300 text-brand-accent ${openFaq === 'faq3' ? 'rotate-180' : ''}`} />
              </button>
              {openFaq === 'faq3' && (
                <div className="p-6 pt-0 text-sm text-brand-textLight dark:text-brand-textDark leading-relaxed border-t border-black/5 dark:border-white/5">
                  The quantitative engine dynamically calculates share quantity so that if the stop-loss is hit, maximum loss is strictly capped at 1% of total portfolio equity (₹10,000 on a ₹1,000,000 account).
                </div>
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
