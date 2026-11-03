package com.wardround.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderSetDao {
    @Query("SELECT * FROM order_sets ORDER BY createdAt DESC")
    fun getAll(): Flow<List<OrderSet>>

    @Query("SELECT * FROM order_set_items WHERE setId = :setId ORDER BY id ASC")
    fun getItems(setId: Long): Flow<List<OrderSetItem>>

    @Query("SELECT * FROM order_set_items WHERE setId = :setId ORDER BY id ASC")
    suspend fun getItemsOnce(setId: Long): List<OrderSetItem>

    @Insert
    suspend fun insertOrderSet(set: OrderSet): Long

    @Update
    suspend fun updateOrderSet(set: OrderSet)

    @Delete
    suspend fun deleteOrderSet(set: OrderSet)

    @Insert
    suspend fun insertItem(item: OrderSetItem): Long

    @Query("DELETE FROM order_set_items WHERE setId = :setId")
    suspend fun deleteItemsBySet(setId: Long)
}
