package com.wardround.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppRepository(
    private val patientDao: PatientDao,
    private val recordDao: WardRoundRecordDao,
    private val focusItemDao: FocusItemDao,
    private val operationLogDao: OperationLogDao,
    private val treatmentDao: TreatmentDao,
    private val medicationDao: MedicationDao,
    private val examDao: ExamDao,
    private val drainageDao: DrainageDao,
    private val treatmentTemplateDao: TreatmentTemplateDao,
    private val templateMedicationDao: TemplateMedicationDao,
    private val infusionGroupDao: InfusionGroupDao,
    private val drugDao: DrugDao,
    private val orderSetDao: OrderSetDao
) {
    // ===== 患者 =====
    fun getAllPatients(): Flow<List<Patient>> = patientDao.getAll()
    fun getPatient(id: Long): Flow<Patient?> = patientDao.getById(id)
    fun searchPatients(query: String): Flow<List<Patient>> =
        if (query.isBlank()) patientDao.getAll() else patientDao.search(query)

    suspend fun addPatient(patient: Patient): Long {
        val id = patientDao.insert(patient)
        operationLogDao.insert(
            OperationLog(
                patientId = id,
                targetType = OperationLog.TYPE_PATIENT,
                targetId = id,
                targetName = patient.name,
                action = OperationLog.ACTION_CREATE,
                detail = "新增患者"
            )
        )
        return id
    }

    suspend fun updatePatient(patient: Patient) {
        patientDao.update(patient.copy(updatedAt = System.currentTimeMillis()))
        operationLogDao.insert(
            OperationLog(
                patientId = patient.id,
                targetType = OperationLog.TYPE_PATIENT,
                targetId = patient.id,
                targetName = patient.name,
                action = OperationLog.ACTION_UPDATE,
                detail = "更新患者信息"
            )
        )
    }

    suspend fun deletePatient(patient: Patient) {
        patientDao.markDeleted(patient.id, System.currentTimeMillis())
        operationLogDao.insert(
            OperationLog(
                patientId = null,
                targetType = OperationLog.TYPE_PATIENT,
                targetId = patient.id,
                targetName = patient.name,
                action = OperationLog.ACTION_DELETE,
                detail = "删除患者（进入回收站）"
            )
        )
    }

    fun getDeletedPatients(): Flow<List<Patient>> = patientDao.getDeleted()

    suspend fun restorePatient(patient: Patient) {
        patientDao.restore(patient.id)
    }

    suspend fun cleanupExpiredPatients() {
        val cutoff = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        patientDao.deleteExpired(cutoff)
    }

    // ===== 查房记录 =====
    fun getRecords(patientId: Long): Flow<List<WardRoundRecord>> = recordDao.getByPatient(patientId)

    suspend fun addRecord(patientId: Long, content: String, imagePath: String = ""): Long {
        val record = WardRoundRecord(patientId = patientId, content = content, imagePath = imagePath)
        val id = recordDao.insert(record)
        operationLogDao.insert(
            OperationLog(
                patientId = patientId,
                targetType = OperationLog.TYPE_RECORD,
                targetId = id,
                targetName = content.take(20),
                action = OperationLog.ACTION_CREATE,
                detail = "新增查房记录"
            )
        )
        return id
    }

    suspend fun updateRecord(record: WardRoundRecord) {
        recordDao.update(record.copy(updatedAt = System.currentTimeMillis()))
        operationLogDao.insert(
            OperationLog(
                patientId = record.patientId,
                targetType = OperationLog.TYPE_RECORD,
                targetId = record.id,
                targetName = record.content.take(20),
                action = OperationLog.ACTION_UPDATE,
                detail = "修改查房记录"
            )
        )
    }

    suspend fun deleteRecord(record: WardRoundRecord) {
        recordDao.delete(record)
        operationLogDao.insert(
            OperationLog(
                patientId = record.patientId,
                targetType = OperationLog.TYPE_RECORD,
                targetId = record.id,
                targetName = record.content.take(20),
                action = OperationLog.ACTION_DELETE,
                detail = "删除查房记录"
            )
        )
    }

    // ===== 重点事项 =====
    fun getFocusItems(patientId: Long): Flow<List<FocusItem>> = focusItemDao.getByPatient(patientId)
    fun getPendingFocusItems(): Flow<List<FocusItem>> =
        focusItemDao.getByStatus(FocusItem.STATUS_PENDING)

    suspend fun addFocusItem(item: FocusItem): Long {
        val id = focusItemDao.insert(item)
        operationLogDao.insert(
            OperationLog(
                patientId = item.patientId,
                targetType = OperationLog.TYPE_FOCUS,
                targetId = id,
                targetName = item.content.take(20),
                action = OperationLog.ACTION_CREATE,
                detail = "新增重点事项"
            )
        )
        return id
    }

    suspend fun updateFocusItem(item: FocusItem) {
        focusItemDao.update(item)
        operationLogDao.insert(
            OperationLog(
                patientId = item.patientId,
                targetType = OperationLog.TYPE_FOCUS,
                targetId = item.id,
                targetName = item.content.take(20),
                action = OperationLog.ACTION_UPDATE,
                detail = "修改重点事项"
            )
        )
    }

    suspend fun completeFocusItem(item: FocusItem) {
        focusItemDao.updateStatus(item.id, FocusItem.STATUS_DONE, System.currentTimeMillis())
        operationLogDao.insert(
            OperationLog(
                patientId = item.patientId,
                targetType = OperationLog.TYPE_FOCUS,
                targetId = item.id,
                targetName = item.content.take(20),
                action = OperationLog.ACTION_COMPLETE,
                detail = "完成重点事项"
            )
        )
    }

    suspend fun undoFocusItem(item: FocusItem) {
        focusItemDao.updateStatus(item.id, FocusItem.STATUS_PENDING, null)
        operationLogDao.insert(
            OperationLog(
                patientId = item.patientId,
                targetType = OperationLog.TYPE_FOCUS,
                targetId = item.id,
                targetName = item.content.take(20),
                action = OperationLog.ACTION_UNDO,
                detail = "撤销完成重点事项"
            )
        )
    }

    suspend fun deleteFocusItem(item: FocusItem) {
        focusItemDao.delete(item)
        operationLogDao.insert(
            OperationLog(
                patientId = item.patientId,
                targetType = OperationLog.TYPE_FOCUS,
                targetId = item.id,
                targetName = item.content.take(20),
                action = OperationLog.ACTION_DELETE,
                detail = "删除重点事项"
            )
        )
    }

    // ===== 操作历史 =====
    fun getLogs(patientId: Long): Flow<List<OperationLog>> = operationLogDao.getByPatient(patientId)
    fun getAllLogs(): Flow<List<OperationLog>> = operationLogDao.getAll()

    // ===== 治疗 =====
    fun getTreatments(patientId: Long): Flow<List<Treatment>> = treatmentDao.getByPatient(patientId)
    fun getLatestTreatment(patientId: Long): Flow<Treatment?> = treatmentDao.getLatestByPatient(patientId)
    fun getMedications(treatmentId: Long): Flow<List<Medication>> = medicationDao.getByTreatment(treatmentId)

    fun getMedicationsByPatient(patientId: Long): Flow<List<Medication>> = medicationDao.getByPatient(patientId)

    suspend fun addTreatment(treatment: Treatment): Long {
        val id = treatmentDao.insert(treatment)
        operationLogDao.insert(
            OperationLog(
                patientId = treatment.patientId,
                targetType = OperationLog.TYPE_TREATMENT,
                targetId = id,
                targetName = "治疗记录",
                action = OperationLog.ACTION_CREATE,
                detail = "新增治疗记录"
            )
        )
        return id
    }

    suspend fun updateTreatment(treatment: Treatment) {
        treatmentDao.update(treatment)
        operationLogDao.insert(
            OperationLog(
                patientId = treatment.patientId,
                targetType = OperationLog.TYPE_TREATMENT,
                targetId = treatment.id,
                targetName = "治疗记录",
                action = OperationLog.ACTION_UPDATE,
                detail = "更新治疗记录"
            )
        )
    }

    suspend fun deleteTreatment(treatment: Treatment) {
        treatmentDao.delete(treatment)
        operationLogDao.insert(
            OperationLog(
                patientId = treatment.patientId,
                targetType = OperationLog.TYPE_TREATMENT,
                targetId = treatment.id,
                targetName = "治疗记录",
                action = OperationLog.ACTION_DELETE,
                detail = "删除治疗记录"
            )
        )
    }

    suspend fun addMedication(medication: Medication) {
        medicationDao.insert(medication)
    }

    suspend fun addMedications(medications: List<Medication>) {
        medications.forEach { medicationDao.insert(it) }
    }

    // ===== 溶媒组 =====
    fun getInfusionGroups(treatmentId: Long): Flow<List<InfusionGroup>> = infusionGroupDao.getByTreatment(treatmentId)
    fun getInfusionGroupsByPatient(patientId: Long): Flow<List<InfusionGroup>> = infusionGroupDao.getByPatient(patientId)

    suspend fun addInfusionGroup(group: InfusionGroup): Long = infusionGroupDao.insert(group)

    suspend fun stopInfusionGroup(group: InfusionGroup) {
        infusionGroupDao.update(group.copy(stopped = true, stoppedDate = System.currentTimeMillis()))
    }

    suspend fun deleteInfusionGroup(group: InfusionGroup) {
        infusionGroupDao.delete(group)
    }

    // ===== 抗生素识别 =====
    private val antibioticKeywords = listOf(
        // 青霉素类
        "青霉素", "苄青霉素", "苯唑西林", "氯唑西林", "氟氯西林", "氨苄西林", "阿莫西林", "阿莫西林克拉维酸",
        "哌拉西林", "哌拉西林他唑巴坦", "替卡西林", "美洛西林", "阿洛西林",
        // 头孢类
        "头孢", "头孢曲松", "头孢他啶", "头孢哌酮", "头孢呋辛", "头孢克肟", "头孢唑林", "头孢噻肟",
        "头孢唑肟", "头孢吡肟", "头孢美唑", "头孢西丁", "头孢替安", "头孢地尼", "头孢泊肟",
        "头孢克洛", "头孢丙烯", "头孢拉定", "头孢羟氨苄", "头孢他美酯", "头孢特仑",
        // 碳青霉烯类
        "美罗培南", "亚胺培南", "厄他培南", "比阿培南", "多尼培南",
        // 喹诺酮类
        "左氧氟沙星", "莫西沙星", "环丙沙星", "诺氟沙星", "氧氟沙星", "洛美沙星", "加替沙星",
        "吉米沙星", "培氟沙星", "依诺沙星", "司帕沙星", "帕珠沙星",
        // 大环内酯类
        "阿奇霉素", "克拉霉素", "红霉素", "罗红霉素", "地红霉素", "交沙霉素", "麦迪霉素", "乙酰螺旋霉素",
        // 氨基糖苷类
        "庆大霉素", "阿米卡星", "妥布霉素", "奈替米星", "依替米星", "链霉素", "卡那霉素", "异帕米星",
        // 四环素类
        "四环素", "多西环素", "米诺环素", "替加环素", "土霉素",
        // 糖肽类
        "万古霉素", "去甲万古霉素", "替考拉宁",
        // 噁唑烷酮类
        "利奈唑胺", "特地唑胺",
        // 林可霉素类
        "克林霉素", "林可霉素",
        // 硝基咪唑类
        "甲硝唑", "替硝唑", "奥硝唑", "塞克硝唑",
        // 抗真菌类
        "氟康唑", "伊曲康唑", "伏立康唑", "泊沙康唑", "卡泊芬净", "米卡芬净", "阿尼芬净",
        "两性霉素", "制霉菌素", "氟胞嘧啶", "艾沙康唑",
        // 抗结核类
        "异烟肼", "利福平", "利福喷汀", "乙胺丁醇", "吡嗪酰胺", "对氨基水杨酸",
        // 磺胺类
        "复方新诺明", "磺胺嘧啶", "磺胺甲噁唑",
        // 其他
        "多粘菌素", "粘菌素", "磷霉素", "夫西地酸", "莫匹罗星", "呋喃妥因", "呋喃唑酮",
        "氯霉素", "甲砜霉素", "达托霉素", "奎奴普丁"
    )

    fun isAntibiotic(name: String): Boolean = antibioticKeywords.any { name.contains(it) }

    suspend fun stopMedication(medication: Medication) {
        medicationDao.update(medication.copy(stopped = true, stoppedDate = System.currentTimeMillis()))
    }

    suspend fun markDischarged(patient: Patient) {
        val today = startOfDay(System.currentTimeMillis())
        updatePatient(patient.copy(status = "出院", dischargeDate = today))
    }

    suspend fun markUndischarged(patient: Patient) {
        updatePatient(patient.copy(status = "在院", dischargeDate = null))
    }

    suspend fun deleteMedication(medication: Medication) {
        medicationDao.delete(medication)
    }

    // ===== 检查结果 =====
    fun getExams(patientId: Long): Flow<List<Exam>> = examDao.getByPatient(patientId)

    suspend fun addExam(exam: Exam): Long {
        val id = examDao.insert(exam)
        operationLogDao.insert(
            OperationLog(
                patientId = exam.patientId,
                targetType = OperationLog.TYPE_EXAM,
                targetId = id,
                targetName = exam.name,
                action = OperationLog.ACTION_CREATE,
                detail = "新增检查结果"
            )
        )
        return id
    }

    suspend fun deleteExam(exam: Exam) {
        examDao.delete(exam)
        operationLogDao.insert(
            OperationLog(
                patientId = exam.patientId,
                targetType = OperationLog.TYPE_EXAM,
                targetId = exam.id,
                targetName = exam.name,
                action = OperationLog.ACTION_DELETE,
                detail = "删除检查结果"
            )
        )
    }

    // ===== 治疗医嘱模板 =====
    fun getTreatmentTemplates(): Flow<List<TreatmentTemplate>> = treatmentTemplateDao.getAll()

    suspend fun addTreatmentTemplate(template: TreatmentTemplate): Long =
        treatmentTemplateDao.insert(template)

    suspend fun deleteTreatmentTemplate(template: TreatmentTemplate) {
        treatmentTemplateDao.delete(template)
    }

    fun getTemplateMedications(templateId: Long): Flow<List<TemplateMedication>> =
        templateMedicationDao.getByTemplate(templateId)

    suspend fun addTemplateMedication(medication: TemplateMedication) {
        templateMedicationDao.insert(medication)
    }

    suspend fun deleteTemplateMedication(medication: TemplateMedication) {
        templateMedicationDao.delete(medication)
    }

    suspend fun saveTemplateWithMeds(template: TreatmentTemplate, meds: List<TemplateMedication>) {
        val id = if (template.id == 0L) {
            treatmentTemplateDao.insert(template)
        } else {
            treatmentTemplateDao.update(template)
            template.id
        }
        templateMedicationDao.deleteByTemplate(id)
        meds.forEach { m ->
            templateMedicationDao.insert(m.copy(id = 0, templateId = id))
        }
    }

    suspend fun applyTemplate(treatmentId: Long, templateId: Long) {
        val meds = templateMedicationDao.getByTemplateOnce(templateId)
        meds.forEach { m ->
            medicationDao.insert(
                Medication(
                    treatmentId = treatmentId,
                    name = m.name,
                    dose = m.dose,
                    doseUnit = m.doseUnit,
                    diluent = m.diluent,
                    diluentVolume = m.diluentVolume
                )
            )
        }
    }

    // ===== 引流量 =====
    fun getDrainageRecords(patientId: Long): Flow<List<DrainageRecord>> = drainageDao.getByPatient(patientId)

    suspend fun saveDrainage(patientId: Long, volume: Int) {
        val today = startOfDay(System.currentTimeMillis())
        val existing = drainageDao.getByDate(patientId, today)
        if (existing != null) {
            drainageDao.upsert(existing.copy(volume = volume))
        } else {
            drainageDao.upsert(DrainageRecord(patientId = patientId, recordDate = today, volume = volume))
        }
    }

    suspend fun getDrainageVolumeByDate(patientId: Long, date: Long): Int? =
        drainageDao.getByDate(patientId, date)?.volume

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

    // ===== 导出汇总 =====
    suspend fun generateSummaryText(): String {
        val patients = patientDao.getAll().first().sortedBy { it.bedNo }
        val records = recordDao.getAll().first()
        val sb = StringBuilder()
        val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.appendLine("查房记录汇总")
        sb.appendLine("导出时间：" + fullFormat.format(Date()))
        sb.appendLine("共 ${patients.size} 位患者")
        sb.appendLine()
        patients.forEach { p ->
            sb.appendLine("患者：${p.name}" + if (p.bedNo.isNotBlank()) "（床号 ${p.bedNo}）" else "")
            if (p.hospitalNo.isNotBlank()) sb.appendLine("  住院号：${p.hospitalNo}")
            if (p.department.isNotBlank()) sb.appendLine("  科室：${p.department}")
            if (p.diagnosis.isNotBlank()) sb.appendLine("  诊断：${p.diagnosis}")
            val pr = records.filter { it.patientId == p.id }.sortedByDescending { it.recordDate }
            if (pr.isEmpty()) {
                sb.appendLine("  （无查房记录）")
            } else {
                sb.appendLine("  查房记录：")
                pr.forEach { r ->
                    val time = fullFormat.format(Date(r.recordDate))
                    val img = if (r.imagePath.isNotBlank()) "[图] " else ""
                    sb.appendLine("    - $time $img${r.content}")
                }
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    // ===== 数据备份 =====
    suspend fun exportAllData(): String {
        val patients = patientDao.getAll().first()
        val records = recordDao.getAll().first()
        val focusItems = focusItemDao.getAll().first()
        val logs = operationLogDao.getAll().first()
        val treatments = treatmentDao.getAll().first()
        val medications = medicationDao.getAll().first()
        val exams = examDao.getAll().first()
        val drainageRecords = drainageDao.getAll().first()
        val templates = treatmentTemplateDao.getAll().first()
        val templateMeds = templateMedicationDao.getAll().first()
        val infusionGroups = infusionGroupDao.getAll().first()
        return JsonBackup.toJson(patients, records, focusItems, logs, treatments, medications, exams, drainageRecords, templates, templateMeds, infusionGroups)
    }

    suspend fun importAllData(json: String): Int {
        val patients = JsonBackup.parsePatients(json)
        val records = JsonBackup.parseRecords(json)
        val focusItems = JsonBackup.parseFocusItems(json)
        val logs = JsonBackup.parseLogs(json)
        val treatments = JsonBackup.parseTreatments(json)
        val medications = JsonBackup.parseMedications(json)
        val exams = JsonBackup.parseExams(json)
        val drainageRecords = JsonBackup.parseDrainageRecords(json)
        val templates = JsonBackup.parseTemplates(json)
        val templateMeds = JsonBackup.parseTemplateMedications(json)
        val infusionGroups = JsonBackup.parseInfusionGroups(json)

        val idMap = mutableMapOf<Long, Long>()
        patients.forEach { old ->
            val newId = patientDao.insert(old.copy(id = 0))
            idMap[old.id] = newId
        }
        records.forEach { r ->
            val newPatientId = idMap[r.patientId] ?: return@forEach
            recordDao.insert(r.copy(id = 0, patientId = newPatientId))
        }
        focusItems.forEach { f ->
            val newPatientId = idMap[f.patientId] ?: return@forEach
            focusItemDao.insert(f.copy(id = 0, patientId = newPatientId))
        }
        logs.forEach { l ->
            val newPatientId = l.patientId?.let { idMap[it] }
            operationLogDao.insert(l.copy(id = 0, patientId = newPatientId))
        }
        val treatmentIdMap = mutableMapOf<Long, Long>()
        treatments.forEach { t ->
            val newPatientId = idMap[t.patientId] ?: return@forEach
            val newId = treatmentDao.insert(t.copy(id = 0, patientId = newPatientId))
            treatmentIdMap[t.id] = newId
        }
        val groupIdMap = mutableMapOf<Long, Long>()
        infusionGroups.forEach { g ->
            val newTreatmentId = treatmentIdMap[g.treatmentId] ?: return@forEach
            val newId = infusionGroupDao.insert(g.copy(id = 0, treatmentId = newTreatmentId))
            groupIdMap[g.id] = newId
        }
        medications.forEach { m ->
            val newTreatmentId = treatmentIdMap[m.treatmentId] ?: return@forEach
            val newGroupId = m.groupId?.let { groupIdMap[it] }
            medicationDao.insert(m.copy(id = 0, treatmentId = newTreatmentId, groupId = newGroupId))
        }
        exams.forEach { e ->
            val newPatientId = idMap[e.patientId] ?: return@forEach
            examDao.insert(e.copy(id = 0, patientId = newPatientId))
        }
        drainageRecords.forEach { dr ->
            val newPatientId = idMap[dr.patientId] ?: return@forEach
            drainageDao.upsert(dr.copy(id = 0, patientId = newPatientId))
        }
        val templateIdMap = mutableMapOf<Long, Long>()
        templates.forEach { t ->
            val newId = treatmentTemplateDao.insert(t.copy(id = 0))
            templateIdMap[t.id] = newId
        }
        templateMeds.forEach { m ->
            val newTemplateId = templateIdMap[m.templateId] ?: return@forEach
            templateMedicationDao.insert(m.copy(id = 0, templateId = newTemplateId))
        }
        return patients.size
    }
    // ===== 常用药字典 =====
    fun getDrugs(): Flow<List<Drug>> = drugDao.getAll()

    suspend fun seedDrugsIfEmpty() {
        if (drugDao.count() == 0) {
            DrugSeed.list().forEach { drugDao.insert(it) }
        }
    }

    suspend fun incrementDrugUse(id: Long) {
        drugDao.incrementUse(id)
    }

    // ===== 医嘱套餐 =====
    fun getOrderSets(): Flow<List<OrderSet>> = orderSetDao.getAll()

    suspend fun saveOrderSet(setId: Long, name: String, items: List<MedicationGroupDraft>): Long {
        val id = if (setId == 0L) {
            orderSetDao.insertOrderSet(OrderSet(name = name))
        } else {
            orderSetDao.updateOrderSet(OrderSet(id = setId, name = name))
            setId
        }
        orderSetDao.deleteItemsBySet(id)
        items.forEach { g ->
            orderSetDao.insertItem(
                OrderSetItem(
                    setId = id,
                    diluent = g.diluent,
                    diluentVolume = g.diluentVolume,
                    orderType = g.orderType,
                    usage = g.usage,
                    medsJson = medsToJson(g.meds)
                )
            )
        }
        return id
    }

    suspend fun deleteOrderSet(set: OrderSet) {
        orderSetDao.deleteOrderSet(set)
    }

    suspend fun getOrderSetDrafts(setId: Long): List<MedicationGroupDraft> {
        return orderSetDao.getItemsOnce(setId).map { item ->
            MedicationGroupDraft(
                diluent = item.diluent,
                diluentVolume = item.diluentVolume,
                orderType = item.orderType,
                usage = item.usage,
                meds = jsonToMeds(item.medsJson)
            )
        }
    }

    /** 复制该患者最近一次（非当前）治疗记录中的在用医嘱 */
    suspend fun getPreviousTreatmentDrafts(patientId: Long): List<MedicationGroupDraft> {
        val treatments = treatmentDao.getByPatient(patientId).first()
        if (treatments.size < 2) return emptyList()
        val prev = treatments[1]
        val groups = infusionGroupDao.getByTreatment(prev.id).first().filter { !it.stopped }
        val meds = medicationDao.getByTreatment(prev.id).first()
        return groups.map { g ->
            val gm = meds.filter { it.groupId == g.id }.map { OrderSetMedEntry(it.name, it.dose, it.doseUnit) }
            MedicationGroupDraft(g.diluent, g.diluentVolume, g.orderType, g.usage, gm)
        }
    }

    private fun medsToJson(meds: List<OrderSetMedEntry>): String {
        val arr = org.json.JSONArray()
        meds.forEach { m ->
            arr.put(org.json.JSONObject().put("name", m.name).put("dose", m.dose).put("doseUnit", m.doseUnit))
        }
        return arr.toString()
    }

    private fun jsonToMeds(json: String): List<OrderSetMedEntry> {
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                OrderSetMedEntry(o.optString("name"), o.optString("dose"), o.optString("doseUnit"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}
