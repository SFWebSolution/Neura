package com.neura.assistant.system

import com.neura.assistant.data.api.models.GeminiFunctionDeclaration
import com.neura.assistant.data.api.models.GeminiToolWrapper
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

object DeviceToolsDefinition {

    val geminiToolWrapper: GeminiToolWrapper by lazy {
        GeminiToolWrapper(
            functionDeclarations = listOf(
                makePhoneCallDecl,
                sendSmsDecl,
                openAppDecl,
                setAlarmDecl,
                setTimerDecl,
                toggleFlashlightDecl,
                adjustVolumeDecl,
                getWeatherDecl,
                getDeviceStatusDecl,
                webSearchDecl,
                openUrlDecl,
                openCameraDecl,
                createCalendarEventDecl
            )
        )
    }

    private val makePhoneCallDecl = GeminiFunctionDeclaration(
        name = "make_phone_call",
        description = "Initiates a phone call to a specified contact name or direct phone number.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("contact_name") {
                    put("type", "STRING")
                    put("description", "Name of the person to call (e.g. 'Mom', 'John Doe')")
                }
                putJsonObject("phone_number") {
                    put("type", "STRING")
                    put("description", "Direct phone number if provided (e.g. '+1234567890')")
                }
            }
        }
    )

    private val sendSmsDecl = GeminiFunctionDeclaration(
        name = "send_sms",
        description = "Prepares or sends an SMS text message to a contact or phone number.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("contact_or_number") {
                    put("type", "STRING")
                    put("description", "Contact name or phone number recipient")
                }
                putJsonObject("message") {
                    put("type", "STRING")
                    put("description", "The text message content to send")
                }
            }
            putJsonArray("required") {
                add("contact_or_number")
                add("message")
            }
        }
    )

    private val openAppDecl = GeminiFunctionDeclaration(
        name = "open_app",
        description = "Launches an installed application on the user's Android phone (e.g. Spotify, YouTube, WhatsApp, Settings, Camera, Maps, Instagram, Telegram, Netflix, Calculator, etc.)",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("app_name") {
                    put("type", "STRING")
                    put("description", "The common name of the application to open")
                }
            }
            putJsonArray("required") {
                add("app_name")
            }
        }
    )

    private val setAlarmDecl = GeminiFunctionDeclaration(
        name = "set_alarm",
        description = "Sets an alarm clock on the Android device for a specific hour and minute.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("hour") {
                    put("type", "INTEGER")
                    put("description", "Hour of the alarm in 24-hour format (0 to 23)")
                }
                putJsonObject("minute") {
                    put("type", "INTEGER")
                    put("description", "Minute of the alarm (0 to 59)")
                }
                putJsonObject("label") {
                    put("type", "STRING")
                    put("description", "Optional label or description for the alarm (e.g. 'Morning workout')")
                }
            }
            putJsonArray("required") {
                add("hour")
                add("minute")
            }
        }
    )

    private val setTimerDecl = GeminiFunctionDeclaration(
        name = "set_timer",
        description = "Sets a countdown timer on the Android device.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("length_seconds") {
                    put("type", "INTEGER")
                    put("description", "Length of the timer in total seconds")
                }
                putJsonObject("label") {
                    put("type", "STRING")
                    put("description", "Optional label for the timer (e.g. 'Pasta timer', 'Rest')")
                }
            }
            putJsonArray("required") {
                add("length_seconds")
            }
        }
    )

    private val toggleFlashlightDecl = GeminiFunctionDeclaration(
        name = "toggle_flashlight",
        description = "Turns the device flashlight / torch ON or OFF.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("state") {
                    put("type", "BOOLEAN")
                    put("description", "true to turn flashlight ON, false to turn OFF")
                }
            }
            putJsonArray("required") {
                add("state")
            }
        }
    )

    private val adjustVolumeDecl = GeminiFunctionDeclaration(
        name = "adjust_volume",
        description = "Controls media, ring, or alarm volume levels, or mutes/unmutes the device.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("action") {
                    put("type", "STRING")
                    put("description", "Action to perform: 'up', 'down', 'mute', 'unmute', 'max', 'set'")
                }
                putJsonObject("percentage") {
                    put("type", "INTEGER")
                    put("description", "Volume percentage between 0 and 100 when action is 'set'")
                }
            }
            putJsonArray("required") {
                add("action")
            }
        }
    )

    private val getWeatherDecl = GeminiFunctionDeclaration(
        name = "get_weather",
        description = "Retrieves live weather data and forecast for a specific city or current location.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("location") {
                    put("type", "STRING")
                    put("description", "City name or location (e.g. 'London', 'New York', 'Tokyo', 'Lagos')")
                }
            }
            putJsonArray("required") {
                add("location")
            }
        }
    )

    private val getDeviceStatusDecl = GeminiFunctionDeclaration(
        name = "get_device_status",
        description = "Retrieves current phone telemetry: battery percentage, charging state, network connectivity, and time.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {}
        }
    )

    private val webSearchDecl = GeminiFunctionDeclaration(
        name = "web_search",
        description = "Opens a web search query in the browser for up-to-date information.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("query") {
                    put("type", "STRING")
                    put("description", "The search query keywords")
                }
            }
            putJsonArray("required") {
                add("query")
            }
        }
    )

    private val openUrlDecl = GeminiFunctionDeclaration(
        name = "open_url",
        description = "Opens a specific web URL in the browser.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("url") {
                    put("type", "STRING")
                    put("description", "The URL to open (e.g. 'https://wikipedia.org')")
                }
            }
            putJsonArray("required") {
                add("url")
            }
        }
    )

    private val openCameraDecl = GeminiFunctionDeclaration(
        name = "open_camera",
        description = "Opens the device camera to take a photo or selfie.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("front_facing") {
                    put("type", "BOOLEAN")
                    put("description", "true for front selfie camera, false for rear camera")
                }
            }
        }
    )

    private val createCalendarEventDecl = GeminiFunctionDeclaration(
        name = "create_calendar_event",
        description = "Adds an event to the device calendar.",
        parameters = buildJsonObject {
            put("type", "OBJECT")
            putJsonObject("properties") {
                putJsonObject("title") {
                    put("type", "STRING")
                    put("description", "Event title or summary")
                }
                putJsonObject("description") {
                    put("type", "STRING")
                    put("description", "Detailed notes for the event")
                }
                putJsonObject("start_time_iso") {
                    put("type", "STRING")
                    put("description", "Start time in ISO format or relative description")
                }
            }
            putJsonArray("required") {
                add("title")
            }
        }
    )
}
