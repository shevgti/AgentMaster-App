package com.agentmaster.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agentmaster.domain.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(tasks: List<Task>, isWorking: Boolean, onSendTask: (String) -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Scaffold(topBar = { TopAppBar(title = { Text("🤖 Агент-Мастер", fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp), state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks) { task ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📝 ${task.userPrompt}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val statusText = when (task.status) {
                                TaskStatus.PENDING -> "⏳ Ожидание..."; TaskStatus.PLANNING -> "🧠 Планирование..."
                                TaskStatus.EXECUTING -> "⚙️ Агенты работают..."; TaskStatus.ASSEMBLING -> "🔗 Сборка..."
                                TaskStatus.DONE -> "✅ Готово"; TaskStatus.ERROR -> "❌ Ошибка"
                            }
                            Text(statusText, style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            for (agent in task.agents) AgentCard(agent)
                            if (task.finalResult.isNotEmpty()) { Spacer(Modifier.height(8.dp)); androidx.compose.material3.HorizontalDivider(); Spacer(Modifier.height(8.dp)); Text("🎯 Итог:", fontWeight = FontWeight.Bold); Text(task.finalResult) }
                        }
                    }
                }
            }
            if (isWorking) { Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator(Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Работаем...", style = MaterialTheme.typography.bodySmall) } }
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = inputText, onValueChange = { inputText = it }, modifier = Modifier.weight(1f), placeholder = { Text("Опиши задачу...") }, shape = RoundedCornerShape(24.dp), maxLines = 3)
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { if (inputText.isNotBlank() && !isWorking) { onSendTask(inputText.trim()); inputText = ""; scope.launch { listState.animateScrollToItem(tasks.size) } } }, enabled = inputText.isNotBlank() && !isWorking) { Icon(Icons.Default.Send, "Send") }
            }
        }
    }
}
