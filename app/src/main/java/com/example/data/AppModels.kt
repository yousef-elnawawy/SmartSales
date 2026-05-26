package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contactNumber: String,
    val email: String,
    val address: String,
    val type: String, // "Customer" or "Vendor"
    val status: Boolean = true,
    val createdDate: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceType: String, // "Cash" or "Credit" (نقدي / آجل)
    val invoiceDate: Long = System.currentTimeMillis(),
    val csId: Int, // Contact ID (Customer/Vendor)
    val totalInvoice: Double, // total before discounts
    val discountCash: Double = 0.0,
    val discountPercentage: Double = 0.0,
    val salesTax: Double = 0.0,
    val netInvoice: Double, // total after discount + tax
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentType: String = "نقدي",
    val userId: Int = 1,
    val status: Int = 1 // 1 = Active, 0 = Cancelled
) : Serializable

@Entity(tableName = "cash_boxes")
data class CashBox(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val description: String = "",
    val status: Boolean = true
) : Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val transactionType: String, // "Debit" or "Credit" (قبض / صرف / دين / سداد)
    val csId: Int, // Contact ID
    val invoiceId: String = "", // Associated invoice code if any
    val transactionDate: Long = System.currentTimeMillis(),
    val partyType: String = "العملاء", // "العملاء" or "الموردين"
    val description: String = "",
    val status: Boolean = true
) : Serializable

@Entity(tableName = "items")
data class SalesItem(
    @PrimaryKey val id: Int,
    val code: String,
    val name: String,
    val saleUnitPrice: Double,
    val purchaseUnitPrice: Double,
    val category: String,
    val currentStock: Double,
    val unit: String = "قطعة"
) : Serializable
