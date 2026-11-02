package com.wardround.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.data.Patient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientInfoCard(patient: Patient) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(patient.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                StatusBadge(patient.status)
            }
            InfoLine("性别", listOfNotNull(patient.gender.takeIf { it.isNotBlank() }, patient.age?.let { "$it 岁" }).joinToString(" · "))
            InfoLine("床位", listOfNotNull(patient.bedNo.takeIf { it.isNotBlank() }, patient.hospitalNo.takeIf { it.isNotBlank() }?.let { "住院号 $it" }).joinToString(" · "))
            InfoLine("科室", patient.department)
            InfoLine("诊断", patient.diagnosis)
            InfoLine("主管医生", patient.attendingDoctor)
            InfoLine("过敏史", patient.allergy)
            if (patient.admissionDate != null) {
                InfoLine("入院日期", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(patient.admissionDate)))
            }
            if (patient.surgeryDate != null) {
                InfoLine("术后", "第 ${daysAfterSurgery(patient.surgeryDate, System.currentTimeMillis())} 天")
            }
            if (patient.note.isNotBlank()) {
                InfoLine("备注", patient.note)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row {
        Text("$label：", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium)
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
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun daysAfterSurgery(start: Long, end: Long): Int {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDay = fmt.parse(fmt.format(Date(start)))!!.time
    val endDay = fmt.parse(fmt.format(Date(end)))!!.time
    return ((endDay - startDay) / (1000 * 60 * 60 * 24)).toInt()
}