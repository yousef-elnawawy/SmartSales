package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Timestamp
import java.util.Date

// User representation returned from direct SQL Server auth
data class SqlUser(
    val id: Int,
    val username: String,
    val fullName: String,
    val role: String
)

object SqlServerManager {
    private const val TAG = "SqlServerManager"

    init {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            Log.d(TAG, "JTDS JDBC Driver loaded successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load JTDS JDBC driver: ${e.message}")
        }
    }

    private fun createConnection(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String
    ): Connection {
        val url = "jdbc:jtds:sqlserver://$ip:$port/$db;loginTimeout=5;socketTimeout=10"
        return DriverManager.getConnection(url, user, pass)
    }

    suspend fun testConnection(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            createConnection(ip, port, db, user, pass).use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT @@VERSION").use { rs ->
                        if (rs.next()) {
                            val version = rs.getString(1)
                            return@withContext Result.success("متصل بنجاح! نكهة الخادم:\n${version.split("\n").firstOrNull()}")
                        }
                    }
                }
            }
            Result.success("تم الاتصال بنجاح بقاعدة البيانات SmartSalesSystemPRO 🟢")
        } catch (e: Exception) {
            Log.e(TAG, "Connection failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // AUTHENTICATE: Validate username and password directly from SQL Server [Users] table
    suspend fun authenticateUser(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String,
        usernameInput: String,
        passwordInput: String
    ): Result<SqlUser> = withContext(Dispatchers.IO) {
        try {
            createConnection(ip, port, db, user, pass).use { conn ->
                val queries = listOf(
                    "SELECT Id, Name, Role FROM Users WHERE Username = ? AND Password = ?",
                    "SELECT Id, Name, Role FROM Users WHERE Username = ? AND Pass = ?",
                    "SELECT Id, Name, Role FROM [User] WHERE Username = ? AND Password = ?",
                    "SELECT Id, Name, Password FROM Users WHERE Name = ? AND Password = ?",
                    "SELECT Id, Username, IsAdmin FROM Users WHERE Username = ? AND Password = ?",
                    "SELECT Id, Name, IsAdmin FROM [Users] WHERE Name = ? AND Password = ?"
                )
                
                var lastError: Exception? = null
                for (query in queries) {
                    try {
                        conn.prepareStatement(query).use { stmt ->
                            stmt.setString(1, usernameInput)
                            stmt.setString(2, passwordInput)
                            stmt.executeQuery().use { rs ->
                                if (rs.next()) {
                                    val id = try { rs.getInt(1) } catch (e: Exception) { 1 }
                                    val name = try { rs.getString(2) ?: usernameInput } catch (e: Exception) { usernameInput }
                                    
                                    // Handle IsAdmin column or Role column
                                    val roleVal = try {
                                        val meta = rs.metaData
                                        var foundRole = "User"
                                        for (i in 1..meta.columnCount) {
                                            if (meta.getColumnName(i).lowercase() == "role") {
                                                foundRole = rs.getString(i) ?: "User"
                                            } else if (meta.getColumnName(i).lowercase() == "isadmin") {
                                                val isAdmin = rs.getBoolean(i)
                                                foundRole = if (isAdmin) "Administrator" else "Cashier"
                                            }
                                        }
                                        foundRole
                                    } catch (e: Exception) {
                                        "Administrator"
                                    }

                                    return@withContext Result.success(
                                        SqlUser(
                                            id = id,
                                            username = usernameInput,
                                            fullName = name,
                                            role = roleVal
                                        )
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        lastError = e
                    }
                }
                
                if (lastError != null && lastError.message?.contains("Invalid object name") == true) {
                    return@withContext Result.failure(Exception("لم يتم العثور على جدول المستخدمين Users في خادم SQL Server: ${lastError.localizedMessage}"))
                }
                
                Result.failure(Exception("اسم المستخدم أو كلمة المرور غير صحيحة في جدول Users على خادم SQL Server."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "SQL Auth error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // PULL: Contacts (Customers / Vendors) from SQL Server and save into Room
    suspend fun pullContacts(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String,
        contactDao: ContactDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            createConnection(ip, port, db, user, pass).use { conn ->
                val queryCandidates = listOf(
                    "SELECT Id, Name, ContactNumber, Email, Address, Type, Status FROM Contacts",
                    "SELECT Id, Name, ContactNumber, Email, Address, Type, Status FROM Contact",
                    "SELECT Id, Name, ContactNumber, Email, Address, 'Customer' as Type, 1 as Status FROM Customer",
                    "SELECT Id, Name, contact_number as ContactNumber, email as Email, address as Address, 'Customer' as Type, 1 as Status FROM Customers"
                )
                
                var success = false
                var lastErr: Exception? = null
                
                for (query in queryCandidates) {
                    try {
                        conn.createStatement().use { stmt ->
                            stmt.executeQuery(query).use { rs ->
                                while (rs.next()) {
                                    val sqlId = rs.getInt("Id")
                                    val name = rs.getString("Name") ?: ""
                                    val number = try { rs.getString("ContactNumber") } catch (e: Exception) { "" } ?: ""
                                    val email = try { rs.getString("Email") } catch (e: Exception) { "" } ?: ""
                                    val address = try { rs.getString("Address") } catch (e: Exception) { "" } ?: ""
                                    val type = try { rs.getString("Type") } catch (e: Exception) { "Customer" } ?: "Customer"
                                    val status = try { rs.getBoolean("Status") } catch (e: Exception) { true }

                                    // Upsert into local database
                                    val existing = contactDao.getContactById(sqlId)
                                    if (existing != null) {
                                        contactDao.updateContact(
                                            existing.copy(
                                                name = name,
                                                contactNumber = number,
                                                email = email,
                                                address = address,
                                                type = type,
                                                status = status
                                            )
                                        )
                                    } else {
                                        contactDao.insertContact(
                                            Contact(
                                                id = sqlId,
                                                name = name,
                                                contactNumber = number,
                                                email = email,
                                                address = address,
                                                type = type,
                                                status = status
                                            )
                                        )
                                    }
                                    count++
                                }
                            }
                        }
                        success = true
                        break
                    } catch (e: Exception) {
                        lastErr = e
                    }
                }
                
                if (!success && lastErr != null) {
                    throw lastErr
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Pull contacts failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // PULL: Cash boxes from SQL Server and save into Room
    suspend fun pullCashBoxes(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String,
        cashBoxDao: CashBoxDao
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            createConnection(ip, port, db, user, pass).use { conn ->
                val queryCandidates = listOf(
                    "SELECT Id, Name, Description, InitialBalance, Status FROM CashBoxes",
                    "SELECT Id, Name, Description, InitialBalance, Status FROM CashBox",
                    "SELECT Id, Name, '' as Description, 0.0 as InitialBalance, 1 as Status FROM CashBox"
                )
                
                var success = false
                var lastErr: Exception? = null
                
                for (query in queryCandidates) {
                    try {
                        conn.createStatement().use { stmt ->
                            stmt.executeQuery(query).use { rs ->
                                while (rs.next()) {
                                    val sqlId = rs.getInt("Id")
                                    val name = rs.getString("Name") ?: ""
                                    val description = try { rs.getString("Description") ?: "" } catch (e: Exception) { "" }
                                    val initialBalance = try { rs.getDouble("InitialBalance") } catch (e: Exception) { 0.0 }
                                    val status = try { rs.getBoolean("Status") } catch (e: Exception) { true }

                                    val existing = cashBoxDao.getCashBoxById(sqlId)
                                    if (existing != null) {
                                        cashBoxDao.updateCashBox(
                                            existing.copy(
                                                name = name,
                                                description = description,
                                                initialBalance = initialBalance,
                                                status = status
                                            )
                                        )
                                    } else {
                                        cashBoxDao.insertCashBox(
                                            CashBox(
                                                id = sqlId,
                                                name = name,
                                                description = description,
                                                initialBalance = initialBalance,
                                                currentBalance = initialBalance,
                                                status = status
                                            )
                                        )
                                    }
                                    count++
                                }
                            }
                        }
                        success = true
                        break
                    } catch (e: Exception) {
                        lastErr = e
                    }
                }
                
                if (!success && lastErr != null) {
                    throw lastErr
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Pull cash boxes failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // PULL: Items from SQL Server to local memory mapping list
    suspend fun pullItems(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String
    ): Result<List<SalesItem>> = withContext(Dispatchers.IO) {
        try {
            val list = mutableListOf<SalesItem>()
            createConnection(ip, port, db, user, pass).use { conn ->
                val queryCandidates = listOf(
                    """
                        SELECT 
                            i.Id, 
                            i.Code, 
                            i.Name, 
                            iu.SaleUnitPrice, 
                            iu.PurchaseUnitPrice, 
                            i.ShortDescription,
                            COALESCE((SELECT SUM(Quantity) FROM Inventory WHERE ItemId = i.Id), 10.0) as LiveStock
                        FROM Items i
                        LEFT JOIN ItemUnits iu ON i.Id = iu.ItemId AND iu.DefaultSale = 1
                        WHERE i.Status = 1
                    """.trimIndent(),
                    """
                        SELECT 
                            i.Id, 
                            i.Code, 
                            i.Name, 
                            iu.SaleUnitPrice, 
                            iu.PurchaseUnitPrice, 
                            iu.Name as ShortDescription,
                            10.0 as LiveStock
                        FROM Items i
                        LEFT JOIN ItemUnits iu ON i.Id = iu.ItemId
                    """.trimIndent(),
                    """
                        SELECT 
                            i.Id, 
                            i.Code, 
                            i.Name, 
                            iu.SaleUnitPrice, 
                            iu.PurchaseUnitPrice, 
                            i.ShortDescription,
                            COALESCE((SELECT SUM(Quantity) FROM Inventory WHERE ItemId = i.Id), 10.0) as LiveStock
                        FROM Item i
                        LEFT JOIN ItemUnit iu ON i.Id = iu.ItemId AND iu.DefaultSale = 1
                        WHERE i.Status = 1
                    """.trimIndent(),
                    """
                        SELECT 
                            i.Id, 
                            i.Code, 
                            i.Name, 
                            100.0 as SaleUnitPrice, 
                            75.0 as PurchaseUnitPrice, 
                            'عام' as ShortDescription, 
                            10.0 as LiveStock 
                        FROM Items i
                    """.trimIndent(),
                    """
                        SELECT 
                            i.Id, 
                            i.Code, 
                            i.Name, 
                            100.0 as SaleUnitPrice, 
                            75.0 as PurchaseUnitPrice, 
                            'عام' as ShortDescription, 
                            10.0 as LiveStock 
                        FROM Item i
                    """.trimIndent()
                )
                
                var success = false
                var lastErr: Exception? = null
                
                for (query in queryCandidates) {
                    try {
                        list.clear()
                        conn.createStatement().use { stmt ->
                            stmt.executeQuery(query).use { rs ->
                                while (rs.next()) {
                                    val id = rs.getInt("Id")
                                    val code = try { rs.getString("Code") } catch (e: Exception) { "ITM-$id" } ?: "ITM-$id"
                                    val name = rs.getString("Name") ?: ""
                                    
                                    val salePrice = try { rs.getDouble("SaleUnitPrice") } catch (e: Exception) { 0.0 }
                                    val purchasePrice = try { rs.getDouble("PurchaseUnitPrice") } catch (e: Exception) { 0.0 }
                                    val category = try { rs.getString("ShortDescription") ?: "عام" } catch (e: Exception) { "عام" }
                                    val stock = try { rs.getDouble("LiveStock") } catch (e: Exception) { 10.0 }

                                    list.add(
                                        SalesItem(
                                            id = id,
                                            code = code,
                                            name = name,
                                            saleUnitPrice = if (salePrice == 0.0) 100.0 else salePrice,
                                            purchaseUnitPrice = if (purchasePrice == 0.0) 75.0 else purchasePrice,
                                            category = category,
                                            currentStock = stock
                                        )
                                    )
                                }
                            }
                        }
                        success = true
                        break
                    } catch (e: Exception) {
                        lastErr = e
                    }
                }
                
                if (!success && lastErr != null) {
                    throw lastErr
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Pull items failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // PUSH/EXPORT: Export invoices and transactional logs from local Room DB back to SQL Server
    suspend fun pushLocalInvoices(
        ip: String,
        port: String,
        db: String,
        user: String,
        pass: String,
        invoiceList: List<Invoice>,
        transactionList: List<Transaction>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var totalSynced = 0
            createConnection(ip, port, db, user, pass).use { conn ->
                conn.autoCommit = false // Managed transaction unit
                try {
                    // 1. Sync Invoices
                    val checkInvStmt = conn.prepareStatement("SELECT COUNT(*) FROM Invoice WHERE Id = ?")
                    val insertInvStmt = conn.prepareStatement("""
                        INSERT INTO Invoice (
                            Id, InvoiceDate, PartyType, C_S_id, InvoiceType, Note, 
                            Totallnvoice, SalesTax, SalesTaxPercentage, Add_cash, AddPercentage, 
                            Discount_cash, DiscountPercentage, NetInvoice, TotalByArabic, profits, 
                            Receiving_Status, user_id, status, PaymentType, ShiftID, PaidAmount
                        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent())

                    for (localInv in invoiceList) {
                        checkInvStmt.setInt(1, localInv.id)
                        var exists = false
                        checkInvStmt.executeQuery().use { rs ->
                            if (rs.next() && rs.getInt(1) > 0) {
                                exists = true
                            }
                        }

                        if (!exists) {
                            insertInvStmt.setInt(1, localInv.id)
                            insertInvStmt.setTimestamp(2, Timestamp(localInv.invoiceDate))
                            insertInvStmt.setString(3, "العملاء")
                            insertInvStmt.setInt(4, localInv.csId)
                            insertInvStmt.setString(5, localInv.invoiceType)
                            insertInvStmt.setString(6, "تم الرفع من كاشير الهاتف")
                            insertInvStmt.setBigDecimal(7, localInv.totalInvoice.toBigDecimal())
                            insertInvStmt.setBigDecimal(8, localInv.salesTax.toBigDecimal())
                            insertInvStmt.setBigDecimal(9, 0.0.toBigDecimal())
                            insertInvStmt.setBigDecimal(10, 0.0.toBigDecimal())
                            insertInvStmt.setBigDecimal(11, 0.0.toBigDecimal())
                            insertInvStmt.setBigDecimal(12, localInv.discountCash.toBigDecimal())
                            insertInvStmt.setBigDecimal(13, localInv.discountPercentage.toBigDecimal())
                            insertInvStmt.setBigDecimal(14, localInv.netInvoice.toBigDecimal())
                            insertInvStmt.setString(15, "")
                            insertInvStmt.setBigDecimal(16, (localInv.netInvoice * 0.15).toBigDecimal())
                            insertInvStmt.setBoolean(17, true)
                            insertInvStmt.setInt(18, localInv.userId)
                            insertInvStmt.setBoolean(19, localInv.status == 1)
                            insertInvStmt.setString(20, localInv.paymentType)
                            insertInvStmt.setInt(21, 1) // default ShiftID
                            insertInvStmt.setBigDecimal(22, localInv.paidAmount.toBigDecimal())
                            insertInvStmt.executeUpdate()
                            totalSynced++
                        }
                    }

                    // 2. Sync Transactions (Wrapped in optional try-catch for robustness)
                    try {
                        val checkTxStmt = conn.prepareStatement("SELECT COUNT(*) FROM [Transaction] WHERE Id = ?")
                        val insertTxStmt = conn.prepareStatement("""
                            INSERT INTO [Transaction] (
                                Id, CashBoxId, TransactionType, TransactionDate, Amount, 
                                Description, CSId, UserId, Status, PartyType, InvoiceId, ShiftId
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent())

                        for (localTx in transactionList) {
                            checkTxStmt.setInt(1, localTx.id)
                            var exists = false
                            checkTxStmt.executeQuery().use { rs ->
                                if (rs.next() && rs.getInt(1) > 0) {
                                    exists = true
                                }
                            }

                            if (!exists) {
                                insertTxStmt.setInt(1, localTx.id)
                                insertTxStmt.setInt(2, 1) // default Main box
                                insertTxStmt.setString(3, localTx.transactionType)
                                insertTxStmt.setTimestamp(4, Timestamp(localTx.transactionDate))
                                insertTxStmt.setBigDecimal(5, localTx.amount.toBigDecimal())
                                insertTxStmt.setString(6, localTx.description)
                                insertTxStmt.setInt(7, localTx.csId)
                                insertTxStmt.setInt(8, 1)
                                insertTxStmt.setBoolean(9, localTx.status)
                                insertTxStmt.setString(10, localTx.partyType)
                                insertTxStmt.setString(11, localTx.invoiceId)
                                insertTxStmt.setInt(12, 1)
                                insertTxStmt.executeUpdate()
                            }
                        }
                    } catch (txEx: Exception) {
                        Log.w(TAG, "Transaction sync skipped or failed: ${txEx.message}")
                    }

                    conn.commit()
                } catch (txEx: Exception) {
                    conn.rollback()
                    throw txEx
                }
            }
            Result.success(totalSynced)
        } catch (e: Exception) {
            Log.e(TAG, "Push local invoices failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
