package com.agentmaster.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(primary = Color(0xFF7C4DFF), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
private val LightColorScheme = lightColorScheme(primary = Color(0xFF6200EE), background = Color(0xFFF5F5F5), surface = Color(0xFFFFFFFF))

@Composable
fun AgentMasterTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme, content = content)
}
