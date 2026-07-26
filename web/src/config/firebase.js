import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "YOUR_RESTRICTED_BROWSER_KEY_HERE",
  projectId: "debtfreein-db",
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
