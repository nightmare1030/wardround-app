package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "template_medications",
    foreignKeys = [ForeignKey(
        entity = TreatmentTemplate::class,
        parentColumns = ["id"],
        childColumns = ["templateId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("templateId")]
)
data class TemplateMedication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val name: String,
    val dose: String = "",
    val doseUnit: String = "",
    val diluent: String = "",
    val diluentVolume: Int = 0,
    val orderType: String = Medication.ORDER_LONG_TERM,
    val usage: String = ""
)