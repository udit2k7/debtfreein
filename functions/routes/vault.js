const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const { verifyToken } = require('../middleware/auth');

/**
 * POST /vault/upstox
 * Securely stores user's Upstox Access Token in Firestore vault.
 * Path is strictly bound to req.user.uid (extracted securely from verified ID token):
 * users/{req.user.uid}/vault/broker_keys
 */
router.post('/upstox', verifyToken, async (req, res) => {
  const { accessToken, apiKey, apiSecret, broker = 'upstox' } = req.body;

  // Enforce UID extraction strictly from verified ID token
  const authUid = req.user && req.user.uid;
  if (!authUid) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid identity context.'
    });
  }

  const tokenToSave = (accessToken || apiKey || '').trim();
  if (!tokenToSave) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'AccessToken or ApiKey is required.'
    });
  }

  try {
    const vaultRef = admin.firestore()
      .collection('users')
      .doc(authUid) // Strictly uses authenticated UID from verifyToken
      .collection('vault')
      .doc('broker_keys');

    const updateData = {
      upstoxAccessToken: tokenToSave,
      apiKey: apiKey ? apiKey.trim() : null,
      apiSecret: apiSecret ? apiSecret.trim() : null,
      broker,
      updatedAt: Date.now()
    };

    await vaultRef.set(updateData, { merge: true });

    return res.status(200).json({
      success: true,
      message: 'Broker credentials stored securely in vault.',
      broker,
      updatedAt: updateData.updatedAt
    });
  } catch (error) {
    console.error('Vault storage error:', error.message);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to store credentials in vault.'
    });
  }
});

/**
 * GET /vault/upstox
 * Retrieves vault status for authenticated user.
 * SECURITY: Never returns the raw token in plaintext to the client.
 */
router.get('/upstox', verifyToken, async (req, res) => {
  const authUid = req.user && req.user.uid;
  if (!authUid) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid identity context.'
    });
  }

  try {
    const vaultDoc = await admin.firestore()
      .collection('users')
      .doc(authUid)
      .collection('vault')
      .doc('broker_keys')
      .get();

    if (!vaultDoc.exists) {
      return res.status(200).json({
        hasToken: false,
        broker: 'upstox',
        updatedAt: null,
        maskedToken: null
      });
    }

    const data = vaultDoc.data();
    const token = data.upstoxAccessToken || data.apiKey || '';
    const hasToken = token.length > 0;

    // Mask token string safely (returns ****-****-1234 format)
    let maskedToken = null;
    if (hasToken) {
      const lastFour = token.slice(-4);
      maskedToken = `****-****-${lastFour}`;
    }

    return res.status(200).json({
      hasToken,
      broker: data.broker || 'upstox',
      updatedAt: data.updatedAt || null,
      maskedToken
    });
  } catch (error) {
    console.error('Vault status check error:', error.message);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to read vault status.'
    });
  }
});

module.exports = router;
