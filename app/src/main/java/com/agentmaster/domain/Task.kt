package com.agentmaster.domain

data class Task(
    val id: Long = System.currentTimeMillis(), val userPrompt: String,
    val status: TaskStatus = TaskStatus.PENDING, val agents: List<AgentInstance> = emptyList(), val finalResult: String = ""
)
enum class TaskStatus { PENDING, PLANNING, EXECUTING, ASSEMBLING, DONE, ERROR }
