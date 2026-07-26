const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const { verifyToken } = require('../middleware/auth');

/**
 * POST /vault/upstox
 * Securely stores user's Upstox Access Token & credentials in Firestore vault.
 * Path: users/{uid}/vault/broker_keys
 */
router.post('/upstox', verifyToken, async (req, res) => {
  const { accessToken, apiKey, apiSecret, broker = 'upstox' } = req.body;

  const tokenToSave = accessToken || apiKey;
  if (!tokenToSave) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'accessToken (or apiKey) is required.'
    });
  }

  try {
    const vaultRef = admin.firestore()
      .collection('users')
      .doc(req.user.uid)
      .collection('vault')
      .doc('broker_keys');

    const updateData = {
      upstoxAccessToken: tokenToSave,
      apiKey: apiKey || null,
      apiSecret: apiSecret || null,
      broker,
      updatedAt: Date.now()
    };

    await vaultRef.set(updateData, { merge: true });

    return res.status(200).json({
      success: true,
      message: 'Upstox broker credentials stored securely in vault.',
      broker,
      updatedAt: updateData.updatedAt
    });
  } catch (error) {
    console.error('Error saving to broker vault:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to store broker credentials in vault.',
      details: error.message
    });
  }
});

/**
 * GET /vault/upstox
 * Retrieves current vault status for the authenticated user.
 */
router.get('/upstox', verifyToken, async (req, res) => {
  try {
    const vaultDoc = await admin.firestore()
      .collection('users')
      .doc(req.user.uid)
      .collection('vault')
      .doc('broker_keys')
      .get();

    if (!vaultDoc.exists) {
      return res.status(200).json({
        hasToken: false,
        broker: 'upstox',
        updatedAt: null
      });
    }

    const data = vaultDoc.data();
    const token = data.upstoxAccessToken || data.apiKey;
    return res.status(200).json({
      hasToken: !!token,
      broker: data.broker || 'upstox',
      updatedAt: data.updatedAt || null,
      maskedToken: token ? `${token.slice(0, 4)}...${token.slice(-4)}` : null
    });
  } catch (error) {
    console.error('Error reading broker vault:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to read vault status.',
      details: error.message
    });
  }
});

module.exports = router;
