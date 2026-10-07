// ==========================================
// CAMPUS LIFE: 9JA UNI SIMULATOR MASTER ENGINE
// Full Story Progression (100L to 400L), Real Mapped Locations,
// Housing & Rent, 6-Stat Needs, and Campus Encounters
// ==========================================

class SoundFX {
  constructor() { this.ctx = null; }
  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      this.ctx = new AudioCtx();
    }
  }
  playCash() {
    this.init();
    if (!this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'triangle';
    osc.frequency.setValueAtTime(800, now);
    osc.frequency.exponentialRampToValueAtTime(1400, now + 0.15);
    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.25);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.25);
  }
  playNotification() {
    this.init();
    if (!this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(600, now);
    osc.frequency.setValueAtTime(950, now + 0.08);
    gain.gain.setValueAtTime(0.15, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.2);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.2);
  }
  playAlert() {
    this.init();
    if (!this.ctx) return;
    const now = this.ctx.currentTime;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(320, now);
    osc.frequency.linearRampToValueAtTime(180, now + 0.3);
    gain.gain.setValueAtTime(0.2, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start(now);
    osc.stop(now + 0.3);
  }
}

const sfx = new SoundFX();

// Real Mapped University Landmarks
const UNIVERSITIES = {
  unilorin: {
    name: "University of Ilorin",
    short: "UNILORIN",
    motto: "Better By Far",
    city: "Ilorin, Kwara State",
    locations: [
      { id: "tanke", name: "Tanke Junction & Oke-Odo", tag: "Off-Campus Hub", icon: "🚕", desc: "Commercial hub packed with Keke queues, Tanke Amala joints, supermarkets, and morning hold-up." },
      { id: "sanrab", name: "Sanrab Hostel Zone", tag: "Student Ghetto", icon: "🏘️", desc: "Prime off-campus living area. Generators humming, barbers, laundry lines, and late-night indomie aroma." },
      { id: "main_gate", name: "Main Gate & Security Post", tag: "Screening Point", icon: "🚧", desc: "Strict Dress Code Marshalls inspecting students' trousers, hairstyles, and ID cards before entry." },
      { id: "ps_walkway", name: "The Walkway & Flyover", tag: "Campus Catwalk", icon: "🚶", desc: "The legendary sheltered walkway. Fashion showcase, political flyers, and the long trek between faculties." },
      { id: "cbt_centre", name: "Permanent Site CBT Centre", tag: "Exam Arena", icon: "💻", desc: "Halls 1 to 5. Biometric thumbprint scanners, nervous students praying, and 8 AM test tension." },
      { id: "faculty_science", name: "Faculty of Science & NLT", tag: "Lecture Halls", icon: "🔬", desc: "500-capacity New Lecture Theatre. Crowded general courses, lab practicals, and 7 AM seat rushing." },
      { id: "senate", name: "Senate Building Complex", tag: "Admin Power", icon: "🏛️", desc: "Imposing administrative tower, Vice Chancellor's office, matriculation ground, and bursary." },
      { id: "dam", name: "Unilorin Dam & Biological Gardens", tag: "Scenic Dates", icon: "🌊", desc: "Cool serene waters, monkeys at the zoo, couple hideouts, and peaceful escape from toxic lecturers." },
      { id: "school_park", name: "School Park Terminal", tag: "Transit Hub", icon: "🚌", desc: "Coaster buses loading students to Post Office, Challenge, and Tanke. Intense rush-hour struggle." },
      { id: "clinic", name: "University Health Services", tag: "Clinic Bay", icon: "🏥", desc: "Campus hospital. Sick bay beds, long queues for medical clearance, and paracetamol prescriptions." }
    ]
  },
  unilag: {
    name: "University of Lagos",
    short: "UNILAG",
    motto: "In Deed and In Truth",
    city: "Akoka, Lagos State",
    locations: [
      { id: "akoka_gate", name: "Akoka Main Gate", tag: "City Border", icon: "🚖", desc: "Yellow Danfo buses from Yaba, campus cabs, security checks, and street food vendors." },
      { id: "new_hall", name: "New Hall Quadrangle", tag: "Campus Core", icon: "🌆", desc: "Heartbeat of Unilag nightlife. King Jaja, Moremi hall fashion catwalk, Shawarma and suya spots." },
      { id: "lagoon_front", name: "Lagoon Front & Senate", tag: "Romantic Breeze", icon: "🌊", desc: "Serene breeze from Lagos lagoon. Lovers on manicured lawns, study groups, iconic photo spot." },
      { id: "faculty_law", name: "Faculty of Law & Arts", tag: "Moot Court", icon: "⚖️", desc: "Corporate dress code, students in black & white, intellectual arguments, and faculty library." },
      { id: "cits", name: "CITS Tech Centre & Library", tag: "Tech & Reading", icon: "💻", desc: "Air-conditioned labs, free Wi-Fi, coding students, and quiet research desks." },
      { id: "amphi", name: "Main Auditorium & Amphitheatre", tag: "Event Stage", icon: "🎭", desc: "SUG election manifestos, comedy shows, campus concerts, fellowship night vigils." },
      { id: "engineering", name: "Faculty of Engineering Labs", tag: "Sleepless Hub", icon: "⚙️", desc: "Heavy machinery workshops, late-night CAD drawings, and exhausted engineering students." },
      { id: "health_centre", name: "Unilag Health Centre", tag: "Medical Bay", icon: "🏥", desc: "Student clinic near Jaja Hall for medical excuses, sick tests, and emergency relief." }
    ]
  }
};

