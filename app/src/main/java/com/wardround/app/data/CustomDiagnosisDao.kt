package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomDiagnosisDao {
    @Query("SELECT * FROM custom_diagnoses ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CustomDiagnosis>>

    @Insert
    suspend fun insert(d: CustomDiagnosis): Long

    @Delete
    suspend fun delete(d: CustomDiagnosis)
}
