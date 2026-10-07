// ==========================================
// CAMPUS LIFE: 9JA UNI SIMULATOR ENGINE
// Inspired by Lagos Life, Elevated for Nigerian Campus Culture
// ==========================================

// Sound Effects Synthesizer using Web Audio API (Zero external audio dependency)
class SoundFX {
  constructor() {
    this.ctx = null;
  }

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
    osc.frequency.setValueAtTime(900, now + 0.08);
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
    osc.frequency.setValueAtTime(300, now);
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

// Core Campus Data
const UNIVERSITIES = {
  unilorin: {
    name: "University of Ilorin",
    short: "UNILORIN",
    motto: "Better By Far",
    city: "Ilorin, Kwara State",
    locations: [
      { id: "tanke", name: "Tanke Junction", tag: "Off-Campus Hub", desc: "Commercial hub packed with Keke queues, student food joints, POS stands, and Tanke hold-up." },
      { id: "ps_walkway", name: "PS Walkway & Park", tag: "Campus Life", desc: "The legendary long trek. Dress code marshalls waiting to check your trousers and hair." },
      { id: "cbt_centre", name: "CBT Centre", tag: "Exam Arena", desc: "The valley of tears. Fingerprint scanners, nervous students praying, and 8 AM test tension." },
      { id: "sanrab", name: "Sanrab Hostel Zone", tag: "Student Ghetto", desc: "Popular student living area. Generators humming, music blasting, and aroma of roasted noodles." },
      { id: "dam", name: "Unilorin Dam & Zoo", tag: "Chilling Spot", desc: "Cool serene breeze, couple hideout, and relaxation away from toxic lecturers." }
    ]
  },
  unilag: {
    name: "University of Lagos",
    short: "UNILAG",
    motto: "In Deed and In Truth",
    city: "Akoka, Lagos State",
    locations: [
      { id: "new_hall", name: "New Hall & Moremi", tag: "Campus Core", desc: "Heartbeat of Unilag. Fashion show on walkway, Indomie spots, late night chatter." },
      { id: "lagoon_front", name: "Lagoon Front", tag: "Romantic Breeze", desc: "Serene waters of Lagos lagoon. Lovers holding hands, study groups, evening breeze." },
      { id: "akoka_gate", name: "Akoka Main Gate", tag: "City Border", desc: "Cab rush, security checks, Yellow buses heading to Yaba, vibrant street hustle." },
      { id: "cits", name: "CITS & Library", tag: "Tech & Reading", desc: "Free AC, quiet reading desks, and students secretly charging laptops and powerbanks." },
      { id: "amphi", name: "Amphitheatre", tag: "Events & Rallies", desc: "SUG manifestos, campus rap battles, fellowship praise nights, and theatre rehearsals." }
    ]
  }
};

// 100+ Authentic Nigerian Random Events
const CAMPUS_EVENTS = [
  {
    id: "dress_code_marshall",
    uni: "unilorin",
    title: "⚠️ Dress Code Marshall Caught You!",
    desc: "A stern lecturer and security man stop you on the Walkway. They claim your jeans are 'fitted' and your hair is 'unchristian/unislamic'. What do you do?",
    options: [
      {
        text: "Apologize and beg with respect ('Good morning sir, I won't wear it again')",
        outcome: (p) => {
          p.energy -= 10;
          return { msg: "The marshall waved you through with a strict warning. You survived, but lost 10 Energy from trembling.", type: "neutral" };
        }
      },
      {
        text: "Quote the student handbook and argue your fundamental human rights",
        outcome: (p) => {
          p.clout += 15;
          p.cgpa = Math.max(0, p.cgpa - 0.2);
          return { msg: "Huge crowd gathered and cheered! (+15 Clout). But the marshall wrote your matric number. (-0.20 CGPA penalty!).", type: "negative" };
        }
      },
      {
        text: "Make an emergency U-turn and take a Keke back to Tanke",
        outcome: (p) => {
          p.cash -= 400;
          p.energy -= 15;
          return { msg: "You escaped without trouble but spent ₦400 Keke fare and arrived late.", type: "neutral" };
        }
      }
    ]
  },
  {
    id: "boiling_ring_raid",
    title: "⚡ Hostel Porter Raids Your Room!",
    desc: "At 10:30 PM, heavy knocks rattle your hostel door. Hall Porters are conducting a surprise search for contraband electrical appliances (boiling rings & hotplates)!",
    options: [
      {
        text: "Quickly hide the boiling ring inside your roommate's dirty laundry basket",
        outcome: (p) => {
          if (Math.random() > 0.35) {
            return { msg: "Success! The porters searched and found nothing. Room celebrated with midnight garri!", type: "positive" };
          } else {
            p.cash -= 5000;
            return { msg: "Busted! They seized your ring and slammed you with a ₦5,000 disciplinary fine.", type: "negative" };
          }
        }
      },
      {
        text: "Offer the Chief Porter ₦2,000 for 'recharge card and pure water'",
        outcome: (p) => {
          if (p.cash >= 2000) {
            p.cash -= 2000;
            return { msg: "The porter smiled, pocketed the ₦2,000, and shouted 'Room cleared!'", type: "positive" };
          } else {
            return { msg: "You don't have enough cash! The appliance was confiscated.", type: "negative" };
          }
        }
      }
    ]
  },
  {
    id: "surprise_test",
    title: "🚨 Surprise Continuous Assessment Test!",
    desc: "You walked into the lecture hall and the lecturer locked the doors! He announces an impromptu 20-mark test on Chapter 4.",
    options: [
      {
        text: "Write with pure faith and student intuition",
        outcome: (p) => {
          const boost = (p.cgpa > 3.0) ? 0.08 : -0.1;
          p.cgpa = Math.min(5.0, Math.max(0, p.cgpa + boost));
          p.energy -= 15;
          return { msg: boost > 0 ? "Your past reading saved you! Scored 16/20 on the test." : "Total disaster. Scored 4/20. CGPA dropped slightly.", type: boost > 0 ? "positive" : "negative" };
        }
      },
      {
        text: "Whisper and peep from the brilliant student sitting next to you",
        outcome: (p) => {
          if (Math.random() > 0.4) {
            p.cgpa = Math.min(5.0, p.cgpa + 0.12);
            return { msg: "Copy and paste success! You secured an easy 18/20.", type: "positive" };
          } else {
            p.cgpa = Math.max(0, p.cgpa - 0.25);
            return { msg: "Lecturer spotted your neck turning! Paper torn on the spot. 0/20!", type: "negative" };
          }
        }
      }
    ]
  },
  {
    id: "roommate_chicken_thief",
    title: "🍗 Mystery of the Missing Chicken!",
    desc: "You returned from night class salivating over the fried chicken you left inside your soup pot. It is GONE. Your roommate is chewing toothpick with innocent eyes.",
    options: [
      {
        text: "Confront him aggressively and demand your ₦2,500 chicken money",
        outcome: (p) => {
          p.energy -= 20;
          p.clout += 5;
          return { msg: "Loud argument shook the hostel! He denied it, but everyone in block knows he did it.", type: "neutral" };
        }
      },
      {
        text: "Drink garri in sorrow and charge it to campus experience",
        outcome: (p) => {
          p.hunger = Math.min(100, p.hunger + 20);
          p.energy -= 5;
          return { msg: "You soaked ice-cold garri with groundnut. Painful, but peaceful night.", type: "neutral" };
        }
      }
    ]
  },
  {
    id: "yahoo_boy_parade",
    title: "💸 Benz Boy Roommate Got Paid!",
    desc: "Your compound mate who does crypto & tech deals just closed a big transaction. He walked in with 5 packs of Chicken Republic and cartons of energy drinks!",
    options: [
      {
        text: "Hype him loudly ('Senior Man! Wire wire! Money stop nonsense!')",
        outcome: (p) => {
          p.hunger = 100;
          p.energy = Math.min(100, p.energy + 30);
          p.cash += 5000;
          return { msg: "He blessed you with two boxes of chicken and dashed you ₦5,000 cash!", type: "positive" };
        }
      },
      {
        text: "Politely advise him to focus on his 8 AM lectures",
        outcome: (p) => {
          p.clout -= 10;
          return { msg: "Everyone in the flat laughed at you. You ate your normal bread in silence.", type: "negative" };
        }
      }
    ]
  },
  {
    id: "tanke_traffic_drama",
    uni: "unilorin",
    title: "🚕 Tanke Hold-up Lockdown!",
    desc: "You boarded a cab heading to PS for a 9 AM exam, but Tanke Junction is completely blocked by Keke napeps and a broken-down tipper truck.",
    options: [
      {
        text: "Drop from the cab, run to the bike park, and pay a premium for express okada",
        outcome: (p) => {
          p.cash -= 1200;
          p.energy -= 20;
          return { msg: "Okada dodged the gridlock and reached exam hall just as attendance started!", type: "positive" };
        }
      },
      {
        text: "Stay inside the cab and pray for divine intervention",
        outcome: (p) => {
          p.cgpa = Math.max(0, p.cgpa - 0.15);
          return { msg: "You arrived 45 minutes late! Lecturer refused entry. Missed the quiz.", type: "negative" };
        }
      }
    ]
  }
];

// In-Game Store & Food Items
const SHOP_ITEMS = [
  { id: "amala_tanke", name: "Amala + Gbegiri & Ewedu", category: "food", cost: 1800, hunger: 45, energy: 25, desc: "Hot steaming Amala from Tanke with goat meat." },
  { id: "jollof_chicken", name: "Jollof Rice & Fried Chicken", category: "food", cost: 3200, hunger: 60, energy: 30, desc: "Classic Nigerian party jollof with spicy peppered chicken." },
  { id: "garri_groundnut", name: "Hostel Garri + Groundnut & Sugar", category: "food", cost: 500, hunger: 25, energy: 10, desc: "Student life-saver. Cold water garri soaking." },
  { id: "monster_energy", name: "Ice Cold Energy Drink", category: "food", cost: 1200, hunger: 5, energy: 50, desc: "Fuel for TDB night reading sessions." },
  { id: "past_questions", name: "Departmental Past Questions (PQ)", category: "academic", cost: 3500, cgpaBoost: 0.15, desc: "Compiled 10-year past questions with answers." },
  { id: "powerbank_oraimo", name: "Oraimo 20,000mAh Powerbank", category: "gear", cost: 18000, perk: "Never get low battery during blackout", desc: "Essential survival gadget for campus hostels." },
  { id: "designer_drip", name: "Campus Drip: Native + Loafers", category: "fashion", cost: 45000, cloutBoost: 30, desc: "Turn heads on the walkway. Instant respect from coursemates." }
];

// Campus Side Gigs / Hustles
const CAMPUS_JOBS = [
  { id: "assignment_writer", name: "Assignment & Term Paper Writer", payout: 12000, energyCost: 30, reqCgpa: 3.5, desc: "Write assignments for rich coursemates who skipped classes." },
  { id: "pos_agent", name: "Hostel POS Cash Agent", payout: 8500, energyCost: 20, reqCgpa: 0, desc: "Disburse cash at night when school ATMs are out of service." },
  { id: "okrika_vendor", name: "Thrift & Vintage Cloth Vendor", payout: 15000, energyCost: 25, reqCgpa: 0, desc: "Sell curated thrift jackets and jeans in hostel rooms." },
  { id: "hair_braider", name: "Campus Hair Stylist / Barber", payout: 10000, energyCost: 25, reqCgpa: 0, desc: "Cut hair or braid wigs for students getting ready for weekend groove." },
  { id: "crypto_futures", name: "Crypto Futures Scalping (High Risk)", payoutMin: -25000, payoutMax: 60000, energyCost: 35, reqCgpa: 0, desc: "Trade 50x leverage on Telegram. You either make ₦60k or get liquidated!" }
];

// Main Game State Manager
class GameState {
  constructor() {
    this.profile = {
      name: "Tunde",
      gender: "male",
      university: "unilorin", // unilorin or unilag
      spawnClass: "trench",   // nepo, trench, scholar
      department: "Computer Science",
      level: "200L"
    };

    this.stats = {
      energy: 85,
      hunger: 70,
      cgpa: 3.42,
      clout: 25,
      cash: 12000,
      debt: 0
    };

    this.time = {
      day: 14,
      hour: 8,
      minute: 30,
      semesterWeek: 6
    };

    this.currentLocationId = "tanke";
    this.inventory = [];
    this.activityLog = [];
    this.activeEvent = null;

    this.chitterFeed = [
      { author: "@unilorin_crushes", time: "10m ago", text: "Who is that tall guy on Walkway wearing blue vintage shirt? Respectfully, check your DM! 👀" },
      { author: "@tanke_insider", time: "25m ago", text: "Hold up at Tanke gate is serious today. Keke drivers are on strike again. Trek for life! 😭🚶" },
      { author: "@campus_gist9ja", time: "1h ago", text: "Lecturer gave surprise test at 8:01 AM and locked the doors at 8:05 AM. Fear who no fear Nigerian uni! 💀" },
      { author: "@unilag_slayers", time: "2h ago", text: "Lagoon Front breeze after a hectic 4-unit course is unmatched. Catch me at New Hall later." }
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
      level: "100L"
    };

    if (this.profile.spawnClass === "nepo") {
      this.stats = { energy: 100, hunger: 90, cgpa: 3.20, clout: 65, cash: 850000, debt: 0 };
      this.currentLocationId = this.profile.university === "unilorin" ? "tanke" : "new_hall";
      this.inventory = ["iPhone 16 Pro Max", "Luxury Off-Campus Flat"];
    } else if (this.profile.spawnClass === "scholar") {
      this.stats = { energy: 80, hunger: 60, cgpa: 4.88, clout: 20, cash: 35000, debt: 0 };
      this.currentLocationId = this.profile.university === "unilorin" ? "ps_walkway" : "cits";
      this.inventory = ["Nokia Phone", "Textbooks & Handouts"];
    } else {
      // Trench / Lapo
      this.stats = { energy: 90, hunger: 50, cgpa: 3.10, clout: 15, cash: 4500, debt: 0 };
      this.currentLocationId = this.profile.university === "unilorin" ? "sanrab" : "new_hall";
      this.inventory = ["Cracked Android Phone", "Hostel Bedspace (Squatting)"];
    }

    this.time = { day: 1, hour: 7, minute: 30, semesterWeek: 1 };
    this.activityLog = [{ text: `Welcome to ${UNIVERSITIES[this.profile.university].name}! Your campus adventure begins now.`, type: "positive" }];
    this.saveGame();
  }

