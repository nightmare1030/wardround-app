package com.wardround.app.ui

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.wardround.app.WardRoundApp
import com.wardround.app.data.FocusItem
import com.wardround.app.data.OperationLog
import com.wardround.app.data.Patient
import com.wardround.app.data.WardRoundRecord
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateTimeFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
private val dayHeaderFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PatientDetailScreen(
    patientId: Long,
    initialTab: Int = 0,
    onBack: () -> Unit,
    onEditPatient: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()

    val patient by repository.getPatient(patientId).collectAsState(initial = null)
    val records by repository.getRecords(patientId).collectAsState(initial = emptyList())
    val focusItems by repository.getFocusItems(patientId).collectAsState(initial = emptyList())
    val logs by repository.getLogs(patientId).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(initialTab) }
    var showAddRecord by remember { mutableStateOf(false) }
    var showAddFocus by remember { mutableStateOf(false) }

    var recordContent by remember { mutableStateOf("") }
    var recordImagePath by remember { mutableStateOf<String?>(null) }
    var pendingDeleteRecord by remember { mutableStateOf<WardRoundRecord?>(null) }
    var pendingDeleteFocus by remember { mutableStateOf<FocusItem?>(null) }
    var pendingCompleteFocus by remember { mutableStateOf<FocusItem?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) recordImagePath = null
    }

    fun takePhoto() {
        val dir = File(context.filesDir, "images").apply { mkdirs() }
        val file = File(dir, "IMG_${System.currentTimeMillis()}.jpg")
        recordImagePath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePictureLauncher.launch(uri)
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(patient?.name ?: "患者详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditPatient(patientId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { padding ->
        var showInfo by remember { mutableStateOf(true) }
        val nestedScroll = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < -2f) showInfo = false
                    else if (available.y > 2f) showInfo = true
                    return Offset.Zero
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(nestedScroll)
        ) {
            AnimatedVisibility(
                visible = showInfo,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                patient?.let { PatientInfoCard(it) }
            }
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 8.dp) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("查房") }, modifier = Modifier.padding(horizontal = 4.dp))
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("事项") }, modifier = Modifier.padding(horizontal = 4.dp))
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("治疗") }, modifier = Modifier.padding(horizontal = 4.dp))
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("检查") }, modifier = Modifier.padding(horizontal = 4.dp))
                Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }, text = { Text("历史") }, modifier = Modifier.padding(horizontal = 4.dp))
                Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }, text = { Text("统计") }, modifier = Modifier.padding(horizontal = 4.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> RecordsSection(
                        records = records,
                        onAdd = {
                            recordContent = ""
                            recordImagePath = null
                            showAddRecord = true
                        },
                        onDelete = { r -> pendingDeleteRecord = r }
                    )
                    1 -> FocusSection(
                        items = focusItems,
                        onAdd = { showAddFocus = true },
                        onComplete = { f -> pendingCompleteFocus = f },
                        onDelete = { f -> pendingDeleteFocus = f }
                    )
                    2 -> TreatmentSection(patientId = patientId, showDrainage = patient?.surgeryDate != null)
                    3 -> ExamSection(patientId = patientId)
                    4 -> HistorySection(logs = logs)
                    5 -> StatisticsSection(patient = patient)
                }
            }
        }
    }

    if (showAddRecord) {
        AddRecordDialog(
            content = recordContent,
            onContentChange = { recordContent = it },
            imagePath = recordImagePath,
            onTakePhoto = { takePhoto() },
            onDismiss = {
                showAddRecord = false
                recordContent = ""
                recordImagePath = null
            },
            onSave = { content ->
                scope.launch { repository.addRecord(patientId, content, recordImagePath ?: "") }
                showAddRecord = false
                recordContent = ""
                recordImagePath = null
            }
        )
    }

    if (showAddFocus) {
        AddFocusDialog(
            patientId = patientId,
            onDismiss = { showAddFocus = false },
            onSave = { item ->
                scope.launch { repository.addFocusItem(item) }
                showAddFocus = false
            }
        )
    }

    pendingDeleteRecord?.let { r ->
        DeleteConfirmDialog(
            message = "确定删除这条查房记录吗？",
            onConfirm = {
                scope.launch { repository.deleteRecord(r) }
                pendingDeleteRecord = null
            },
            onDismiss = { pendingDeleteRecord = null }
        )
    }

    pendingDeleteFocus?.let { f ->
        DeleteConfirmDialog(
            message = "确定删除重点事项「${f.content.take(20)}」吗？",
            onConfirm = {
                scope.launch { repository.deleteFocusItem(f) }
                pendingDeleteFocus = null
            },
            onDismiss = { pendingDeleteFocus = null }
        )
    }

    pendingCompleteFocus?.let { f ->
        ConfirmDialog(
            title = "完成事项",
            message = "确定将重点事项「${f.content.take(20)}」标记为已完成吗？",
            confirmText = "完成",
            onConfirm = {
                scope.launch { repository.completeFocusItem(f) }
                pendingCompleteFocus = null
            },
            onDismiss = { pendingCompleteFocus = null }
        )
    }
}

