package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatments",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["id"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("patientId")]
)
data class Treatment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val treatmentDate: Long = System.currentTimeMillis(),
    val doctorOrder: String = "",
    val drainageVolume: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)