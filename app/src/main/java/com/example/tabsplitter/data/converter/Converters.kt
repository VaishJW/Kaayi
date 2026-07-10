package com.example.tabsplitter.data.converter

import androidx.room.TypeConverter
import com.example.tabsplitter.data.entity.TabStatus
import com.example.tabsplitter.data.entity.TransactionDirection
import com.example.tabsplitter.data.entity.PaymentStatus

class Converters {
    @TypeConverter
    fun fromTabStatus(value: TabStatus): String {
        return value.name
    }

    @TypeConverter
    fun toTabStatus(value: String): TabStatus {
        return TabStatus.valueOf(value)
    }

    @TypeConverter
    fun fromTransactionDirection(value: TransactionDirection): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionDirection(value: String): TransactionDirection {
        return TransactionDirection.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }
}
