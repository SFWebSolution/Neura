package com.neura.assistant.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.telephony.SmsManager
import com.neura.assistant.data.api.WeatherService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed class ActionResult {
    data class Success(val message: String, val cardData: ActionCardData? = null) : ActionResult()
    data class Error(val errorMessage: String) : ActionResult()
}

sealed class ActionCardData {
    data class WeatherCard(
        val location: String,
        val temp: Double,
        val condition: String,
        val humidity: Int,
        val windSpeed: Double,
        val isDay: Boolean
    ) : ActionCardData()

    data class CallCard(val contactOrNumber: String) : ActionCardData()
    data class AlarmCard(val hour: Int, val minute: Int, val label: String) : ActionCardData()
    data class TimerCard(val seconds: Int, val label: String) : ActionCardData()
    data class FlashlightCard(val isOn: Boolean) : ActionCardData()
    data class AppLaunchCard(val appName: String) : ActionCardData()
    data class TelemetryCard(val battery: Int, val isCharging: Boolean, val network: String, val time: String) : ActionCardData()
}

class DeviceActionExecutor(
    private val context: Context,
    private val flashlightController: FlashlightController = FlashlightController(context),
    private val volumeController: AudioVolumeController = AudioVolumeController(context),
    private val appLauncher: AppLauncherHelper = AppLauncherHelper(context),
    private val telemetryHelper: DeviceTelemetryHelper = DeviceTelemetryHelper(context),
    private val weatherService: WeatherService = WeatherService(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun executeTool(functionName: String, argumentsJson: String): ActionResult {
        return try {
            val args = if (argumentsJson.isNotBlank()) json.parseToJsonElement(argumentsJson).jsonObject else null

            when (functionName) {
                "make_phone_call" -> {
                    val contactName = args?.get("contact_name")?.jsonPrimitive?.content ?: ""
                    val phoneNumber = args?.get("phone_number")?.jsonPrimitive?.content ?: contactName
                    makePhoneCall(phoneNumber)
                }

                "send_sms" -> {
                    val recipient = args?.get("contact_or_number")?.jsonPrimitive?.content ?: ""
                    val message = args?.get("message")?.jsonPrimitive?.content ?: ""
                    sendSms(recipient, message)
                }

                "open_app" -> {
                    val appName = args?.get("app_name")?.jsonPrimitive?.content ?: ""
                    val result = appLauncher.launchAppByName(appName)
                    if (result.isSuccess) {
                        ActionResult.Success(result.getOrThrow(), ActionCardData.AppLaunchCard(appName))
                    } else {
                        ActionResult.Error(result.exceptionOrNull()?.message ?: "Failed to open app")
                    }
                }

                "set_alarm" -> {
                    val hour = args?.get("hour")?.jsonPrimitive?.intOrNull ?: 8
                    val minute = args?.get("minute")?.jsonPrimitive?.intOrNull ?: 0
                    val label = args?.get("label")?.jsonPrimitive?.content ?: "Neura Alarm"
                    setAlarm(hour, minute, label)
                }

                "set_timer" -> {
                    val seconds = args?.get("length_seconds")?.jsonPrimitive?.intOrNull ?: 60
                    val label = args?.get("label")?.jsonPrimitive?.content ?: "Neura Timer"
                    setTimer(seconds, label)
                }

                "toggle_flashlight" -> {
                    val state = args?.get("state")?.jsonPrimitive?.booleanOrNull ?: true
                    val res = flashlightController.toggleFlashlight(state)
                    if (res.isSuccess) {
                        ActionResult.Success(res.getOrThrow(), ActionCardData.FlashlightCard(state))
                    } else {
                        ActionResult.Error(res.exceptionOrNull()?.message ?: "Flashlight error")
                    }
                }

                "adjust_volume" -> {
                    val action = args?.get("action")?.jsonPrimitive?.content ?: "up"
                    val percentage = args?.get("percentage")?.jsonPrimitive?.intOrNull
                    val res = volumeController.adjustVolume(action, percentage)
                    if (res.isSuccess) {
                        ActionResult.Success(res.getOrThrow())
                    } else {
                        ActionResult.Error(res.exceptionOrNull()?.message ?: "Volume error")
                    }
                }

                "get_weather" -> {
                    val location = args?.get("location")?.jsonPrimitive?.content ?: "London"
                    val res = weatherService.fetchWeather(location)
                    if (res.isSuccess) {
                        val w = res.getOrThrow()
                        val card = ActionCardData.WeatherCard(
                            location = w.location,
                            temp = w.temperatureCelsius,
                            condition = w.weatherDescription,
                            humidity = w.humidityPercent,
                            windSpeed = w.windSpeedKmh,
                            isDay = w.isDay
                        )
                        val summary = "The current weather in ${w.location} is ${w.temperatureCelsius.toInt()}°C with ${w.weatherDescription.lowercase()}."
                        ActionResult.Success(summary, card)
                    } else {
                        ActionResult.Error(res.exceptionOrNull()?.message ?: "Weather error")
                    }
                }

                "get_device_status" -> {
                    val telemetry = telemetryHelper.getDeviceStatus()
                    val card = ActionCardData.TelemetryCard(
                        battery = telemetry.batteryPercent,
                        isCharging = telemetry.isCharging,
                        network = telemetry.networkStatus,
                        time = telemetry.currentFormattedTime
                    )
                    val statusText = "Battery is at ${telemetry.batteryPercent}% (${if (telemetry.isCharging) "Charging" else "Not charging"}). Network is ${telemetry.networkStatus}. Current time: ${telemetry.currentFormattedTime}."
                    ActionResult.Success(statusText, card)
                }

                "web_search" -> {
                    val query = args?.get("query")?.jsonPrimitive?.content ?: ""
                    webSearch(query)
                }

                "open_url" -> {
                    val url = args?.get("url")?.jsonPrimitive?.content ?: ""
                    openBrowserUrl(url)
                }

                "open_camera" -> {
                    openCamera()
                }

                "create_calendar_event" -> {
                    val title = args?.get("title")?.jsonPrimitive?.content ?: "New Event"
                    val description = args?.get("description")?.jsonPrimitive?.content ?: ""
                    createCalendarEvent(title, description)
                }

                else -> ActionResult.Error("Unknown tool: $functionName")
            }
        } catch (e: Exception) {
            ActionResult.Error("Failed to execute $functionName: ${e.message}")
        }
    }

    private fun makePhoneCall(numberOrContact: String): ActionResult {
        return try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$numberOrContact")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            ActionResult.Success("Calling $numberOrContact", ActionCardData.CallCard(numberOrContact))
        } catch (e: Exception) {
            ActionResult.Error("Could not initiate phone call: ${e.message}")
        }
    }

    private fun sendSms(recipient: String, messageText: String): ActionResult {
        return try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$recipient")
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            ActionResult.Success("Prepared message to $recipient: \"$messageText\"")
        } catch (e: Exception) {
            ActionResult.Error("Could not open messaging app: ${e.message}")
        }
    }

    private fun setAlarm(hour: Int, minute: Int, label: String): ActionResult {
        return try {
            val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(alarmIntent)
            val timeFormatted = String.format("%02d:%02d", hour, minute)
            ActionResult.Success("Alarm set for $timeFormatted ($label)", ActionCardData.AlarmCard(hour, minute, label))
        } catch (e: Exception) {
            ActionResult.Error("Could not set alarm: ${e.message}")
        }
    }

    private fun setTimer(seconds: Int, label: String): ActionResult {
        return try {
            val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(timerIntent)
            ActionResult.Success("Timer started for $seconds seconds ($label)", ActionCardData.TimerCard(seconds, label))
        } catch (e: Exception) {
            ActionResult.Error("Could not set timer: ${e.message}")
        }
    }

    private fun webSearch(query: String): ActionResult {
        return try {
            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchIntent)
            ActionResult.Success("Searching web for \"$query\"")
        } catch (e: Exception) {
            ActionResult.Error("Could not open web search: ${e.message}")
        }
    }

    private fun openBrowserUrl(rawUrl: String): ActionResult {
        return try {
            val fixedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) "https://$rawUrl" else rawUrl
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            ActionResult.Success("Opened $fixedUrl")
        } catch (e: Exception) {
            ActionResult.Error("Could not open URL: ${e.message}")
        }
    }

    private fun openCamera(): ActionResult {
        return try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(cameraIntent)
            ActionResult.Success("Opened Camera")
        } catch (e: Exception) {
            ActionResult.Error("Could not open camera: ${e.message}")
        }
    }

    private fun createCalendarEvent(title: String, description: String): ActionResult {
        return try {
            val calIntent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(calIntent)
            ActionResult.Success("Created calendar event: \"$title\"")
        } catch (e: Exception) {
            ActionResult.Error("Could not create calendar event: ${e.message}")
        }
    }
}