// Player Housing Tiers
const HOUSING_TIERS = {
  squatter: {
    id: "squatter",
    name: "Hostel Squatter Bunk",
    rentCost: 0,
    roomType: "8 guys in 1 room (Village / New Hall)",
    desc: "Sleeping on a thin mattress on the floor or sharing a bunk with coursemate. Zero privacy.",
    perks: "Free accommodation. High risk of boiling ring raids and missing food."
  },
  school_hostel: {
    id: "school_hostel",
    name: "Official School Hostel Bedspace",
    rentCost: 45000,
    roomType: "4-Man Hostel Room",
    desc: "Balloted hostel room on campus. Close to 8 AM lectures, but water and light fluctuate.",
    perks: "Trek to class in 5 minutes. Shared bathroom and hall porter rules."
  },
  self_contain: {
    id: "self_contain",
    name: "Self-Contained Flat (Sanrab / Yaba)",
    rentCost: 180000,
    roomType: "Personal Room + Kitchen & Bathroom",
    desc: "Private off-campus sanctuary. You control your space, have your own generator and gas cooker.",
    perks: "Private bathroom (+Hygiene boost), host friends anytime, pay NEPA bill share."
  },
  luxury_flat: {
    id: "luxury_flat",
    name: "2-Bedroom Luxury Serviced Flat (Tanke Oke / Lekki)",
    rentCost: 650000,
    roomType: "Luxury Gated Estate Apartment",
    desc: "Prestige living for Nepo babies and top ballers. Solar inverter, 24/7 security, tiled parking.",
    perks: "100% comfort, massive +35 Clout boost, invite crushes for private dinner."
  }
};

// Vehicles & Mobility Tiers
const VEHICLE_TIERS = {
  trek: { id: "trek", name: "Leggedis Benz (Trekking in Sun)", cost: 0, speed: 1, energyCost: 20, clout: 0 },
  keke: { id: "keke", name: "Campus Keke & Shuttles", cost: 500, speed: 2, energyCost: 10, clout: 5 },
  okada: { id: "okada", name: "Commercial Motorcycle (Okada)", cost: 1500, speed: 3, energyCost: 5, clout: 10 },
  corolla: { id: "corolla", name: "Toyota Corolla 'Muscle'", cost: 2800000, speed: 4, energyCost: 0, clout: 35 },
  benz: { id: "benz", name: "Mercedes Benz C300 / Lexus ES350", cost: 8500000, speed: 5, energyCost: 0, clout: 60 }
};

