package com.agentmaster.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentApiKey: String,
    currentApiUrl: String,
    currentModel: String,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var apiUrl by remember { mutableStateOf(currentApiUrl) }
    var model by remember { mutableStateOf(currentModel) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Настройки", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Подключение к ИИ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it; validationError = null },
                label = { Text("API-ключ") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = apiUrl,
                onValueChange = { apiUrl = it; validationError = null },
                label = { Text("Адрес сервера") },
                supportingText = { Text("Например: https://api.openai.com/v1") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it; validationError = null },
                label = { Text("Модель") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            if (validationError != null) {
                Text(validationError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    val parsed = runCatching { Uri.parse(apiUrl.trim()) }.getOrNull()
                    when {
                        apiKey.isBlank() -> validationError = "Введи API-ключ."
                        model.isBlank() -> validationError = "Укажи название модели."
                        parsed == null || parsed.host.isNullOrBlank() ||
                            (parsed.scheme != "https" && parsed.scheme != "http") ->
                            validationError = "Укажи корректный адрес, начинающийся с https:// или http://."
                        else -> onSave(apiKey.trim(), apiUrl.trim().trimEnd('/'), model.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("💾 Сохранить")
            }
        }
    }
}
