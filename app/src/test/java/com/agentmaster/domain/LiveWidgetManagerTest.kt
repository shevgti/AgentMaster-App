package com.agentmaster.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Test

class LiveWidgetManagerTest {
    @Test fun hideAndShowPreserveWidgetConfiguration() {
        val original = LiveWidget(
            id = "clock-1",
            type = LiveWidgetType.CLOCK,
            position = WidgetPosition(0.3f, 0.4f),
            scale = 1.25f,
            city = "Berlin"
        )
        val layout = LiveWidgetLayout(widgets = listOf(original))
        val hidden = LiveWidgetManager.apply(layout, LiveWidgetCommand.Hide(LiveWidgetType.CLOCK))
        assertEquals(false, hidden.widgets.single().visible)
        val restored = LiveWidgetManager.apply(hidden, LiveWidgetCommand.Show(LiveWidgetType.CLOCK))
        assertEquals(original.id, restored.widgets.single().id)
        assertEquals(original.position, restored.widgets.single().position)
        assertEquals(original.scale, restored.widgets.single().scale)
        assertEquals(original.city, restored.widgets.single().city)
        assertEquals(true, restored.widgets.single().visible)
    }

    @Test fun hideAllDoesNotDeleteWidgets() {
        val layout = LiveWidgetLayout(widgets = listOf(
            LiveWidget(id = "c", type = LiveWidgetType.CLOCK),
            LiveWidget(id = "b", type = LiveWidgetType.BATTERY)
        ))
        val hidden = LiveWidgetManager.apply(layout, LiveWidgetCommand.HideAll)
        assertEquals(2, hidden.widgets.size)
        assertEquals(listOf(false, false), hidden.widgets.map { it.visible })
    }

    @Test fun directionalMoveIsRelativeAndBounded() {
        val layout = LiveWidgetLayout(widgets = listOf(
            LiveWidget(id = "c", type = LiveWidgetType.CLOCK, position = WidgetPosition(0.8f, 0.5f))
        ))
        val moved = LiveWidgetManager.apply(layout, LiveWidgetCommand.Move(LiveWidgetType.CLOCK, 0.12f, -0.12f))
        assertEquals(0.92f, moved.widgets.single().position.x, 0.001f)
        assertEquals(0.38f, moved.widgets.single().position.y, 0.001f)
    }

    @Test fun resizeIsClamped() {
        val layout = LiveWidgetLayout(widgets = listOf(
            LiveWidget(id = "c", type = LiveWidgetType.CLOCK, scale = 1.7f)
        ))
        val resized = LiveWidgetManager.apply(layout, LiveWidgetCommand.Resize(LiveWidgetType.CLOCK, 2f))
        assertEquals(1.8f, resized.widgets.single().scale, 0.001f)
    }

    @Test fun parserRecognizesRussianAndEnglishCommandsButNotQuestions() {
        assertEquals(LiveWidgetCommand.HideAll, LiveWidgetCommandParser.parse("Спрячь все блоки"))
        assertEquals(LiveWidgetCommand.Show(LiveWidgetType.BATTERY), LiveWidgetCommandParser.parse("Show battery"))
        assertEquals(LiveWidgetCommand.Add(LiveWidgetType.CLOCK), LiveWidgetCommandParser.parse("Добавь ещё часы"))
        assertEquals(LiveWidgetCommand.Add(LiveWidgetType.BATTERY), LiveWidgetCommandParser.parse("Add another battery"))
        assertEquals(LiveWidgetCommand.Resize(LiveWidgetType.CLOCK, 1.15f), LiveWidgetCommandParser.parse("Сделай часы крупнее"))
        assertEquals(LiveWidgetCommand.Move(LiveWidgetType.CLOCK, 0.12f, 0f), LiveWidgetCommandParser.parse("Перемести часы вправо"))
        assertEquals(LiveWidgetCommand.Move(LiveWidgetType.BATTERY, 0f, -0.12f), LiveWidgetCommandParser.parse("Move battery up"))
        assertNull(LiveWidgetCommandParser.parse("Какая сейчас погода?"))
    }

    @Test fun addCreatesIndependentInstancesOfSameType() {
        val first = LiveWidget(id = "clock-one", type = LiveWidgetType.CLOCK)
        val layout = LiveWidgetLayout(widgets = listOf(first))
        val updated = LiveWidgetManager.apply(layout, LiveWidgetCommand.Add(LiveWidgetType.CLOCK))
        assertEquals(2, updated.widgets.size)
        assertEquals(2, updated.widgets.map { it.id }.distinct().size)
        assertEquals(2, updated.widgets.map { it.zIndex }.distinct().size)
        assertNotEquals(first.position, updated.widgets.last().position)
    }

    @Test fun showCreatesMissingWidget() {
        val result = LiveWidgetManager.apply(LiveWidgetLayout(), LiveWidgetCommand.Show(LiveWidgetType.WEATHER))
        assertNotNull(result.widgets.singleOrNull())
        assertEquals(LiveWidgetType.WEATHER, result.widgets.single().type)
    }
}