// Story Chapters by Academic Level
const STORY_CHAPTERS = {
  "100L": {
    year: 1,
    title: "The Jambite Fresher Experience",
    milestoneEvent: "Matriculation Ceremony",
    desc: "Fresh from secondary school. Overwhelmed by lecture queues, 8 AM CBT halls, and hostel politics."
  },
  "200L": {
    year: 2,
    title: "Departmental Deep Waters",
    milestoneEvent: "First True Campus Love & Serious Tests",
    desc: "No longer a novice. Tough 3-unit courses, off-campus hostel search, and deciding your campus clique."
  },
  "300L": {
    year: 3,
    title: "SIWES Internship & SUG Politics",
    milestoneEvent: "6-Month Industrial Training & Student Politics",
    desc: "Internship grind in Lagos or Ilorin. Departmental elections, hustle boom or bust, and adulthood pressure."
  },
  "400L": {
    year: 4,
    title: "Final Year: Project, Sign-Out & Glory",
    milestoneEvent: "Final Year Project Defense & Convocation",
    desc: "Project supervisor wahala, white T-shirt signing, degree clearance, and step into the real world!"
  }
};

// Master Game State Manager
class GameState {
  constructor() {
    this.profile = {
      name: "Tunde",
      gender: "male",
      university: "unilorin", // unilorin or unilag
      spawnClass: "trench",   // nepo, trench, scholar
      department: "Computer Science",
      level: "100L",
      avatar: { skin: "caramel", hair: "fade", outfit: "street", accessory: "shades" }
    };

    // 6-Need Stat Engine
    this.stats = {
      energy: 85,    // 0 - 100
      hunger: 70,    // 0 - 100
      hygiene: 80,   // 0 - 100
      fun: 65,       // 0 - 100
      health: 95,    // 0 - 100
      cgpa: 3.45     // 0.00 - 5.00
    };

    this.finances = {
      cash: 12000,
      debt: 0,
      rentDueInDays: 30
    };

    this.housing = "squatter";
    this.vehicle = "trek";
    this.inventory = ["Student ID Card", "Bic Pen"];
    this.roomFurniture = [];

    this.time = {
      day: 1,
      hour: 8,
      minute: 0,
      semesterWeek: 1,
      level: "100L"
    };

    this.currentLocationId = "tanke";
    this.activityLog = [];
    this.activeEvent = null;

    this.chitterFeed = [
      { author: "@tanke_insider", time: "5m ago", text: "Hold-up from Tipper garage to Tanke junction is wicked today. Enter bike if you have 8 AM test! 😭" },
      { author: "@unilorin_crushes", time: "18m ago", text: "Who was that guy in black senator at CBT Centre Hall 2? Your perfume almost made me forget my matric number! 👀🔥" },
      { author: "@unilag_slayers", time: "42m ago", text: "Lagoon Front evening breeze with cold stone ice cream cures all 4-unit course depression. 🌊🍦" },
      { author: "@campus_gist9ja", time: "1h ago", text: "Hostel porter caught 14 boiling rings during midnight search in Block D. Be careful out there! ⚡💀" }
    ];

    this.loadGame();
  }

