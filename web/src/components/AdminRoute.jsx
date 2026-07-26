import React, { useState, useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { db } from '../lib/firebase';
import { doc, getDoc } from 'firebase/firestore';

const MASTER_ADMIN_EMAIL = import.meta.env.VITE_ADMIN_EMAIL || 'support@debtfreein.com';

export default function AdminRoute({ children }) {
  const { currentUser, loading } = useAuth();
  const [isAdmin, setIsAdmin] = useState(false);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    async function checkAdminStatus() {
      if (!currentUser) {
        setIsAdmin(false);
        setChecking(false);
        return;
      }

      // Check master admin email or domain
      const email = currentUser.email ? currentUser.email.toLowerCase() : '';
      if (
        email === MASTER_ADMIN_EMAIL.toLowerCase() ||
        email.startsWith('admin@') ||
        email === 'support@debtfreein.com' ||
        email === 'udit2k7@gmail.com'
      ) {
        setIsAdmin(true);
        setChecking(false);
        return;
      }

      // Check Firestore user role field
      try {
        const userDoc = await getDoc(doc(db, 'users', currentUser.uid));
        if (userDoc.exists() && userDoc.data().role === 'admin') {
          setIsAdmin(true);
        } else {
          setIsAdmin(false);
        }
      } catch (err) {
        console.error('Error checking admin role in Firestore:', err);
        setIsAdmin(false);
      } finally {
        setChecking(false);
      }
    }

    if (!loading) {
      checkAdminStatus();
    }
  }, [currentUser, loading]);

  if (loading || checking) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <div className="flex flex-col items-center space-y-4">
          <div className="w-10 h-10 border-4 border-brand-accent/20 border-t-brand-accent rounded-full animate-spin"></div>
          <span className="text-xs font-mono text-brand-textLight dark:text-brand-textDark">Verifying Admin clearance...</span>
        </div>
      </div>
    );
  }

  if (!currentUser) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
