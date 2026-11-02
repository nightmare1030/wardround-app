package com.wardround.app.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.OperationLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val historyTimeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
private val historyDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }

    val logs by repository.getAllLogs().collectAsState(initial = emptyList())
    val patients by repository.getAllPatients().collectAsState(initial = emptyList())
    val patientNameMap = remember(patients) { patients.associate { it.id to it.name } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("操作历史") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无操作历史", color = Color.Gray)
            }
        } else {
            val grouped = logs.groupBy { historyDayFormat.format(Date(it.createdAt)) }
            var expandedDates by remember { mutableStateOf(setOf<String>()) }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (date, dayLogs) ->
                    val expanded = date in expandedDates
                    item(key = "header_$date") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedDates = if (expanded) expandedDates - date else expandedDates + date
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                date,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expanded) "收起" else "展开",
                                tint = Color(0xFF1565C0)
                            )
                        }
                    }
                    if (expanded) {
                        items(dayLogs, key = { it.id }) { log ->
                            HistoryLogCard(log = log, patientName = log.patientId?.let { patientNameMap[it] })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryLogCard(log: OperationLog, patientName: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    logActionLabel(log.action),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = logActionColor(log.action)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    historyTimeFormat.format(Date(log.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(4.dp))
            val detailText = buildString {
                if (!patientName.isNullOrBlank()) append("[$patientName] ")
                append(log.detail)
                if (log.targetName.isNotBlank() && log.targetType != OperationLog.TYPE_PATIENT) {
                    append("：${log.targetName}")
                }
            }
            Text(detailText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun logActionLabel(action: String): String = when (action) {
    OperationLog.ACTION_CREATE -> "新增"
    OperationLog.ACTION_UPDATE -> "修改"
    OperationLog.ACTION_DELETE -> "删除"
    OperationLog.ACTION_COMPLETE -> "完成"
    OperationLog.ACTION_UNDO -> "撤销完成"
    else -> action
}

private fun logActionColor(action: String): Color = when (action) {
    OperationLog.ACTION_COMPLETE -> Color(0xFF2E7D32)
    OperationLog.ACTION_DELETE -> Color(0xFFD32F2F)
    OperationLog.ACTION_CREATE -> Color(0xFF1565C0)
    else -> Color.Gray
}