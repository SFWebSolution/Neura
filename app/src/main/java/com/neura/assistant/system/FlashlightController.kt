package com.neura.assistant.system

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class FlashlightController(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn: Boolean = false

    fun toggleFlashlight(turnOn: Boolean): Result<String> {
        if (cameraManager == null) {
            return Result.failure(Exception("Camera service not available on this device"))
        }

        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId == null) {
                return Result.failure(Exception("No camera flash found on this device"))
            }

            cameraManager.setTorchMode(cameraId, turnOn)
            isTorchOn = turnOn
            Result.success(if (turnOn) "Flashlight turned ON" else "Flashlight turned OFF")
        } catch (e: CameraAccessException) {
            Result.failure(Exception("Flashlight error: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isFlashlightOn(): Boolean = isTorchOn
}
