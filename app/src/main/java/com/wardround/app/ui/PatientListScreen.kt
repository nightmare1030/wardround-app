package com.wardround.app.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wardround.app.WardRoundApp
import com.wardround.app.data.Patient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PatientListScreen(
    onBack: () -> Unit,
    onAddPatient: () -> Unit,
    onOpenPatient: (Long) -> Unit,
    statusFilter: String? = null
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as WardRoundApp).repository
    val viewModel: PatientListViewModel = viewModel(
        factory = PatientListViewModel.factory(repository)
    )
    val patients by viewModel.patients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var pendingDelete by remember { mutableStateOf<Patient?>(null) }
    var actionPatient by remember { mutableStateOf<Patient?>(null) }
    var datePickerPatient by remember { mutableStateOf<Patient?>(null) }
    var pendingDischarge by remember { mutableStateOf<Patient?>(null) }
    var pendingUndischarge by remember { mutableStateOf<Patient?>(null) }
    var showDateFilter by remember { mutableStateOf(false) }
    var filterStart by remember { mutableStateOf<Long?>(null) }
    var filterEnd by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (statusFilter != null) "出院患者" else "患者列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (statusFilter == "出院") {
                        TextButton(onClick = { showDateFilter = true }) { Text("查询") }
                    }
                }
            )
        },
        floatingActionButton = {
            if (statusFilter == null) {
                FloatingActionButton(onClick = onAddPatient) {
                    Icon(Icons.Default.Add, contentDescription = "新增患者")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索姓名 / 床号 / 住院号") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            val displayPatients = when {
                statusFilter != null -> patients.filter { it.status == statusFilter }
                else -> patients.filter { it.status != "出院" }
            }.filter { p ->
                (filterStart == null || p.updatedAt >= filterStart!!) &&
                    (filterEnd == null || p.updatedAt <= filterEnd!!)
            }.sortedBy { bedNoNumber(it.bedNo) }
            if (displayPatients.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("暂无患者", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("点击右下角 + 新增患者，长按患者可设置手术日期/出院", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayPatients, key = { it.id }) { patient ->
                        PatientCard(
                            patient = patient,
                            onClick = { onOpenPatient(patient.id) },
                            onLongPress = { actionPatient = patient },
                            onDelete = { pendingDelete = patient }
                        )
                    }
                }
            }
        }
    }

    // 长按操作菜单
    actionPatient?.let { p ->
        AlertDialog(
            onDismissRequest = { actionPatient = null },
            title = { Text(p.name) },
            text = {
                Column {
                    TextButton(onClick = {
                        actionPatient = null
                        datePickerPatient = p
                    }) { Text("设置手术信息") }
                    if (p.status == "出院") {
                        TextButton(onClick = {
                            actionPatient = null
                            pendingUndischarge = p
                        }) { Text("取消出院", color = Color(0xFF1565C0)) }
                    } else {
                        TextButton(onClick = {
                            actionPatient = null
                            pendingDischarge = p
                        }) { Text("标记出院", color = Color(0xFFD32F2F)) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { actionPatient = null }) { Text("取消") }
            }
        )
    }

    // 手术信息设置
    datePickerPatient?.let { patient ->
        SurgeryInfoDialog(
            patient = patient,
            onDismiss = { datePickerPatient = null },
            onSave = { surgeryDate, startTime, endTime ->
                viewModel.setSurgeryInfo(patient, surgeryDate, startTime, endTime)
                datePickerPatient = null
            }
        )
    }

    // 出院确认
    pendingDischarge?.let { p ->
        ConfirmDialog(
            title = "标记出院",
            message = "确定将患者「${p.name}」标记为出院吗？",
            confirmText = "出院",
            onConfirm = {
                viewModel.markDischarged(p)
                pendingDischarge = null
            },
            onDismiss = { pendingDischarge = null }
        )
    }

    // 取消出院确认
    pendingUndischarge?.let { p ->
        ConfirmDialog(
            title = "取消出院",
            message = "确定取消患者「${p.name}」的出院状态吗？将重新回到患者列表。",
            confirmText = "取消出院",
            onConfirm = {
                viewModel.markUndischarged(p)
                pendingUndischarge = null
            },
            onDismiss = { pendingUndischarge = null }
        )
    }

    // 日期范围查询
    if (showDateFilter) {
        DateRangeFilterDialog(
            onDismiss = { showDateFilter = false },
            onConfirm = { start, end ->
                filterStart = start
                filterEnd = end
                showDateFilter = false
            }
        )
    }

    // 删除确认
    pendingDelete?.let { patient ->
        DeleteConfirmDialog(
            message = "确定删除患者「${patient.name}」吗？其查房记录、重点事项、治疗、检查等关联数据会一并删除。",
            onConfirm = {
                viewModel.deletePatient(patient)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onClick = onClick,
            onLongClick = onLongPress
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(patient.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (patient.hospitalNo.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(patient.hospitalNo, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    StatusBadge(patient.status)
                }
                Spacer(Modifier.height(4.dp))
                if (patient.status == "出院") {
                    Text(
                        "出院日期：" + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(patient.updatedAt)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    val surgeryDays = daysAfterSurgery(patient.surgeryDate)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (patient.bedNo.isNotBlank()) {
                            Text("床号 ${patient.bedNo}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (surgeryDays != null) "术后第 $surgeryDays 天" else "待手术",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (surgeryDays != null) Color(0xFF1565C0) else Color.Gray
                        )
                    }
                }
                if (patient.diagnosis.isNotBlank()) {
                    Text(patient.diagnosis, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val color = when (status) {
        "在院" -> Color(0xFF2E7D32)
        "出院" -> Color(0xFF757575)
        "转科" -> Color(0xFFEF6C00)
        else -> Color.Gray
    }
    Text(
        status,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
private fun bedNoNumber(bedNo: String): Int {
    val digits = bedNo.filter { it.isDigit() }
    return digits.toIntOrNull() ?: Int.MAX_VALUE
}

private fun daysAfterSurgery(surgeryDate: Long?): Int? {
    if (surgeryDate == null) return null
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val start = fmt.parse(fmt.format(Date(surgeryDate)))!!.time
    val end = fmt.parse(fmt.format(Date()))!!.time
    return ((end - start) / (1000 * 60 * 60 * 24)).toInt()
}
@Composable
private fun DateRangeFilterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long?, Long?) -> Unit
) {
    val context = LocalContext.current
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var start by remember { mutableStateOf<Long?>(null) }
    var end by remember { mutableStateOf<Long?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("按出院日期查询") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开始日期", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(
                        start?.let { dateFmt.format(Date(it)) } ?: "不限",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("结束日期", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(
                        end?.let { dateFmt.format(Date(it)) } ?: "不限",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                TextButton(onClick = { start = null; end = null }) { Text("清除筛选") }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(start, end) }) { Text("查询") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    if (showStartPicker) {
        val cal = Calendar.getInstance()
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    start = Calendar.getInstance().apply {
                        set(y, m, d, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    showStartPicker = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    if (showEndPicker) {
        val cal = Calendar.getInstance()
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    end = Calendar.getInstance().apply {
                        set(y, m, d, 23, 59, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.timeInMillis
                    showEndPicker = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

@Composable
private fun SurgeryInfoDialog(
    patient: Patient,
    onDismiss: () -> Unit,
    onSave: (surgeryDate: Long, startTime: Long, endTime: Long) -> Unit
) {
    val context = LocalContext.current
    val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    var dateText by remember { mutableStateOf(patient.surgeryDate?.let { dateFmt.format(Date(it)) } ?: dateFmt.format(Date())) }
    var startText by remember { mutableStateOf(patient.surgeryStartTime?.let { timeFmt.format(Date(it)) } ?: "08:00") }
    var endText by remember { mutableStateOf(patient.surgeryEndTime?.let { timeFmt.format(Date(it)) } ?: "10:00") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    fun parse(date: String, time: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("$date $time")!!.time

    val duration = (parse(dateText, endText) - parse(dateText, startText)) / 60000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置手术信息") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("手术日期", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(dateText, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showStartPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("开始时间", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(startText, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showEndPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("结束时间", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(endText, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
                if (duration >= 0) {
                    Text("手术时长：$duration 分钟", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1565C0))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    parse(dateText, "00:00"),
                    parse(dateText, startText),
                    parse(dateText, endText)
                )
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )

    if (showDatePicker) {
        val cal = Calendar.getInstance()
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d -> dateText = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d); showDatePicker = false },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    if (showStartPicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, h, m -> startText = String.format(Locale.getDefault(), "%02d:%02d", h, m); showStartPicker = false },
                8, 0, true
            ).show()
        }
    }

    if (showEndPicker) {
        LaunchedEffect(Unit) {
            TimePickerDialog(
                context,
                { _, h, m -> endText = String.format(Locale.getDefault(), "%02d:%02d", h, m); showEndPicker = false },
                10, 0, true
            ).show()
        }
    }
}