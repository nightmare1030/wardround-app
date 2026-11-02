package com.wardround.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_logs")
data class OperationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long? = null,
    val targetType: String,
    val targetId: Long? = null,
    val targetName: String = "",
    val action: String,
    val detail: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_PATIENT = "PATIENT"
        const val TYPE_RECORD = "RECORD"
        const val TYPE_FOCUS = "FOCUS"
        const val TYPE_TREATMENT = "TREATMENT"
        const val TYPE_EXAM = "EXAM"

        const val ACTION_CREATE = "CREATE"
        const val ACTION_UPDATE = "UPDATE"
        const val ACTION_DELETE = "DELETE"
        const val ACTION_COMPLETE = "COMPLETE"
        const val ACTION_UNDO = "UNDO"
    }
}