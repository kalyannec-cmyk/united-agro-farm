package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room Entity representing a customer or farm order in the database.
 */
@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val orderId: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "customer_name")
    val customerName: String,

    @ColumnInfo(name = "customer_phone")
    val customerPhone: String,

    @ColumnInfo(name = "delivery_address")
    val deliveryAddress: String = "",

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double = 0.0,

    @ColumnInfo(name = "status")
    val status: String = "PENDING_VERIFICATION",

    @ColumnInfo(name = "payment_mode")
    val paymentMode: String = "BANK_TRANSFER",

    @ColumnInfo(name = "sender_bank_name")
    val senderBankName: String = "",

    @ColumnInfo(name = "sender_account_name")
    val senderAccountName: String = "",

    @ColumnInfo(name = "transaction_ref_number")
    val transactionRefNumber: String = "",

    @ColumnInfo(name = "transferred_amount")
    val transferredAmount: Double = 0.0,

    @ColumnInfo(name = "order_date")
    val orderDate: String = "",

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
