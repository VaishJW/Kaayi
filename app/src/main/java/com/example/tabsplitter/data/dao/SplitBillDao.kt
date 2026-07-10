package com.example.tabsplitter.data.dao

import androidx.room.*
import com.example.tabsplitter.data.entity.SplitBill
import com.example.tabsplitter.data.entity.SplitBillParticipant
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitBillDao {
    @Insert
    suspend fun insertSplitBill(splitBill: SplitBill): Long

    @Insert
    suspend fun insertParticipants(participants: List<SplitBillParticipant>)

    @Update
    suspend fun updateParticipant(participant: SplitBillParticipant)

    @Query("SELECT * FROM split_bills ORDER BY createdAt DESC")
    fun getAllSplitBillsFlow(): Flow<List<SplitBill>>

    @Query("SELECT * FROM split_bills WHERE id = :id")
    suspend fun getSplitBillById(id: Long): SplitBill?

    @Query("SELECT * FROM split_bill_participants WHERE splitBillId = :splitBillId")
    fun getParticipantsForSplitBillFlow(splitBillId: Long): Flow<List<SplitBillParticipant>>

    @Query("SELECT * FROM split_bill_participants WHERE splitBillId = :splitBillId")
    suspend fun getParticipantsForSplitBillDirect(splitBillId: Long): List<SplitBillParticipant>

    @Query("SELECT * FROM split_bill_participants")
    fun getAllParticipantsFlow(): Flow<List<SplitBillParticipant>>

    @Query("DELETE FROM split_bills WHERE id = :id")
    suspend fun deleteSplitBillById(id: Long)
}
