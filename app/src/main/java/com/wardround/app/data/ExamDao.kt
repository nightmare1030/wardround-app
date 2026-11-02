package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams")
    fun getAll(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE patientId = :patientId ORDER BY examDate DESC")
    fun getByPatient(patientId: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE patientId = :patientId AND type = :type ORDER BY examDate DESC")
    fun getByPatientAndType(patientId: Long, type: String): Flow<List<Exam>>

    @Insert
    suspend fun insert(exam: Exam): Long

    @Delete
    suspend fun delete(exam: Exam)
}