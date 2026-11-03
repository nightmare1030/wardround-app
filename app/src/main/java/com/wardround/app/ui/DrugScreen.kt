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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.DiagnosisList
import com.wardround.app.data.Drug
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()
    val drugs by repository.getDrugs().collectAsState(initial = emptyList())

    var editing by remember { mutableStateOf<Drug?>(null) }
    var pendingDelete by remember { mutableStateOf<Drug?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("常用药管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = Drug(name = "") }) {
                Icon(Icons.Default.Add, contentDescription = "新增常用药")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (drugs.isEmpty()) {
                item { Text("暂无常用药", color = Color.Gray) }
            } else {
                items(drugs, key = { it.id }) { d ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(d.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    if (d.isAntibiotic) {
                                        Spacer(Modifier.width(6.dp))
                                        Text("抗生素", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD32F2F))
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    buildString {
                                        if (d.defaultDose.isNotBlank()) append("${d.defaultDose}${d.defaultUnit}")
                                        if (d.defaultUsage.isNotBlank()) append(" · ${d.defaultUsage}")
                                        if (isNotEmpty()) append(" · 常用 ${d.useCount} 次")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { editing = d }) { Icon(Icons.Default.Edit, contentDescription = "编辑") }
                            IconButton(onClick = { pendingDelete = d }) { Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }

    editing?.let { d ->
        DrugEditDialog(
            drug = d,
            onDismiss = { editing = null },
            onSave = { saved ->
                scope.launch { repository.saveDrug(saved) }
                editing = null
            }
        )
    }

    pendingDelete?.let { d ->
        DeleteConfirmDialog(
            message = "确定删除常用药「${d.name}」吗？",
            onConfirm = { scope.launch { repository.deleteDrug(d) }; pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrugEditDialog(
    drug: Drug,
    onDismiss: () -> Unit,
    onSave: (Drug) -> Unit
) {
    var name by remember(drug.id) { mutableStateOf(drug.name) }
    var dose by remember(drug.id) { mutableStateOf(drug.defaultDose) }
    var unit by remember(drug.id) { mutableStateOf(drug.defaultUnit.ifBlank { "g" }) }
    var usage by remember(drug.id) { mutableStateOf(drug.defaultUsage.ifBlank { usages.first() }) }
    var isAntibiotic by remember(drug.id) { mutableStateOf(drug.isAntibiotic) }

    var unitExpanded by remember { mutableStateOf(false) }
    var usageExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (drug.id == 0L) "新增常用药" else "编辑常用药") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("药名 *") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dose,
                        onValueChange = { dose = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("常用剂量") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("单位") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().width(100.dp)
                        )
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            doseUnits.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = usageExpanded, onExpandedChange = { usageExpanded = it }) {
                    OutlinedTextField(
                        value = usage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("常用用法") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = usageExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = usageExpanded, onDismissRequest = { usageExpanded = false }) {
                        usages.forEach { u ->
                            DropdownMenuItem(text = { Text(u) }, onClick = { usage = u; usageExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("标记为抗生素", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = isAntibiotic, onCheckedChange = { isAntibiotic = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        drug.copy(
                            name = name.trim(),
                            initials = DiagnosisList.pinyinInitials(name.trim()),
                            defaultDose = dose.trim(),
                            defaultUnit = unit,
                            defaultUsage = usage,
                            isAntibiotic = isAntibiotic
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
