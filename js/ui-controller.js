// ==========================================
// CAMPUS LIFE: UI CONTROLLER & VIEW BINDINGS
// ==========================================

const UI = {
  init() {
    this.bindDOM();
    this.bindEvents();

    // Check if character already exists or needs spawn
    const hasSave = localStorage.getItem("campus_life_save");
    if (!hasSave) {
      this.openSpawnModal();
    } else {
      this.renderAll();
    }

    // Initialize CloudSync if available
    if (window.cloudSync) {
      window.cloudSync.init();
    }

    // Live clock ticker
    setInterval(() => {
      game.advanceTime(1);
      this.updateHeaderAndStats();
    }, 4000); // 1 in-game minute every 4 seconds for a fun, lively pace
  },

  bindDOM() {
    this.dom = {
      appContainer: document.getElementById("app-container"),
      schoolTitle: document.getElementById("school-title"),
      schoolSubtitle: document.getElementById("school-subtitle"),
      clockTime: document.getElementById("clock-time"),
      clockPhase: document.getElementById("clock-phase"),
      fillEnergy: document.getElementById("fill-energy"),
      fillHunger: document.getElementById("fill-hunger"),
      fillCgpa: document.getElementById("fill-cgpa"),
      fillClout: document.getElementById("fill-clout"),
      valEnergy: document.getElementById("val-energy"),
      valHunger: document.getElementById("val-hunger"),
      valCgpa: document.getElementById("val-cgpa"),
      valClout: document.getElementById("val-clout"),
      walletAmount: document.getElementById("wallet-amount"),
      semesterBadge: document.getElementById("semester-badge"),
      locationName: document.getElementById("location-name"),
      locationTag: document.getElementById("location-tag"),
      locationDesc: document.getElementById("location-desc"),
      feedLogs: document.getElementById("feed-logs"),
      phoneModal: document.getElementById("phone-modal"),
      phoneFloatingBtn: document.getElementById("phone-floating-btn"),
      eventModal: document.getElementById("event-modal"),
      spawnScreen: document.getElementById("spawn-screen"),
      authModal: document.getElementById("auth-modal"),
      authBadge: document.getElementById("auth-badge")
    };
  },

  bindEvents() {
    // Dock Navigation buttons
    document.querySelectorAll(".dock-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        document.querySelectorAll(".dock-btn").forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        const tab = btn.dataset.tab;
        this.switchTab(tab);
      });
    });

    // Phone Floating Button Toggle
    if (this.dom.phoneFloatingBtn) {
      this.dom.phoneFloatingBtn.addEventListener("click", () => {
        this.openPhoneModal();
      });
    }

    // Phone Home Bar click closes phone
    const homeBar = document.getElementById("phone-home-bar");
    if (homeBar) {
      homeBar.addEventListener("click", () => {
        this.closePhoneModal();
      });
    }

    // Phone Back buttons
    document.querySelectorAll(".app-back-btn").forEach(btn => {
      btn.addEventListener("click", () => {
        this.showPhoneHomeScreen();
      });
    });

    // Phone App Grid icons
    document.querySelectorAll(".app-icon-item").forEach(item => {
      item.addEventListener("click", () => {
        const appName = item.dataset.app;
        this.openPhoneApp(appName);
      });
    });

    // Spawn Class Selection
    document.querySelectorAll(".class-card").forEach(card => {
      card.addEventListener("click", () => {
        document.querySelectorAll(".class-card").forEach(c => c.classList.remove("selected"));
        card.classList.add("selected");
        document.getElementById("selected-class").value = card.dataset.class;
      });
    });

    // Start Life button
    const startLifeBtn = document.getElementById("start-life-btn");
    if (startLifeBtn) {
      startLifeBtn.addEventListener("click", () => {
        this.handleSpawnSubmit();
      });
    }

    // Auth Button in header
    if (this.dom.authBadge) {
      this.dom.authBadge.addEventListener("click", () => {
        this.openAuthModal();
      });
    }
  },

  renderAll() {
    this.updateHeaderAndStats();
    this.renderLocation();
    this.renderActivityLogs();
    this.checkPendingEvent();
  },

  updateHeaderAndStats() {
    const uni = UNIVERSITIES[game.profile.university] || UNIVERSITIES.unilorin;
    this.dom.schoolTitle.textContent = `${uni.short} • ${game.profile.name}`;
    this.dom.schoolSubtitle.textContent = `${uni.motto} (${game.profile.level} ${game.profile.department})`;

    const phase = game.getTimePhase();
    this.dom.clockTime.textContent = game.formatTime();
    this.dom.clockPhase.textContent = `${phase.label} • Day ${game.time.day}`;

    // Update body theme class for dynamic sky lighting
    document.body.className = `time-${phase.phase}`;

    // Update Meters
    this.dom.fillEnergy.style.width = `${game.stats.energy}%`;
    this.dom.fillHunger.style.width = `${game.stats.hunger}%`;
    this.dom.fillCgpa.style.width = `${(game.stats.cgpa / 5.0) * 100}%`;
    this.dom.fillClout.style.width = `${game.stats.clout}%`;

    this.dom.valEnergy.textContent = `${game.stats.energy}%`;
    this.dom.valHunger.textContent = `${game.stats.hunger}%`;
    this.dom.valCgpa.textContent = `${game.stats.cgpa.toFixed(2)}`;
    this.dom.valClout.textContent = `${game.stats.clout}%`;

    // Wallet & Semester
    this.dom.walletAmount.textContent = game.formatMoney(game.stats.cash);
    this.dom.semesterBadge.textContent = `Week ${game.time.semesterWeek} • 1st Sem`;
  },

  updateAuthBadge(user) {
    if (!this.dom.authBadge) return;
    if (user) {
      const email = user.email ? user.email.split("@")[0] : "Guest Player";
      this.dom.authBadge.innerHTML = `<span style="color:#00e676;">☁️</span> ${email}`;
      this.dom.authBadge.title = "Cloud Save Synced via Firebase";
    } else {
      this.dom.authBadge.innerHTML = `<span style="color:#ffab00;">👤</span> Sign In`;
      this.dom.authBadge.title = "Click to Sign Up or Login";
    }
  },

  openAuthModal() {
    if (this.dom.authModal) {
      this.dom.authModal.classList.add("open");
    }
  },

  closeAuthModal() {
    if (this.dom.authModal) {
      this.dom.authModal.classList.remove("open");
    }
  },

  renderLocation() {
    const uni = UNIVERSITIES[game.profile.university] || UNIVERSITIES.unilorin;
    const loc = uni.locations.find(l => l.id === game.currentLocationId) || uni.locations[0];

    this.dom.locationName.innerHTML = `<span>📍</span> ${loc.name}`;
    this.dom.locationTag.textContent = loc.tag;
    this.dom.locationDesc.textContent = loc.desc;
  },

  renderActivityLogs() {
    if (!this.dom.feedLogs) return;
    this.dom.feedLogs.innerHTML = "";
    game.activityLog.slice(0, 10).forEach(log => {
      const div = document.createElement("div");
      div.className = `log-item event-${log.type}`;
      div.innerHTML = `<strong>[${log.time || game.formatTime()}]</strong> ${log.text}`;
      this.dom.feedLogs.appendChild(div);
    });
  },

  checkPendingEvent() {
    if (game.activeEvent) {
      this.renderEventModal(game.activeEvent);
    }
  },

  renderEventModal(event) {
    const modal = this.dom.eventModal;
    const title = document.getElementById("event-title");
    const desc = document.getElementById("event-desc");
    const optionsContainer = document.getElementById("event-options");

    title.textContent = event.title;
    desc.textContent = event.desc;
    optionsContainer.innerHTML = "";

    event.options.forEach((opt, idx) => {
      const btn = document.createElement("button");
      btn.className = "event-opt-btn";
      btn.textContent = `${idx + 1}. ${opt.text}`;
      btn.onclick = () => {
        const res = opt.outcome(game.stats);
        game.addLog(res.msg, res.type);
        game.activeEvent = null;
        modal.classList.remove("open");
        this.renderAll();
      };
      optionsContainer.appendChild(btn);
    });

    modal.classList.add("open");
  },

  // In-Game Phone Management
  openPhoneModal() {
    this.dom.phoneModal.classList.add("open");
    this.showPhoneHomeScreen();
    sfx.playNotification();

    // Update phone time
    document.getElementById("phone-time-clock").textContent = game.formatTime();
    document.getElementById("phone-date-clock").textContent = `Day ${game.time.day} • Semester Week ${game.time.semesterWeek}`;
  },

  closePhoneModal() {
    this.dom.phoneModal.classList.remove("open");
  },

  showPhoneHomeScreen() {
    document.querySelectorAll(".phone-app-window").forEach(w => w.classList.remove("active"));
    document.getElementById("phone-home-screen").style.display = "block";
  },

  openPhoneApp(appName) {
    document.getElementById("phone-home-screen").style.display = "none";
    document.querySelectorAll(".phone-app-window").forEach(w => w.classList.remove("active"));

    const targetApp = document.getElementById(`app-${appName}`);
    if (targetApp) {
      targetApp.classList.add("active");
      this.renderAppContent(appName);
    }
    sfx.playNotification();
  },

  renderAppContent(appName) {
    if (appName === "palmpay") {
      document.getElementById("palmpay-balance").textContent = game.formatMoney(game.stats.cash);
      document.getElementById("palmpay-debt").textContent = game.formatMoney(game.stats.debt);
    } else if (appName === "chowdeck") {
      this.renderFoodMenu();
    } else if (appName === "chitter") {
      this.renderChitterFeed();
    } else if (appName === "hustle") {
      this.renderHustleGigs();
    } else if (appName === "portal") {
      this.renderStudentPortal();
    }
  },

  renderFoodMenu() {
    const list = document.getElementById("chowdeck-food-list");
    list.innerHTML = "";
    SHOP_ITEMS.filter(item => item.category === "food").forEach(item => {
      const card = document.createElement("div");
      card.className = "meter-card";
      card.style.flexDirection = "row";
      card.style.justifyContent = "space-between";
      card.style.alignItems = "center";
      card.style.padding = "10px";

      card.innerHTML = `
        <div>
          <div style="font-size:13px; font-weight:800; color:#fff;">${item.name}</div>
          <div style="font-size:11px; color:var(--text-muted);">${item.desc}</div>
          <div style="font-size:12px; font-weight:800; color:var(--primary); margin-top:2px;">${game.formatMoney(item.cost)}</div>
        </div>
        <button class="action-btn" style="padding:6px 12px; border-radius:10px; background:var(--primary); color:#000; font-weight:800; border:none; cursor:pointer;">Order</button>
      `;

      card.querySelector("button").onclick = () => {
        if (game.buyShopItem(item)) {
          this.renderAll();
          document.getElementById("palmpay-balance").textContent = game.formatMoney(game.stats.cash);
        }
      };

      list.appendChild(card);
    });
  },

  renderChitterFeed() {
    const container = document.getElementById("chitter-posts-list");
    if (!container) return;
    container.innerHTML = "";
    game.chitterFeed.forEach(post => {
      const card = document.createElement("div");
      card.className = "meter-card";
      card.style.padding = "10px";
      card.style.background = "#141728";
      card.innerHTML = `
        <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
          <span style="font-size:12px; font-weight:800; color:var(--cyan);">${post.author}</span>
          <span style="font-size:10px; color:var(--text-muted);">${post.time || "Just now"}</span>
        </div>
        <div style="font-size:12.5px; color:#fff; line-height:1.4;">${post.text}</div>
      `;
      container.appendChild(card);
    });

    const sendBtn = document.getElementById("chitter-send-btn");
    const input = document.getElementById("chitter-input");
    if (sendBtn) {
      sendBtn.onclick = () => {
        if (input.value.trim()) {
          game.postChitterTweet(input.value.trim());
          input.value = "";
          this.renderChitterFeed();
          this.renderAll();
        }
      };
    }
  },

  renderHustleGigs() {
    const list = document.getElementById("hustle-jobs-list");
    list.innerHTML = "";
    CAMPUS_JOBS.forEach(job => {
      const card = document.createElement("div");
      card.className = "meter-card";
      card.style.padding = "12px";
      card.style.marginBottom = "8px";
      card.innerHTML = `
        <div style="font-size:13.5px; font-weight:800; color:#fff;">${job.name}</div>
        <div style="font-size:11px; color:var(--text-muted); margin:3px 0 6px;">${job.desc}</div>
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <span style="font-size:12px; color:var(--accent); font-weight:700;">Energy: -${job.energyCost}⚡</span>
          <button class="action-btn" style="padding:6px 14px; background:var(--accent); color:#000; font-weight:800; border:none; border-radius:10px; cursor:pointer;">Hustle</button>
        </div>
      `;

      card.querySelector("button").onclick = () => {
        game.doCampusHustle(job);
        this.renderAll();
      };

      list.appendChild(card);
    });
  },

  renderStudentPortal() {
    document.getElementById("portal-cgpa-score").textContent = game.stats.cgpa.toFixed(2);
    let standing = "First Class Honours (Distinction)";
    if (game.stats.cgpa < 4.5) standing = "Second Class Upper (2:1)";
    if (game.stats.cgpa < 3.5) standing = "Second Class Lower (2:2)";
    if (game.stats.cgpa < 2.4) standing = "Third Class (Spillover Risk!)";
    if (game.stats.cgpa < 1.5) standing = "Advised to Withdraw (Probation)";
    document.getElementById("portal-standing-text").textContent = standing;
  },

  // Tab Switcher on Dock
  switchTab(tab) {
    if (tab === "campus") {
      this.renderLocation();
    } else if (tab === "map") {
      this.openMapLocationSelector();
    } else if (tab === "hustle") {
      this.openPhoneModal();
      this.openPhoneApp("hustle");
    } else if (tab === "profile") {
      this.openPhoneModal();
      this.openPhoneApp("portal");
    }
  },

  openMapLocationSelector() {
    const uni = UNIVERSITIES[game.profile.university] || UNIVERSITIES.unilorin;
    const locNames = uni.locations.map(l => l.name);
    const chosen = prompt(`Choose Campus Location to visit:\n\n${locNames.map((n, i) => `${i + 1}. ${n}`).join("\n")}`);
    const index = parseInt(chosen) - 1;
    if (!isNaN(index) && uni.locations[index]) {
      game.currentLocationId = uni.locations[index].id;
      game.advanceTime(20);
      game.addLog(`Boarded a campus shuttle to ${uni.locations[index].name}.`, "positive");
      this.renderAll();
      sfx.playNotification();
    }
  },

  // Spawn Screen Handlers
  openSpawnModal() {
    this.dom.spawnScreen.style.display = "flex";
  },

  handleSpawnSubmit() {
    const name = document.getElementById("spawn-name").value.trim() || "Student";
    const gender = document.getElementById("spawn-gender").value;
    const university = document.getElementById("spawn-uni").value;
    const department = document.getElementById("spawn-dept").value;
    const spawnClass = document.getElementById("selected-class").value || "trench";

    game.initFromSpawn({
      name,
      gender,
      university,
      department,
      spawnClass
    });

    this.dom.spawnScreen.style.display = "none";
    this.renderAll();
    sfx.playCash();
  }
};

// Global hooks for direct button onclicks
window.UI = UI;
window.attendLecture = () => { game.attendLecture(); UI.renderAll(); };
window.readNightClass = () => { game.readNightClass(); UI.renderAll(); };
window.takeNap = () => { game.takeNap(); UI.renderAll(); };
window.sleepFullNight = () => { game.sleepFullNight(); UI.renderAll(); };
window.partyNight = () => { game.partyNight(); UI.renderAll(); };
window.openPhone = () => { UI.openPhoneModal(); };

document.addEventListener("DOMContentLoaded", () => {
  UI.init();
});
