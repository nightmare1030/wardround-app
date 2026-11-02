package com.wardround.app.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wardround.app.WardRoundApp
import com.wardround.app.data.Patient
import com.wardround.app.data.Treatment
import com.wardround.app.data.WardRoundRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val statDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
fun StatisticsSection(patient: Patient?) {
    if (patient == null) return
    val context = LocalContext.current
    val repository = remember { (context.applicationContext as WardRoundApp).repository }

    val records by repository.getRecords(patient.id).collectAsState(initial = emptyList())
    val treatments by repository.getTreatments(patient.id).collectAsState(initial = emptyList())
    val groups by repository.getInfusionGroupsByPatient(patient.id).collectAsState(initial = emptyList())
    val meds by repository.getMedicationsByPatient(patient.id).collectAsState(initial = emptyList())

    val surgeryDurationMinutes = if (patient.surgeryStartTime != null && patient.surgeryEndTime != null) {
        ((patient.surgeryEndTime - patient.surgeryStartTime) / 60000).toInt()
    } else null

    val antibioticUsage = meds.filter { repository.isAntibiotic(it.name) }
        .groupBy { it.name }
        .map { (name, ms) ->
            val gids = ms.mapNotNull { it.groupId }.toSet()
            val gs = groups.filter { it.id in gids }
            val start = gs.minOfOrNull { it.createdAt } ?: System.currentTimeMillis()
            val end = gs.maxOfOrNull { it.stoppedDate ?: System.currentTimeMillis() } ?: System.currentTimeMillis()
            val days = ((startOfDay(end) - startOfDay(start)) / 86400000L + 1).toInt()
            name to days
        }
        .sortedByDescending { it.second }

    val endDay = if (patient.status == "出院") (patient.dischargeDate ?: patient.updatedAt) else System.currentTimeMillis()
    val endDayStart = startOfDay(endDay)

    val admissionDays = listOf(1, 2, 3, 6)
    val surgeryDays = listOf(1, 2, 3, 6, 9, 12)

    val admissionStats = patient.admissionDate?.let { adm ->
        val admStart = startOfDay(adm)
        admissionDays.mapNotNull { n ->
            val dayStart = admStart + n * 86400000L
            if (dayStart <= endDayStart) n to dayStart else null
        }
    } ?: emptyList()

    val surgeryStats = patient.surgeryDate?.let { surg ->
        val surgStart = startOfDay(surg)
        surgeryDays.mapNotNull { n ->
            val dayStart = surgStart + n * 86400000L
            if (dayStart <= endDayStart) n to dayStart else null
        }
    } ?: emptyList()

    fun dayRecords(dayStart: Long): List<WardRoundRecord> =
        records.filter { it.recordDate >= dayStart && it.recordDate < dayStart + 86400000L }

    fun dayTreatments(dayStart: Long): List<Treatment> =
        treatments.filter { it.treatmentDate >= dayStart && it.treatmentDate < dayStart + 86400000L }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (antibioticUsage.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("抗生素使用", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                        Spacer(Modifier.height(4.dp))
                        antibioticUsage.forEach { (name, days) ->
                            Text("· $name：$days 天", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (surgeryDurationMinutes != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Text("手术时长", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${surgeryDurationMinutes / 60}小时${surgeryDurationMinutes % 60}分钟（$surgeryDurationMinutes 分钟）",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (admissionStats.isEmpty() && surgeryStats.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无统计数据（需填写入院日期或手术日期）", color = Color.Gray)
                }
            }
        }

        if (admissionStats.isNotEmpty()) {
            item {
                Text(
                    "入院后",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }
            admissionStats.forEach { (n, dayStart) ->
                item(key = "adm_$n") {
                    StatDayCard(
                        title = "入院后第 $n 天（${statDayFormat.format(Date(dayStart))}）",
                        records = dayRecords(dayStart),
                        treatments = dayTreatments(dayStart)
                    )
                }
            }
        }

        if (surgeryStats.isNotEmpty()) {
            item {
                Text(
                    "术后",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF6C00)
                )
            }
            surgeryStats.forEach { (n, dayStart) ->
                item(key = "surg_$n") {
                    StatDayCard(
                        title = "术后第 $n 天（${statDayFormat.format(Date(dayStart))}）",
                        records = dayRecords(dayStart),
                        treatments = dayTreatments(dayStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatDayCard(
    title: String,
    records: List<WardRoundRecord>,
    treatments: List<Treatment>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (records.isEmpty() && treatments.isEmpty()) {
                Text("当日无记录", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                records.forEach { r ->
                    Text("· ${r.content}", style = MaterialTheme.typography.bodyMedium)
                }
                treatments.forEach { t ->
                    Text(
                        "· 治疗：${t.doctorOrder.ifBlank { "（无医嘱文字）" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
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