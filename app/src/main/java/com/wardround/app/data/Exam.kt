package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exams",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class Exam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val type: String = TYPE_LAB,
    val name: String,
    val note: String = "",
    val imagePath: String = "",
    val examDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_LAB = "LAB"
        const val TYPE_IMAGING = "IMAGING"
    }
}