  initFromSpawn(spawnData) {
    this.profile = {
      name: spawnData.name || "Student",
      gender: spawnData.gender || "male",
      university: spawnData.university || "unilorin",
      spawnClass: spawnData.spawnClass || "trench",
      department: spawnData.department || "Computer Science",
      level: "100L",
      avatar: spawnData.avatar || { skin: "caramel", hair: "fade", outfit: "street", accessory: "shades" }
    };

    if (this.profile.spawnClass === "nepo") {
      this.stats = { energy: 100, hunger: 90, hygiene: 100, fun: 80, health: 100, cgpa: 3.20 };
      this.finances = { cash: 850000, debt: 0, rentDueInDays: 60 };
      this.housing = "luxury_flat";
      this.vehicle = "corolla";
      this.currentLocationId = this.profile.university === "unilorin" ? "tanke" : "new_hall";
      this.inventory = ["iPhone 16 Pro Max", "Designer Sunglasses", "Perfume Oil"];
      this.roomFurniture = ["Air Conditioner", "Solar Inverter", "PlayStation 5"];
    } else if (this.profile.spawnClass === "scholar") {
      this.stats = { energy: 85, hunger: 65, hygiene: 85, fun: 45, health: 95, cgpa: 4.88 };
      this.finances = { cash: 35000, debt: 0, rentDueInDays: 30 };
      this.housing = "school_hostel";
      this.vehicle = "trek";
      this.currentLocationId = this.profile.university === "unilorin" ? "ps_walkway" : "cits";
      this.inventory = ["Nokia Torch Phone", "10-Year Past Questions", "Reading Glasses"];
      this.roomFurniture = ["Rechargeable Reading Lamp", "Book Shelf"];
    } else {
      // Trench Grinder
      this.stats = { energy: 90, hunger: 50, hygiene: 70, fun: 40, health: 90, cgpa: 3.10 };
      this.finances = { cash: 4500, debt: 0, rentDueInDays: 14 };
      this.housing = "squatter";
      this.vehicle = "trek";
      this.currentLocationId = this.profile.university === "unilorin" ? "sanrab" : "new_hall";
      this.inventory = ["Cracked Android Phone", "Boiling Ring", "Plastic Bucket"];
      this.roomFurniture = ["Foam Mattress on Floor"];
    }

    this.time = { day: 1, hour: 7, minute: 30, semesterWeek: 1, level: "100L" };
    this.activityLog = [{ text: `Admitted into ${UNIVERSITIES[this.profile.university].name}! 100L Fresher journey begins.`, type: "positive" }];
    this.saveGame();
  }

  saveGame() {
    try {
      const data = {
        profile: this.profile,
        stats: this.stats,
        finances: this.finances,
        housing: this.housing,
        vehicle: this.vehicle,
        inventory: this.inventory,
        roomFurniture: this.roomFurniture,
        time: this.time,
        currentLocationId: this.currentLocationId,
        activityLog: this.activityLog.slice(0, 20)
      };
      localStorage.setItem("campus_life_save", JSON.stringify(data));
      if (window.cloudSync) {
        window.cloudSync.saveToCloud();
      }
    } catch (e) {}
  }

  loadGame() {
    try {
      const saved = localStorage.getItem("campus_life_save");
      if (saved) {
        const parsed = JSON.parse(saved);
        this.profile = parsed.profile || this.profile;
        this.stats = parsed.stats || this.stats;
        this.finances = parsed.finances || this.finances;
        this.housing = parsed.housing || this.housing;
        this.vehicle = parsed.vehicle || this.vehicle;
        this.inventory = parsed.inventory || this.inventory;
        this.roomFurniture = parsed.roomFurniture || this.roomFurniture;
        this.time = parsed.time || this.time;
        this.currentLocationId = parsed.currentLocationId || this.currentLocationId;
        this.activityLog = parsed.activityLog || this.activityLog;
        return true;
      }
    } catch (e) {}
    return false;
  }

  advanceTime(minutes) {
    this.time.minute += minutes;
    while (this.time.minute >= 60) {
      this.time.minute -= 60;
      this.time.hour += 1;
      
      // Hourly subtle needs decay
      this.stats.hunger = Math.max(0, this.stats.hunger - 3);
      this.stats.energy = Math.max(0, this.stats.energy - 2);
      this.stats.hygiene = Math.max(0, this.stats.hygiene - 2);
      this.stats.fun = Math.max(0, this.stats.fun - 2);

      // Health drops if starving or exhausted
      if (this.stats.hunger === 0 || this.stats.energy === 0) {
        this.stats.health = Math.max(0, this.stats.health - 5);
      }
    }

    if (this.time.hour >= 24) {
      this.time.hour -= 24;
      this.time.day += 1;
      this.finances.rentDueInDays = Math.max(0, this.finances.rentDueInDays - 1);

      if (this.time.day % 7 === 0) {
        this.time.semesterWeek += 1;
        this.checkSemesterMilestones();
      }
    }

    this.saveGame();
    this.rollRandomEventChance();
  }

