import React from 'react';

export default function Privacy() {
  return (
    <main className="py-16 md:py-24">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
        <div className="border-b border-black/10 dark:border-white/10 pb-8 space-y-3">
          <h1 className="text-3xl sm:text-4xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">Privacy Policy</h1>
          <p className="text-sm font-mono text-brand-accent">Last Updated: July 26, 2026</p>
        </div>

        <div className="space-y-8 text-base leading-relaxed">
          {/* 1. Introduction */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">1. Introduction</h2>
            <p>
              DebtFreeIn ("we," "our," or "us") respects your privacy and is committed to protecting it through our compliance with this policy. This Privacy Policy describes the types of information we may collect from you or that you may provide when you visit the DebtFreeIn website or use the mobile application, and our practices for collecting, using, maintaining, protecting, and disclosing that information.
            </p>
          </section>

          {/* 2. Information We Collect */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">2. Information We Collect</h2>
            <p>
              Because DebtFreeIn is strictly a simulated paper-trading environment, <strong className="text-brand-headingLight dark:text-brand-headingDark">we DO NOT collect sensitive real-world financial data</strong>. We do not ask for, nor do we store, your bank account details, Social Security Numbers, KYC (Know Your Customer) documents, or live brokerage API keys.
            </p>
            <p>We may collect the following types of information:</p>
            <ul className="list-disc pl-6 space-y-2 text-sm text-brand-textLight dark:text-brand-textDark">
              <li><strong className="text-brand-headingLight dark:text-brand-headingDark">Account Data:</strong> Email address, username, and authentication tokens (managed securely via Google Firebase).</li>
              <li><strong className="text-brand-headingLight dark:text-brand-headingDark">Simulation Data:</strong> Your paper-trading transaction history, saved AI quantitative strategies, simulated portfolio balances, and interactions with the AI models.</li>
              <li><strong className="text-brand-headingLight dark:text-brand-headingDark">Device and Usage Data:</strong> IP addresses, device identifiers, operating system types, app crash reports, and analytics data regarding how you navigate the application to help us improve performance.</li>
            </ul>
          </section>

          {/* 3. How We Use Your Information */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">3. How We Use Your Information</h2>
            <p>We use the information we collect strictly to operate the simulation engine and provide the services described:</p>
            <ul className="list-disc pl-6 space-y-2 text-sm text-brand-textLight dark:text-brand-textDark">
              <li>To create and maintain your simulated trading account.</li>
              <li>To process and display your simulated trades and AI engine interactions.</li>
              <li>To improve our algorithms, user interface, and overall user experience.</li>
              <li>To monitor for technical anomalies, crashes, and unauthorized usage.</li>
              <li>To comply with legal obligations and enforce our Terms and Conditions.</li>
            </ul>
          </section>

          {/* 4. Third-Party Data Processors */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">4. Third-Party Data Processors</h2>
            <p>To operate the Platform, we share necessary technical data with trusted third-party infrastructure providers. These include:</p>
            <ul className="list-disc pl-6 space-y-2 text-sm text-brand-textLight dark:text-brand-textDark">
              <li><strong className="text-brand-headingLight dark:text-brand-headingDark">Google Firebase & Google Cloud:</strong> Used for secure database hosting, user authentication, and crash analytics.</li>
              <li><strong className="text-brand-headingLight dark:text-brand-headingDark">AI Service Providers (e.g., Google Gemini):</strong> Used to process the quantitative logic and chat prompts. <em>Note: We do not send personally identifiable information (PII) to AI models; only the required market parameters for simulation.</em></li>
            </ul>
            <p>We DO NOT sell, rent, or lease your personal information to third-party marketers or data brokers.</p>
          </section>

          {/* 5. Data Security */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">5. Data Security</h2>
            <p>
              We have implemented measures designed to secure your personal information from accidental loss and from unauthorized access, use, alteration, and disclosure. All information you provide to us is stored on our secure servers behind firewalls, utilizing industry-standard encryption protocols. However, the transmission of information via the internet is not completely secure, and we cannot guarantee the absolute security of your personal information transmitted to our app.
            </p>
          </section>

          {/* 6. Account and Data Deletion Instructions */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">Account and Data Deletion Instructions</h2>
            <p>
              In strict accordance with Google Play Store policies and global data privacy standards, users maintain full control over their account and personal data.
            </p>
            <div className="p-5 rounded-2xl bg-brand-accent/10 border border-brand-accent/30 space-y-3">
              <h3 className="font-bold text-base text-brand-headingLight dark:text-brand-headingDark">How to Request Account &amp; Data Deletion</h3>
              <p className="text-sm leading-relaxed">
                Users can request account and data deletion by emailing <a href="mailto:support@debtfreein.com" className="text-brand-accent underline">support@debtfreein.com</a>. Upon request, all associated user data (Firestore records, Auth records, simulated portfolio balances, and transaction history) will be permanently wiped within 7 days. You may also delete your account directly within the app under Settings &gt; Account &gt; Delete Account.
              </p>
            </div>
          </section>

          {/* 7. Changes to Our Privacy Policy */}
          <section className="space-y-4">
            <h2 className="text-xl font-bold text-brand-headingLight dark:text-brand-headingDark">7. Changes to Our Privacy Policy</h2>
            <p>
              We reserve the right to update or change our Privacy Policy at any time. Any changes will be posted on this page with an updated "Last Updated" date. Your continued use of the Platform after we make changes is deemed to be acceptance of those changes.
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}
