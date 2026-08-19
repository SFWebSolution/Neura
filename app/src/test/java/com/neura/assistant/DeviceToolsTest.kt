package com.neura.assistant

import com.neura.assistant.data.api.models.OpenAiChatRequest
import com.neura.assistant.data.api.models.OpenAiMessage
import com.neura.assistant.system.DeviceToolsDefinition
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceToolsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

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

    @Test
    fun testChatRequestSerialization() {
        val request = OpenAiChatRequest(
            model = "gpt-4o",
            messages = listOf(
                OpenAiMessage(role = "system", content = "You are Neura"),
                OpenAiMessage(role = "user", content = "Turn on the flashlight")
            ),
            tools = DeviceToolsDefinition.allTools,
            temperature = 0.7
        )

        val jsonStr = json.encodeToString(request)
        assertNotNull(jsonStr)
        assertTrue(jsonStr.contains("toggle_flashlight"))
        assertTrue(jsonStr.contains("Turn on the flashlight"))
    }
}
