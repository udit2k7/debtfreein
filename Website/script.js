/**
 * DebtFreeIn Marketing Website Interactive Engine
 * Handles navigation toggles, live telemetry state simulations, and release tracking.
 */

document.addEventListener('DOMContentLoaded', () => {
  // 1. Mobile Menu Toggle
  const mobileToggle = document.getElementById('mobileToggle');
  const navMenu = document.getElementById('navMenu');

  if (mobileToggle && navMenu) {
    mobileToggle.addEventListener('click', () => {
      navMenu.classList.toggle('active');
      const isExpanded = navMenu.classList.contains('active');
      mobileToggle.setAttribute('aria-expanded', isExpanded);
      mobileToggle.textContent = isExpanded ? '✕' : '☰';
    });

    // Close menu when clicking outside or on a link
    document.addEventListener('click', (e) => {
      if (!mobileToggle.contains(e.target) && !navMenu.contains(e.target)) {
        navMenu.classList.remove('active');
        mobileToggle.textContent = '☰';
      }
    });
  }

  // 2. Dynamic Version Badge Update
  const versionPill = document.querySelectorAll('.app-version-tag');
  const currentVersion = "v1.1.3";
  versionPill.forEach(el => {
    el.textContent = currentVersion;
  });

  // 3. Simulated Live Telemetry Feed (Interactive UI element)
  const pnlElement = document.getElementById('telemetryPnl');
  const logsConsole = document.getElementById('telemetryConsole');

  if (pnlElement && logsConsole) {
    let basePnL = 1250.00;
    const sampleSymbols = ['RELIANCE', 'HDFCBANK', 'INFY', 'ICICIBANK', 'TCS'];
    const sampleActions = ['BUY', 'SELL'];

    function updateTelemetry() {
      // Small random PnL fluctuation
      const delta = (Math.random() * 40 - 15);
      basePnL += delta;
      
      const formattedPnL = (basePnL >= 0 ? '+₹' : '-₹') + Math.abs(basePnL).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      });

      pnlElement.textContent = formattedPnL;
      pnlElement.style.color = basePnL >= 0 ? '#10b981' : '#f43f5e';

      // Append new console line
      const symbol = sampleSymbols[Math.floor(Math.random() * sampleSymbols.length)];
      const action = sampleActions[Math.floor(Math.random() * sampleActions.length)];
      const qty = Math.floor(Math.random() * 25) + 5;
      const price = (Math.random() * 1500 + 500).toFixed(2);
      const timeStr = new Date().toLocaleTimeString('en-US', { hour12: false });

      const newLine = document.createElement('div');
      newLine.className = 'console-line';
      newLine.innerHTML = `<span class="ts">[${timeStr}]</span> <span class="tag">[TELEMETRY]</span> Executed ${action} ${symbol} x${qty} @ ₹${price} (Slippage: 0.05%)`;

      logsConsole.insertBefore(newLine, logsConsole.firstChild);

      // Keep console trimmed to 6 lines
      while (logsConsole.children.length > 6) {
        logsConsole.removeChild(logsConsole.lastChild);
      }
    }

    // Run telemetry tick every 4 seconds
    setInterval(updateTelemetry, 4000);
  }

  // 4. Smooth Anchor Link Scrolling
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
      const targetId = this.getAttribute('href');
      if (targetId === '#') return;
      
      const targetElement = document.querySelector(targetId);
      if (targetElement) {
        e.preventDefault();
        targetElement.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
        
        // Close mobile nav if open
        if (navMenu && navMenu.classList.contains('active')) {
          navMenu.classList.remove('active');
          if (mobileToggle) mobileToggle.textContent = '☰';
        }
      }
    });
  });
});
