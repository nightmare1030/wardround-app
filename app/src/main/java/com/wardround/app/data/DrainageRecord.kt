package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "drainage_records",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId"), Index(value = ["patientId", "recordDate"], unique = true)]
)
data class DrainageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val recordDate: Long,
    val volume: Int,
    val createdAt: Long = System.currentTimeMillis()
)