  checkSemesterMilestones() {
    // 15 weeks per semester; at week 15, exams happen!
    if (this.time.semesterWeek === 15) {
      this.triggerSemesterExamWeek();
    }
  }

  triggerSemesterExamWeek() {
    let outcomeScore = (this.stats.cgpa * 0.7) + (this.stats.health * 0.15) + (this.stats.energy * 0.15);
    let delta = 0;
    if (outcomeScore > 65) {
      delta = 0.15;
      this.addLog(`🎉 Exam Week Concluded! Your dedication paid off. CGPA boosted (+0.15)!`, "positive");
    } else {
      delta = -0.20;
      this.addLog(`⚠️ Exam Week Disaster! Exhaustion and poor prep took a toll. CGPA dropped (-0.20).`, "negative");
    }
    this.stats.cgpa = Math.min(5.0, Math.max(0.5, +(this.stats.cgpa + delta).toFixed(2)));

    // Progress Academic Level
    if (this.time.level === "100L") {
      this.time.level = "200L";
      this.profile.level = "200L";
      this.addLog(`🎓 Congratulations! Passed 100L. You are now officially a 200 Level Student!`, "special");
    } else if (this.time.level === "200L") {
      this.time.level = "300L";
      this.profile.level = "300L";
      this.addLog(`🚀 200L Complete! Welcome to 300 Level. SIWES / IT Internship commences.`, "special");
    } else if (this.time.level === "300L") {
      this.time.level = "400L";
      this.profile.level = "400L";
      this.addLog(`🏆 Final Year! You made it to 400L. Final Project & Sign-Out awaits!`, "special");
    } else if (this.time.level === "400L") {
      this.triggerGraduationCeremony();
    }

    this.time.semesterWeek = 1;
    this.saveGame();
  }

  triggerGraduationCeremony() {
    let grade = "Third Class";
    if (this.stats.cgpa >= 4.5) grade = "First Class Honours (Distinction 🏆)";
    else if (this.stats.cgpa >= 3.5) grade = "Second Class Upper (2:1)";
    else if (this.stats.cgpa >= 2.4) grade = "Second Class Lower (2:2)";

    this.activeEvent = {
      title: "🎓 Convocation & Degree Sign-Out!",
      desc: `You have completed your 4-year degree at ${UNIVERSITIES[this.profile.university].name}! Your final CGPA is ${this.stats.cgpa.toFixed(2)} (${grade}). You survived the lectures, tests, landlord wahala, and Keke queues!`,
      options: [
        {
          text: "Wear your convocation gown, celebrate with parents and coursemates!",
          outcome: (p) => {
            return { msg: `Graduated with ${grade}! Your campus story is immortalized.`, type: "positive" };
          }
        }
      ]
    };
    sfx.playCash();
  }

  getTimePhase() {
    const h = this.time.hour;
    if (h >= 6 && h < 12) return { phase: "morning", label: "Morning" };
    if (h >= 12 && h < 17) return { phase: "afternoon", label: "Afternoon" };
    if (h >= 17 && h < 21) return { phase: "evening", label: "Evening" };
    return { phase: "night", label: "Night" };
  }

  addLog(text, type = "neutral") {
    this.activityLog.unshift({ text, type, time: this.formatTime() });
    if (this.activityLog.length > 30) this.activityLog.pop();
    this.saveGame();
  }

  formatTime() {
    const h = String(this.time.hour).padStart(2, "0");
    const m = String(this.time.minute).padStart(2, "0");
    return `${h}:${m}`;
  }

