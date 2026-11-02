package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WardRoundRecordDao {
    @Query("SELECT * FROM ward_round_records")
    fun getAll(): Flow<List<WardRoundRecord>>

    @Query("SELECT * FROM ward_round_records WHERE patientId = :patientId ORDER BY recordDate DESC")
    fun getByPatient(patientId: Long): Flow<List<WardRoundRecord>>

    @Query("SELECT * FROM ward_round_records WHERE id = :id")
    fun getById(id: Long): Flow<WardRoundRecord?>

    @Insert
    suspend fun insert(record: WardRoundRecord): Long

    @Update
    suspend fun update(record: WardRoundRecord)

    @Delete
    suspend fun delete(record: WardRoundRecord)
}