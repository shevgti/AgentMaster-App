package com.agentmaster.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.agentmaster.domain.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(tasks: List<Task>, isWorking: Boolean, onSendTask: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val submitPrompt = {
        val prompt = inputText.trim()
        if (prompt.isNotEmpty() && !isWorking) {
            onSendTask(prompt)
            inputText = ""
            scope.launch {
                if (tasks.isNotEmpty()) listState.animateScrollToItem(tasks.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("🤖 Агент-Мастер", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📝 ${task.userPrompt}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val statusText = when (task.status) {
                                "PENDING" -> "⏳ Ожидание..."
                                "PLANNING" -> "🧠 Планирование..."
                                "EXECUTING" -> "⚙️ Агенты работают..."
                                "ASSEMBLING" -> "🔗 Сборка..."
                                "DONE" -> "✅ Готово"
                                "PARTIAL" -> "⚠️ Выполнено с ошибками"
                                else -> "❌ Ошибка"
                            }
                            Text(statusText, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            for (agent in task.agents) AgentCard(agent)
                            if (task.finalResult.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Text("🎯 Итог:", fontWeight = FontWeight.Bold)
                                Text(task.finalResult)
                            }
                        }
                    }
                }
            }

            if (isWorking) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Работаем...", style = MaterialTheme.typography.bodySmall)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Опиши задачу...") },
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { submitPrompt() })
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { submitPrompt() },
                        enabled = inputText.isNotBlank() && !isWorking,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Отправить")
                    }
                }
            }
        }
    }
}
