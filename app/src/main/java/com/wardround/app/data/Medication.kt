package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [ForeignKey(
        entity = Treatment::class,
        parentColumns = ["id"],
        childColumns = ["treatmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("treatmentId")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val treatmentId: Long,
    val groupId: Long? = null,
    val name: String,
    val dose: String = "",
    val doseUnit: String = "",
    val diluent: String = "",
    val diluentVolume: Int = 0,
    val orderType: String = ORDER_LONG_TERM,
    val usage: String = "",
    val stopped: Boolean = false,
    val stoppedDate: Long? = null
) {
    companion object {
        const val ORDER_LONG_TERM = "长期"
        const val ORDER_TEMP = "临时"
    }
}