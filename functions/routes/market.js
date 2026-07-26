const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const axios = require('axios');
const { verifyToken, checkSubscription } = require('../middleware/auth');

/**
 * Helper to fetch the stored Upstox access token for a given user from Firestore vault.
 * Path is strictly scoped to authenticated user's sub-collection.
 */
async function getUserUpstoxToken(uid) {
  const doc = await admin.firestore()
    .collection('users')
    .doc(uid)
    .collection('vault')
    .doc('broker_keys')
    .get();

  if (!doc.exists) return null;
  const data = doc.data();
  return data.upstoxAccessToken || data.apiKey || null;
}

// Strict Enum & Key Allow-lists for Upstox Order Payload
const ALLOWED_ORDER_KEYS = new Set([
  'quantity',
  'product',
  'validity',
  'price',
  'instrument_token',
  'order_type',
  'transaction_type',
  'disclosed_quantity',
  'trigger_price',
  'is_amo'
]);

const ALLOWED_TRANSACTION_TYPES = new Set(['BUY', 'SELL']);
const ALLOWED_ORDER_TYPES = new Set(['MARKET', 'LIMIT', 'SL', 'SL-M']);
const ALLOWED_PRODUCTS = new Set(['I', 'D']);
const ALLOWED_VALIDITY = new Set(['DAY', 'IOC']);

/**
 * Strict Input Validation Middleware for Upstox v2 Order Requests.
 */
const validateUpstoxOrder = (req, res, next) => {
  if (!req.body || typeof req.body !== 'object') {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'Request body must be a valid JSON object.'
    });
  }

  const bodyKeys = Object.keys(req.body);

  // 1. Property Allow-listing: Reject unknown properties
  for (const key of bodyKeys) {
    if (!ALLOWED_ORDER_KEYS.has(key)) {
      return res.status(400).json({
        error: 'Bad Request',
        message: `Disallowed property '${key}' in order payload.`
      });
    }
  }

  const {
    quantity,
    product,
    validity,
    price,
    instrument_token,
    order_type,
    transaction_type,
    trigger_price
  } = req.body;

  // 2. Validate transaction_type (strictly BUY or SELL)
  if (!transaction_type || !ALLOWED_TRANSACTION_TYPES.has(transaction_type)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: "transaction_type must be strictly 'BUY' or 'SELL'."
    });
  }

  // 3. Validate order_type (strictly MARKET, LIMIT, SL, SL-M)
  if (!order_type || !ALLOWED_ORDER_TYPES.has(order_type)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: "order_type must be strictly 'MARKET', 'LIMIT', 'SL', or 'SL-M'."
    });
  }

  // 4. Validate product (strictly I for Intraday or D for Delivery)
  if (!product || !ALLOWED_PRODUCTS.has(product)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: "product must be strictly 'I' (Intraday) or 'D' (Delivery)."
    });
  }

  // 5. Validate validity (strictly DAY or IOC)
  if (!validity || !ALLOWED_VALIDITY.has(validity)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: "validity must be strictly 'DAY' or 'IOC'."
    });
  }

  // 6. Validate instrument_token
  if (!instrument_token || typeof instrument_token !== 'string' || !instrument_token.trim()) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'instrument_token must be a non-empty string.'
    });
  }

  // 7. Validate quantity (strictly positive integer > 0)
  const numQuantity = Number(quantity);
  if (!Number.isInteger(numQuantity) || numQuantity <= 0) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'quantity must be a positive integer greater than 0.'
    });
  }

  // 8. Validate price
  if (price !== undefined && price !== null) {
    const numPrice = Number(price);
    if (isNaN(numPrice) || numPrice < 0) {
      return res.status(400).json({
        error: 'Bad Request',
        message: 'price must be a valid non-negative number.'
      });
    }
  }

  if ((order_type === 'LIMIT' || order_type === 'SL') && (!price || Number(price) <= 0)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: `price > 0 is required for order_type '${order_type}'.`
    });
  }

  // 9. Validate trigger_price
  if (trigger_price !== undefined && trigger_price !== null) {
    const numTrigger = Number(trigger_price);
    if (isNaN(numTrigger) || numTrigger < 0) {
      return res.status(400).json({
        error: 'Bad Request',
        message: 'trigger_price must be a valid non-negative number.'
      });
    }
  }

  if ((order_type === 'SL' || order_type === 'SL-M') && (!trigger_price || Number(trigger_price) <= 0)) {
    return res.status(400).json({
      error: 'Bad Request',
      message: `trigger_price > 0 is required for order_type '${order_type}'.`
    });
  }

  return next();
};

