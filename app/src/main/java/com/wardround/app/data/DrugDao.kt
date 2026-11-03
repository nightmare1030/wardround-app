package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs ORDER BY useCount DESC, name ASC")
    fun getAll(): Flow<List<Drug>>

    @Query("SELECT * FROM drugs")
    suspend fun getAllOnce(): List<Drug>

    @Query("SELECT * FROM drugs WHERE id = :id")
    suspend fun getById(id: Long): Drug?

    @Insert
    suspend fun insert(drug: Drug): Long

    @Update
    suspend fun update(drug: Drug)

    @Delete
    suspend fun delete(drug: Drug)

    @Query("UPDATE drugs SET useCount = useCount + 1 WHERE id = :id")
    suspend fun incrementUse(id: Long)

    @Query("SELECT COUNT(*) FROM drugs")
    suspend fun count(): Int
}
