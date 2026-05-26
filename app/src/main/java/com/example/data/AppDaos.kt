package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Int): Contact?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Int): Invoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Update
    suspend fun updateInvoice(invoice: Invoice)

    @Delete
    suspend fun deleteInvoice(invoice: Invoice)
}

@Dao
interface CashBoxDao {
    @Query("SELECT * FROM cash_boxes ORDER BY name ASC")
    fun getAllCashBoxes(): Flow<List<CashBox>>

    @Query("SELECT * FROM cash_boxes WHERE id = :id LIMIT 1")
    suspend fun getCashBoxById(id: Int): CashBox?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashBox(cashBox: CashBox): Long

    @Update
    suspend fun updateCashBox(cashBox: CashBox)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE csId = :csId ORDER BY transactionDate ASC")
    fun getTransactionsForContact(csId: Int): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): Flow<List<SalesItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SalesItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SalesItem>)

    @Query("DELETE FROM items")
    suspend fun deleteAllItems()

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getCount(): Int
}
