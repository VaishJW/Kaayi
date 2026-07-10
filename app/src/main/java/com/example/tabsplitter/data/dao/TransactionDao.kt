package com.example.tabsplitter.data.dao

import androidx.room.*
import com.example.tabsplitter.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE id IN (:ids)")
    suspend fun getTransactionsByIds(ids: List<Long>): List<Transaction>

    @Query("DELETE FROM transactions WHERE id IN (:ids)")
    suspend fun deleteTransactions(ids: List<Long>)

    @Query("SELECT * FROM transactions")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE tabId = :tabId")
    fun getTransactionsByTabId(tabId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE tabId = :tabId")
    suspend fun getTransactionsByTabIdDirect(tabId: Long): List<Transaction>

    @Query("""
        SELECT transactions.* FROM transactions 
        INNER JOIN tabs ON transactions.tabId = tabs.id 
        WHERE tabs.status = 'SETTLED'
    """)
    fun getAllSettledHistory(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE createdAt >= :startTimestamp AND createdAt <= :endTimestamp")
    fun getTransactionsByMonth(startTimestamp: Long, endTimestamp: Long): Flow<List<Transaction>>
}
