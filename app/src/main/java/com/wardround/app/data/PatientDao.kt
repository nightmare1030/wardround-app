package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id AND deleted = 0")
    fun getById(id: Long): Flow<Patient?>

    @Query("SELECT * FROM patients WHERE deleted = 0 AND (name LIKE '%' || :query || '%' OR bedNo LIKE '%' || :query || '%' OR hospitalNo LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE deleted = 0 AND status = :status ORDER BY updatedAt DESC")
    fun getByStatus(status: String): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE deleted = 1 ORDER BY deletedAt DESC")
    fun getDeleted(): Flow<List<Patient>>

    @Insert
    suspend fun insert(patient: Patient): Long

    @Update
    suspend fun update(patient: Patient)

    @Query("UPDATE patients SET deleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun markDeleted(id: Long, deletedAt: Long)

    @Query("UPDATE patients SET deleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM patients WHERE deleted = 1 AND deletedAt < :cutoff")
    suspend fun deleteExpired(cutoff: Long)
}