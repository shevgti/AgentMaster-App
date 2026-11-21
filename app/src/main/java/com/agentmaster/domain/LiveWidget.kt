package com.agentmaster.domain

import java.util.UUID

enum class LiveWidgetType {
    CLOCK, WEATHER, BATTERY
}

data class WidgetPosition(
    val x: Float = 0.72f,
    val y: Float = 0.12f
)

data class LiveWidget(
    val id: String = UUID.randomUUID().toString(),
    val type: LiveWidgetType,
    val position: WidgetPosition = WidgetPosition(),
    val scale: Float = 1f,
    val visible: Boolean = true,
    val zIndex: Int = 0,
    val city: String = ""
)

data class LiveWidgetLayout(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val widgets: List<LiveWidget> = emptyList()
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

sealed interface LiveWidgetCommand {
    data class Add(val type: LiveWidgetType) : LiveWidgetCommand
    data class SetCity(val city: String) : LiveWidgetCommand
    data class Show(val type: LiveWidgetType) : LiveWidgetCommand
    data class Hide(val type: LiveWidgetType) : LiveWidgetCommand
    data object HideAll : LiveWidgetCommand
    data class Move(val type: LiveWidgetType, val dx: Float, val dy: Float) : LiveWidgetCommand
    data class Place(val type: LiveWidgetType, val x: Float, val y: Float) : LiveWidgetCommand
    data class Resize(val type: LiveWidgetType, val factor: Float) : LiveWidgetCommand
    data class Remove(val type: LiveWidgetType) : LiveWidgetCommand
}

/** Pure state transitions; persistence and UI remain outside the domain layer. */
object LiveWidgetManager {
    private const val MIN_SCALE = 0.65f
    private const val MAX_SCALE = 1.8f
    private const val MOVE_STEP = 0.12f

    fun apply(layout: LiveWidgetLayout, command: LiveWidgetCommand): LiveWidgetLayout {
        val widgets = layout.widgets.toMutableList()
        when (command) {
            is LiveWidgetCommand.SetCity -> {
                val index = widgets.indexOfFirst { it.type == LiveWidgetType.WEATHER }
                if (index >= 0) widgets[index] = widgets[index].copy(city = command.city, visible = true)
                else widgets += LiveWidget(type = LiveWidgetType.WEATHER, city = command.city, zIndex = nextZ(widgets))
            }
            is LiveWidgetCommand.Add -> {
                val sameTypeCount = widgets.count { it.type == command.type }
                val offset = 0.08f * (sameTypeCount + 1)
                widgets += LiveWidget(
                    type = command.type,
                    position = WidgetPosition(
                        x = (0.72f - offset).coerceIn(0.04f, 0.92f),
                        y = (0.12f + offset).coerceIn(0.04f, 0.92f)
                    ),
                    zIndex = nextZ(widgets)
                )
            }
            is LiveWidgetCommand.Show -> {
                val index = widgets.indexOfFirst { it.type == command.type }
                if (index >= 0) {
                    widgets[index] = widgets[index].copy(visible = true, zIndex = nextZ(widgets))
                } else {
                    widgets += LiveWidget(type = command.type, zIndex = nextZ(widgets))
                }
            }
            is LiveWidgetCommand.Hide -> widgets.indices.forEach { i ->
                if (widgets[i].type == command.type) widgets[i] = widgets[i].copy(visible = false)
            }
            LiveWidgetCommand.HideAll -> widgets.indices.forEach { i ->
                widgets[i] = widgets[i].copy(visible = false)
            }
            is LiveWidgetCommand.Move -> widgets.indices.firstOrNull { widgets[it].type == command.type && widgets[it].visible }?.let { i ->
                val w = widgets[i]
                widgets[i] = w.copy(
                    position = WidgetPosition(
                        x = (w.position.x + command.dx).coerceIn(0f, 1f),
                        y = (w.position.y + command.dy).coerceIn(0f, 1f)
                    ),
                    zIndex = nextZ(widgets)
                )
            }
            is LiveWidgetCommand.Place -> widgets.indices.firstOrNull { widgets[it].type == command.type }?.let { i ->
                widgets[i] = widgets[i].copy(
                    visible = true,
                    position = WidgetPosition(command.x.coerceIn(0f, 1f), command.y.coerceIn(0f, 1f)),
                    zIndex = nextZ(widgets)
                )
            }
            is LiveWidgetCommand.Resize -> widgets.indices.firstOrNull { widgets[it].type == command.type && widgets[it].visible }?.let { i ->
                widgets[i] = widgets[i].copy(scale = (widgets[i].scale * command.factor).coerceIn(MIN_SCALE, MAX_SCALE))
            }
            is LiveWidgetCommand.Remove -> widgets.removeAll { it.type == command.type }
        }
        return layout.copy(widgets = widgets)
    }

    private fun nextZ(widgets: List<LiveWidget>): Int = (widgets.maxOfOrNull { it.zIndex } ?: -1) + 1
}

