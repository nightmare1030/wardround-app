package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "infusion_groups",
    foreignKeys = [ForeignKey(
        entity = Treatment::class,
        parentColumns = ["id"],
        childColumns = ["treatmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("treatmentId")]
)
data class InfusionGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val treatmentId: Long,
    val diluent: String,
    val diluentVolume: Int = 0,
    val orderType: String = Medication.ORDER_LONG_TERM,
    val usage: String = "",
    val stopped: Boolean = false,
    val stoppedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)