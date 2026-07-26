const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
require('dotenv').config();

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const app = express();

// Enable CORS for cross-origin requests
app.use(cors({ origin: true }));

// Body parser middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Import routes
const adminRoutes = require('./routes/admin');
const marketRoutes = require('./routes/market');
const vaultRoutes = require('./routes/vault');

// Mount routes
app.use('/admin', adminRoutes);
app.use('/market', marketRoutes);
app.use('/vault', vaultRoutes);

// Health check endpoint
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    service: 'DebtFreeIn API Gateway',
    timestamp: new Date().toISOString()
  });
});

app.get('/', (req, res) => {
  res.status(200).json({
    name: 'DebtFreeIn API Gateway',
    version: '1.0.0',
    endpoints: {
      health: '/health',
      admin: '/admin/*',
      market: '/market/*',
      vault: '/vault/*'
    }
  });
});

// Export Express app as HTTPS Cloud Function
exports.api = functions.https.onRequest(app);
