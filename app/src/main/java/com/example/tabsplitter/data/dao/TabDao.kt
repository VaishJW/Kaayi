package com.example.tabsplitter.data.dao

import androidx.room.*
import com.example.tabsplitter.data.entity.Tab
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {
    @Insert
    suspend fun insert(tab: Tab): Long

    @Update
    suspend fun update(tab: Tab)

    @Delete
    suspend fun delete(tab: Tab)

    @Query("SELECT * FROM tabs WHERE id = :id")
    suspend fun getById(id: Long): Tab?

    @Query("SELECT * FROM tabs WHERE id = :id")
    fun getTabByIdFlow(id: Long): Flow<Tab?>

    @Query("SELECT * FROM tabs")
    fun getAll(): Flow<List<Tab>>

    @Query("SELECT * FROM tabs WHERE friendId = :friendId")
    fun getTabsByFriendId(friendId: Long): Flow<List<Tab>>

    @Query("SELECT * FROM tabs WHERE status = 'OPEN'")
    fun getAllOpenTabs(): Flow<List<Tab>>

    @Query("SELECT * FROM tabs WHERE friendId = :friendId AND status = 'OPEN' LIMIT 1")
    suspend fun getOpenTabForFriend(friendId: Long): Tab?
}
