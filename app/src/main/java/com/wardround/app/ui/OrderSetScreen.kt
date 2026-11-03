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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.wardround.app.data.InfusionGroup
import com.wardround.app.data.Medication
import com.wardround.app.data.MedicationGroupDraft
import com.wardround.app.data.OrderSet
import com.wardround.app.data.OrderSetMedEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSetScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()
    val sets by repository.getOrderSets().collectAsState(initial = emptyList())

    var editingSet by remember { mutableStateOf<OrderSet?>(null) }
    var pendingDelete by remember { mutableStateOf<OrderSet?>(null) }

    if (editingSet == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("医嘱套餐") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { editingSet = OrderSet(name = "") }) {
                    Icon(Icons.Default.Add, contentDescription = "新建套餐")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (sets.isEmpty()) {
                    item {
                        Text("暂无套餐，点右下角新建。", color = Color.Gray)
                    }
                } else {
                    items(sets, key = { it.id }) { set ->
                        Card(onClick = { editingSet = set }, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(set.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("点按可编辑", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                IconButton(onClick = { pendingDelete = set }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }

        pendingDelete?.let { set ->
            DeleteConfirmDialog(
                message = "确定删除套餐「${set.name}」吗？",
                onConfirm = { scope.launch { repository.deleteOrderSet(set) }; pendingDelete = null },
                onDismiss = { pendingDelete = null }
            )
        }
    } else {
        OrderSetEdit(
            set = editingSet!!,
            onBack = { editingSet = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderSetEdit(
    set: OrderSet,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(set.name) }
    val items = remember { mutableStateListOf<MedicationGroupDraft>() }
    var showAddGroup by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(set.id) {
        if (set.id != 0L && !loaded) {
            repository.getOrderSetDrafts(set.id).let { items.addAll(it) }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (set.id == 0L) "新建套餐" else "编辑套餐") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("套餐名称") },
                placeholder = { Text("例如：术后常规") },
                singleLine = true
            )

            Text("溶媒组（${items.size}）", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            if (items.isEmpty()) {
                Text("暂无溶媒组，点下方「添加一组」", color = Color.Gray)
            } else {
                items.forEachIndexed { i, g ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${g.diluent} ${g.diluentVolume}ml",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        g.meds.joinToString(" + ") { "${it.name} ${it.dose}${it.doseUnit}" },
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        if (g.orderType == Medication.ORDER_TEMP) "临时 · ${g.usage}" else "长期 · ${g.usage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                TextButton(onClick = { items.removeAt(i) }) { Text("删除") }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { showAddGroup = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加一组")
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = {
                    scope.launch {
                        repository.saveOrderSet(set.id, name.trim(), items.toList())
                        onBack()
                    }
                },
                enabled = name.isNotBlank() && items.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("保存套餐") }
        }
    }

    if (showAddGroup) {
        MedicationInputDialog(
            title = "添加溶媒组",
            existingGroups = emptyList(),
            forceNewGroup = true,
            onDismiss = { showAddGroup = false },
            onSave = { _, diluent, vol, orderType, usage, meds ->
                items.add(
                    MedicationGroupDraft(
                        diluent = diluent,
                        diluentVolume = vol,
                        orderType = orderType,
                        usage = usage,
                        meds = meds.map { OrderSetMedEntry(it.name, it.dose, it.doseUnit) }
                    )
                )
                showAddGroup = false
            }
        )
    }
}

