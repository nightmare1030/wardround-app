package com.wardround.app.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.core.content.ContextCompat
import com.wardround.app.WardRoundApp
import com.wardround.app.reminder.ReminderManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenDeletedPatients: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.6"
        } catch (e: Exception) { "1.6" }
    }


    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = repository.exportAllData()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    } ?: throw Exception("无法写入文件")
                    Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    } ?: throw Exception("无法读取文件")
                    val count = repository.importAllData(json)
                    Toast.makeText(context, "导入成功，恢复 $count 位患者", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导入失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportTextLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = repository.generateSummaryText()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(text.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "文本汇总导出成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val exportPdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = repository.generateSummaryText()
                    val pdfBytes = generateSummaryPdf(text)
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(pdfBytes)
                    }
                    Toast.makeText(context, "PDF 汇总导出成功", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            HorizontalDivider()
            Spacer(Modifier.height(4.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "医嘱模板",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenTemplates) { Text("管理") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "已删除患者",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenDeletedPatients) { Text("查看") }
                }
            }

            Text("数据管理", style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("导出查房汇总", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "按患者导出全部查房记录汇总",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        TextButton(onClick = { exportTextLauncher.launch("查房汇总.txt") }) { Text("导出文本") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { exportPdfLauncher.launch("查房汇总.pdf") }) { Text("导出 PDF") }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("备份数据", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "将全部患者、查房记录、重点事项和操作历史导出为 JSON 文件",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { exportLauncher.launch("查房记录备份.json") }
                    ) { Text("导出备份") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("恢复数据", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "从之前导出的 JSON 备份文件恢复数据（会追加到现有数据）",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    ) { Text("导入备份") }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "查看全局操作历史",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onOpenHistory) { Text("进入") }
                }
            }

            Text("关于", style = MaterialTheme.typography.titleMedium, color = Color.Gray)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("版本号", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(versionName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("制作者", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text("你看我今天几点下班", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
private fun generateSummaryPdf(text: String): ByteArray {
    val pdf = PdfDocument()
    val paint = Paint().apply {
        textSize = 12f
        color = android.graphics.Color.BLACK
        isAntiAlias = true
    }
    val pageWidth = 595
    val pageHeight = 842
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    var page = pdf.startPage(pageInfo)
    var canvas = page.canvas
    var y = 40f
    val lineHeight = 18f
    for (line in text.split("\n")) {
        if (y > pageHeight - 40f) {
            pdf.finishPage(page)
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            page = pdf.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }
        canvas.drawText(line, 40f, y, paint)
        y += lineHeight
    }
    pdf.finishPage(page)
    val baos = ByteArrayOutputStream()
    pdf.writeTo(baos)
    pdf.close()
    return baos.toByteArray()
}