// ===== 查房记录 =====
@Composable
private fun RecordsSection(
    records: List<WardRoundRecord>,
    onAdd: () -> Unit,
    onDelete: (WardRoundRecord) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (records.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无查房记录，点右下角添加", color = Color.Gray)
                    }
                }
            } else {
                items(records, key = { it.id }) { record ->
                    RecordCard(record = record, onDelete = { onDelete(record) })
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加记录")
        }
    }
}

@Composable
private fun RecordCard(record: WardRoundRecord, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(record.content, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        dateTimeFormat.format(Date(record.recordDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                }
            }
            if (record.imagePath.isNotBlank() && File(record.imagePath).exists()) {
                Spacer(Modifier.height(8.dp))
                LocalImage(record.imagePath, Modifier.fillMaxWidth().height(180.dp))
            }
        }
    }
}

@Composable
private fun LocalImage(path: String, modifier: Modifier = Modifier) {
    val bitmap = remember(path) {
        val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
        BitmapFactory.decodeFile(path, opts)
    }
    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = modifier)
    }
}

// ===== 重点事项 =====
@Composable
private fun FocusSection(
    items: List<FocusItem>,
    onAdd: () -> Unit,
    onComplete: (FocusItem) -> Unit,
    onDelete: (FocusItem) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无重点事项，点右下角添加", color = Color.Gray)
                    }
                }
            } else {
                items(items, key = { it.id }) { item ->
                    FocusItemCard(
                        item = item,
                        onComplete = { onComplete(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加重点事项")
        }
    }
}

@Composable
private fun FocusItemCard(
    item: FocusItem,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val done = item.status == FocusItem.STATUS_DONE
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (done) Color.Gray else Color.Unspecified
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(item.priority)
                    if (done && item.completedAt != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "完成于 " + dateTimeFormat.format(Date(item.completedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            if (!done) {
                Button(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("完成")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: Int) {
    val (label, color) = when (priority) {
        2 -> "高" to Color(0xFFD32F2F)
        1 -> "中" to Color(0xFFEF6C00)
        else -> "低" to Color(0xFF757575)
    }
    Text("优先级 $label", style = MaterialTheme.typography.labelSmall, color = color)
}

// ===== 操作历史 =====
@Composable
private fun HistorySection(logs: List<OperationLog>) {
    val grouped = logs.groupBy { dayHeaderFormat.format(Date(it.createdAt)) }
    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (logs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无操作历史", color = Color.Gray)
                }
            }
        }
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
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    actionLabel(log.action),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = actionColor(log.action)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    log.detail + if (log.targetName.isNotBlank()) "：${log.targetName}" else "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    dateTimeFormat.format(Date(log.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun actionLabel(action: String): String = when (action) {
    OperationLog.ACTION_CREATE -> "新增"
    OperationLog.ACTION_UPDATE -> "修改"
    OperationLog.ACTION_DELETE -> "删除"
    OperationLog.ACTION_COMPLETE -> "完成"
    OperationLog.ACTION_UNDO -> "撤销完成"
    else -> action
}

private fun actionColor(action: String): Color = when (action) {
    OperationLog.ACTION_COMPLETE -> Color(0xFF2E7D32)
    OperationLog.ACTION_DELETE -> Color(0xFFD32F2F)
    OperationLog.ACTION_CREATE -> Color(0xFF1565C0)
    else -> Color.Gray
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

// ===== 添加记录对话框 =====
@Composable
private fun AddRecordDialog(
    content: String,
    onContentChange: (String) -> Unit,
    imagePath: String?,
    onTakePhoto: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val templates = listOf("生命体征平稳", "伤口换药", "引流管通畅", "调整用药", "复查血常规", "无特殊不适")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加查房记录") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("记录今天做了什么…") },
                    minLines = 3
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onTakePhoto) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("拍照")
                    }
                }
                if (imagePath != null && File(imagePath).exists()) {
                    Spacer(Modifier.height(8.dp))
                    Text("已拍摄照片", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    LocalImage(imagePath, Modifier.fillMaxWidth().height(160.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("快速短语（点击插入）", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    templates.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { t ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onContentChange(if (content.isBlank()) t else content + "；" + t)
                                    },
                                    label = { Text(t) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (content.isNotBlank()) onSave(content) }, enabled = content.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

// ===== 添加重点事项对话框 =====
@Composable
private fun AddFocusDialog(
    patientId: Long,
    onDismiss: () -> Unit,
    onSave: (FocusItem) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加重点事项") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：复查血常规") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Text("优先级", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "低", 1 to "中", 2 to "高").forEach { (p, label) ->
                        FilterChip(selected = priority == p, onClick = { priority = p }, label = { Text(label) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(FocusItem(patientId = patientId, content = content, priority = priority)) },
                enabled = content.isNotBlank()
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
