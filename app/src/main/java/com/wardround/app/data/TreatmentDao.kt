package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TreatmentDao {
    @Query("SELECT * FROM treatments")
    fun getAll(): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE patientId = :patientId ORDER BY treatmentDate DESC")
    fun getByPatient(patientId: Long): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE patientId = :patientId ORDER BY treatmentDate DESC LIMIT 1")
    fun getLatestByPatient(patientId: Long): Flow<Treatment?>

    @Query("SELECT * FROM treatments WHERE id = :id")
    fun getById(id: Long): Flow<Treatment?>

    @Insert
    suspend fun insert(treatment: Treatment): Long

    @Update
    suspend fun update(treatment: Treatment)

    @Delete
    suspend fun delete(treatment: Treatment)
}