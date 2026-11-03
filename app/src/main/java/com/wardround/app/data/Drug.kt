package com.wardround.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "drugs", indices = [Index("name")])
data class Drug(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val initials: String = "",
    val defaultDose: String = "",
    val defaultUnit: String = "g",
    val defaultUsage: String = "qd",
    val useCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
