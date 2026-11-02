package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications")
    fun getAll(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE treatmentId = :treatmentId ORDER BY id ASC")
    fun getByTreatment(treatmentId: Long): Flow<List<Medication>>

    @Query("SELECT m.* FROM medications m INNER JOIN treatments t ON m.treatmentId = t.id WHERE t.patientId = :patientId")
    fun getByPatient(patientId: Long): Flow<List<Medication>>

    @Insert
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)
}