  formatMoney(num) {
    return "₦" + Number(num).toLocaleString();
  }

  rollRandomEventChance() {
    if (Math.random() < 0.28 && !this.activeEvent) {
      const eligible = CAMPUS_EVENTS.filter(e => !e.uni || e.uni === this.profile.university);
      const chosen = eligible[Math.floor(Math.random() * eligible.length)];
      this.activeEvent = chosen;
      sfx.playAlert();
    }
  }

  // Room & House Actions
  takeShower() {
    this.stats.hygiene = 100;
    this.stats.energy = Math.min(100, this.stats.energy + 10);
    this.advanceTime(25);
    this.addLog("Took a refreshing shower with cold water. Hygiene restored to 100% 🚿", "positive");
    sfx.playNotification();
  }

  sleepInRoom() {
    this.stats.energy = 100;
    this.stats.hunger = Math.max(0, this.stats.hunger - 20);
    this.time.hour = 7;
    this.time.minute = 0;
    this.time.day += 1;
    this.addLog("Slept peacefully on your bed. Woke up fresh at 7:00 AM (Energy 100% ⚡)", "positive");
    sfx.playNotification();
    this.saveGame();
  }

  cookConcoctionRice() {
    if (this.finances.cash < 800) {
      this.addLog("Not enough cash for rice, maggi, and pepper (Need ₦800)", "negative");
      return;
    }
    this.finances.cash -= 800;
    this.stats.hunger = Math.min(100, this.stats.hunger + 55);
    this.advanceTime(45);
    this.addLog("Cooked hot concoction rice in hostel pot. Hunger satisfied (+55 🍔)", "positive");
    sfx.playNotification();
  }

