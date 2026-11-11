package com.agentmaster.ui
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
fun SettingsScreen(currentApiKey: String, currentApiUrl: String, currentModel: String, onSave: (String, String, String) -> Unit, onBack: () -> Unit) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var apiUrl by remember { mutableStateOf(currentApiUrl) }
    var model by remember { mutableStateOf(currentModel) }
    Scaffold(topBar = { TopAppBar(title = { Text("⚙️ Настройки", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Подключение к ИИ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API-ключ") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = apiUrl, onValueChange = { apiUrl = it }, label = { Text("Адрес сервера") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Модель") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Button(onClick = { onSave(apiKey.trim(), apiUrl.trim(), model.trim()) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text("💾 Сохранить") }
        }
    }
}