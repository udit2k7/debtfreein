const express = require('express');
const router = express.Router();
const admin = require('firebase-admin');
const { verifyToken } = require('../middleware/auth');

/**
 * Middleware to ensure requesting user is an admin.
 */
const verifyAdminRole = async (req, res, next) => {
  if (req.user && (req.user.admin === true || req.user.role === 'admin')) {
    return next();
  }

  try {
    const userDoc = await admin.firestore().collection('users').doc(req.user.uid).get();
    if (userDoc.exists && userDoc.data().role === 'admin') {
      return next();
    }
  } catch (err) {
    console.error('Error checking admin role:', err);
  }

  return res.status(403).json({
    error: 'Forbidden',
    message: 'Admin privileges required for this endpoint.'
  });
};

/**
 * POST /admin/approve-user
 * Approves a user and sets a 6-month subscription expiry timestamp.
 */
router.post('/approve-user', verifyToken, verifyAdminRole, async (req, res) => {
  const { targetUid, months = 6 } = req.body;

  if (!targetUid) {
    return res.status(400).json({
      error: 'Bad Request',
      message: 'targetUid is required in request body.'
    });
  }

  try {
    const now = Date.now();
    // 6 months calculation (~ 180 days)
    const durationMs = months * 30 * 24 * 60 * 60 * 1000;
    const expiryTimestamp = now + durationMs;

    const userRef = admin.firestore().collection('users').doc(targetUid);
    await userRef.set({
      status: 'active',
      subscription_expiry_date: expiryTimestamp,
      approved_at: now,
      approved_by: req.user.uid
    }, { merge: true });

    return res.status(200).json({
      success: true,
      message: `User ${targetUid} approved for ${months} months.`,
      status: 'active',
      subscription_expiry_date: expiryTimestamp,
      expiryIsoDate: new Date(expiryTimestamp).toISOString()
    });
  } catch (error) {
    console.error('Error approving user:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to approve user and update subscription.',
      details: error.message
    });
  }
});

/**
 * GET /admin/pending-users
 * Returns list of users pending subscription approval.
 */
router.get('/pending-users', verifyToken, verifyAdminRole, async (req, res) => {
  try {
    const snapshot = await admin.firestore()
      .collection('users')
      .where('status', '==', 'pending')
      .get();

    const pendingUsers = snapshot.docs.map(doc => ({
      uid: doc.id,
      ...doc.data()
    }));

    return res.status(200).json({
      success: true,
      count: pendingUsers.length,
      users: pendingUsers
    });
  } catch (error) {
    console.error('Error fetching pending users:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to fetch pending users list.',
      details: error.message
    });
  }
});

module.exports = router;
