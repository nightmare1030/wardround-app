package com.wardround.app.ui

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.TemplateMedication
import com.wardround.app.data.TreatmentTemplate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()
    val templates by repository.getTreatmentTemplates().collectAsState(initial = emptyList())

    var showEdit by remember { mutableStateOf(false) }
    var editTemplate by remember { mutableStateOf<TreatmentTemplate?>(null) }
    var pendingDelete by remember { mutableStateOf<TreatmentTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("医嘱模板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editTemplate = null; showEdit = true }) {
                Icon(Icons.Default.Add, contentDescription = "新增模板")
            }
        }
    ) { padding ->
        if (templates.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("暂无医嘱模板", color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text("点右下角 + 新建用药方案模板", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.id }) { t ->
                    TemplateCard(
                        template = t,
                        onClick = { editTemplate = t; showEdit = true },
                        onDelete = { pendingDelete = t }
                    )
                }
            }
        }
    }

    if (showEdit) {
        TemplateEditDialog(
            template = editTemplate,
            onDismiss = { showEdit = false },
            onSave = { name, meds ->
                scope.launch {
                    repository.saveTemplateWithMeds(
                        TreatmentTemplate(
                            id = editTemplate?.id ?: 0L,
                            name = name,
                            content = ""
                        ),
                        meds
                    )
                }
                showEdit = false
            }
        )
    }

    pendingDelete?.let { t ->
        DeleteConfirmDialog(
            message = "确定删除医嘱模板「${t.name}」吗？",
            onConfirm = {
                scope.launch { repository.deleteTreatmentTemplate(t) }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun TemplateCard(
    template: TreatmentTemplate,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("点击编辑用药方案", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateEditDialog(
    template: TreatmentTemplate?,
    onDismiss: () -> Unit,
    onSave: (String, List<TemplateMedication>) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()

    var name by remember(template?.id) { mutableStateOf(template?.name ?: "") }
    val meds = remember { mutableStateListOf<TemplateMedication>() }
    var showAddMed by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(template?.id) {
        if (template != null && !loaded) {
            val existing = repository.getTemplateMedications(template.id).first()
            meds.clear()
            meds.addAll(existing)
            loaded = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "新增医嘱模板" else "编辑医嘱模板") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("模板名称 *") },
                    placeholder = { Text("例如：常规抗感染") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "用药明细（${meds.size}）",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = { showAddMed = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("添加药")
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (meds.isEmpty()) {
                    Text("暂无用药，点「添加药」录入", color = Color.Gray)
                } else {
                    meds.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                val desc = buildString {
                                    if (m.dose.isNotBlank()) append("剂量 ${m.dose}${m.doseUnit}")
                                    if (m.diluent.isNotBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append(m.diluent)
                                    }
                                    if (m.diluentVolume > 0) append(" ${m.diluentVolume}ml")
                                }
                                if (desc.isNotBlank()) {
                                    Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                            IconButton(onClick = { meds.remove(m) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, meds.toList()) },
                enabled = name.isNotBlank() && meds.isNotEmpty()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )

    if (showAddMed) {
        MedicationInputDialog(
            title = "添加药",
            existingGroups = emptyList(),
            onDismiss = { showAddMed = false },
            onSave = { groupId, diluent, vol, orderType, usage, entries ->
                entries.forEach { e ->
                    meds.add(
                        TemplateMedication(
                            templateId = template?.id ?: 0L,
                            name = e.name,
                            dose = e.dose,
                            doseUnit = e.doseUnit,
                            diluent = diluent,
                            diluentVolume = vol,
                            orderType = orderType,
                            usage = usage
                        )
                    )
                }
                showAddMed = false
            }
        )
    }
}