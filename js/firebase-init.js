import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

const firebaseConfig = {
  apiKey: "YOUR_RESTRICTED_BROWSER_KEY_HERE", // ARCHITECT: Paste restricted key here
  projectId: "debtfreein-db",
};

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app);
