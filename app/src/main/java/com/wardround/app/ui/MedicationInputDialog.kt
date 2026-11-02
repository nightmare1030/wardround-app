package com.wardround.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wardround.app.data.InfusionGroup
import com.wardround.app.data.Medication

internal val doseUnits = listOf("g", "mg", "μg", "ml", "U", "片", "支", "袋", "瓶")
internal val diluents = listOf("生理盐水", "5%葡萄糖", "10%葡萄糖", "葡萄糖氯化钠", "复方氯化钠", "灭菌注射用水")
internal val volumes = listOf(50, 100, 250, 500, 1440)
internal val usages = listOf("qd", "bid", "tid", "qid", "q6h", "q8h", "q12h", "qn", "qod", "st", "prn", "biw", "tiw", "睡前", "晨服", "餐前", "餐后")

data class MedEntry(val name: String, val dose: String, val doseUnit: String)

data class MedDraft(
    val groupId: Long?,
    val diluent: String,
    val diluentVolume: Int,
    val orderType: String,
    val usage: String,
    val meds: List<MedEntry>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MedicationInputDialog(
    title: String,
    existingGroups: List<InfusionGroup>,
    initial: MedDraft? = null,
    onDismiss: () -> Unit,
    onSave: (groupId: Long?, diluent: String, diluentVolume: Int, orderType: String, usage: String, meds: List<MedEntry>) -> Unit
) {
    var groupMode by remember { mutableStateOf(if (initial?.groupId != null) "existing" else "new") }
    var selectedGroup by remember { mutableStateOf(existingGroups.firstOrNull { it.id == initial?.groupId }) }
    var diluent by remember { mutableStateOf(initial?.diluent ?: diluents.first()) }
    var volumeText by remember { mutableStateOf(initial?.diluentVolume?.takeIf { it > 0 }?.toString() ?: "") }
    var selectedVolume by remember { mutableStateOf<Int?>(initial?.diluentVolume?.takeIf { it in volumes }) }
    var orderType by remember { mutableStateOf(initial?.orderType ?: Medication.ORDER_LONG_TERM) }
    var usage by remember { mutableStateOf(initial?.usage ?: usages.first()) }

    val meds = remember { mutableStateListOf<MedEntry>().apply { initial?.meds?.let { addAll(it) } } }
    var curName by remember { mutableStateOf("") }
    var curDose by remember { mutableStateOf("") }
    var curUnit by remember { mutableStateOf("g") }

    var diluentExpanded by remember { mutableStateOf(false) }
    var usageExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    val isTemp = orderType == Medication.ORDER_TEMP
    val isNewGroup = groupMode == "new"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 药物录入
                Text("药物（可添加多味）", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = curName,
                        onValueChange = { curName = it },
                        modifier = Modifier.weight(1.3f),
                        label = { Text("药名") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = curDose,
                        onValueChange = { curDose = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("剂量") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                        OutlinedTextField(
                            value = curUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("单位") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.menuAnchor().width(90.dp)
                        )
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            doseUnits.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { curUnit = u; unitExpanded = false })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = {
                        if (curName.isNotBlank()) {
                            meds.add(MedEntry(curName, curDose, curUnit))
                            curName = ""
                            curDose = ""
                        }
                    }) { Text("加入药物") }
                    Spacer(Modifier.width(8.dp))
                    Text("已加入 ${meds.size} 味", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                if (meds.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    meds.forEach { m ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "· ${m.name} ${m.dose}${m.doseUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { meds.remove(m) }) { Text("删除") }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider2()
                Spacer(Modifier.height(8.dp))

                // 溶媒组选择
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = isNewGroup, onClick = { groupMode = "new" }, label = { Text("新开溶媒组") })
                    FilterChip(selected = !isNewGroup, onClick = { groupMode = "existing" }, label = { Text("加入已有组") })
                }
                Spacer(Modifier.height(8.dp))

                if (isNewGroup) {
                    ExposedDropdownMenuBox(expanded = diluentExpanded, onExpandedChange = { diluentExpanded = it }) {
                        OutlinedTextField(
                            value = diluent,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("配伍用水（溶媒）") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diluentExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = diluentExpanded, onDismissRequest = { diluentExpanded = false }) {
                            diluents.forEach { d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = { diluent = d; diluentExpanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("用水量 (ml)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        volumes.forEach { v ->
                            FilterChip(selected = selectedVolume == v, onClick = { selectedVolume = v; volumeText = v.toString() }, label = { Text(v.toString()) })
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = volumeText,
                        onValueChange = { volumeText = it.filter { c -> c.isDigit() }; selectedVolume = null },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("自定义水量 (ml)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("医嘱类型", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(Medication.ORDER_LONG_TERM, Medication.ORDER_TEMP).forEach { t ->
                            FilterChip(selected = orderType == t, onClick = { orderType = t; if (t == Medication.ORDER_TEMP) usage = "st" }, label = { Text(if (t == Medication.ORDER_LONG_TERM) "长期医嘱" else "临时医嘱") })
                        }
                    }
                    if (!isTemp) {
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(expanded = usageExpanded, onExpandedChange = { usageExpanded = it }) {
                            OutlinedTextField(
                                value = usage,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("用法") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = usageExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = usageExpanded, onDismissRequest = { usageExpanded = false }) {
                                usages.forEach { u ->
                                    DropdownMenuItem(text = { Text(u) }, onClick = { usage = u; usageExpanded = false })
                                }
                            }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = groupExpanded, onExpandedChange = { groupExpanded = it }) {
                        OutlinedTextField(
                            value = selectedGroup?.let { "${it.diluent} ${it.diluentVolume}ml" } ?: "无可用组",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("选择溶媒组") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                            existingGroups.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text("${g.diluent} ${g.diluentVolume}ml（${if (g.orderType == Medication.ORDER_LONG_TERM) "长期" else "临时"}）") },
                                    onClick = { selectedGroup = g; groupExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalGroupId = if (isNewGroup) null else selectedGroup?.id
                    val finalDiluent = if (isNewGroup) diluent else selectedGroup?.diluent ?: ""
                    val finalVolume = if (isNewGroup) (volumeText.toIntOrNull() ?: 0) else (selectedGroup?.diluentVolume ?: 0)
                    val finalUsage = if (isTemp) "st" else usage
                    onSave(finalGroupId, finalDiluent, finalVolume, orderType, finalUsage, meds.toList())
                },
                enabled = meds.isNotEmpty() && (isNewGroup || selectedGroup != null)
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun HorizontalDivider2() {
    androidx.compose.material3.HorizontalDivider()
}