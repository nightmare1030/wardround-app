package com.wardround.app.ui

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.DrainageRecord
import com.wardround.app.data.InfusionGroup
import com.wardround.app.data.Medication
import com.wardround.app.data.Patient
import com.wardround.app.data.Treatment
import com.wardround.app.data.OrderSet
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val treatmentTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
private val fullDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

private fun usageMultiplier(usage: String): Double = when (usage) {
    "bid", "q12h" -> 2.0
    "tid", "q8h" -> 3.0
    "qid", "q6h" -> 4.0
    "qod" -> 0.5
    "biw" -> 2.0 / 7.0
    "tiw" -> 3.0 / 7.0
    else -> 1.0
}


@Composable
fun TreatmentSection(patientId: Long, showDrainage: Boolean = true) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()

    val treatment by repository.getLatestTreatment(patientId).collectAsState(initial = null)
    val currentTreatment = treatment
    val groupsFlow = remember(currentTreatment?.id) {
        if (currentTreatment == null) flowOf(emptyList()) else repository.getInfusionGroups(currentTreatment.id)
    }
    val groups by groupsFlow.collectAsState(initial = emptyList())
    val medsFlow = remember(currentTreatment?.id) {
        if (currentTreatment == null) flowOf(emptyList()) else repository.getMedications(currentTreatment.id)
    }
    val medications by medsFlow.collectAsState(initial = emptyList())

    val activeGroups = groups.filter { !it.stopped }
    val longGroups = groups.filter { it.orderType == Medication.ORDER_LONG_TERM }
    val tempGroups = groups.filter { it.orderType == Medication.ORDER_TEMP }
    val longTermVolume = activeGroups.filter { it.orderType == Medication.ORDER_LONG_TERM }
        .sumOf { (it.diluentVolume * usageMultiplier(it.usage)).roundToInt() }
    val tempVolume = activeGroups.filter { it.orderType == Medication.ORDER_TEMP }
        .sumOf { (it.diluentVolume * usageMultiplier(it.usage)).roundToInt() }
    val totalVolume = longTermVolume + tempVolume

    val drainages by repository.getDrainageRecords(patientId).collectAsState(initial = emptyList())
    val todayStart = startOfDay(System.currentTimeMillis())
    val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L
    val todayRecord = drainages.firstOrNull { it.recordDate == todayStart }
    val yesterdayRecord = drainages.firstOrNull { it.recordDate == yesterdayStart }

    val allGroupsByPatient by repository.getInfusionGroupsByPatient(patientId).collectAsState(initial = emptyList())
    val volumeHistory = allGroupsByPatient.groupBy { it.treatmentId }
        .map { (_, gs) ->
            val lv = gs.filter { !it.stopped && it.orderType == Medication.ORDER_LONG_TERM }.sumOf { (it.diluentVolume * usageMultiplier(it.usage)).roundToInt() }
            val tv = gs.filter { !it.stopped && it.orderType == Medication.ORDER_TEMP }.sumOf { (it.diluentVolume * usageMultiplier(it.usage)).roundToInt() }
            val date = gs.firstOrNull()?.createdAt ?: 0L
            Triple(date, lv, tv)
        }.sortedByDescending { it.first }.take(7)

    var showEditTreatment by remember { mutableStateOf(false) }
    var showAddMedication by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showOrderSetPicker by remember { mutableStateOf(false) }
    var pendingDeleteTreatment by remember { mutableStateOf<Treatment?>(null) }
    var pendingDeleteMedication by remember { mutableStateOf<Medication?>(null) }
    var pendingStopGroup by remember { mutableStateOf<InfusionGroup?>(null) }
    var pendingDeleteGroup by remember { mutableStateOf<InfusionGroup?>(null) }
    var longTermExpanded by remember { mutableStateOf(true) }
    var tempExpanded by remember { mutableStateOf(true) }
    var showStopped by remember { mutableStateOf(false) }
    val pendingMeds = remember { mutableStateListOf<MedDraft>() }
    var editDraftIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (showDrainage) {
                item { DrainageCard(patientId, todayRecord, yesterdayRecord, drainages) }
            }
            if (volumeHistory.isNotEmpty()) {
                item {
                    Text("补液量历史（最近7天）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            volumeHistory.forEach { (date, lv, tv) ->
                                Text("${fullDayFormat.format(Date(date))}：长期 $lv ml + 临时 $tv ml = ${lv + tv} ml", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }

            item {
                Text("治疗记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (currentTreatment == null) {
                item {
                    Button(onClick = { showEditTreatment = true }, modifier = Modifier.fillMaxWidth()) { Text("添加治疗") }
                }
            } else {
                item {
                    TreatmentInfoCard(
                        treatment = currentTreatment,
                        longTermVolume = longTermVolume,
                        tempVolume = tempVolume,
                        onEdit = { showEditTreatment = true },
                        onDelete = { pendingDeleteTreatment = currentTreatment }
                    )
                }

                if (pendingMeds.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text("待审核用药（${pendingMeds.size}）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00))
                                Spacer(Modifier.height(8.dp))
                                pendingMeds.forEach { d ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(d.meds.joinToString(" + ") { "${it.name} ${it.dose}${it.doseUnit}" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            val desc = buildString {
                                                if (d.groupId != null) append("加入已有组")
                                                else if (d.diluent.isNotBlank()) append("${d.diluent} ${d.diluentVolume}ml")
                                                if (d.orderType == Medication.ORDER_TEMP) append(" · 临时")
                                                else if (d.usage.isNotBlank()) append(" · ${d.usage}")
                                            }
                                            if (desc.isNotBlank()) Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        TextButton(onClick = { editDraftIndex = pendingMeds.indexOf(d) }) { Text("编辑") }
                                        TextButton(onClick = { pendingMeds.remove(d) }) { Text("移除") }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row {
                                    Button(onClick = {
                                        val toAdd = pendingMeds.toList()
                                        pendingMeds.clear()
                                        scope.launch {
                                            toAdd.forEach { d ->
                                                if (d.groupId == null) {
                                                    val gid = repository.addInfusionGroup(InfusionGroup(treatmentId = currentTreatment.id, diluent = d.diluent, diluentVolume = d.diluentVolume, orderType = d.orderType, usage = d.usage))
                                                    d.meds.forEach { m ->
                                                        repository.addMedication(Medication(treatmentId = currentTreatment.id, groupId = gid, name = m.name, dose = m.dose, doseUnit = m.doseUnit))
                                                    }
                                                } else {
                                                    d.meds.forEach { m ->
                                                        repository.addMedication(Medication(treatmentId = currentTreatment.id, groupId = d.groupId, name = m.name, dose = m.dose, doseUnit = m.doseUnit))
                                                    }
                                                }
                                            }
                                        }
                                    }) { Text("确认加入") }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(onClick = { pendingMeds.clear() }) { Text("清空") }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("用药明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { showAddMedication = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("添加用药")
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showTemplatePicker = true }) { Text("调用模板") }
                        TextButton(onClick = { showOrderSetPicker = true }) { Text("套用套餐") }
                        TextButton(onClick = {
                            scope.launch {
                                val drafts = repository.getPreviousTreatmentDrafts(patientId)
                                if (drafts.isEmpty()) {
                                    Toast.makeText(context, "没有可复制的历史医嘱", Toast.LENGTH_SHORT).show()
                                } else {
                                    drafts.forEach { d ->
                                        pendingMeds.add(MedDraft(null, d.diluent, d.diluentVolume, d.orderType, d.usage, d.meds.map { MedEntry(it.name, it.dose, it.doseUnit) }))
                                    }
                                }
                            }
                        }) { Text("复制医嘱") }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth().clickable { longTermExpanded = !longTermExpanded }, verticalAlignment = Alignment.CenterVertically) {
                        Text("长期医嘱（${longGroups.count { !it.stopped }}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
                        Icon(if (longTermExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF1565C0))
                    }
                }

                if (longTermExpanded) {
                    val visibleLong = if (showStopped) longGroups else longGroups.filter { !it.stopped }
                    if (visibleLong.isEmpty()) {
                        item { Text("暂无长期医嘱", color = Color.Gray) }
                    } else {
                        items(visibleLong, key = { it.id }) { g ->
                            InfusionGroupCard(
                                group = g,
                                meds = medications.filter { it.groupId == g.id },
                                onStop = { pendingStopGroup = g },
                                onDelete = { pendingDeleteGroup = g },
                                onDeleteMed = { pendingDeleteMedication = it }
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth().clickable { tempExpanded = !tempExpanded }, verticalAlignment = Alignment.CenterVertically) {
                        Text("临时医嘱（${tempGroups.size}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFEF6C00), modifier = Modifier.weight(1f))
                        Icon(if (tempExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFEF6C00))
                    }
                }

                if (tempExpanded) {
                    if (tempGroups.isEmpty()) {
                        item { Text("暂无临时医嘱", color = Color.Gray) }
                    } else {
                        items(tempGroups, key = { it.id }) { g ->
                            InfusionGroupCard(
                                group = g,
                                meds = medications.filter { it.groupId == g.id },
                                onStop = null,
                                onDelete = { pendingDeleteGroup = g },
                                onDeleteMed = { pendingDeleteMedication = it }
                            )
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("显示已停止", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(1f))
                        FilterChip(selected = showStopped, onClick = { showStopped = !showStopped }, label = { Text(if (showStopped) "开" else "关") })
                    }
                }
            }
        }
    }

    if (showEditTreatment) {
        TreatmentEditDialog(
            treatment = currentTreatment,
            patientId = patientId,
            onDismiss = { showEditTreatment = false },
            onSave = { t ->
                scope.launch { if (t.id == 0L) repository.addTreatment(t) else repository.updateTreatment(t) }
                showEditTreatment = false
            }
        )
    }

    if (showAddMedication && currentTreatment != null) {
        MedicationInputDialog(
            title = "添加用药",
            existingGroups = groups.filter { !it.stopped },
            onDismiss = { showAddMedication = false },
            onSave = { groupId, diluent, vol, orderType, usage, meds ->
                pendingMeds.add(MedDraft(groupId, diluent, vol, orderType, usage, meds))
                showAddMedication = false
            }
        )
    }

    if (editDraftIndex != null && editDraftIndex!! < pendingMeds.size) {
        val draft = pendingMeds[editDraftIndex!!]
        MedicationInputDialog(
            title = "编辑药物",
            existingGroups = groups.filter { !it.stopped },
            initial = draft,
            onDismiss = { editDraftIndex = null },
            onSave = { groupId, diluent, vol, orderType, usage, meds ->
                pendingMeds[editDraftIndex!!] = MedDraft(groupId, diluent, vol, orderType, usage, meds)
                editDraftIndex = null
            }
        )
    }

    if (showTemplatePicker && currentTreatment != null) {
        TreatmentTemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onPick = { templateId ->
                scope.launch {
                    repository.getTemplateDrafts(templateId).forEach { d ->
                        pendingMeds.add(MedDraft(null, d.diluent, d.diluentVolume, d.orderType, d.usage, d.meds.map { MedEntry(it.name, it.dose, it.doseUnit) }))
                    }
                }
                showTemplatePicker = false
            }
        )
    }

    if (showOrderSetPicker && currentTreatment != null) {
        OrderSetPickerDialog(
            onDismiss = { showOrderSetPicker = false },
            onPick = { setId ->
                scope.launch {
                    repository.getOrderSetDrafts(setId).forEach { d ->
                        pendingMeds.add(MedDraft(null, d.diluent, d.diluentVolume, d.orderType, d.usage, d.meds.map { MedEntry(it.name, it.dose, it.doseUnit) }))
                    }
                }
                showOrderSetPicker = false
            }
        )
    }

    pendingDeleteTreatment?.let { t ->
        DeleteConfirmDialog(
            message = "确定删除这条治疗记录吗？其下所有用药也会一并删除。",
            onConfirm = { scope.launch { repository.deleteTreatment(t) }; pendingDeleteTreatment = null },
            onDismiss = { pendingDeleteTreatment = null }
        )
    }

    pendingStopGroup?.let { g ->
        ConfirmDialog(
            title = "停止医嘱",
            message = "确定停止长期医嘱「${g.diluent} ${g.diluentVolume}ml」吗？停止后不再计入补液量。",
            confirmText = "停止",
            onConfirm = { scope.launch { repository.stopInfusionGroup(g) }; pendingStopGroup = null },
            onDismiss = { pendingStopGroup = null }
        )
    }

    pendingDeleteGroup?.let { g ->
        DeleteConfirmDialog(
            message = "确定删除溶媒组「${g.diluent} ${g.diluentVolume}ml」吗？组内所有药物也会删除。",
            onConfirm = { scope.launch { repository.deleteInfusionGroup(g) }; pendingDeleteGroup = null },
            onDismiss = { pendingDeleteGroup = null }
        )
    }

    pendingDeleteMedication?.let { m ->
        DeleteConfirmDialog(
            message = "确定删除用药「${m.name}」吗？",
            onConfirm = { scope.launch { repository.deleteMedication(m) }; pendingDeleteMedication = null },
            onDismiss = { pendingDeleteMedication = null }
        )
    }
}

@Composable
private fun InfusionGroupCard(
    group: InfusionGroup,
    meds: List<Medication>,
    onStop: (() -> Unit)?,
    onDelete: () -> Unit,
    onDeleteMed: (Medication) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    meds.joinToString(" + ") { "${it.name}${if (it.dose.isNotBlank()) " ${it.dose}${it.doseUnit}" else ""}" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (group.stopped) {
                    Text("（已停止）", style = MaterialTheme.typography.labelSmall, color = Color.Red)
                }
                if (onStop != null) {
                    TextButton(onClick = onStop) { Text("停止") }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除组", tint = Color.Red)
                }
            }
            Text(
                "${group.diluent} ${group.diluentVolume}ml · ${group.usage}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            meds.forEach { m ->
                Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("· ${m.name} ${m.dose}${m.doseUnit}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onDeleteMed(m) }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除药", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
private fun DrainageCard(
    patientId: Long,
    todayRecord: DrainageRecord?,
    yesterdayRecord: DrainageRecord?,
    history: List<DrainageRecord>
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()
    var volumeText by remember(todayRecord?.volume) { mutableStateOf(todayRecord?.volume?.toString() ?: "") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("今日引流量", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = volumeText,
                    onValueChange = { volumeText = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("引流量 (ml)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    val v = volumeText.toIntOrNull()
                    if (v != null) scope.launch { repository.saveDrainage(patientId, v) }
                }) { Text("记录") }
            }
            Spacer(Modifier.height(8.dp))
            Text("昨日引流：" + if (yesterdayRecord != null) "${yesterdayRecord.volume} ml" else "未记录", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF1565C0))
            if (history.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("历史引流", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                history.take(7).forEach { r ->
                    Text("${fullDayFormat.format(Date(r.recordDate))}：${r.volume} ml", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun TreatmentInfoCard(
    treatment: Treatment,
    longTermVolume: Int,
    tempVolume: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("目前治疗", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "编辑") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red) }
            }
            Spacer(Modifier.height(4.dp))
            Text("日期：" + treatmentTimeFormat.format(Date(treatment.treatmentDate)), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            if (treatment.doctorOrder.isNotBlank()) {
                Text("文字医嘱：", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(treatment.doctorOrder, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                "补液量：长期 $longTermVolume ml + 临时 $tempVolume ml = ${longTermVolume + tempVolume} ml",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )
        }
    }
}

@Composable
private fun TreatmentEditDialog(
    treatment: Treatment?,
    patientId: Long,
    onDismiss: () -> Unit,
    onSave: (Treatment) -> Unit
) {
    var doctorOrder by remember(treatment?.id) { mutableStateOf(treatment?.doctorOrder ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (treatment == null) "添加治疗" else "编辑治疗") },
        text = {
            Column {
                OutlinedTextField(
                    value = doctorOrder,
                    onValueChange = { doctorOrder = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("文字医嘱") },
                    placeholder = { Text("例如：继续抗感染治疗") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = if (treatment == null) Treatment(patientId = patientId, doctorOrder = doctorOrder) else treatment.copy(doctorOrder = doctorOrder)
                onSave(t)
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun TreatmentTemplatePickerDialog(
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val templates by repository.getTreatmentTemplates().collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择医嘱模板") },
        text = {
            Column {
                if (templates.isEmpty()) {
                    Text("暂无模板，请先在设置「医嘱模板」中创建", color = Color.Gray)
                } else {
                    templates.forEach { t ->
                        TextButton(onClick = { onPick(t.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(t.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun OrderSetPickerDialog(
    onDismiss: () -> Unit,
    onPick: (Long) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val sets by repository.getOrderSets().collectAsState(initial = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择医嘱套餐") },
        text = {
            Column {
                if (sets.isEmpty()) {
                    Text("暂无套餐，请先在设置「医嘱套餐」中创建", color = Color.Gray)
                } else {
                    sets.forEach { s ->
                        TextButton(onClick = { onPick(s.id) }, modifier = Modifier.fillMaxWidth()) {
                            Text(s.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun startOfDay(timestamp: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timestamp
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}