package com.agentmaster.domain

class Orchestrator {
    fun planAgents(userTask: String): List<AgentInstance> {
        val agents = mutableListOf<AgentInstance>()
        var id = 1
        val t = userTask.lowercase()
        if (t.matches(Regex(".*(найди|узнай|что такое|информация|факт|расскажи).*"))) 
            agents.add(AgentInstance(id++, AgentType.RESEARCHER, "Собери информацию: $userTask"))
        if (t.matches(Regex(".*(анализ|сравни|оцени|данные|статистика|тренд|вывод).*"))) 
            agents.add(AgentInstance(id++, AgentType.ANALYST, "Проанализируй и сделай выводы: $userTask"))
        if (t.matches(Regex(".*(напиши|текст|статья|описание|письмо|резюме).*"))) 
            agents.add(AgentInstance(id++, AgentType.WRITER, "Напиши текст: $userTask"))
        if (t.matches(Regex(".*(код|скрипт|функци|алгоритм|программ|приложени|бот|python|java|kotlin).*"))) 
            agents.add(AgentInstance(id++, AgentType.CODER, "Напиши код: $userTask"))
        if (t.matches(Regex(".*(план|шаг|расписани|маршрут|стратег|организ).*"))) 
            agents.add(AgentInstance(id++, AgentType.PLANNER, "Составь план: $userTask"))
        if (agents.isEmpty()) agents.add(AgentInstance(id++, AgentType.RESEARCHER, "Разбери задачу: $userTask"))
        return agents
    }
    fun assembleResult(task: Task): String {
        val sb = StringBuilder("📋 Задача: ${task.userPrompt}\n\n")
        for (agent in task.agents) sb.appendLine("${agent.type.emoji} ${agent.type.label}:\n${agent.result.ifEmpty { "(в работе...)" }}\n")
        return sb.toString()
    }
}
