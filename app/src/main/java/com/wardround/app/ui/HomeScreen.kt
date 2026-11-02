package com.wardround.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.FocusItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPatientFocus: (Long) -> Unit,
    onOpenPatients: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDischarged: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }

    val patients by repository.getAllPatients().collectAsState(initial = emptyList())
    val pending by repository.getPendingFocusItems().collectAsState(initial = emptyList())

    val patientNameMap = remember(patients) { patients.associate { it.id to it.name } }
    val patientBedMap = remember(patients) { patients.associate { it.id to it.bedNo } }
    val inHospitalCount = patients.count { it.status == "在院" }
    val dischargedCount = patients.count { it.status == "出院" }

    val pendingGroups = remember(pending, patientNameMap, patientBedMap) {
        pending.groupBy { it.patientId }
            .map { (pid, items) ->
                PendingGroup(
                    patientId = pid,
                    patientName = patientNameMap[pid] ?: "未知患者",
                    bedNo = patientBedMap[pid] ?: "",
                    items = items
                )
            }
            .sortedBy { it.patientName }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日查房") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OverviewCard(
                        label = "在院患者",
                        value = inHospitalCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    OverviewCard(
                        label = "待办事项",
                        value = pending.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(onClick = onOpenDischarged, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "出院患者",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "$dischargedCount 人",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "今日待办",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onOpenPatients) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("患者列表")
                    }
                }
            }

            if (pending.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无待办事项", color = Color.Gray)
                    }
                }
            } else {
                items(pendingGroups, key = { it.patientId }) { group ->
                    PendingTodoGroupCard(
                        group = group,
                        onClick = { onOpenPatientFocus(group.patientId) }
                    )
                }
            }
        }
    }
}

private data class PendingGroup(
    val patientId: Long,
    val patientName: String,
    val bedNo: String,
    val items: List<FocusItem>
)

@Composable
private fun OverviewCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
private fun PendingTodoGroupCard(
    group: PendingGroup,
    onClick: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        group.patientName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (group.bedNo.isNotBlank()) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${group.bedNo}床",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                group.items.forEach { item ->
                    Text(
                        "· ${item.content}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3A3A3C)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${group.items.size} 项",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text("待办", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "进入",
                tint = Color.Gray
            )
        }
    }
}
