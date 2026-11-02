package com.wardround.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.data.DiagnosisList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiagnosisPickerSheet(
    initial: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    var query by remember { mutableStateOf("") }
    val results = DiagnosisList.filter(query)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Text("选择诊断", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入拼音首字母（如 fx）或汉字（如 肺炎）") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            if (selected.isNotEmpty()) {
                Text("已选诊断（${selected.size}）", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    selected.forEach { s ->
                        AssistChip(
                            onClick = {},
                            label = { Text(s) },
                            trailingIcon = {
                                IconButton(onClick = { selected.remove(s) }, modifier = Modifier.height(20.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.height(14.dp))
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
            }

            if (query.isNotBlank()) {
                Text("匹配结果（${results.size}）", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
            }

            LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                if (query.isBlank()) {
                    item { Text("输入关键词开始搜索", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp)) }
                } else if (results.isEmpty()) {
                    item { Text("无匹配诊断，可手动输入", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp)) }
                } else {
                    items(results) { name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (!selected.contains(name)) selected.add(name) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selected.contains(name)) Color(0xFF007AFF) else Color.Unspecified,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected.contains(name)) {
                                Text("已选", style = MaterialTheme.typography.labelSmall, color = Color(0xFF007AFF))
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onConfirm(selected.toList()) },
                    modifier = Modifier.weight(1f)
                ) { Text("确定（${selected.size}）") }
            }
        }
    }
}