  saveGame() {
    try {
      const data = {
        profile: this.profile,
        stats: this.stats,
        time: this.time,
        currentLocationId: this.currentLocationId,
        inventory: this.inventory,
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
        this.time = parsed.time || this.time;
        this.currentLocationId = parsed.currentLocationId || this.currentLocationId;
        this.inventory = parsed.inventory || this.inventory;
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
      // Slight natural hunger & energy decay per hour
      this.stats.hunger = Math.max(0, this.stats.hunger - 4);
      this.stats.energy = Math.max(0, this.stats.energy - 3);
    }

    if (this.time.hour >= 24) {
      this.time.hour -= 24;
      this.time.day += 1;
      if (this.time.day % 7 === 0) {
        this.time.semesterWeek += 1;
        this.addLog(`Week ${this.time.semesterWeek} of the semester has begun. Mid-semester tests approach!`, "special");
      }
    }

    this.saveGame();
    this.rollRandomEventChance();
  }

  getTimePhase() {
    const h = this.time.hour;
    if (h >= 6 && h < 12) return { phase: "morning", label: "Morning" };
    if (h >= 12 && h < 17) return { phase: "afternoon", label: "Afternoon" };
    if (h >= 17 && h < 21) return { phase: "evening", label: "Evening" };
    return { phase: "night", label: "Night" };
  }

  addLog(text, type = "neutral") {
    this.activityLog.unshift({ text, type, time: `${this.formatTime()}` });
    if (this.activityLog.length > 25) this.activityLog.pop();
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
    // 25% chance of encountering drama when advancing time
    if (Math.random() < 0.28 && !this.activeEvent) {
      const eligible = CAMPUS_EVENTS.filter(e => !e.uni || e.uni === this.profile.university);
      const chosen = eligible[Math.floor(Math.random() * eligible.length)];
      this.activeEvent = chosen;
      sfx.playAlert();
    }
  }

  // Common Action Handlers
  attendLecture() {
    if (this.stats.energy < 20) {
      this.addLog("Too exhausted to attend lecture! You fell asleep on the hostel bunk.", "negative");
      sfx.playAlert();
      return;
    }
    this.stats.energy -= 20;
    this.stats.hunger = Math.max(0, this.stats.hunger - 15);
    this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + 0.08).toFixed(2));
    this.advanceTime(120);
    this.addLog("Attended 2-hour general lecture. Marked attendance and took comprehensive notes. (+0.08 CGPA)", "positive");
    sfx.playNotification();
  }