  // Campus Core Actions
  attendLecture() {
    if (this.stats.energy < 20) {
      this.addLog("Too weak to attend lecture! You fell asleep on the hostel bunk.", "negative");
      sfx.playAlert();
      return;
    }
    this.stats.energy -= 20;
    this.stats.hunger = Math.max(0, this.stats.hunger - 15);
    this.stats.hygiene = Math.max(0, this.stats.hygiene - 10);
    this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + 0.08).toFixed(2));
    this.advanceTime(120);
    this.addLog("Attended 2-hour lecture. Marked attendance and took class notes (+0.08 CGPA 📚)", "positive");
    sfx.playNotification();
  }

  readNightClass() {
    if (this.stats.energy < 35) {
      this.addLog("Not enough energy for TDB night reading! Take a nap first.", "negative");
      sfx.playAlert();
      return;
    }
    this.stats.energy -= 35;
    this.stats.hunger = Math.max(0, this.stats.hunger - 20);
    this.stats.hygiene = Math.max(0, this.stats.hygiene - 15);
    this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + 0.22).toFixed(2));
    this.advanceTime(300);
    this.addLog("Read TDB overnight at campus lecture hall. Solved past questions (+0.22 CGPA 📚)", "positive");
    sfx.playNotification();
  }

  visitClinic() {
    this.stats.health = 100;
    this.finances.cash = Math.max(0, this.finances.cash - 1500);
    this.advanceTime(90);
    this.addLog("Visited university clinic. Received treatment & rest. Health restored to 100% 💊", "positive");
    sfx.playNotification();
  }

  partyNight() {
    if (this.finances.cash < 8000) {
      this.addLog("Need at least ₦8,000 for club entry, drinks, and late-night cab!", "negative");
      sfx.playAlert();
      return;
    }
    this.finances.cash -= 8000;
    this.stats.energy = Math.max(0, this.stats.energy - 35);
    this.stats.fun = 100;
    this.advanceTime(240);
    this.addLog("Partied all night with campus ballers! Fun 100% 🎉", "positive");
    sfx.playCash();
  }

  // Travel between Locations
  travelTo(locationId) {
    const uni = UNIVERSITIES[this.profile.university];
    const loc = uni.locations.find(l => l.id === locationId);
    if (!loc) return;

    const v = VEHICLE_TIERS[this.vehicle] || VEHICLE_TIERS.trek;
    this.stats.energy = Math.max(0, this.stats.energy - v.energyCost);
    this.currentLocationId = locationId;
    this.advanceTime(20);
    this.addLog(`Arrived at ${loc.name} via ${v.name}.`, "positive");
    sfx.playNotification();
    this.saveGame();
  }

  // Housing Upgrade
  upgradeHousing(tierKey) {
    const tier = HOUSING_TIERS[tierKey];
    if (!tier) return;
    if (this.finances.cash < tier.rentCost) {
      this.addLog(`Insufficient funds for ${tier.name}! (Need ${this.formatMoney(tier.rentCost)})`, "negative");
      sfx.playAlert();
      return;
    }
    this.finances.cash -= tier.rentCost;
    this.housing = tierKey;
    this.finances.rentDueInDays = 60;
    this.addLog(`Moved into ${tier.name}! Your campus comfort has reached a new level.`, "special");
    sfx.playCash();
    this.saveGame();
  }

  buyShopItem(item) {
    if (this.finances.cash < item.cost) {
      this.addLog(`Insufficient funds for ${item.name}! (Cost: ${this.formatMoney(item.cost)})`, "negative");
      sfx.playAlert();
      return false;
    }

    this.finances.cash -= item.cost;
    if (item.hunger) this.stats.hunger = Math.min(100, this.stats.hunger + item.hunger);
    if (item.energy) this.stats.energy = Math.min(100, this.stats.energy + item.energy);
    if (item.hygiene) this.stats.hygiene = Math.min(100, this.stats.hygiene + item.hygiene);
    if (item.fun) this.stats.fun = Math.min(100, this.stats.fun + item.fun);
    if (item.cgpaBoost) this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + item.cgpaBoost).toFixed(2));

    if (item.category !== "food") {
      this.inventory.push(item.name);
    }

    this.addLog(`Purchased ${item.name} for ${this.formatMoney(item.cost)}.`, "positive");
    sfx.playCash();
    this.saveGame();
    return true;
  }

  doCampusHustle(job) {
    if (this.stats.energy < job.energyCost) {
      this.addLog(`Too exhausted to work ${job.name}! Need ${job.energyCost} Energy ⚡.`, "negative");
      sfx.playAlert();
      return;
    }

    this.stats.energy -= job.energyCost;
    this.advanceTime(90);

    let earned = job.payout;
    if (job.id === "crypto_futures") {
      earned = Math.floor(Math.random() * (job.payoutMax - job.payoutMin + 1)) + job.payoutMin;
    }

    this.finances.cash = Math.max(0, this.finances.cash + earned);
    if (earned >= 0) {
      this.addLog(`Worked ${job.name} and earned ${this.formatMoney(earned)}!`, "positive");
      sfx.playCash();
    } else {
      this.addLog(`Crypto trade liquidated! Lost ${this.formatMoney(Math.abs(earned))}. 📉`, "negative");
      sfx.playAlert();
    }
    this.saveGame();
  }

  postChitterTweet(tweetText) {
    if (!tweetText.trim()) return;
    this.stats.fun = Math.min(100, this.stats.fun + 8);
    this.chitterFeed.unshift({
      author: `@${this.profile.name.toLowerCase()}_${this.profile.university}`,
      time: "Just now",
      text: tweetText
    });
    this.addLog("Posted a viral take on Chitter! Gained fun and engagement 🔥", "positive");
    sfx.playNotification();
    if (window.cloudSync) {
      window.cloudSync.postLiveTweet(tweetText);
    }
    this.saveGame();
  }
}

// Global Game Engine Instance
window.game = new GameState();
window.HOUSING_TIERS = HOUSING_TIERS;
window.VEHICLE_TIERS = VEHICLE_TIERS;
window.STORY_CHAPTERS = STORY_CHAPTERS;
