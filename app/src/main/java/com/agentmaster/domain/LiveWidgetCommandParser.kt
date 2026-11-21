package com.agentmaster.domain

/** Parses explicit widget-control requests. Ordinary informational questions return null. */
object LiveWidgetCommandParser {
    fun parse(raw: String): LiveWidgetCommand? {
        val text = raw.trim().lowercase().replace('ё', 'е')
        if (text.isBlank()) return null
        if (listOf("спрячь все блоки", "скрой все блоки", "убери все блоки", "hide all", "hide all widgets").any(text::contains)) {
            return LiveWidgetCommand.HideAll
        }

        val type = when {
            listOf("часы", "часов", "clock", "time widget").any(text::contains) -> LiveWidgetType.CLOCK
            listOf("погод", "weather").any(text::contains) -> LiveWidgetType.WEATHER
            listOf("батар", "заряд", "battery").any(text::contains) -> LiveWidgetType.BATTERY
            else -> return null
        }

        val hide = listOf("спрячь", "скрой", "убери", "hide", "не показывай").any(text::contains)
        val show = listOf("покажи", "верни", "открой", "show", "display").any(text::contains)
        if (hide) return LiveWidgetCommand.Hide(type)
        if (show) return LiveWidgetCommand.Show(type)

        val bigger = listOf("крупнее", "больше", "увелич", "bigger", "larger", "increase size").any(text::contains)
        val smaller = listOf("мельче", "меньше", "уменьш", "smaller", "reduce size").any(text::contains)
        if (bigger) return LiveWidgetCommand.Resize(type, 1.15f)
        if (smaller) return LiveWidgetCommand.Resize(type, 0.87f)

        val directions = when {
            listOf("вправо", "right").any(text::contains) -> 1f to 0f
            listOf("влево", "left").any(text::contains) -> -1f to 0f
            listOf("вверх", "up").any(text::contains) -> 0f to -1f
            listOf("вниз", "down").any(text::contains) -> 0f to 1f
            else -> null
        }
        if (directions != null && listOf("перемести", "сдвинь", "move", "shift").any(text::contains)) {
            return LiveWidgetCommand.Move(type, directions.first * LIVE_WIDGET_MOVE_STEP, directions.second * LIVE_WIDGET_MOVE_STEP)
        }
        if (listOf("удали блок", "удали виджет", "remove widget", "delete widget").any(text::contains)) {
            return LiveWidgetCommand.Remove(type)
        }
        return null
    }
}
