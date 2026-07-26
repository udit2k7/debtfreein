const admin = require('firebase-admin');

/**
 * Middleware to verify Firebase ID Token from Authorization header.
 * Expects header format: "Authorization: Bearer <token>"
 */
const verifyToken = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || typeof authHeader !== 'string' || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({
        error: 'Unauthorized',
        message: 'No valid Bearer token provided in Authorization header.'
      });
    }

    const parts = authHeader.split(' ');
    if (parts.length !== 2 || !parts[1].trim()) {
      return res.status(401).json({
        error: 'Unauthorized',
        message: 'Malformed Authorization header.'
      });
    }

    const idToken = parts[1].trim();
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    req.user = decodedToken;
    return next();
  } catch (error) {
    console.error('Authentication verification failure:', error.code || error.message);
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Invalid or expired authentication token.'
    });
  }
};

/**
 * Middleware to check user's subscription status in Firestore.
 * Requires verifyToken to have attached req.user.
 * Edge cases handled:
 * - User document missing -> 403 Forbidden
 * - Missing/null/invalid subscription_expiry_date -> 403 Forbidden
 * - Clock skew tolerance: 60-second grace window on Date.now() comparison
 */
const checkSubscription = async (req, res, next) => {
  if (!req.user || !req.user.uid) {
    return res.status(401).json({
      error: 'Unauthorized',
      message: 'Authentication context missing.'
    });
  }

  try {
    const userDocRef = admin.firestore().collection('users').doc(req.user.uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'User account record not found.'
      });
    }

    const userData = userDoc.data();
    if (!userData) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'User record data is empty.'
      });
    }

    const { status, subscription_expiry_date } = userData;

    if (status !== 'active') {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Subscription is not active.'
      });
    }

    // Safely check for missing or null subscription_expiry_date
    if (subscription_expiry_date === undefined || subscription_expiry_date === null) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Subscription expiry date is unconfigured or invalid.'
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

    if (!expiryMs || isNaN(expiryMs)) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Invalid subscription expiry date format.'
      });
    }

    // Allow a 60-second grace window to account for server clock / NTP skew
    const CLOCK_SKEW_GRACE_MS = 60 * 1000;
    if (expiryMs < (Date.now() - CLOCK_SKEW_GRACE_MS)) {
      return res.status(403).json({
        error: 'Forbidden',
        message: 'Subscription has expired.'
      });
    }

    req.subscription = {
      status,
      expiryMs
    };

    return next();
  } catch (error) {
    console.error('Subscription verification exception:', error.message);
    return res.status(500).json({
      error: 'Internal Server Error',
      message: 'Subscription check failed.'
    });
  }
};

module.exports = {
  verifyToken,
  checkSubscription
};
