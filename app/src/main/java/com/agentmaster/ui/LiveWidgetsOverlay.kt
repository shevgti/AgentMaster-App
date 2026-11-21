package com.agentmaster.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.agentmaster.domain.LiveWidget
import com.agentmaster.domain.LiveWidgetType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveWidgetsOverlay(
    widgets: List<LiveWidget>,
    onWidgetChanged: (LiveWidget) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val width = maxWidth
        val height = maxHeight
        widgets.filter { it.visible }.sortedBy { it.zIndex }.forEach { widget ->
            val x = (width * widget.position.x).coerceIn(0.dp, (width - 156.dp).coerceAtLeast(0.dp))
            val y = (height * widget.position.y).coerceIn(0.dp, (height - 88.dp).coerceAtLeast(0.dp))
            LiveWidgetCard(
                widget = widget,
                modifier = Modifier
                    .offset(x = x, y = y)
                    .graphicsLayer {
                        scaleX = widget.scale
                        scaleY = widget.scale
                    }
                    .pointerInput(widget.id, width, height) {
                        var currentX = widget.position.x
                        var currentY = widget.position.y
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            currentX = (currentX + dragAmount.x / width.toPx()).coerceIn(0f, 1f)
                            currentY = (currentY + dragAmount.y / height.toPx()).coerceIn(0f, 1f)
                            val front = (widgets.maxOfOrNull { it.zIndex } ?: 0) + 1
                            onWidgetChanged(widget.copy(
                                position = widget.position.copy(x = currentX, y = currentY),
                                zIndex = front
                            ))
                        }
                    }
            )
        }
    }
}

@Composable
private fun LiveWidgetCard(widget: LiveWidget, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var time by remember { mutableStateOf(nowTime()) }
    var batteryPercent by remember { mutableIntStateOf(readBattery(context).first) }
    var charging by remember { mutableStateOf(readBattery(context).second) }

    LaunchedEffect(widget.id, widget.type) {
        while (widget.type == LiveWidgetType.CLOCK) {
            time = nowTime()
            delay(1_000)
        }
    }
    DisposableEffect(widget.id, widget.type) {
        if (widget.type != LiveWidgetType.BATTERY) return@DisposableEffect onDispose { }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) batteryPercent = (level * 100) / scale
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { runCatching { context.unregisterReceiver(receiver) } }
    }

    AnimatedVisibility(
        visible = widget.visible,
        enter = fadeIn(animationSpec = spring()),
        exit = fadeOut(animationSpec = spring()),
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                when (widget.type) {
                    LiveWidgetType.CLOCK -> {
                        Text(time, style = MaterialTheme.typography.headlineSmall)
                        Text("Мегаагент • часы", style = MaterialTheme.typography.labelSmall)
                    }
                    LiveWidgetType.WEATHER -> {
                        Text("Погода", style = MaterialTheme.typography.titleMedium)
                        Text(if (widget.city.isBlank()) "Укажите город в настройках" else widget.city)
                        Text("Источник погоды ещё не подключён", style = MaterialTheme.typography.labelSmall)
                    }
                    LiveWidgetType.BATTERY -> {
                        Text("$batteryPercent%", style = MaterialTheme.typography.headlineSmall)
                        Text(if (charging) "Заряжается" else "Разряжается", style = MaterialTheme.typography.labelSmall)
                        if (batteryPercent <= 20) Text("Низкий заряд", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun nowTime(): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

private fun readBattery(context: Context): Pair<Int, Boolean> {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val percent = if (level >= 0 && scale > 0) level * 100 / scale else 0
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL
    return percent to isCharging
}
