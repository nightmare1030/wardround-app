package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_items",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class FocusItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val content: String,
    val priority: Int = 0,
    val dueDate: Long? = null,
    val status: String = "PENDING",
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_DONE = "DONE"
    }
}