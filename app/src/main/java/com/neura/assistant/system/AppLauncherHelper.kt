package com.neura.assistant.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri

class AppLauncherHelper(private val context: Context) {
    private val packageManager: PackageManager = context.packageManager

    private val commonPackageAliases = mapOf(
        "youtube" to listOf("com.google.android.youtube"),
        "spotify" to listOf("com.spotify.music"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "instagram" to listOf("com.instagram.android"),
        "facebook" to listOf("com.facebook.katana"),
        "messenger" to listOf("com.facebook.orca"),
        "telegram" to listOf("org.telegram.messenger"),
        "twitter" to listOf("com.twitter.android"),
        "x" to listOf("com.twitter.android"),
        "tiktok" to listOf("com.zhiliaoapp.musically"),
        "netflix" to listOf("com.netflix.mediaclient"),
        "calculator" to listOf("com.google.android.calculator", "com.android.calculator2", "com.sec.android.app.popupcalculator"),
        "camera" to listOf("com.google.android.GoogleCamera", "com.sec.android.app.camera", "com.android.camera"),
        "settings" to listOf("com.android.settings"),
        "chrome" to listOf("com.android.chrome"),
        "maps" to listOf("com.google.android.apps.maps"),
        "photos" to listOf("com.google.android.apps.photos", "com.sec.android.gallery3d"),
        "clock" to listOf("com.google.android.deskclock", "com.sec.android.app.clockpackage")
    )

    fun launchAppByName(appNameQuery: String): Result<String> {
        val query = appNameQuery.trim().lowercase()

        // 1. Check known aliases first
        commonPackageAliases[query]?.forEach { pkg ->
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return Result.success("Opened $appNameQuery")
            }
        }

        // 2. Search all installed launchable apps
        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                val label = packageManager.getApplicationLabel(app).toString().lowercase()
                if (label.contains(query) || query.contains(label)) {
                    val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        val appLabel = packageManager.getApplicationLabel(app).toString()
                        return Result.success("Opened $appLabel")
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        // 3. Fallback: Search in Google Play Store
        return try {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$query")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
            Result.success("Searching for '$appNameQuery' on Play Store")
        } catch (e: Exception) {
            Result.failure(Exception("Could not find or open app '$appNameQuery'"))
        }
    }
}
