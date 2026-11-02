package com.wardround.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String = "",
    val age: Int? = null,
    val bedNo: String = "",
    val hospitalNo: String = "",
    val department: String = "",
    val diagnosis: String = "",
    val admissionDate: Long? = null,
    val surgeryDate: Long? = null,
    val dischargeDate: Long? = null,
    val surgeryStartTime: Long? = null,
    val surgeryEndTime: Long? = null,
    val attendingDoctor: String = "",
    val allergy: String = "",
    val note: String = "",
    val status: String = "在院",
    val deleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)