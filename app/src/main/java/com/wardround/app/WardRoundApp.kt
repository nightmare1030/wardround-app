package com.wardround.app

import android.app.Application
import com.wardround.app.data.AppDatabase
import com.wardround.app.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WardRoundApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            repository.seedDrugsIfEmpty()
            repository.cleanupExpiredPatients()
        }
    }
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy {
        AppRepository(
            patientDao = database.patientDao(),
            recordDao = database.recordDao(),
            focusItemDao = database.focusItemDao(),
            operationLogDao = database.operationLogDao(),
            treatmentDao = database.treatmentDao(),
            medicationDao = database.medicationDao(),
            examDao = database.examDao(),
            drainageDao = database.drainageDao(),
            treatmentTemplateDao = database.treatmentTemplateDao(),
            templateMedicationDao = database.templateMedicationDao(),
            infusionGroupDao = database.infusionGroupDao(),
            drugDao = database.drugDao(),
            orderSetDao = database.orderSetDao()
        )
    }
}
