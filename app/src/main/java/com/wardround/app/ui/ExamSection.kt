package com.wardround.app.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.wardround.app.WardRoundApp
import com.wardround.app.data.Exam
import com.wardround.app.data.Patient
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val examTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
fun ExamSection(patientId: Long) {
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }
    val scope = rememberCoroutineScope()

    val exams by repository.getExams(patientId).collectAsState(initial = emptyList())
    var showAddExam by remember { mutableStateOf(false) }
    var pendingDeleteExam by remember { mutableStateOf<Exam?>(null) }
    var previewImage by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (exams.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无检查结果，点右下角添加", color = Color.Gray)
                    }
                }
            } else {
                items(exams, key = { it.id }) { exam ->
                    ExamCard(exam = exam, onDelete = { pendingDeleteExam = exam }, onImageClick = { previewImage = exam.imagePath })
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddExam = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加检查")
        }
    }

    if (showAddExam) {
        AddExamDialog(
            patientId = patientId,
            onDismiss = { showAddExam = false },
            onSave = { exam ->
                scope.launch { repository.addExam(exam) }
                showAddExam = false
            }
        )
    }

    previewImage?.let { path ->
        ImagePreviewDialog(path = path, onDismiss = { previewImage = null })
    }

    pendingDeleteExam?.let { exam ->
        DeleteConfirmDialog(
            message = "确定删除检查「${exam.name}」吗？",
            onConfirm = {
                scope.launch { repository.deleteExam(exam) }
                pendingDeleteExam = null
            },
            onDismiss = { pendingDeleteExam = null }
        )
    }
}

@Composable
private fun ExamCard(exam: Exam, onDelete: () -> Unit, onImageClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExamTypeBadge(exam.type)
                        Spacer(Modifier.width(8.dp))
                        Text(exam.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        examTimeFormat.format(Date(exam.examDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    if (exam.note.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(exam.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color.Red)
                }
            }
            if (exam.imagePath.isNotBlank() && File(exam.imagePath).exists()) {
                Spacer(Modifier.height(8.dp))
                LocalExamImage(exam.imagePath, Modifier.fillMaxWidth().height(180.dp).clickable { onImageClick() })
            }
        }
    }
}

@Composable
private fun ExamTypeBadge(type: String) {
    val (label, color) = if (type == Exam.TYPE_LAB) {
        "检验" to Color(0xFF1565C0)
    } else {
        "检查" to Color(0xFF6A1B9A)
    }
    Text(
        label,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun LocalExamImage(path: String, modifier: Modifier = Modifier) {
    val bitmap = remember(path) {
        val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
        BitmapFactory.decodeFile(path, opts)
    }
    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = modifier)
    }
}

@Composable
private fun AddExamDialog(
    patientId: Long,
    onDismiss: () -> Unit,
    onSave: (Exam) -> Unit
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(Exam.TYPE_LAB) }
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) imagePath = null
    }

    fun takePhoto() {
        val dir = File(context.filesDir, "images").apply { mkdirs() }
        val file = File(dir, "EXAM_${System.currentTimeMillis()}.jpg")
        imagePath = file.absolutePath
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePictureLauncher.launch(uri)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加检查结果") },
        text = {
            Column {
                Text("类型", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Exam.TYPE_LAB to "检验", Exam.TYPE_IMAGING to "检查").forEach { (t, label) ->
                        FilterChip(selected = type == t, onClick = { type = t }, label = { Text(label) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("项目名称 *") },
                    placeholder = { Text("如：血常规 / 胸部CT") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注 / 结果摘要") },
                    minLines = 2
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { takePhoto() }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("拍照上传")
                }
                val currentImage = imagePath
                if (currentImage != null && File(currentImage).exists()) {
                    Spacer(Modifier.height(8.dp))
                    LocalExamImage(currentImage, Modifier.fillMaxWidth().height(160.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        Exam(
                            patientId = patientId,
                            type = type,
                            name = name,
                            note = note,
                            imagePath = imagePath ?: ""
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
@Composable
private fun ImagePreviewDialog(path: String, onDismiss: () -> Unit) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 6f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        }
                )
            }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) { Text("关闭", color = Color.White) }
        }
    }
}