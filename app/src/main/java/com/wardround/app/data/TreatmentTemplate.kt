package com.wardround.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treatment_templates")
data class TreatmentTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)