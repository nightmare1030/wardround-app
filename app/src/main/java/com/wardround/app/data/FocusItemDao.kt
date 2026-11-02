package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusItemDao {
    @Query("SELECT * FROM focus_items")
    fun getAll(): Flow<List<FocusItem>>

    @Query("SELECT * FROM focus_items WHERE patientId = :patientId ORDER BY status ASC, priority DESC, createdAt ASC")
    fun getByPatient(patientId: Long): Flow<List<FocusItem>>

    @Query("SELECT * FROM focus_items WHERE patientId = :patientId AND status = :status ORDER BY priority DESC, createdAt ASC")
    fun getByPatientAndStatus(patientId: Long, status: String): Flow<List<FocusItem>>

    @Query("SELECT * FROM focus_items WHERE status = :status ORDER BY priority DESC, createdAt ASC")
    fun getByStatus(status: String): Flow<List<FocusItem>>

    @Query("SELECT * FROM focus_items WHERE id = :id")
    fun getById(id: Long): Flow<FocusItem?>

    @Insert
    suspend fun insert(item: FocusItem): Long

    @Update
    suspend fun update(item: FocusItem)

    @Delete
    suspend fun delete(item: FocusItem)

    @Query("UPDATE focus_items SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedAt: Long?)
}