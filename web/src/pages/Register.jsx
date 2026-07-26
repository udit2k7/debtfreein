import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { UserPlus, AlertCircle } from 'lucide-react';

export default function Register() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register, googleSignIn } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      return setError('Passwords do not match.');
    }
    setError('');
    setLoading(true);
    try {
      await register(email, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to create account.');
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSignIn = async () => {
    setError('');
    setLoading(true);
    try {
      await googleSignIn();
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Google authentication failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="py-20 flex items-center justify-center min-h-[75vh] px-4">
      <div className="w-full max-w-md relative">
        <div className="absolute -inset-1 rounded-3xl bg-gradient-to-r from-brand-accent/40 to-blue-600/40 blur-xl opacity-50"></div>
        <div className="relative bg-brand-cardLight dark:bg-brand-cardDark border border-black/10 dark:border-white/10 rounded-2xl p-8 shadow-2xl space-y-6">
          <div className="text-center space-y-2">
            <div className="w-12 h-12 rounded-xl bg-brand-accent/10 border border-brand-accent/30 flex items-center justify-center text-brand-accent mx-auto mb-3">
              <UserPlus className="w-6 h-6" />
            </div>
            <h1 className="text-2xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
              Create Trader Account
            </h1>
            <p className="text-xs text-brand-textLight dark:text-brand-textDark">
              Unlock paper trading simulations & quantitative signals
            </p>
          </div>

          {error && (
            <div className="p-3.5 rounded-xl bg-red-500/10 border border-red-500/30 text-red-400 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Email Address
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="trader@quant.com"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="At least 6 characters"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <div>
              <label className="block text-xs font-mono font-semibold text-brand-headingLight dark:text-brand-headingDark uppercase mb-1.5">
                Confirm Password
              </label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                placeholder="Re-enter password"
                className="w-full px-4 py-3 rounded-xl bg-black/5 dark:bg-black/40 border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark text-sm focus:outline-none focus:border-brand-accent transition-colors"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 rounded-xl font-bold text-sm bg-brand-accent text-black hover:shadow-[0_0_20px_rgba(0,229,255,0.4)] transition-all duration-300 disabled:opacity-50 mt-2"
            >
              {loading ? 'Creating Account...' : 'Register Account'}
            </button>
          </form>

          <div className="relative flex items-center justify-center my-4">
            <div className="border-t border-black/10 dark:border-white/10 w-full"></div>
            <span className="bg-brand-cardLight dark:bg-brand-cardDark px-3 text-[10px] font-mono uppercase text-brand-textLight dark:text-brand-textDark shrink-0">
              OR
            </span>
          </div>

          <button
            onClick={handleGoogleSignIn}
            disabled={loading}
            className="w-full py-3.5 rounded-xl font-semibold text-sm border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark hover:bg-black/5 dark:hover:bg-white/5 transition-all duration-300 flex items-center justify-center gap-3 disabled:opacity-50"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
              />
            </svg>
            <span>Continue with Google</span>
          </button>

          <p className="text-center text-xs text-brand-textLight dark:text-brand-textDark">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-accent font-semibold hover:underline">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </main>
  );
}
