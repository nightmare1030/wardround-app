package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DrainageDao {
    @Query("SELECT * FROM drainage_records WHERE patientId = :patientId ORDER BY recordDate DESC")
    fun getByPatient(patientId: Long): Flow<List<DrainageRecord>>

    @Query("SELECT * FROM drainage_records WHERE patientId = :patientId AND recordDate = :date")
    suspend fun getByDate(patientId: Long, date: Long): DrainageRecord?

    @Query("SELECT * FROM drainage_records")
    fun getAll(): Flow<List<DrainageRecord>>

    @Upsert
    suspend fun upsert(record: DrainageRecord)
}