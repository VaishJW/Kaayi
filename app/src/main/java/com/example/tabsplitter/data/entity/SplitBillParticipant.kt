package com.example.tabsplitter.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "split_bill_participants",
    foreignKeys = [
        ForeignKey(
            entity = SplitBill::class,
            parentColumns = ["id"],
            childColumns = ["splitBillId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Friend::class,
            parentColumns = ["id"],
            childColumns = ["friendId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["splitBillId"]),
        Index(value = ["friendId"])
    ]
)
data class SplitBillParticipant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitBillId: Long,
    val friendId: Long,
    val shareAmount: Double,
    val paymentStatus: PaymentStatus
)
