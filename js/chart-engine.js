/**
 * DebtFreeIn Quantitative Chart Engine
 * Powered by TradingView Lightweight Charts
 */

document.addEventListener('DOMContentLoaded', () => {
  const container = document.getElementById('quant-chart');
  if (!container) return;

  // Initialize TradingView Lightweight Chart with dark mode palette
  const chart = LightweightCharts.createChart(container, {
    width: container.clientWidth,
    height: container.clientHeight || 384,
    layout: {
      background: { type: 'solid', color: '#0B0F19' },
      textColor: '#9CA3AF',
    },
    grid: {
      vertLines: { color: '#1F2937' },
      horzLines: { color: '#1F2937' },
    },
    crosshair: {
      mode: LightweightCharts.CrosshairMode.Normal,
    },
    rightPriceScale: {
      borderColor: '#1F2937',
    },
    timeScale: {
      borderColor: '#1F2937',
      timeVisible: true,
      secondsVisible: false,
    },
  });

  const candlestickSeries = chart.addCandlestickSeries({
    upColor: '#00E5FF',
    downColor: '#EF4444',
    borderVisible: false,
    wickUpColor: '#00E5FF',
    wickDownColor: '#EF4444',
  });

  // Generate at least 30 realistic candlestick data points showing an upward breakout pattern
  const data = [];
  const startDate = new Date(2026, 5, 1); // June 1, 2026
  let currentPrice = 2420.0;

  for (let i = 0; i < 35; i++) {
    const timeDate = new Date(startDate);
    timeDate.setDate(startDate.getDate() + i);
    const timeStr = timeDate.toISOString().split('T')[0];

    // Consolidation for first 20 days, followed by explosive AI confluence breakout
    const isBreakout = i >= 20;
    const baseDelta = isBreakout ? 14.0 : (Math.random() > 0.48 ? 3.5 : -3.0);
    const open = Math.round(currentPrice * 100) / 100;
    const change = Math.round((Math.random() * 12.0 + 2.0) * (baseDelta >= 0 ? 1 : -1) * 100) / 100;
    const close = Math.round((open + change) * 100) / 100;
    const high = Math.round((Math.max(open, close) + Math.random() * 8.0) * 100) / 100;
    const low = Math.round((Math.min(open, close) - Math.random() * 6.0) * 100) / 100;

    currentPrice = close;

    data.push({
      time: timeStr,
      open,
      high,
      low,
      close,
    });
  }

  candlestickSeries.setData(data);
  chart.timeScale().fitContent();

  // Responsive Resize Observer
  const resizeObserver = new ResizeObserver((entries) => {
    if (entries.length === 0 || !entries[0].contentRect) return;
    const newWidth = entries[0].contentRect.width;
    const newHeight = entries[0].contentRect.height || 384;
    chart.applyOptions({ width: newWidth, height: newHeight });
  });

  resizeObserver.observe(container);
});
