const express = require('express');
const router = express.Router();
const { verifyToken, checkSubscription } = require('../middleware/auth');

/**
 * GET /market/quotes
 * Placeholder route for Upstox / Broker data proxying.
 * Protected by verifyToken and checkSubscription.
 */
router.get('/quotes', verifyToken, checkSubscription, async (req, res) => {
  const { symbol = 'NIFTY50' } = req.query;

  return res.status(200).json({
    success: true,
    symbol,
    status: 'connected',
    provider: 'Upstox Proxy Gateway',
    timestamp: Date.now(),
    data: {
      lastPrice: 24500.50,
      change: +120.30,
      pChange: +0.49,
      high: 24550.00,
      low: 24380.00,
      volume: 1542000
    }
  });
});

/**
 * POST /market/order
 * Placeholder route for broker order execution proxying.
 */
router.post('/order', verifyToken, checkSubscription, async (req, res) => {
  const { symbol, transactionType, quantity, orderType } = req.body;

  if (!symbol || !transactionType || !quantity) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'symbol, transactionType, and quantity are required.'
    });
  }

  return res.status(200).json({
    success: true,
    orderId: `ORD-${Date.now()}`,
    status: 'COMPLETE',
    details: {
      symbol,
      transactionType,
      quantity,
      orderType: orderType || 'MARKET',
      executedAt: new Date().toISOString()
    }
  });
});

module.exports = router;
