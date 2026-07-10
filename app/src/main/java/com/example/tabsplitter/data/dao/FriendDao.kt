package com.example.tabsplitter.data.dao

import androidx.room.*
import com.example.tabsplitter.data.entity.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Insert
    suspend fun insert(friend: Friend): Long

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(friend: Friend)

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun getById(id: Long): Friend?

    @Query("SELECT * FROM friends")
    fun getAll(): Flow<List<Friend>>

    @Query("""
        SELECT DISTINCT friends.* FROM friends 
        INNER JOIN tabs ON friends.id = tabs.friendId 
        WHERE tabs.status = 'OPEN'
    """)
    fun getAllFriendsWithOpenTabs(): Flow<List<Friend>>
}
