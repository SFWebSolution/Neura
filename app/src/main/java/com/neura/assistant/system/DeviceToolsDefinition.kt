package com.neura.assistant.system

import com.neura.assistant.data.api.models.OpenAiFunction
import com.neura.assistant.data.api.models.OpenAiTool
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

object DeviceToolsDefinition {

    val allTools: List<OpenAiTool> by lazy {
        listOf(
            makePhoneCallTool,
            sendSmsTool,
            openAppTool,
            setAlarmTool,
            setTimerTool,
            toggleFlashlightTool,
            adjustVolumeTool,
            getWeatherTool,
            getDeviceStatusTool,
            webSearchTool,
            openUrlTool,
            openCameraTool,
            createCalendarEventTool
        )
    }

    private val makePhoneCallTool = OpenAiTool(
        function = OpenAiFunction(
            name = "make_phone_call",
            description = "Initiates a phone call to a specified contact name or direct phone number.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("contact_name") {
                        put("type", "string")
                        put("description", "Name of the person to call (e.g. 'Mom', 'John Doe')")
                    }
                    putJsonObject("phone_number") {
                        put("type", "string")
                        put("description", "Direct phone number if provided (e.g. '+1234567890')")
                    }
                }
            }
        )
    )

    private val sendSmsTool = OpenAiTool(
        function = OpenAiFunction(
            name = "send_sms",
            description = "Prepares or sends an SMS text message to a contact or phone number.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("contact_or_number") {
                        put("type", "string")
                        put("description", "Contact name or phone number recipient")
                    }
                    putJsonObject("message") {
                        put("type", "string")
                        put("description", "The text message content to send")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("contact_or_number"))
                    add(kotlinx.serialization.json.JsonPrimitive("message"))
                }
            }
        )
    )

    private val openAppTool = OpenAiTool(
        function = OpenAiFunction(
            name = "open_app",
            description = "Launches an installed application on the user's Android phone (e.g. Spotify, YouTube, WhatsApp, Settings, Camera, Maps, Instagram, Telegram, Netflix, Calculator, etc.)",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("app_name") {
                        put("type", "string")
                        put("description", "The common name of the application to open")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("app_name"))
                }
            }
        )
    )

    private val setAlarmTool = OpenAiTool(
        function = OpenAiFunction(
            name = "set_alarm",
            description = "Sets an alarm clock on the Android device for a specific hour and minute.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("hour") {
                        put("type", "integer")
                        put("description", "Hour of the alarm in 24-hour format (0 to 23)")
                    }
                    putJsonObject("minute") {
                        put("type", "integer")
                        put("description", "Minute of the alarm (0 to 59)")
                    }
                    putJsonObject("label") {
                        put("type", "string")
                        put("description", "Optional label or description for the alarm (e.g. 'Morning workout')")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("hour"))
                    add(kotlinx.serialization.json.JsonPrimitive("minute"))
                }
            }
        )
    )

    private val setTimerTool = OpenAiTool(
        function = OpenAiFunction(
            name = "set_timer",
            description = "Sets a countdown timer on the Android device.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("length_seconds") {
                        put("type", "integer")
                        put("description", "Length of the timer in total seconds")
                    }
                    putJsonObject("label") {
                        put("type", "string")
                        put("description", "Optional label for the timer (e.g. 'Pasta timer', 'Rest')")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("length_seconds"))
                }
            }
        )
    )

    private val toggleFlashlightTool = OpenAiTool(
        function = OpenAiFunction(
            name = "toggle_flashlight",
            description = "Turns the device flashlight / torch ON or OFF.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("state") {
                        put("type", "boolean")
                        put("description", "true to turn flashlight ON, false to turn OFF")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("state"))
                }
            }
        )
    )

    private val adjustVolumeTool = OpenAiTool(
        function = OpenAiFunction(
            name = "adjust_volume",
            description = "Controls media, ring, or alarm volume levels, or mutes/unmutes the device.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("action") {
                        put("type", "string")
                        put("description", "Action to perform: 'up', 'down', 'mute', 'unmute', 'max', 'set'")
                    }
                    putJsonObject("percentage") {
                        put("type", "integer")
                        put("description", "Volume percentage between 0 and 100 when action is 'set'")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("action"))
                }
            }
        )
    )

    private val getWeatherTool = OpenAiTool(
        function = OpenAiFunction(
            name = "get_weather",
            description = "Retrieves live weather data and forecast for a specific city or current location.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("location") {
                        put("type", "string")
                        put("description", "City name or location (e.g. 'London', 'New York', 'Tokyo', 'Lagos')")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("location"))
                }
            }
        )
    )

    private val getDeviceStatusTool = OpenAiTool(
        function = OpenAiFunction(
            name = "get_device_status",
            description = "Retrieves current phone telemetry: battery percentage, charging state, network connectivity, and time.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {}
            }
        )
    )

    private val webSearchTool = OpenAiTool(
        function = OpenAiFunction(
            name = "web_search",
            description = "Opens a web search query in the browser for up-to-date information.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("query") {
                        put("type", "string")
                        put("description", "The search query keywords")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("query"))
                }
            }
        )
    )

    private val openUrlTool = OpenAiTool(
        function = OpenAiFunction(
            name = "open_url",
            description = "Opens a specific web URL in the browser.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("url") {
                        put("type", "string")
                        put("description", "The URL to open (e.g. 'https://wikipedia.org')")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("url"))
                }
            }
        )
    )

    private val openCameraTool = OpenAiTool(
        function = OpenAiFunction(
            name = "open_camera",
            description = "Opens the device camera to take a photo or selfie.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("front_facing") {
                        put("type", "boolean")
                        put("description", "true for front selfie camera, false for rear camera")
                    }
                }
            }
        )
    )

    private val createCalendarEventTool = OpenAiTool(
        function = OpenAiFunction(
            name = "create_calendar_event",
            description = "Adds an event to the device calendar.",
            parameters = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("title") {
                        put("type", "string")
                        put("description", "Event title or summary")
                    }
                    putJsonObject("description") {
                        put("type", "string")
                        put("description", "Detailed notes for the event")
                    }
                    putJsonObject("start_time_iso") {
                        put("type", "string")
                        put("description", "Start time in ISO format or relative description")
                    }
                }
                putJsonArray("required") {
                    add(kotlinx.serialization.json.JsonPrimitive("title"))
                }
            }
        )
    )
}
