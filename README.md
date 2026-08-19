# 🌟 Neura: Next-Gen AI Voice Assistant for Android

**Neura** is a premier, intelligent AI voice assistant built natively for Android. Powered by OpenAI's state-of-the-art models (GPT-4o, Whisper STT, Neural TTS) and native Android hardware integrations, Neura delivers an unparalleled hands-free assistant experience.

---

## 🚀 Key Features

### 🎙️ 1. Complete Voice Interaction & Dynamic Audio Sphere
- **Neura Glowing Orb**: Fluid, pulsating neon audio visualizer responding dynamically to voice amplitude and AI speech synthesis.
- **Dual Recognition**: Fast local Android Speech Recognition with OpenAI Whisper fallback.
- **Expressive Speech**: Neural text-to-speech with configurable pitch, speed, and voice tones.

### 📱 2. Deep Native Android Device Control (Function Calling)
Neura uses OpenAI Tool Calling to autonomously perform native system tasks:
- 📞 **Phone & Contacts**: Place phone calls to contacts or numbers hands-free (`make_phone_call`).
- 💬 **SMS Messaging**: Draft and send text messages directly (`send_sms`).
- 🚀 **App Launcher**: Open any installed application dynamically (`open_app`).
- ⏰ **Alarms & Timers**: Schedule alarms and countdown timers (`set_alarm`, `set_timer`).
- 🔦 **Flashlight**: Toggle camera torch on/off (`toggle_flashlight`).
- 🔊 **Audio & Media**: Adjust media/ring volume and control music playback (`adjust_volume`).
- 🌦️ **Live Weather**: Instant weather forecasts and current conditions worldwide (`get_weather`).
- 🔋 **Device Telemetry**: Real-time battery %, charging status, and network information (`get_device_status`).
- 🌐 **Web Search**: Instant browser searches and web page launching (`web_search`, `open_url`).
- 📷 **Camera**: Instant photo capture launch (`open_camera`).
- 📅 **Calendar**: Quick event scheduling (`create_calendar_event`).

### ⚡ 3. System Invocations & Background Presence
- **Default Digital Assistant Integration**: Integrates directly with Android's `VoiceInteractionService` so holding the device power button or swiping up from bottom corners activates Neura instantly.
- **Floating Orb Bubble**: System overlay (`SYSTEM_ALERT_WINDOW`) that floats over any application for one-tap voice interaction anywhere.
- **Persistent Background Service**: Keeps the assistant ready with quick-listen notification shortcuts.
- **Quick Settings Tile**: Add the "Neura Voice" tile to your Android pull-down notification shade.

---

## 🎨 Technology Stack

- **UI Framework**: Jetpack Compose + Material 3 (Dark Neon Glassmorphism)
- **Language**: Kotlin 2.1
- **Architecture**: Clean Architecture / Repository Pattern with Kotlin Coroutines & StateFlow
- **AI Backend**: OpenAI API (GPT-4o, Whisper, Audio Speech TTS)
- **Data Persistence**: Android Jetpack DataStore Preferences
- **Weather API**: Open-Meteo REST API (Real-time meteorological telemetry)
- **Android Target**: Android 8.0 (API 26) up to Android 15 (API 35)

---

## 🛠️ Configuration & Settings

Neura comes pre-configured with OpenAI integration and features a dedicated Settings screen to:
- Customize or replace the OpenAI API key.
- Switch between AI models (`gpt-4o`, `gpt-4o-mini`).
- Toggle between Android system TTS and OpenAI Neural Audio TTS.
- Fine-tune voice pitch and speech rate.
- Toggle floating bubble and background foreground service.
- Open Android settings to set Neura as the device's Default Digital Assistant.

---

## 📦 Building & Testing

To build the APK in Android Studio or via Gradle CLI:
```bash
./gradlew assembleDebug
```
To run tests:
```bash
./gradlew test
```
