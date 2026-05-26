package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SalesRepository(
    val contactDao: ContactDao,
    val invoiceDao: InvoiceDao,
    val cashBoxDao: CashBoxDao,
    val transactionDao: TransactionDao,
    val itemDao: ItemDao
) {

    // Simulating SignalR real-time notification listener
    val liveNotifications = flow<String> {
        val notifications = listOf(
            "تم تحصيل دفعة بقيمة 5,000 ج.م من العميل أحمد المهدي عبر Stripe 💳",
            "تنبيه: مخزون 'شاشات سامسونج 55' شارف على الانتهاء! ⚠️",
            "تم تسجيل دخول مشرف النظام من جهاز هاتف جديد 🔐",
            "فاتورة مبيعات جديدة رقم #INV-9289 تمت إضافتها بنجاح 📋",
            "تم تسوية الحساب المالي لشركة مكة للمقاولات بقيمة 12,000 ج.م 🏦"
        )
        // Keep emitting simulated real-time SignalR notifications
        var index = 0
        while (true) {
            kotlinx.coroutines.delay(18000) // Emit notification every 18 seconds
            emit(notifications[index % notifications.size])
            index++
        }
    }

    // Static Mock Items List representing products from EF Core "Items"
    val mockItems = listOf(
        SalesItem(1, "ITM-001", "شاشة تلفزيون سامسونج الذكية 55 بوصة", 12500.0, 9500.0, "شاشات وأجهزة", 24.0),
        SalesItem(2, "ITM-002", "أيفون 15 برو ماكس 256 جيجا", 58000.0, 49000.0, "هواتف ذكية", 8.0),
        SalesItem(3, "ITM-003", "سماعات بلوتوث جيه بي إل لاسلكية", 3200.0, 2400.0, "إكسسوارات", 65.0),
        SalesItem(4, "ITM-004", "حاسوب محمول اتش بي كور آي 7", 29500.0, 24000.0, "أجهزة كمبيوتر", 12.0),
        SalesItem(5, "ITM-005", "راوتر منزلي تي بي لينك فايبر 5G", 1800.0, 1200.0, "شبكات الاتصال", 40.0),
        SalesItem(6, "ITM-006", "شاحن سريع أنكر 65 واط مخارج متعددة", 1500.0, 950.0, "إكسسوارات", 110.0),
        SalesItem(7, "ITM-007", "كاميرا سوني رقمية بدون مرأة A7 IV", 95000.0, 82000.0, "أجهزة ميديا", 3.0)
    )

    // Observables from DB
    val contacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val invoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    val cashBoxes: Flow<List<CashBox>> = cashBoxDao.getAllCashBoxes()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val items: Flow<List<SalesItem>> = itemDao.getAllItems()

    // Query transactions for a single Account Statement view
    fun getTransactionsForContact(csId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsForContact(csId)

    // Populate data on first app launch if database is empty
    suspend fun populateDemoDataIfEmpty() {
        val currentContacts = contactDao.getAllContacts().first()
        if (currentContacts.isEmpty()) {
            // 0. Populate Items
            itemDao.insertItems(mockItems)
            // 1. Populate Cash Boxes
            val box1Id = cashBoxDao.insertCashBox(
                CashBox(
                    name = "خزينة المعرض الرئيسية",
                    initialBalance = 50000.0,
                    currentBalance = 135800.0,
                    description = "الخزينة المخصصة للمبيعات النقدية المباشرة في المعرض"
                )
            )
            val box2Id = cashBoxDao.insertCashBox(
                CashBox(
                    name = "بوابة الدفع الإلكتروني (Stripe)",
                    initialBalance = 0.0,
                    currentBalance = 42000.0,
                    description = "رصيد المدفوعات المستلمة أونلاين عبر الفيزا والماستركارد"
                )
            )

            // 2. Populate Contacts (Customers and Vendors)
            val c1 = contactDao.insertContact(
                Contact(
                    name = "أحمد محمد المهدي (عميل مميز)",
                    contactNumber = "01002345678",
                    email = "ahmed.mahdi@gmail.com",
                    address = "المهندسين، الجيزة، مصر",
                    type = "Customer"
                )
            ).toInt()

            val c2 = contactDao.insertContact(
                Contact(
                    name = "شركة مكة للمقاولات والاستيراد",
                    contactNumber = "01229988776",
                    email = "info@makka-const.com",
                    address = "شارع صلاح سالم، مصر الجديدة، القاهرة",
                    type = "Vendor"
                )
            ).toInt()

            val c3 = contactDao.insertContact(
                Contact(
                    name = "المؤسسة العالمية للتجارة والتسويق",
                    contactNumber = "01115544332",
                    email = "sales@global-trade.eg",
                    address = "المنطقة الصناعية، مدينة العبور",
                    type = "Customer"
                )
            ).toInt()

            val c4 = contactDao.insertContact(
                Contact(
                    name = "مكتب الأفق للتوزيع والخدمات",
                    contactNumber = "01556633221",
                    email = "contact@al-ofoq.com",
                    address = "وسط البلد، الإسكندرية",
                    type = "Vendor"
                )
            ).toInt()

            val c5 = contactDao.insertContact(
                Contact(
                    name = "منى عبد الله سلامة",
                    contactNumber = "01009988112",
                    email = "mona.salama99@yahoo.com",
                    address = "المعادي، القاهرة",
                    type = "Customer"
                )
            ).toInt()

            // 3. Populate Invoices (and associated transactions for historical ledger)
            // Invoice 1: Customer c1 (Ahmed Mahdi) purchased and paid partially
            val inv1Id = invoiceDao.insertInvoice(
                Invoice(
                    invoiceType = "Credit",
                    csId = c1,
                    totalInvoice = 15700.0,
                    discountCash = 700.0,
                    netInvoice = 15000.0,
                    paidAmount = 10000.0,
                    remainingAmount = 5000.0,
                    paymentType = "آجل",
                    status = 1
                )
            ).toInt()

            // Debit Transaction (Value of Goods Sold)
            transactionDao.insertTransaction(
                Transaction(
                    amount = 15000.0,
                    transactionType = "Debit", // مدين (عليه ثمن الفاتورة)
                    csId = c1,
                    invoiceId = "INV-#1001",
                    description = "فاتورة مبيعات رقم INV-#1001"
                )
            )

            // Credit Transaction (Deposit Received)
            transactionDao.insertTransaction(
                Transaction(
                    amount = 10000.0,
                    transactionType = "Credit", // دائن (دفع دفعة نقداً)
                    csId = c1,
                    invoiceId = "INV-#1001",
                    description = "دفعة نقدية مسددة للـ INV-#1001"
                )
            )

            // Update Cash Box
            val box1 = cashBoxDao.getCashBoxById(box1Id.toInt())
            if (box1 != null) {
                cashBoxDao.updateCashBox(box1.copy(currentBalance = box1.currentBalance + 10000.0))
            }

            // Invoice 2: Customer c3 (Global trade) paid fully
            val inv2Id = invoiceDao.insertInvoice(
                Invoice(
                    invoiceType = "Cash",
                    csId = c3,
                    totalInvoice = 32000.0,
                    discountCash = 0.0,
                    netInvoice = 32000.0,
                    paidAmount = 32000.0,
                    remainingAmount = 0.0,
                    paymentType = "نقدي",
                    status = 1
                )
            ).toInt()

            transactionDao.insertTransaction(
                Transaction(
                    amount = 32000.0,
                    transactionType = "Debit",
                    csId = c3,
                    invoiceId = "INV-#1002",
                    description = "فاتورة مبيعات رقم INV-#1002"
                )
            )

            transactionDao.insertTransaction(
                Transaction(
                    amount = 32000.0,
                    transactionType = "Credit",
                    csId = c3,
                    invoiceId = "INV-#1002",
                    description = "سداد كلي نقدي للفاتورة INV-#1002"
                )
            )

            val updatedBox1 = cashBoxDao.getCashBoxById(box1Id.toInt())
            if (updatedBox1 != null) {
                cashBoxDao.updateCashBox(updatedBox1.copy(currentBalance = updatedBox1.currentBalance + 32000.0))
            }

            // Invoice 3: Vendor c2 (Makkah Construction) Purchase Invoice
            val inv3Id = invoiceDao.insertInvoice(
                Invoice(
                    invoiceType = "Credit",
                    csId = c2,
                    totalInvoice = 45000.0,
                    netInvoice = 45000.0,
                    paidAmount = 15000.0,
                    remainingAmount = 30000.0,
                    paymentType = "آجل",
                    status = 1
                )
            ).toInt()

            // Purchases act in opposition: Vendor accounts.
            // Vendor credits us for supplies, we debit them when paying.
            transactionDao.insertTransaction(
                Transaction(
                    amount = 45000.0,
                    transactionType = "Credit", // دائن (له ثمن البضاعة)
                    csId = c2,
                    invoiceId = "PUR-#5001",
                    partyType = "الموردين",
                    description = "فاتورة مشتريات بضاعة رقم PUR-#5001"
                )
            )

            transactionDao.insertTransaction(
                Transaction(
                    amount = 15000.0,
                    transactionType = "Debit", // مدين (سددنا له مبلغ)
                    csId = c2,
                    invoiceId = "PUR-#5001",
                    partyType = "الموردين",
                    description = "دفعة مسددة للمورد من الخزينة الرئيسية"
                )
            )

            // Let's also simulate a Stripe payment for Mona Salama (c5)
            val invStripeId = invoiceDao.insertInvoice(
                Invoice(
                    invoiceType = "Cash",
                    csId = c5,
                    totalInvoice = 12500.0,
                    netInvoice = 12500.0,
                    paidAmount = 12500.0,
                    remainingAmount = 0.0,
                    paymentType = "فيزا (Stripe)",
                    status = 1
                )
            ).toInt()

            transactionDao.insertTransaction(
                Transaction(
                    amount = 12500.0,
                    transactionType = "Debit",
                    csId = c5,
                    invoiceId = "INV-#1003",
                    description = "شراء شاشة سامسونج الذكية 55 بوصة"
                )
            )

            transactionDao.insertTransaction(
                Transaction(
                    amount = 12500.0,
                    transactionType = "Credit",
                    csId = c5,
                    invoiceId = "INV-#1003",
                    description = "دفع إلكتروني آمن بوابة Stripe"
                )
            )

            val stripeBox = cashBoxDao.getCashBoxById(box2Id.toInt())
            if (stripeBox != null) {
                cashBoxDao.updateCashBox(stripeBox.copy(currentBalance = stripeBox.currentBalance + 12500.0))
            }
        }
    }

    // Insert new customer/contact
    suspend fun addContact(name: String, phone: String, email: String, address: String, type: String) {
        contactDao.insertContact(
            Contact(
                name = name,
                contactNumber = phone,
                email = email,
                address = address,
                type = type
            )
        )
    }

    // Insert Invoice and update cash balance and transaction log
    suspend fun createInvoice(
        csId: Int,
        total: Double,
        discount: Double,
        paid: Double,
        payType: String,
        boxId: Int
    ): Int {
        val net = total - discount
        val rem = if (net > paid) net - paid else 0.0
        val type = if (rem > 0) "Credit" else "Cash"

        val invId = invoiceDao.insertInvoice(
            Invoice(
                invoiceType = type,
                csId = csId,
                totalInvoice = total,
                discountCash = discount,
                netInvoice = net,
                paidAmount = paid,
                remainingAmount = rem,
                paymentType = payType,
                status = 1
            )
        ).toInt()

        val itemCode = "INV-#${1000 + invId}"

        // Create transaction logs
        // 1. Debit the Customer account for the net purchase value
        transactionDao.insertTransaction(
            Transaction(
                amount = net,
                transactionType = "Debit",
                csId = csId,
                invoiceId = itemCode,
                description = "فاتورة مبيعات رقم $itemCode"
            )
        )

        // 2. If customer paid anything, credit the customer and update the specified cashbox
        if (paid > 0) {
            transactionDao.insertTransaction(
                Transaction(
                    amount = paid,
                    transactionType = "Credit",
                    csId = csId,
                    invoiceId = itemCode,
                    description = "دفعة مسددة للفاتورة $itemCode"
                )
            )

            val box = cashBoxDao.getCashBoxById(boxId)
            if (box != null) {
                cashBoxDao.updateCashBox(box.copy(currentBalance = box.currentBalance + paid))
            }
        }

        return invId
    }

    // Submit a Payment transaction manually (كشف حساب دفعة)
    suspend fun addPayment(csId: Int, amount: Double, description: String, boxId: Int, txType: String) {
        transactionDao.insertTransaction(
            Transaction(
                amount = amount,
                transactionType = txType, // "Credit" (استلام نقدية) or "Debit" (دفع نقدية)
                csId = csId,
                description = description
            )
        )

        val box = cashBoxDao.getCashBoxById(boxId)
        if (box != null) {
            val mult = if (txType == "Credit") 1.0 else -1.0
            cashBoxDao.updateCashBox(box.copy(currentBalance = box.currentBalance + (amount * mult)))
        }
    }

    // Compute account statement status for clean ledger
    suspend fun getAccountSummary(csId: Int): AccountSummary {
        val contactRef = contactDao.getContactById(csId) ?: return AccountSummary(0.0, 0.0, 0.0, "Customer")
        val txList = transactionDao.getTransactionsForContact(csId).first()

        var totalDebit = 0.0 // المدفوعات المستحقة عليه (Debit)
        var totalCredit = 0.0 // المبالغ المسددة منه (Credit)

        for (tx in txList) {
            if (tx.transactionType == "Debit") {
                totalDebit += tx.amount
            } else if (tx.transactionType == "Credit") {
                totalCredit += tx.amount
            }
        }

        val balance = if (contactRef.type == "Customer") {
            totalDebit - totalCredit // For customer: Positive means they owe us money
        } else {
            totalCredit - totalDebit // For vendor: Positive means we owe them money
        }

        return AccountSummary(
            totalDebit = totalDebit,
            totalCredit = totalCredit,
            currentBalance = balance,
            partyType = contactRef.type
        )
    }

    // Simulated Stripe Card Stripe Payment Session Initiator
    suspend fun processStripeCharge(
        amount: Double,
        cardNumber: String,
        cvc: String,
        expDate: String,
        customerName: String
    ): StripeResult {
        // Simulating electronic stripe processing request delay
        kotlinx.coroutines.delay(2000)
        return if (cardNumber.length >= 15 && cvc.length >= 3) {
            val chargeId = "ch_stripe_" + Random.nextInt(100000, 999999).toString() + "sec"
            StripeResult(success = true, chargeId = chargeId, message = "تمت عملية الدفع بنجاح! رقم المعاملة: $chargeId")
        } else {
            StripeResult(success = false, chargeId = "", message = "خطأ في معالجة بطاقة Stripe الائتمانية. يرجى التحقق من المدخلات.")
        }
    }
}

data class AccountSummary(
    val totalDebit: Double,
    val totalCredit: Double,
    val currentBalance: Double,
    val partyType: String
)

data class StripeResult(
    val success: Boolean,
    val chargeId: String,
    val message: String
)
