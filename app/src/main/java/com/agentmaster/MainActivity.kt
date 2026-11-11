package com.agentmaster

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.agentmaster.data.AgentRepository
import com.agentmaster.domain.Orchestrator
import com.agentmaster.domain.Task
import com.agentmaster.ui.ChatScreen
import com.agentmaster.ui.SettingsScreen
import com.agentmaster.ui.theme.AgentMasterTheme
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
                    ChatScreen(
                        tasks = tasks,
                        isWorking = isWorking,
                        onOpenSettings = { screen = "settings" },
                        onSendTask = { prompt ->
                            if (isWorking || prompt.isBlank()) return@ChatScreen
                            scope.launch {
                                isWorking = true
                                val newTask = Task(userPrompt = prompt)
                                tasks = tasks + newTask
                                try {
                                    tasks = tasks.map {
                                        if (it.id == newTask.id) it.copy(status = "PLANNING") else it
                                    }
                                    val agents = orchestrator.planAgents(prompt)
                                    tasks = tasks.map {
                                        if (it.id == newTask.id) it.copy(status = "EXECUTING", agents = agents) else it
                                    }

                                    val key = prefs.getString("api_key", "") ?: ""
                                    if (key.isBlank()) {
                                        tasks = tasks.map {
                                            if (it.id == newTask.id) {
                                                it.copy(status = "ERROR", finalResult = "Сначала добавь API-ключ в настройках.")
                                            } else it
                                        }
                                        return@launch
                                    }

                                    val apiUrl = prefs.getString("api_url", "https://api.openai.com/v1")
                                        ?: "https://api.openai.com/v1"
                                    val model = prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini"
                                    val repository = AgentRepository(key, apiUrl, model)
                                    val executed = repository.runAllAgents(
                                        tasks.first { it.id == newTask.id }.copy(agents = agents)
                                    )
                                    val hasErrors = executed.agents.any { it.status == "ERROR" }
                                    val finalResult = orchestrator.assembleResult(executed)
                                    tasks = tasks.map {
                                        if (it.id == newTask.id) {
                                            it.copy(
                                                status = if (hasErrors) "PARTIAL" else "DONE",
                                                agents = executed.agents,
                                                finalResult = finalResult
                                            )
                                        } else it
                                    }
                                } catch (e: Exception) {
                                    val reason = e.localizedMessage ?: "Неизвестная ошибка"
                                    tasks = tasks.map {
                                        if (it.id == newTask.id) {
                                            it.copy(status = "ERROR", finalResult = "Не удалось выполнить задачу: $reason")
                                        } else it
                                    }
                                } finally {
                                    isWorking = false
                                }
                            }
                        }
                    )
                } else {
                    SettingsScreen(
                        prefs.getString("api_key", "") ?: "",
                        prefs.getString("api_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
                        prefs.getString("model", "gpt-4o-mini") ?: "gpt-4o-mini",
                        onSave = { key, url, model ->
                            prefs.edit()
                                .putString("api_key", key)
                                .putString("api_url", url)
                                .putString("model", model)
                                .apply()
                            screen = "chat"
                        },
                        onBack = { screen = "chat" }
                    )
                }
            }
        }
    }
}
