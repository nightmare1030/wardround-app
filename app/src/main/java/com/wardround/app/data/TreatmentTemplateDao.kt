package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentTemplateDao {
    @Query("SELECT * FROM treatment_templates ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TreatmentTemplate>>

    @Insert
    suspend fun insert(template: TreatmentTemplate): Long

    @Update
    suspend fun update(template: TreatmentTemplate)

    @Delete
    suspend fun delete(template: TreatmentTemplate)
}