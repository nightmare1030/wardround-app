package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateMedicationDao {
    @Query("SELECT * FROM template_medications WHERE templateId = :templateId ORDER BY id ASC")
    fun getByTemplate(templateId: Long): Flow<List<TemplateMedication>>

    @Query("SELECT * FROM template_medications WHERE templateId = :templateId ORDER BY id ASC")
    suspend fun getByTemplateOnce(templateId: Long): List<TemplateMedication>

    @Query("SELECT * FROM template_medications")
    fun getAll(): Flow<List<TemplateMedication>>

    @Insert
    suspend fun insert(medication: TemplateMedication): Long

    @Delete
    suspend fun delete(medication: TemplateMedication)

    @Query("DELETE FROM template_medications WHERE templateId = :templateId")
    suspend fun deleteByTemplate(templateId: Long)
}