package com.wardround.app.data

import org.json.JSONArray
import org.json.JSONObject

object JsonBackup {
    const val VERSION = 1

    fun toJson(
        patients: List<Patient>,
        records: List<WardRoundRecord>,
        focusItems: List<FocusItem>,
        logs: List<OperationLog>,
        treatments: List<Treatment>,
        medications: List<Medication>,
        exams: List<Exam>,
        drainageRecords: List<DrainageRecord>,
        templates: List<TreatmentTemplate>,
        templateMeds: List<TemplateMedication>,
        infusionGroups: List<InfusionGroup>
    ): String {
        val root = JSONObject()
        root.put("version", VERSION)
        root.put("exportTime", System.currentTimeMillis())
        root.put("patients", JSONArray().apply { patients.forEach { put(it.toJson()) } })
        root.put("records", JSONArray().apply { records.forEach { put(it.toJson()) } })
        root.put("focusItems", JSONArray().apply { focusItems.forEach { put(it.toJson()) } })
        root.put("logs", JSONArray().apply { logs.forEach { put(it.toJson()) } })
        root.put("treatments", JSONArray().apply { treatments.forEach { put(it.toJson()) } })
        root.put("medications", JSONArray().apply { medications.forEach { put(it.toJson()) } })
        root.put("exams", JSONArray().apply { exams.forEach { put(it.toJson()) } })
        root.put("drainageRecords", JSONArray().apply { drainageRecords.forEach { put(it.toJson()) } })
        root.put("templates", JSONArray().apply { templates.forEach { put(it.toJson()) } })
        root.put("templateMeds", JSONArray().apply { templateMeds.forEach { put(it.toJson()) } })
        root.put("infusionGroups", JSONArray().apply { infusionGroups.forEach { put(it.toJson()) } })
        return root.toString(2)
    }

