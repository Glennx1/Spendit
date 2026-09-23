package com.spendsplit.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spendsplit.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE personId = :personId ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByPerson(personId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC, createdAt DESC")
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC, createdAt DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1 ORDER BY date DESC")
    fun getRecurringTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT description FROM transactions WHERE description LIKE '%' || :query || '%' ORDER BY createdAt DESC LIMIT 8")
    suspend fun searchPastDescriptions(query: String): List<String>

    @Query("SELECT * FROM transactions WHERE description = :description ORDER BY createdAt DESC LIMIT 1")
    suspend fun getRecentTransactionByDescription(description: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE description = :description")
    suspend fun countByDescription(description: String): Int

    @Query("UPDATE transactions SET categoryId = :newCategoryId, categoryName = :newCategoryName WHERE categoryId = :oldCategoryId")
    suspend fun reassignCategory(oldCategoryId: Long, newCategoryId: Long, newCategoryName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
