package com.agentmaster.ui
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agentmaster.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCard(agent: AgentInstance) {
    val statusColor by animateColorAsState(when (agent.status) {
        AgentStatus.IDLE -> Color.Gray; AgentStatus.WORKING -> Color(0xFFFFA000)
        AgentStatus.DONE -> Color(0xFF4CAF50); AgentStatus.ERROR -> Color(0xFFF44336)
    }, label = "statusColor")
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${agent.type.emoji} ${agent.type.label}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(modifier = Modifier.size(12.dp), shape = RoundedCornerShape(6.dp), color = statusColor) {}
            }
            Spacer(Modifier.height(4.dp))
            Text(agent.taskDescription, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            if (agent.result.isNotEmpty()) { Spacer(Modifier.height(8.dp)); androidx.compose.material3.HorizontalDivider(); Spacer(Modifier.height(8.dp)); Text(agent.result, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
