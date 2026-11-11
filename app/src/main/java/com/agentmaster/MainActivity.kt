package com.agentmaster
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.agentmaster.data.AgentRepository
import com.agentmaster.domain.*
import com.agentmaster.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val orchestrator = Orchestrator()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("agent_master", Context.MODE_PRIVATE)
        setContent {
            AgentMasterTheme {
                var screen by remember { mutableStateOf("chat") }
                var tasks by remember { mutableStateOf(listOf<Task>()) }
                var isWorking by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                if (screen == "chat") {
                    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { screen = "settings" }) { Icon(Icons.Default.Settings, "Settings") } }) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            ChatScreen(tasks, isWorking) { prompt ->
                                scope.launch {
                                    isWorking = true
                                    val newTask = Task(userPrompt = prompt)
                                    tasks += newTask
                                    try {
                                        tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.PLANNING) else it }
                                        val agents = orchestrator.planAgents(prompt)
                                        tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.EXECUTING, agents = agents) else it }
                                        val key = prefs.getString("api_key", "") ?: ""
                                        if (key.isBlank()) {
                                            tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.ERROR, finalResult = "⚠️ Настрой API в настройках ⚙️") else it }
                                            isWorking = false; return@launch
                                        }
                                        val repo = AgentRepository(key, prefs.getString("api_url", "https://api.openai.com/v1") ?: "", prefs.getString("model", "gpt-4o-mini") ?: "")
                                        val executed = repo.runAllAgents(tasks.first { it.id == newTask.id })
                                        tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.ASSEMBLING) else it }
                                        val finalResult = orchestrator.assembleResult(executed)
                                        tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.DONE, finalResult = finalResult) else it }
                                    } catch (e: Exception) {
                                        tasks = tasks.map { if (it.id == newTask.id) it.copy(status = TaskStatus.ERROR, finalResult = "❌ ${e.localizedMessage}") else it }
                                    }
                                    isWorking = false
                                }
                            }
                        }
                    }
                } else {
                    SettingsScreen(
                        prefs.getString("api_key", "") ?: "", prefs.getString("api_url", "https://api.openai.com/v1") ?: "", prefs.getString("model", "gpt-4o-mini") ?: "",
                        onSave = { k, u, m -> prefs.edit().putString("api_key", k).putString("api_url", u).putString("model", m).apply(); screen = "chat" },
                        onBack = { screen = "chat" }
                    )
                }
            }
        }
    }
}
