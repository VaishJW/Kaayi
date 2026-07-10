package com.example.tabsplitter.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabs",
    foreignKeys = [
        ForeignKey(
            entity = Friend::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["friendId"])]
)
data class Tab(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: Long,
    val createdAt: Long,
    val status: TabStatus,
    val totalOwedByMe: Double,
    val totalOwedToMe: Double
)
