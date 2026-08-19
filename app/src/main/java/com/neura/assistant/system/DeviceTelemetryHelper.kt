package com.neura.assistant.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceTelemetry(
    val batteryPercent: Int,
    val isCharging: Boolean,
    val networkStatus: String,
    val currentFormattedTime: String
)

class DeviceTelemetryHelper(private val context: Context) {

    fun getDeviceStatus(): DeviceTelemetry {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, batteryFilter)

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 100

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkStatus = when {
            connManager == null -> "Offline"
            else -> {
                val activeNetwork = connManager.activeNetwork
                val capabilities = connManager.getNetworkCapabilities(activeNetwork)
                when {
                    capabilities == null -> "Offline"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Connected to Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Connected to Cellular Data"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Connected to Ethernet"
                    else -> "Connected"
                }
            }
        }

        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a", Locale.getDefault())
        val formattedTime = dateFormat.format(Date())

        return DeviceTelemetry(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            networkStatus = networkStatus,
            currentFormattedTime = formattedTime
        )
    }
}
