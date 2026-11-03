package com.wardround.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "order_sets")
data class OrderSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_set_items",
    foreignKeys = [ForeignKey(
        entity = OrderSet::class,
        parentColumns = ["id"],
        childColumns = ["setId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("setId")]
)
data class OrderSetItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val setId: Long,
    val diluent: String,
    val diluentVolume: Int = 0,
    val orderType: String = Medication.ORDER_LONG_TERM,
    val usage: String = "",
    val medsJson: String = "[]"
)

/** 套餐中的单个药 */
data class OrderSetMedEntry(val name: String, val dose: String = "", val doseUnit: String = "")

/** 一条完整溶媒组 + 药，用于套用套餐 / 复制医嘱 */
data class MedicationGroupDraft(
    val diluent: String,
    val diluentVolume: Int,
    val orderType: String,
    val usage: String,
    val meds: List<OrderSetMedEntry>
)
