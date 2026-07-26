const admin = require('firebase-admin');

/**
 * Middleware to verify Firebase ID Token from Authorization header.
 * Expects header format: "Authorization: Bearer <token>"
 */
const verifyToken = async (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'No Bearer token provided in Authorization header.'
    });
  }

  const idToken = authHeader.split('Bearer ')[1].trim();

  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    return next();
  } catch (error) {
    console.error('Error verifying Firebase ID token:', error);
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid or expired authentication token.',
      details: error.message
    });
  }
};

/**
 * Middleware to check user's subscription status in Firestore.
 * Requires verifyToken to have attached req.user.
 * Rejects with 403 if status !== 'active' or subscription_expiry_date < Date.now()
 */
const checkSubscription = async (req, res, next) => {
  if (!req.user || !req.user.uid) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'User authentication required before subscription validation.'
    });
  }

  try {
    const userDocRef = admin.firestore().collection('users').doc(req.user.uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'User record not found in system.'
      });
    }

    const userData = userDoc.data();
    const { status, subscription_expiry_date } = userData;

    if (status !== 'active') {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Account subscription is not active.',
        currentStatus: status || 'inactive'
      });
    }

    // Convert expiry date (handles ms number, Date string, or Firestore Timestamp)
    let expiryMs = 0;
    if (typeof subscription_expiry_date === 'number') {
      expiryMs = subscription_expiry_date;
    } else if (subscription_expiry_date && typeof subscription_expiry_date.toMillis === 'function') {
      expiryMs = subscription_expiry_date.toMillis();
    } else if (subscription_expiry_date) {
      expiryMs = new Date(subscription_expiry_date).getTime();
    }

    if (!expiryMs || expiryMs < Date.now()) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Subscription has expired.',
        expiryDate: expiryMs ? new Date(expiryMs).toISOString() : null
      });
    }

    req.subscription = {
      status,
      expiryMs,
      userData
    };

    return next();
  } catch (error) {
    console.error('Error checking subscription:', error);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Failed to validate subscription status.',
      details: error.message
    });
  }
};

module.exports = {
  verifyToken,
  checkSubscription
};
