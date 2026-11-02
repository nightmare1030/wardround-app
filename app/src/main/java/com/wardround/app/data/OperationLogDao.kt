package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OperationLogDao {
    @Query("SELECT * FROM operation_logs WHERE patientId = :patientId ORDER BY createdAt DESC")
    fun getByPatient(patientId: Long): Flow<List<OperationLog>>

    @Query("SELECT * FROM operation_logs ORDER BY createdAt DESC")
    fun getAll(): Flow<List<OperationLog>>

    @Query("SELECT * FROM operation_logs ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<OperationLog>>

    @Insert
    suspend fun insert(log: OperationLog): Long
}