/**
 * GET /market/quotes
 * Proxies market quote requests to Upstox API v2 over HTTPS.
 * Requires: verifyToken, checkSubscription
 */
router.get('/quotes', verifyToken, checkSubscription, async (req, res) => {
  const { instrument_key, symbol } = req.query;
  const targetKey = instrument_key || symbol || 'NSE_INDEX|Nifty 50';

  try {
    const upstoxToken = await getUserUpstoxToken(req.user.uid);

    if (!upstoxToken) {
      return res.status(200).json({
        success: true,
        mode: 'simulated_fallback',
        message: 'No Upstox token in vault. Configure BYOK Vault in Settings for live Upstox data.',
        instrument_key: targetKey,
        data: {
          last_price: 24500.50,
          change: +120.30,
          p_change: +0.49,
          high: 24550.00,
          low: 24380.00,
          volume: 1542000
        }
      });
    }

    const upstoxResponse = await axios.get('https://api.upstox.com/v2/market-quote/quotes', {
      params: { instrument_key: targetKey },
      headers: {
        'Accept': 'application/json',
        'Authorization': `Bearer ${upstoxToken}`
      },
      timeout: 10000
    });

    return res.status(200).json({
      success: true,
      mode: 'live_upstox_proxy',
      data: upstoxResponse.data
    });

  } catch (error) {
    const statusCode = error.response?.status || 500;
    const upstoxErrorMessage = error.response?.data?.errors?.[0]?.message || 'Failed to fetch market quotes from broker.';

    console.error('Market Proxy Request Failed:', {
      status: statusCode,
      instrument: targetKey,
      message: error.message
    });

    return res.status(statusCode).json({
      error: 'Broker Proxy Error',
      message: upstoxErrorMessage
    });
  }
});

/**
 * POST /market/order
 * Proxies order placement requests to Upstox API v2 HFT engine over HTTPS.
 * Requires: verifyToken, checkSubscription, validateUpstoxOrder
 */
router.post('/order', verifyToken, checkSubscription, validateUpstoxOrder, async (req, res) => {
  const {
    quantity,
    product,
    validity,
    price,
    instrument_token,
    order_type,
    transaction_type,
    disclosed_quantity,
    trigger_price,
    is_amo
  } = req.body;

  try {
    const upstoxToken = await getUserUpstoxToken(req.user.uid);

    if (!upstoxToken) {
      return res.status(200).json({
        success: true,
        mode: 'paper_trading_fallback',
        message: 'No Upstox token found in vault. Order executed in paper-trading simulation mode.',
        orderId: `SIM-ORD-${Date.now()}`,
        status: 'COMPLETE',
        details: {
          instrument_token,
          quantity: Number(quantity),
          transaction_type,
          order_type,
          price: Number(price || 0),
          executedAt: new Date().toISOString()
        }
      });
    }

    const payload = {
      quantity: Number(quantity),
      product,
      validity,
      price: Number(price || 0),
      tag: 'debtfreein_ai',
      instrument_token,
      order_type,
      transaction_type,
      disclosed_quantity: Number(disclosed_quantity || 0),
      trigger_price: Number(trigger_price || 0),
      is_amo: Boolean(is_amo || false)
    };

    const upstoxResponse = await axios.post('https://api-hft.upstox.com/v2/order/place', payload, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': `Bearer ${upstoxToken}`
      },
      timeout: 10000
    });

    return res.status(200).json({
      success: true,
      mode: 'live_upstox_execution',
      orderData: upstoxResponse.data
    });

  } catch (error) {
    const statusCode = error.response?.status || 500;
    const upstoxErrorMessage = error.response?.data?.errors?.[0]?.message || 'Failed to place order with broker.';

    console.error('Order Execution Request Failed:', {
      status: statusCode,
      token: instrument_token,
      message: error.message
    });

    return res.status(statusCode).json({
      error: 'Order Execution Error',
      message: upstoxErrorMessage
    });
  }
});

module.exports = router;
