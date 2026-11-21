package com.agentmaster.data

import android.content.SharedPreferences
import com.agentmaster.domain.LiveWidgetLayout
import com.google.gson.Gson
import com.google.gson.JsonParser

class LiveWidgetRepository(private val preferences: SharedPreferences) {
    private val gson = Gson()
    private val key = "live_widgets_layout_v1"

    fun load(): LiveWidgetLayout {
        val raw = preferences.getString(key, null) ?: return LiveWidgetLayout()
        return runCatching {
            val root = JsonParser.parseString(raw).asJsonObject
            val version = root.get("schemaVersion")?.asInt ?: 0
            require(version <= LiveWidgetLayout.CURRENT_SCHEMA_VERSION) { "Unsupported Live Widgets schema: $version" }
            gson.fromJson(raw, LiveWidgetLayout::class.java) ?: LiveWidgetLayout()
        }.getOrElse {
            // Keep the invalid payload for recovery/debugging instead of silently deleting user data.
            preferences.edit().putString("${key}_recovery", raw).apply()
            LiveWidgetLayout()
        }
    }

    fun save(layout: LiveWidgetLayout) {
        val safe = layout.copy(schemaVersion = LiveWidgetLayout.CURRENT_SCHEMA_VERSION)
        preferences.edit().putString(key, gson.toJson(safe)).apply()
    }
}
