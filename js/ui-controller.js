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
      fillHygiene: document.getElementById("fill-hygiene"),
      fillFun: document.getElementById("fill-fun"),
      fillHealth: document.getElementById("fill-health"),
      fillCgpa: document.getElementById("fill-cgpa"),
      valEnergy: document.getElementById("val-energy"),
      valHunger: document.getElementById("val-hunger"),
      valHygiene: document.getElementById("val-hygiene"),
      valFun: document.getElementById("val-fun"),
      valHealth: document.getElementById("val-health"),
      valCgpa: document.getElementById("val-cgpa"),
      walletAmount: document.getElementById("wallet-amount"),
      semesterBadge: document.getElementById("semester-badge"),
      vehicleBadge: document.getElementById("vehicle-badge"),
      locationName: document.getElementById("location-name"),
      locationTag: document.getElementById("location-tag"),
      locationDesc: document.getElementById("location-desc"),
      feedLogs: document.getElementById("feed-logs"),
      phoneModal: document.getElementById("phone-modal"),
      phoneFloatingBtn: document.getElementById("phone-floating-btn"),
      eventModal: document.getElementById("event-modal"),
      spawnScreen: document.getElementById("spawn-screen"),
      authModal: document.getElementById("auth-modal"),
      authBadge: document.getElementById("auth-badge"),
      campusMapView: document.getElementById("campus-map-view"),
      playerRoomView: document.getElementById("player-room-view"),
      campusActionsView: document.getElementById("campus-actions-view"),
      mapUniTitle: document.getElementById("map-uni-title"),
      mapCurrentLocBadge: document.getElementById("map-current-loc-badge"),
      mapPinsContainer: document.getElementById("map-pins-container"),
      houseTitle: document.getElementById("house-title"),
      houseRentStatus: document.getElementById("house-rent-status"),
      houseDesc: document.getElementById("house-desc")
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
    this.renderVisualMap();
    this.renderHousingRoom();
    this.renderActivityLogs();
    this.checkPendingEvent();
  },

  switchViewportView(viewName) {
    document.querySelectorAll(".view-tab-btn").forEach(b => b.classList.remove("active"));
    const activeBtn = document.getElementById(`tab-btn-${viewName}`);
    if (activeBtn) activeBtn.classList.add("active");

    if (this.dom.campusMapView) this.dom.campusMapView.style.display = viewName === "map" ? "flex" : "none";
    if (this.dom.playerRoomView) this.dom.playerRoomView.style.display = viewName === "room" ? "flex" : "none";
    if (this.dom.campusActionsView) this.dom.campusActionsView.style.display = viewName === "actions" ? "block" : "none";
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

    // Update 6 Core Stat Meters
    if (this.dom.fillEnergy) this.dom.fillEnergy.style.width = `${game.stats.energy}%`;
    if (this.dom.fillHunger) this.dom.fillHunger.style.width = `${game.stats.hunger}%`;
    if (this.dom.fillHygiene) this.dom.fillHygiene.style.width = `${game.stats.hygiene}%`;
    if (this.dom.fillFun) this.dom.fillFun.style.width = `${game.stats.fun}%`;
    if (this.dom.fillHealth) this.dom.fillHealth.style.width = `${game.stats.health}%`;
    if (this.dom.fillCgpa) this.dom.fillCgpa.style.width = `${(game.stats.cgpa / 5.0) * 100}%`;

    if (this.dom.valEnergy) this.dom.valEnergy.textContent = `${game.stats.energy}%`;
    if (this.dom.valHunger) this.dom.valHunger.textContent = `${game.stats.hunger}%`;
    if (this.dom.valHygiene) this.dom.valHygiene.textContent = `${game.stats.hygiene}%`;
    if (this.dom.valFun) this.dom.valFun.textContent = `${game.stats.fun}%`;
    if (this.dom.valHealth) this.dom.valHealth.textContent = `${game.stats.health}%`;
    if (this.dom.valCgpa) this.dom.valCgpa.textContent = `${game.stats.cgpa.toFixed(2)}`;

    // Wallet, Vehicle & Semester
    if (this.dom.walletAmount) this.dom.walletAmount.textContent = game.formatMoney(game.finances.cash || game.stats.cash || 0);
    if (this.dom.semesterBadge) this.dom.semesterBadge.textContent = `Week ${game.time.semesterWeek} • ${game.profile.level}`;

    const v = VEHICLE_TIERS[game.vehicle] || VEHICLE_TIERS.trek;
    if (this.dom.vehicleBadge) this.dom.vehicleBadge.innerHTML = `<span>🚲</span> ${v.name}`;
  },

  renderVisualMap() {
    if (!this.dom.mapPinsContainer) return;
    const uni = UNIVERSITIES[game.profile.university] || UNIVERSITIES.unilorin;
    if (this.dom.mapUniTitle) this.dom.mapUniTitle.innerHTML = `<span>🏛️</span> ${uni.name} Map`;

    const currLoc = uni.locations.find(l => l.id === game.currentLocationId) || uni.locations[0];
    if (this.dom.mapCurrentLocBadge) this.dom.mapCurrentLocBadge.textContent = `📍 ${currLoc.name}`;

    this.dom.mapPinsContainer.innerHTML = "";
    uni.locations.forEach(loc => {
      const card = document.createElement("div");
      const isCurrent = loc.id === game.currentLocationId;
      card.className = `map-landmark-card ${isCurrent ? "current-location" : ""}`;
      card.innerHTML = `
        <div class="landmark-top">
          <span class="landmark-icon">${loc.icon || "📍"}</span>
          <span class="landmark-badge">${isCurrent ? "HERE" : loc.tag}</span>
        </div>
        <div class="landmark-name">${loc.name}</div>
        <div class="landmark-vibe">${loc.desc}</div>
      `;

      card.onclick = () => {
        if (!isCurrent) {
          game.travelTo(loc.id);
          this.renderAll();
        }
      };

      this.dom.mapPinsContainer.appendChild(card);
    });
  },

  renderHousingRoom() {
    const tier = HOUSING_TIERS[game.housing] || HOUSING_TIERS.squatter;
    if (this.dom.houseTitle) this.dom.houseTitle.innerHTML = `<span>🏠</span> ${tier.name}`;
    if (this.dom.houseRentStatus) {
      if (tier.rentCost === 0) {
        this.dom.houseRentStatus.textContent = "Rent Free (Squatter)";
      } else {
        this.dom.houseRentStatus.textContent = `Rent: ${game.formatMoney(tier.rentCost)}/yr (${game.finances.rentDueInDays || 30}d left)`;
      }
    }
    if (this.dom.houseDesc) {
      this.dom.houseDesc.textContent = `${tier.desc} Perks: ${tier.perks}`;
    }
  },

  openHousingUpgradeSelector() {
    const keys = Object.keys(HOUSING_TIERS);
    const options = keys.map((k, i) => {
      const h = HOUSING_TIERS[k];
      return `${i + 1}. ${h.name} (${game.formatMoney(h.rentCost)}/yr) - ${h.roomType}`;
    }).join("\n\n");

    const choice = prompt(`Select New Accommodation to rent:\n\n${options}`);
    const idx = parseInt(choice) - 1;
    if (!isNaN(idx) && keys[idx]) {
      game.upgradeHousing(keys[idx]);
      this.renderAll();
    }
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
window.takeShower = () => { game.takeShower(); UI.renderAll(); };
window.sleepInRoom = () => { game.sleepInRoom(); UI.renderAll(); };
window.cookConcoctionRice = () => { game.cookConcoctionRice(); UI.renderAll(); };
window.visitClinic = () => { game.visitClinic(); UI.renderAll(); };
window.partyNight = () => { game.partyNight(); UI.renderAll(); };
window.openPhone = () => { UI.openPhoneModal(); };

document.addEventListener("DOMContentLoaded", () => {
  UI.init();
});
