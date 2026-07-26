const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const axios = require('axios');
const { verifyToken, checkSubscription } = require('../middleware/auth');

/**
 * Helper to fetch the stored Upstox access token for a given user from Firestore vault.
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

/**
 * GET /market/quotes
 * Proxies market quote requests to Upstox API v2.
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
      }
    });

    return res.status(200).json({
      success: true,
      mode: 'live_upstox_proxy',
      data: upstoxResponse.data
    });

  } catch (error) {
    console.error('Error fetching Upstox market quotes:', error.response?.data || error.message);
    
    return res.status(error.response?.status || 500).json({
      error: 'Upstox Proxy Error',
      message: error.response?.data?.errors?.[0]?.message || 'Failed to fetch Upstox quotes.',
      details: error.response?.data || error.message
    });
  }
});

/**
 * POST /market/order
 * Proxies order placement requests to Upstox API v2 HFT engine.
 * Requires: verifyToken, checkSubscription
 */
router.post('/order', verifyToken, checkSubscription, async (req, res) => {
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

  if (!quantity || !product || !validity || !instrument_token || !order_type || !transaction_type) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'Missing required Upstox order fields (quantity, product, validity, instrument_token, order_type, transaction_type).'
    });
  }

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
          quantity,
          transaction_type,
          order_type,
          price: price || 0,
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
      }
    });

    return res.status(200).json({
      success: true,
      mode: 'live_upstox_execution',
      orderData: upstoxResponse.data
    });

  } catch (error) {
    console.error('Error executing Upstox order:', error.response?.data || error.message);

    return res.status(error.response?.status || 500).json({
      error: 'Upstox Order Execution Error',
      message: error.response?.data?.errors?.[0]?.message || 'Failed to place order with Upstox.',
      details: error.response?.data || error.message
    });
  }
});

module.exports = router;
