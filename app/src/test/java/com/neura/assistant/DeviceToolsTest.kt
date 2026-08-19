package com.neura.assistant

import com.neura.assistant.system.DeviceToolsDefinition
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceToolsTest {

    @Test
    fun testToolDefinitionsAreValid() {
        val tools = DeviceToolsDefinition.allTools
        assertTrue(tools.isNotEmpty())

        val toolNames = tools.map { it.function.name }
        assertTrue(toolNames.contains("make_phone_call"))
        assertTrue(toolNames.contains("send_sms"))
        assertTrue(toolNames.contains("open_app"))
        assertTrue(toolNames.contains("set_alarm"))
        assertTrue(toolNames.contains("set_timer"))
        assertTrue(toolNames.contains("toggle_flashlight"))
        assertTrue(toolNames.contains("adjust_volume"))
        assertTrue(toolNames.contains("get_weather"))
        assertTrue(toolNames.contains("get_device_status"))
        assertTrue(toolNames.contains("web_search"))
        assertTrue(toolNames.contains("open_url"))
    }
}
