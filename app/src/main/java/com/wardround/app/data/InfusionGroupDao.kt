package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InfusionGroupDao {
    @Query("SELECT * FROM infusion_groups")
    fun getAll(): Flow<List<InfusionGroup>>

    @Query("SELECT * FROM infusion_groups WHERE treatmentId = :treatmentId ORDER BY id DESC")
    fun getByTreatment(treatmentId: Long): Flow<List<InfusionGroup>>

    @Query("SELECT g.* FROM infusion_groups g INNER JOIN treatments t ON g.treatmentId = t.id WHERE t.patientId = :patientId")
    fun getByPatient(patientId: Long): Flow<List<InfusionGroup>>

    @Insert
    suspend fun insert(group: InfusionGroup): Long

    @Update
    suspend fun update(group: InfusionGroup)

    @Delete
    suspend fun delete(group: InfusionGroup)
}