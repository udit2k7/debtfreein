import React from 'react';

export default function Terms() {
  return (
    <main className="py-16 md:py-24">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        <div className="border-b border-black/10 dark:border-white/10 pb-8 space-y-3">
          <h1 className="text-3xl sm:text-4xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">Terms and Conditions of Use</h1>
          <p className="text-sm font-mono text-brand-accent">Last Updated: July 26, 2026</p>
        </div>

        <div className="space-y-8 text-base leading-relaxed">
          {/* 1. Introduction and Nature of Service */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">1. Introduction and Nature of Service</h2>
            <p>
              Welcome to DebtFreeIn. By downloading, accessing, or using the DebtFreeIn mobile application, website, or any associated services (collectively, the "Platform"), you expressly agree to be bound by these Terms and Conditions. <strong className="text-brand-headingLight dark:text-brand-headingDark">READ THESE TERMS CAREFULLY. IF YOU DO NOT AGREE TO THESE TERMS, YOU MUST IMMEDIATELY CEASE ALL USE OF THE PLATFORM.</strong>
            </p>
            <div className="p-5 rounded-2xl bg-amber-500/10 border border-amber-500/30 text-amber-900 dark:text-amber-300 space-y-2">
              <h3 className="font-extrabold text-sm uppercase tracking-wider text-amber-600 dark:text-amber-400">Critical Disclaimer: Not a Trading Platform</h3>
              <p className="text-sm leading-relaxed">
                DebtFreeIn is strictly an educational, artificial intelligence-driven <strong>PAPER TRADING AND SIMULATION</strong> application. It is NOT a brokerage, NOT an exchange, and NOT a registered financial advisory service. No real money, fiat currency, cryptocurrencies, or actual securities are traded, exchanged, or transacted on this Platform. All portfolios, trades, balances, and market positions are 100% simulated, fictitious, and exist solely for educational and testing purposes.
              </p>
            </div>
          </section>

          {/* 2. Brand Name Disclaimer */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">2. Brand Name Disclaimer</h2>
            <p>
              You explicitly acknowledge and agree that the name "DebtFreeIn" is utilized purely for branding, marketing, and identification purposes. The name DOES NOT represent the nature, capabilities, or guaranteed outcomes of the service. We make ABSOLUTELY NO PROMISES, warranties, or representations that using this Platform will result in financial success, debt reduction, debt elimination, or profitability in the real world. The name shall not be construed as a financial guarantee or financial advice under any jurisdiction.
            </p>
          </section>

          {/* 3. No Financial, Legal, or Tax Advice */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">3. No Financial, Legal, or Tax Advice</h2>
            <p>
              We are not motivating, promoting, or inciting any individual to engage in real-world investing, trading, or financial speculation. The AI models (including but not limited to the Dual-Brain AI quantitative engine), historical backtesting, and market simulations provided within the Platform are for informational and entertainment purposes only. Nothing contained on the Platform constitutes financial, legal, tax, or investment advice. You should consult with a licensed, qualified professional before making any real-world financial decisions.
            </p>
          </section>

          {/* 4. Assumption of Real-World Investment Risks */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">4. Assumption of Real-World Investment Risks</h2>
            <p>
              If you choose to apply any concepts, strategies, or AI-generated patterns observed in our simulated environment to real-world financial markets, you do so entirely at your own risk.
            </p>
            <div className="p-5 rounded-2xl bg-red-500/10 border border-red-500/30 text-red-900 dark:text-red-300 space-y-2">
              <h3 className="font-extrabold text-sm uppercase tracking-wider text-red-600 dark:text-red-400">Investment Risk Warning</h3>
              <p className="text-sm leading-relaxed">
                Trading in equities, options, futures, forex, or any financial instrument involves a high degree of risk and may not be suitable for all investors. The degree of leverage can work against you as well as for you. Before deciding to invest in real markets, you should carefully consider your investment objectives, level of experience, and risk appetite. <strong>You could sustain a loss of some or all of your initial investment.</strong> Past performance in a simulated environment is absolutely no guarantee of future results in live markets. The creators of DebtFreeIn bear zero responsibility for any financial losses you may incur in real-life trading.
              </p>
            </div>
          </section>

          {/* 5. Accuracy of Information and Market Data */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">5. Accuracy of Information and Market Data</h2>
            <p>
              While DebtFreeIn utilizes third-party APIs to simulate market conditions, we do not guarantee the accuracy, completeness, or timeliness of any market data, quantitative metrics, or AI-generated signals. Data may be delayed, interrupted, or mathematically inaccurate. The Platform is provided on a strictly "AS-IS" and "AS-AVAILABLE" basis without warranties of any kind, either express or implied.
            </p>
          </section>

          {/* 6. Limitation of Liability and Indemnification */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">6. Limitation of Liability and Indemnification</h2>
            <p>
              To the maximum extent permitted by applicable law, in no event shall DebtFreeIn, its developers, founders, affiliates, or licensors be liable for any direct, indirect, punitive, incidental, special, consequential, or exemplary damages, including without limitation damages for loss of profits, goodwill, use, data, or other intangible losses, arising out of or relating to the use of, or inability to use, this Platform. You agree to defend, indemnify, and hold harmless DebtFreeIn and its creators from any claims, liabilities, damages, judgments, awards, losses, costs, expenses, or fees arising out of your violation of These Terms or your use of the Platform.
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}
