import React, { useState, useEffect } from 'react';
import { db } from '../lib/firebase';
import { collection, getDocs } from 'firebase/firestore';
import api from '../lib/api';
import { ShieldCheck, UserCheck, Clock, AlertCircle, RefreshCw, CheckCircle2 } from 'lucide-react';

export default function Admin() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [approvingUid, setApprovingUid] = useState(null);
  const [message, setMessage] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const querySnapshot = await getDocs(collection(db, 'users'));
      const userList = [];
      querySnapshot.forEach((doc) => {
        userList.push({ uid: doc.id, ...doc.data() });
      });
      setUsers(userList);
    } catch (error) {
      console.error('Error fetching users:', error);
      setMessage({ type: 'error', text: 'Failed to fetch registered users.' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleApproveUser = async (targetUid) => {
    setApprovingUid(targetUid);
    setMessage(null);
    try {
      const res = await api.post('/admin/approve-user', { targetUid, months: 6 });
      setMessage({
        type: 'success',
        text: `User approved successfully! Expiry set to ${res.expiryIsoDate ? new Date(res.expiryIsoDate).toLocaleDateString() : '6 Months'}.`
      });
      await fetchUsers();
    } catch (error) {
      console.error('Error approving user:', error);
      setMessage({ type: 'error', text: error.message || 'Failed to approve user.' });
    } finally {
      setApprovingUid(null);
    }
  };

  const formatExpiry = (expiryDate) => {
    if (!expiryDate) return 'N/A';
    let ms = 0;
    if (typeof expiryDate === 'number') ms = expiryDate;
    else if (expiryDate.toMillis) ms = expiryDate.toMillis();
    else ms = new Date(expiryDate).getTime();
    if (!ms) return 'N/A';
    return new Date(ms).toLocaleDateString();
  };

  return (
    <main className="py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8">
        {/* Header */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-black/10 dark:border-white/10 pb-6">
          <div>
            <div className="flex items-center gap-2 text-brand-accent text-xs font-mono font-semibold mb-1">
              <ShieldCheck className="w-4 h-4 text-brand-accent" />
              <span>PLATFORM ADMIN CONSOLE</span>
            </div>
            <h1 className="text-3xl font-black text-brand-headingLight dark:text-brand-headingDark tracking-tight">
              User &amp; Subscription Management
            </h1>
            <p className="text-xs text-brand-textLight dark:text-brand-textDark mt-1">
              Review registered users and approve 6-month SaaS access passes.
            </p>
          </div>

          <button
            onClick={fetchUsers}
            disabled={loading}
            className="px-4 py-2 rounded-xl font-bold text-xs bg-brand-cardLight dark:bg-brand-cardDark border border-black/10 dark:border-white/10 text-brand-headingLight dark:text-brand-headingDark hover:border-brand-accent/50 transition-all flex items-center gap-2"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-brand-accent ${loading ? 'animate-spin' : ''}`} />
            <span>Refresh List</span>
          </button>
        </div>

        {/* Status Message */}
        {message && (
          <div className={`p-4 rounded-xl text-xs font-semibold flex items-center gap-2 border ${
            message.type === 'success'
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-400'
          }`}>
            {message.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
            <span>{message.text}</span>
          </div>
        )}

        {/* Users Table Card */}
        <div className="bg-brand-cardLight dark:bg-brand-cardDark border border-black/5 dark:border-white/10 rounded-2xl p-6 shadow-sm space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-lg font-bold text-brand-headingLight dark:text-brand-headingDark">
              Registered Accounts ({users.length})
            </h2>
          </div>

          {loading ? (
            <div className="py-12 text-center text-xs font-mono text-brand-textLight dark:text-brand-textDark animate-pulse">
              Loading registered users...
            </div>
          ) : users.length === 0 ? (
            <div className="py-12 text-center text-xs font-mono text-brand-textLight dark:text-brand-textDark">
              No registered users found.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs font-mono">
                <thead>
                  <tr className="border-b border-black/10 dark:border-white/10 text-brand-textLight dark:text-brand-textDark uppercase">
                    <th className="pb-3">User Email / UID</th>
                    <th className="pb-3">Status</th>
                    <th className="pb-3">Subscription Expiry</th>
                    <th className="pb-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-black/5 dark:divide-white/5">
                  {users.map((u) => {
                    const isPending = !u.status || u.status === 'pending';
                    const isActive = u.status === 'active';
                    return (
                      <tr key={u.uid}>
                        <td className="py-4">
                          <div className="font-bold text-brand-headingLight dark:text-brand-headingDark">
                            {u.email || 'No Email'}
                          </div>
                          <div className="text-[10px] text-brand-textLight dark:text-brand-textDark opacity-75">
                            {u.uid}
                          </div>
                        </td>
                        <td className="py-4">
                          {isActive ? (
                            <span className="px-2.5 py-1 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 text-[10px] font-bold inline-flex items-center gap-1">
                              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
                              ACTIVE
                            </span>
                          ) : (
                            <span className="px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30 text-[10px] font-bold inline-flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              PENDING
                            </span>
                          )}
                        </td>
                        <td className="py-4">
                          <span className={isActive ? 'text-brand-accent font-semibold' : 'text-brand-textLight dark:text-brand-textDark'}>
                            {formatExpiry(u.subscription_expiry_date)}
                          </span>
                        </td>
                        <td className="py-4 text-right">
                          {isPending ? (
                            <button
                              onClick={() => handleApproveUser(u.uid)}
                              disabled={approvingUid === u.uid}
                              className="px-3 py-1.5 rounded-lg bg-brand-accent text-black font-bold hover:shadow-[0_0_12px_rgba(0,229,255,0.4)] transition-all inline-flex items-center gap-1 disabled:opacity-50"
                            >
                              <UserCheck className="w-3.5 h-3.5" />
                              <span>{approvingUid === u.uid ? 'Approving...' : 'Approve (6 Months)'}</span>
                            </button>
                          ) : (
                            <span className="text-[11px] text-emerald-400 font-semibold inline-flex items-center gap-1">
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              <span>Approved</span>
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