    fun parsePatients(json: String): List<Patient> {
        val arr = JSONObject(json).optJSONArray("patients") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toPatient() }
    }

    fun parseRecords(json: String): List<WardRoundRecord> {
        val arr = JSONObject(json).optJSONArray("records") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toRecord() }
    }

    fun parseFocusItems(json: String): List<FocusItem> {
        val arr = JSONObject(json).optJSONArray("focusItems") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toFocusItem() }
    }

    fun parseLogs(json: String): List<OperationLog> {
        val arr = JSONObject(json).optJSONArray("logs") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toLog() }
    }

    fun parseTreatments(json: String): List<Treatment> {
        val arr = JSONObject(json).optJSONArray("treatments") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toTreatment() }
    }

    fun parseMedications(json: String): List<Medication> {
        val arr = JSONObject(json).optJSONArray("medications") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toMedication() }
    }

    fun parseExams(json: String): List<Exam> {
        val arr = JSONObject(json).optJSONArray("exams") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toExam() }
    }

    fun parseDrainageRecords(json: String): List<DrainageRecord> {
        val arr = JSONObject(json).optJSONArray("drainageRecords") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toDrainageRecord() }
    }

    fun parseTemplates(json: String): List<TreatmentTemplate> {
        val arr = JSONObject(json).optJSONArray("templates") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toTemplate() }
    }

    fun parseTemplateMedications(json: String): List<TemplateMedication> {
        val arr = JSONObject(json).optJSONArray("templateMeds") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toTemplateMedication() }
    }

    fun parseInfusionGroups(json: String): List<InfusionGroup> {
        val arr = JSONObject(json).optJSONArray("infusionGroups") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).toInfusionGroup() }
    }

    private fun Patient.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("gender", gender)
        put("age", age ?: JSONObject.NULL)
        put("bedNo", bedNo)
        put("hospitalNo", hospitalNo)
        put("department", department)
        put("diagnosis", diagnosis)
        put("admissionDate", admissionDate ?: JSONObject.NULL)
        put("surgeryDate", surgeryDate ?: JSONObject.NULL)
        put("dischargeDate", dischargeDate ?: JSONObject.NULL)
        put("surgeryStartTime", surgeryStartTime ?: JSONObject.NULL)
        put("surgeryEndTime", surgeryEndTime ?: JSONObject.NULL)
        put("attendingDoctor", attendingDoctor)
        put("allergy", allergy)
        put("note", note)
        put("status", status)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toPatient(): Patient = Patient(
        id = getLong("id"),
        name = getString("name"),
        gender = optString("gender", ""),
        age = if (isNull("age")) null else optInt("age"),
        bedNo = optString("bedNo", ""),
        hospitalNo = optString("hospitalNo", ""),
        department = optString("department", ""),
        diagnosis = optString("diagnosis", ""),
        admissionDate = if (isNull("admissionDate")) null else optLong("admissionDate"),
        surgeryDate = if (isNull("surgeryDate")) null else optLong("surgeryDate"),
        dischargeDate = if (isNull("dischargeDate")) null else optLong("dischargeDate"),
        surgeryStartTime = if (isNull("surgeryStartTime")) null else optLong("surgeryStartTime"),
        surgeryEndTime = if (isNull("surgeryEndTime")) null else optLong("surgeryEndTime"),
        attendingDoctor = optString("attendingDoctor", ""),
        allergy = optString("allergy", ""),
        note = optString("note", ""),
        status = optString("status", "在院"),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        updatedAt = optLong("updatedAt", System.currentTimeMillis())
    )

    private fun WardRoundRecord.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId)
        put("content", content)
        put("imagePath", imagePath)
        put("recordDate", recordDate)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    private fun JSONObject.toRecord(): WardRoundRecord = WardRoundRecord(
        id = getLong("id"),
        patientId = getLong("patientId"),
        content = getString("content"),
        imagePath = optString("imagePath", ""),
        recordDate = optLong("recordDate", System.currentTimeMillis()),
        createdAt = optLong("createdAt", System.currentTimeMillis()),
        updatedAt = optLong("updatedAt", System.currentTimeMillis())
    )

    private fun FocusItem.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId)
        put("content", content)
        put("priority", priority)
        put("dueDate", dueDate ?: JSONObject.NULL)
        put("status", status)
        put("completedAt", completedAt ?: JSONObject.NULL)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toFocusItem(): FocusItem = FocusItem(
        id = getLong("id"),
        patientId = getLong("patientId"),
        content = getString("content"),
        priority = optInt("priority", 0),
        dueDate = if (isNull("dueDate")) null else optLong("dueDate"),
        status = optString("status", FocusItem.STATUS_PENDING),
        completedAt = if (isNull("completedAt")) null else optLong("completedAt"),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun OperationLog.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId ?: JSONObject.NULL)
        put("targetType", targetType)
        put("targetId", targetId ?: JSONObject.NULL)
        put("targetName", targetName)
        put("action", action)
        put("detail", detail)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toLog(): OperationLog = OperationLog(
        id = getLong("id"),
        patientId = if (isNull("patientId")) null else optLong("patientId"),
        targetType = getString("targetType"),
        targetId = if (isNull("targetId")) null else optLong("targetId"),
        targetName = optString("targetName", ""),
        action = getString("action"),
        detail = optString("detail", ""),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun Treatment.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId)
        put("treatmentDate", treatmentDate)
        put("doctorOrder", doctorOrder)
        put("drainageVolume", drainageVolume ?: JSONObject.NULL)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toTreatment(): Treatment = Treatment(
        id = getLong("id"),
        patientId = getLong("patientId"),
        treatmentDate = optLong("treatmentDate", System.currentTimeMillis()),
        doctorOrder = optString("doctorOrder", ""),
        drainageVolume = if (isNull("drainageVolume")) null else optInt("drainageVolume"),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun Medication.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("treatmentId", treatmentId)
        put("groupId", groupId ?: JSONObject.NULL)
        put("name", name)
        put("dose", dose)
        put("doseUnit", doseUnit)
        put("diluent", diluent)
        put("diluentVolume", diluentVolume)
        put("orderType", orderType)
        put("usage", usage)
        put("stopped", stopped)
        put("stoppedDate", stoppedDate ?: JSONObject.NULL)
    }

    private fun JSONObject.toMedication(): Medication = Medication(
        id = getLong("id"),
        treatmentId = getLong("treatmentId"),
        groupId = if (isNull("groupId")) null else optLong("groupId"),
        name = getString("name"),
        dose = optString("dose", ""),
        doseUnit = optString("doseUnit", ""),
        diluent = optString("diluent", ""),
        diluentVolume = optInt("diluentVolume", 0),
        orderType = optString("orderType", Medication.ORDER_LONG_TERM),
        usage = optString("usage", ""),
        stopped = optBoolean("stopped", false),
        stoppedDate = if (isNull("stoppedDate")) null else optLong("stoppedDate")
    )

    private fun Exam.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId)
        put("type", type)
        put("name", name)
        put("note", note)
        put("imagePath", imagePath)
        put("examDate", examDate)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toExam(): Exam = Exam(
        id = getLong("id"),
        patientId = getLong("patientId"),
        type = optString("type", Exam.TYPE_LAB),
        name = getString("name"),
        note = optString("note", ""),
        imagePath = optString("imagePath", ""),
        examDate = optLong("examDate", System.currentTimeMillis()),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun DrainageRecord.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("patientId", patientId)
        put("recordDate", recordDate)
        put("volume", volume)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toDrainageRecord(): DrainageRecord = DrainageRecord(
        id = getLong("id"),
        patientId = getLong("patientId"),
        recordDate = optLong("recordDate", 0L),
        volume = optInt("volume", 0),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun TreatmentTemplate.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("content", content)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toTemplate(): TreatmentTemplate = TreatmentTemplate(
        id = getLong("id"),
        name = getString("name"),
        content = getString("content"),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )

    private fun TemplateMedication.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("templateId", templateId)
        put("name", name)
        put("dose", dose)
        put("doseUnit", doseUnit)
        put("diluent", diluent)
        put("diluentVolume", diluentVolume)
        put("orderType", orderType)
        put("usage", usage)
    }

    private fun JSONObject.toTemplateMedication(): TemplateMedication = TemplateMedication(
        id = getLong("id"),
        templateId = getLong("templateId"),
        name = getString("name"),
        dose = optString("dose", ""),
        doseUnit = optString("doseUnit", ""),
        diluent = optString("diluent", ""),
        diluentVolume = optInt("diluentVolume", 0),
        orderType = optString("orderType", Medication.ORDER_LONG_TERM),
        usage = optString("usage", "")
    )

    private fun InfusionGroup.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("treatmentId", treatmentId)
        put("diluent", diluent)
        put("diluentVolume", diluentVolume)
        put("orderType", orderType)
        put("usage", usage)
        put("stopped", stopped)
        put("stoppedDate", stoppedDate ?: JSONObject.NULL)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toInfusionGroup(): InfusionGroup = InfusionGroup(
        id = getLong("id"),
        treatmentId = getLong("treatmentId"),
        diluent = getString("diluent"),
        diluentVolume = optInt("diluentVolume", 0),
        orderType = optString("orderType", Medication.ORDER_LONG_TERM),
        usage = optString("usage", ""),
        stopped = optBoolean("stopped", false),
        stoppedDate = if (isNull("stoppedDate")) null else optLong("stoppedDate"),
        createdAt = optLong("createdAt", System.currentTimeMillis())
    )
}