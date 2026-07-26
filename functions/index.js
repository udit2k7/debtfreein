const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
require('dotenv').config();

// Initialize Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp();
}

const app = express();

// Rate Limiting Policy: Limit to 60 requests per minute per IP to defend against DDoS and protect broker API keys
const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 60, // Maximum 60 requests per IP per minute
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too Many Requests',
    message: 'API rate limit exceeded. Maximum 60 requests per minute allowed.'
  }
});

// Apply rate limiter
app.use(apiLimiter);

// Allowed Origins for CORS (Production Frontend Domain & Local Dev Environments)
const ALLOWED_ORIGINS = [
  'https://debtfreein.com',
  'https://www.debtfreein.com',
  'https://debtfreein-db.web.app',
  'https://debtfreein-db.firebaseapp.com',
  'http://localhost:5173',
  'http://localhost:3000',
  'http://127.0.0.1:5173',
  'http://127.0.0.1:3000'
];

const corsOptions = {
  origin: (origin, callback) => {
    // Allow server-to-server or tools without an origin header (e.g. Postman/Curl during dev)
    if (!origin) return callback(null, true);
    if (ALLOWED_ORIGINS.includes(origin)) {
      return callback(null, true);
    }
    return callback(new Error('CORS Policy: Access denied for this origin.'));
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
};

// Enable Strict CORS Policy
app.use(cors(corsOptions));

// Body parser middleware
app.use(express.json({ limit: '10kb' }));
app.use(express.urlencoded({ extended: true, limit: '10kb' }));

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

// Express Error Handler for CORS & Middleware Exception Catching
app.use((err, req, res, _next) => {
  if (err.message && err.message.includes('CORS Policy')) {
    return res.status(403).json({
      error: 'Forbidden',
      message: err.message
    });
  }
  console.error('Unhandled Gateway Exception:', err.message);
  return res.status(500).json({
    error: 'Internal Server Error',
    message: 'An unexpected security condition occurred.'
  });
});

// Export Express app as HTTPS Cloud Function
exports.api = functions.https.onRequest(app);