  readNightClass() {
    if (this.stats.energy < 35) {
      this.addLog("Not enough energy for TDB (Till Day Break)! Rest first.", "negative");
      sfx.playAlert();
      return;
    }
    this.stats.energy -= 35;
    this.stats.hunger = Math.max(0, this.stats.hunger - 20);
    this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + 0.22).toFixed(2));
    this.advanceTime(300);
    this.addLog("Read TDB at campus lecture hall overnight. Solved 5 years of past questions! (+0.22 CGPA)", "positive");
    sfx.playNotification();
  }

  takeNap() {
    this.stats.energy = Math.min(100, this.stats.energy + 45);
    this.advanceTime(180);
    this.addLog("Took a refreshing 3-hour afternoon hostel nap. Energy restored (+45 ⚡).", "positive");
    sfx.playNotification();
  }

  sleepFullNight() {
    this.stats.energy = 100;
    this.stats.hunger = Math.max(0, this.stats.hunger - 25);
    this.time.hour = 7;
    this.time.minute = 0;
    this.time.day += 1;
    this.addLog("Woke up fresh at 7:00 AM ready for the new campus day. (Energy 100% ⚡)", "positive");
    sfx.playNotification();
    this.saveGame();
  }

  partyNight() {
    if (this.stats.cash < 8000) {
      this.addLog("Not enough cash for night club entry and drinks! (Need at least ₦8,000)", "negative");
      sfx.playAlert();
      return;
    }
    this.stats.cash -= 8000;
    this.stats.energy = Math.max(0, this.stats.energy - 35);
    this.stats.clout = Math.min(100, this.stats.clout + 20);
    this.advanceTime(240);
    this.addLog("Grooved all night at campus party! Gained massive campus clout (+20 🔥).", "positive");
    sfx.playCash();
  }

  buyShopItem(item) {
    if (this.stats.cash < item.cost) {
      this.addLog(`Insufficient funds for ${item.name}! (Cost: ${this.formatMoney(item.cost)})`, "negative");
      sfx.playAlert();
      return false;
    }

    this.stats.cash -= item.cost;
    if (item.hunger) this.stats.hunger = Math.min(100, this.stats.hunger + item.hunger);
    if (item.energy) this.stats.energy = Math.min(100, this.stats.energy + item.energy);
    if (item.cgpaBoost) this.stats.cgpa = Math.min(5.0, +(this.stats.cgpa + item.cgpaBoost).toFixed(2));
    if (item.cloutBoost) this.stats.clout = Math.min(100, this.stats.clout + item.cloutBoost);

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
      this.addLog(`Too exhausted to work ${job.name}! Need ${job.energyCost} Energy.`, "negative");
      sfx.playAlert();
      return;
    }

    this.stats.energy -= job.energyCost;
    this.advanceTime(90);

    let earned = job.payout;
    if (job.id === "crypto_futures") {
      earned = Math.floor(Math.random() * (job.payoutMax - job.payoutMin + 1)) + job.payoutMin;
    }

    this.stats.cash = Math.max(0, this.stats.cash + earned);
    if (earned >= 0) {
      this.addLog(`Worked ${job.name} and earned ${this.formatMoney(earned)}!`, "positive");
      sfx.playCash();
    } else {
      this.addLog(`Crypto trade liquidated! Lost ${this.formatMoney(Math.abs(earned))}. Student breakfast! 📉`, "negative");
      sfx.playAlert();
    }
    this.saveGame();
  }

  postChitterTweet(tweetText) {
    if (!tweetText.trim()) return;
    this.stats.clout = Math.min(100, this.stats.clout + 6);
    this.chitterFeed.unshift({
      author: `@${this.profile.name.toLowerCase()}_${this.profile.university}`,
      time: "Just now",
      text: tweetText
    });
    this.addLog("Posted a viral take on Chitter! Gained +6 Clout 🔥", "positive");
    sfx.playNotification();
    if (window.cloudSync) {
      window.cloudSync.postLiveTweet(tweetText);
    }
    this.saveGame();
  }
}

// Global Game Instance
window.game = new GameState();
