// ==========================================
// CAMPUS LIFE: CLOUD SYNC & MULTIPLAYER ENGINE
// ==========================================

import {
  auth,
  db,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  signInAnonymously,
  onAuthStateChanged,
  signOut,
  doc,
  setDoc,
  getDoc,
  collection,
  addDoc,
  onSnapshot,
  query,
  orderBy,
  limit,
  serverTimestamp
} from "./firebase-config.js";

class CloudSync {
  constructor() {
    this.currentUser = null;
    this.isOnline = false;
    this.unsubTweets = null;
    this.unsubLeaderboard = null;
  }

  init() {
    onAuthStateChanged(auth, async (user) => {
      this.currentUser = user;
      if (user) {
        this.isOnline = true;
        console.log("Logged into Campus Life as:", user.uid);
        
        // Load cloud save
        await this.loadCloudSave(user.uid);
        
        // Listen to live multiplayer Chitter feed
        this.listenToMultiplayerTweets();

        // Update UI
        if (window.UI) {
          window.UI.updateAuthBadge(user);
        }
      } else {
        this.isOnline = false;
        if (window.UI) {
          window.UI.updateAuthBadge(null);
        }
      }
    });
  }

  async signUp(email, password, displayName) {
    try {
      const cred = await createUserWithEmailAndPassword(auth, email, password);
      this.currentUser = cred.user;
      return { success: true, user: cred.user };
    } catch (err) {
      return { success: false, error: err.message };
    }
  }

  async login(email, password) {
    try {
      const cred = await signInWithEmailAndPassword(auth, email, password);
      this.currentUser = cred.user;
      return { success: true, user: cred.user };
    } catch (err) {
      return { success: false, error: err.message };
    }
  }

  async guestLogin() {
    try {
      const cred = await signInAnonymously(auth);
      this.currentUser = cred.user;
      return { success: true, user: cred.user };
    } catch (err) {
      return { success: false, error: err.message };
    }
  }

  async logout() {
    if (this.unsubTweets) this.unsubTweets();
    await signOut(auth);
    localStorage.removeItem("campus_life_save");
    location.reload();
  }

  async saveToCloud() {
    if (!this.currentUser || !window.game) return;
    try {
      const userRef = doc(db, "students", this.currentUser.uid);
      const data = {
        uid: this.currentUser.uid,
        email: this.currentUser.email || "guest@campuslife.ng",
        profile: window.game.profile,
        stats: window.game.stats,
        time: window.game.time,
        currentLocationId: window.game.currentLocationId,
        inventory: window.game.inventory,
        lastUpdated: serverTimestamp()
      };
      await setDoc(userRef, data, { merge: true });
    } catch (e) {
      console.warn("Cloud save error:", e);
    }
  }

  async loadCloudSave(uid) {
    try {
      const userRef = doc(db, "students", uid);
      const snap = await getDoc(userRef);
      if (snap.exists()) {
        const data = snap.data();
        if (data.profile && window.game) {
          window.game.profile = data.profile;
          window.game.stats = data.stats;
          window.game.time = data.time;
          window.game.currentLocationId = data.currentLocationId;
          window.game.inventory = data.inventory || [];
          window.game.saveGame();
          if (window.UI) window.UI.renderAll();
        }
      }
    } catch (e) {
      console.warn("Cloud load error:", e);
    }
  }

  listenToMultiplayerTweets() {
    try {
      const tweetsCol = collection(db, "campus_tweets");
      const q = query(tweetsCol, orderBy("createdAt", "desc"), limit(25));
      this.unsubTweets = onSnapshot(q, (snapshot) => {
        const liveTweets = [];
        snapshot.forEach((docSnap) => {
          liveTweets.push(docSnap.data());
        });
        if (liveTweets.length > 0 && window.game) {
          window.game.chitterFeed = liveTweets;
          if (window.UI) window.UI.renderChitterFeed();
        }
      });
    } catch (e) {
      console.warn("Live tweets error:", e);
    }
  }

  async postLiveTweet(tweetText) {
    if (!this.currentUser || !window.game) return;
    try {
      const tweetsCol = collection(db, "campus_tweets");
      await addDoc(tweetsCol, {
        author: `@${window.game.profile.name.toLowerCase()}_${window.game.profile.university}`,
        studentName: window.game.profile.name,
        university: window.game.profile.university,
        department: window.game.profile.department,
        text: tweetText,
        time: "Just now",
        createdAt: serverTimestamp()
      });
    } catch (e) {
      console.warn("Post tweet error:", e);
    }
  }
}

export const cloudSync = new CloudSync();
window.cloudSync = cloudSync;
