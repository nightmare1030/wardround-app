package com.wardround.app.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.wardround.app.WardRoundApp
import com.wardround.app.data.DiagnosisList
import com.wardround.app.data.Patient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientEditScreen(
    patientId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = (context.applicationContext as WardRoundApp).repository
    val scope = rememberCoroutineScope()
    val isEdit = patientId > 0

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("男") }
    var ageText by remember { mutableStateOf("") }
    var bedNo by remember { mutableStateOf("") }
    var hospitalNo by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var admissionDateText by remember { mutableStateOf("") }
    var showAdmissionDatePicker by remember { mutableStateOf(false) }
    var showDiagnosisPicker by remember { mutableStateOf(false) }
    var attendingDoctor by remember { mutableStateOf("") }
    var allergy by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("在院") }
    var original by remember { mutableStateOf<Patient?>(null) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        if (isEdit) {
            val p = repository.getPatient(patientId).first()
            original = p
            p?.let {
                name = it.name
                gender = it.gender
                ageText = it.age?.toString() ?: ""
                bedNo = it.bedNo
                hospitalNo = it.hospitalNo
                department = it.department
                diagnosis = it.diagnosis
                admissionDateText = it.admissionDate?.let { d ->
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
                } ?: ""

                attendingDoctor = it.attendingDoctor
                allergy = it.allergy
                note = it.note
                status = it.status
            }
        }
    }

    fun save() {
        if (name.isBlank()) {
            nameError = true
            return
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val admissionDate = admissionDateText.takeIf { it.isNotBlank() }?.let {
            try { dateFormat.parse(it)?.time } catch (e: Exception) { null }
        }

        val age = ageText.toIntOrNull()

        scope.launch {
            if (isEdit && original != null) {
                repository.updatePatient(
                    original!!.copy(
                        name = name,
                        gender = gender,
                        age = age,
                        bedNo = bedNo,
                        hospitalNo = hospitalNo,
                        department = department,
                        diagnosis = diagnosis,
                        admissionDate = admissionDate,
                        attendingDoctor = attendingDoctor,
                        allergy = allergy,
                        note = note,
                        status = status
                    )
                )
            } else {
                repository.addPatient(
                    Patient(
                        name = name,
                        gender = gender,
                        age = age,
                        bedNo = bedNo,
                        hospitalNo = hospitalNo,
                        department = department,
                        diagnosis = diagnosis,
                        admissionDate = admissionDate,
                        attendingDoctor = attendingDoctor,
                        allergy = allergy,
                        note = note,
                        status = status
                    )
                )
            }
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "编辑患者" else "新增患者") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("姓名 *") },
                isError = nameError,
                supportingText = if (nameError) { { Text("姓名不能为空") } } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("性别", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("男", "女").forEach { g ->
                    FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                }
            }

            OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it.filter { c -> c.isDigit() } },
                label = { Text("年龄") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = bedNo,
                onValueChange = { bedNo = it },
                label = { Text("床号") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = hospitalNo,
                onValueChange = { hospitalNo = it },
                label = { Text("住院号") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("科室") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("诊断") },
                    placeholder = { Text("点击选择诊断（可多选）") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "选择诊断")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDiagnosisPicker = true }
                )
            }

            OutlinedTextField(
                value = admissionDateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("入院日期") },
                placeholder = { Text("点击右侧日历图标选择日期") },
                trailingIcon = {
                    IconButton(onClick = { showAdmissionDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "选择日期")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )


            OutlinedTextField(
                value = attendingDoctor,
                onValueChange = { attendingDoctor = it },
                label = { Text("主管医生") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = allergy,
                onValueChange = { allergy = it },
                label = { Text("过敏史") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("状态", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("在院", "出院", "转科").forEach { s ->
                    FilterChip(selected = status == s, onClick = { status = s }, label = { Text(s) })
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("保存")
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDiagnosisPicker) {
        DiagnosisPickerSheet(
            initial = diagnosis.split("；").filter { it.isNotBlank() },
            onDismiss = { showDiagnosisPicker = false },
            onConfirm = { list ->
                diagnosis = list.joinToString("；")
                showDiagnosisPicker = false
            }
        )
    }

    if (showAdmissionDatePicker) {
        val cal = Calendar.getInstance().apply {
            admissionDateText.takeIf { it.isNotBlank() }?.let { txt ->
                try {
                    timeInMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(txt)!!.time
                } catch (e: Exception) {
                }
            }
        }
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    admissionDateText = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d)
                    showAdmissionDatePicker = false
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

