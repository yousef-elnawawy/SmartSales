package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SalesViewModel(private val repository: SalesRepository) : ViewModel() {

    // --- Authentication & Permission state ---
    private val _currentUserState = MutableStateFlow(UserState())
    val currentUserState: StateFlow<UserState> = _currentUserState.asStateFlow()

    // --- Live DB states ---
    val contacts: StateFlow<List<Contact>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashBoxes: StateFlow<List<CashBox>> = repository.cashBoxes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Notification Streams ---
    private val _notificationsList = MutableStateFlow<List<String>>(
        listOf(
            "مرحباً بك في نظام SmartSales PRO المترابط 🚀",
            "تم تفعيل الاتصال والتزامن المحلي بقاعدة البيانات SmartSalesSystemPRO بنجاح."
        )
    )
    val notificationsList: StateFlow<List<String>> = _notificationsList.asStateFlow()

    // --- Detailed Account Statement States ---
    private val _selectedContactForStatement = MutableStateFlow<Contact?>(null)
    val selectedContactForStatement: StateFlow<Contact?> = _selectedContactForStatement.asStateFlow()

    private val _statementTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val statementTransactions: StateFlow<List<Transaction>> = _statementTransactions.asStateFlow()

    private val _statementSummary = MutableStateFlow<AccountSummary?>(null)
    val statementSummary: StateFlow<AccountSummary?> = _statementSummary.asStateFlow()

    // --- Stripe Charging Processing states ---
    private val _stripeProcessingState = MutableStateFlow<StripeUiState>(StripeUiState.Idle)
    val stripeProcessingState: StateFlow<StripeUiState> = _stripeProcessingState.asStateFlow()

    // --- Active Stock items for POS list (Fully reactive from Room database) ---
    val productsList: StateFlow<List<SalesItem>> = repository.items
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SQL Server Connection Parameters ---
    private val _sqlIp = MutableStateFlow("192.168.8.6")
    val sqlIp: StateFlow<String> = _sqlIp.asStateFlow()

    private val _sqlPort = MutableStateFlow("1433")
    val sqlPort: StateFlow<String> = _sqlPort.asStateFlow()

    private val _sqlDatabase = MutableStateFlow("SmartSalesSystemPRO")
    val sqlDatabase: StateFlow<String> = _sqlDatabase.asStateFlow()

    private val _sqlUsername = MutableStateFlow("mo")
    val sqlUsername: StateFlow<String> = _sqlUsername.asStateFlow()

    private val _sqlPassword = MutableStateFlow("123123000")
    val sqlPassword: StateFlow<String> = _sqlPassword.asStateFlow()

    private val _sqlConnectionStatus = MutableStateFlow("غير متصل (اضغط لاختبار الاتصال) ⚪")
    val sqlConnectionStatus: StateFlow<String> = _sqlConnectionStatus.asStateFlow()

    private val _sqlSyncProgress = MutableStateFlow("")
    val sqlSyncProgress: StateFlow<String> = _sqlSyncProgress.asStateFlow()

    private val _sqlIsSyncing = MutableStateFlow(false)
    val sqlIsSyncing: StateFlow<Boolean> = _sqlIsSyncing.asStateFlow()

    init {
        // Prepopulate database if vacant
        viewModelScope.launch {
            repository.populateDemoDataIfEmpty()
        }

        // Collect Real-time SignalR simulated system signals
        viewModelScope.launch {
            repository.liveNotifications.collect { newNotification ->
                _notificationsList.value = listOf(newNotification) + _notificationsList.value
            }
        }
    }

    fun updateSqlConfigs(ip: String, port: String, db: String, user: String, pass: String) {
        _sqlIp.value = ip
        _sqlPort.value = port
        _sqlDatabase.value = db
        _sqlUsername.value = user
        _sqlPassword.value = pass
    }

    fun testSqlServerConnection() {
        viewModelScope.launch {
            _sqlConnectionStatus.value = "جاري الاتصال بقاعدة البيانات... 🟡"
            val result = SqlServerManager.testConnection(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value
            )
            result.onSuccess { msg ->
                _sqlConnectionStatus.value = "متصل بنجاح: SmartSalesSystemPRO 🟢"
                _notificationsList.value = listOf("اتصال ناجح: متصل ومتجاوب بذكاء مع خادم SQL Server المحلي 💻") + _notificationsList.value
            }.onFailure { err ->
                _sqlConnectionStatus.value = "فشل الاتصال: ${err.localizedMessage} 🔴"
                _notificationsList.value = listOf("خطأ اتصال: تحقق من منفذ 1433 وإعدادات TCP/IP في اللابتوب ⚠️") + _notificationsList.value
            }
        }
    }

    fun syncPullFromSqlServer() {
        viewModelScope.launch {
            _sqlIsSyncing.value = true
            _sqlSyncProgress.value = "جاري الاتصال وسحب بيانات العملاء... ⏳"
            
            // 1. Pull contacts
            val contactsResult = SqlServerManager.pullContacts(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value,
                contactDao = repository.contactDao
            )

            _sqlSyncProgress.value = "جاري ترحيل أرصدة الخزينة والحسابات الدائنة... ⏳"
            // 2. Pull cashboxes
            val boxesResult = SqlServerManager.pullCashBoxes(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value,
                cashBoxDao = repository.cashBoxDao
            )

            _sqlSyncProgress.value = "جاري جلب السلع والأسعار والمخزون الحي... ⏳"
            // 3. Pull products
            val itemsResult = SqlServerManager.pullItems(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value
            )

            var summary = "اكتمل التزامن من خادم SQL Server! ✨\n"
            contactsResult.onSuccess { summary += "• تم جلب وتحديث $it عميل ومورد بنجاح\n" }
            boxesResult.onSuccess { summary += "• تم مطابقة $it حساب نقدي رسمي\n" }
            itemsResult.onSuccess { items ->
                repository.itemDao.deleteAllItems()
                repository.itemDao.insertItems(items)
                summary += "• تم تحديث ${items.size} سلعة تجارية نشطة بالأسعار الفعلية من خادم SQL Server\n"
            }
            itemsResult.onFailure { err ->
                summary += "• خطأ في جلب السلع: ${err.localizedMessage}\n"
            }

            _sqlSyncProgress.value = summary
            _sqlIsSyncing.value = false
            _notificationsList.value = listOf("مزامنة مستوردة: تم سحب السلع والعملاء والخزائن مباشر من اللابتوب 📱") + _notificationsList.value
        }
    }

    fun syncPushToSqlServer() {
        viewModelScope.launch {
            _sqlIsSyncing.value = true
            _sqlSyncProgress.value = "جاري ترحيل فواتير المبيعات الصادرة... ⏳"

            // Direct querying lists from databases
            val currentInvoices = repository.invoices.first()
            val currentTransactions = repository.transactions.first()

            val result = SqlServerManager.pushLocalInvoices(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value,
                invoiceList = currentInvoices,
                transactionList = currentTransactions
            )

            result.onSuccess { count ->
                _sqlSyncProgress.value = "اكتمل رفع البيانات! تم ترحيل $count فواتير POS ومعاملات نقدية جديدة إلى SQL Server. ✓"
                _notificationsList.value = listOf("مزامنة المصدر: تم ترحيل كافة فواتير الدفع المباشر والآجل بنجاح للابتوب 💻") + _notificationsList.value
            }.onFailure { err ->
                _sqlSyncProgress.value = "فشل الترحيل والمزامنة: ${err.localizedMessage}"
                _notificationsList.value = listOf("خطأ مزامنة: فشل ترحيل فواتير الهاتف للابتوب: ${err.localizedMessage} ⚠️") + _notificationsList.value
            }
            _sqlIsSyncing.value = false
        }
    }

    // --- Authentication Async Loading states ---
    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Attempt authorization session asynchronously with direct SQL Server lookup and local fallback
    fun performLoginAsync(usernameInput: String, passwordInput: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _loginError.value = null
            
            // Try SQL Server Auth first
            val sqlResult = SqlServerManager.authenticateUser(
                ip = _sqlIp.value,
                port = _sqlPort.value,
                db = _sqlDatabase.value,
                user = _sqlUsername.value,
                pass = _sqlPassword.value,
                usernameInput = usernameInput.trim(),
                passwordInput = passwordInput.trim()
            )
            
            sqlResult.onSuccess { sqlUser ->
                // Successful Auth on SQL Server Users table!
                val fullPermissions = mapOf(
                    "A1_SALES_DASHBOARD" to true,
                    "A2_NEW_INVOICE" to true,
                    "A3_CUSTOMER_MANAGEMENT" to true,
                    "A4_ACCOUNT_STATEMENT" to (sqlUser.role.lowercase() != "cashier" && !sqlUser.role.contains("كاشير")),
                    "A5_CASH_BOXES" to true,
                    "A6_STRIPE_PAYMENT" to true,
                    "A7_DELETE_RECORDS" to (sqlUser.role.lowercase() != "cashier" && !sqlUser.role.contains("كاشير"))
                )
                _currentUserState.value = UserState(
                    userId = sqlUser.id,
                    username = sqlUser.username,
                    fullName = sqlUser.fullName,
                    role = sqlUser.role,
                    isLoggedIn = true,
                    permissions = fullPermissions
                )
                 _isLoggingIn.value = false
                 _notificationsList.value = listOf("أهلاً بك ${sqlUser.fullName}! تم التحقق من هويتك بنجاح من جدول Users على الخادم 💻") + _notificationsList.value
                 
                 // Immediately pull all active items, units, pricing, cash boxes and clients from SQL Server
                 syncPullFromSqlServer()
                 
                 onComplete(true)
            }.onFailure { err ->
                // If SQL auth failed, check offline/local master fallback values first
                val cleanUser = usernameInput.trim().lowercase()
                if (cleanUser == "admin" && passwordInput == "123123") {
                    val fullPermissions = mapOf(
                        "A1_SALES_DASHBOARD" to true,
                        "A2_NEW_INVOICE" to true,
                        "A3_CUSTOMER_MANAGEMENT" to true,
                        "A4_ACCOUNT_STATEMENT" to true,
                        "A5_CASH_BOXES" to true,
                        "A6_STRIPE_PAYMENT" to true,
                        "A7_DELETE_RECORDS" to true
                    )
                    _currentUserState.value = UserState(
                        userId = 1,
                        username = "admin",
                        fullName = "مشرف النظام العام (محلي)",
                        role = "Administrator",
                        isLoggedIn = true,
                        permissions = fullPermissions
                    )
                    _isLoggingIn.value = false
                    onComplete(true)
                } else if (cleanUser == "cashier" && passwordInput == "1122") {
                    val cashierPermissions = mapOf(
                        "A1_SALES_DASHBOARD" to true,
                        "A2_NEW_INVOICE" to true,
                        "A3_CUSTOMER_MANAGEMENT" to true,
                        "A4_ACCOUNT_STATEMENT" to false,
                        "A5_CASH_BOXES" to true,
                        "A6_STRIPE_PAYMENT" to true,
                        "A7_DELETE_RECORDS" to false
                    )
                    _currentUserState.value = UserState(
                        userId = 2,
                        username = "cashier",
                        fullName = "كاشير المبيعات المباشرة (محلي)",
                        role = "Cashier",
                        isLoggedIn = true,
                        permissions = cashierPermissions
                    )
                    _isLoggingIn.value = false
                    onComplete(true)
                } else {
                    // Report the SQL error if no offline master credentials match
                    _loginError.value = err.localizedMessage ?: "اسم المستخدم أو كلمة المرور غير صحيحة."
                    _isLoggingIn.value = false
                    onComplete(false)
                }
            }
        }
    }

    // Attempt authorization session
    fun performLogin(usernameInput: String, passwordInput: String): Boolean {
        // Kept for backward compatibility if any test triggers it
        if (usernameInput.trim().lowercase() == "admin" && passwordInput == "123123") {
            val fullPermissions = mapOf(
                "A1_SALES_DASHBOARD" to true,
                "A2_NEW_INVOICE" to true,
                "A3_CUSTOMER_MANAGEMENT" to true,
                "A4_ACCOUNT_STATEMENT" to true,
                "A5_CASH_BOXES" to true,
                "A6_STRIPE_PAYMENT" to true,
                "A7_DELETE_RECORDS" to true
            )
            _currentUserState.value = UserState(
                userId = 1,
                username = "admin",
                fullName = "مشرف النظام العام",
                role = "Administrator",
                isLoggedIn = true,
                permissions = fullPermissions
            )
            return true
        }
        return false
    }

    // Close auth credentials
    fun performLogout() {
        _currentUserState.value = UserState()
    }

    // Add new custom contact client
    fun addNewContact(name: String, phone: String, email: String, address: String, type: String) {
        viewModelScope.launch {
            repository.addContact(name, phone, email, address, type)
            _notificationsList.value = listOf("تم تسجيل الحساب المالي الجديد: $name ($type) بنجاح ✅") + _notificationsList.value
        }
    }

    // Create complete sales invoice transaction
    fun createSaleInvoice(csId: Int, total: Double, discount: Double, paid: Double, payType: String, boxId: Int) {
        viewModelScope.launch {
            val invId = repository.createInvoice(csId, total, discount, paid, payType, boxId)
            _notificationsList.value = listOf("مبيعات: ترحيل الفاتورة رقم INV-#${1000 + invId} بنجاح بقيمة ${total - discount} ج.م 🧾") + _notificationsList.value
        }
    }

    // Add explicit payment movement to accounts
    fun registerManualPayment(csId: Int, amount: Double, desc: String, boxId: Int, type: String) {
        viewModelScope.launch {
            repository.addPayment(csId, amount, desc, boxId, type)
            val action = if (type == "Credit") "تحصيل نقدية" else "صرف نقدية"
            _notificationsList.value = listOf("حركة مالية: تم $action بقيمة $amount ج.م لبيان ($desc) 💸") + _notificationsList.value
            // Refresh statement if this customer is currently monitored
            val currentSelected = _selectedContactForStatement.value
            if (currentSelected != null && currentSelected.id == csId) {
                loadContactStatement(currentSelected)
            }
        }
    }

    // Populate Account Statement results
    fun loadContactStatement(contact: Contact) {
        viewModelScope.launch {
            _selectedContactForStatement.value = contact
            repository.getTransactionsForContact(contact.id).collectLatest { txs ->
                _statementTransactions.value = txs
                _statementSummary.value = repository.getAccountSummary(contact.id)
            }
        }
    }

    // Clear state ledger selection
    fun clearContactStatementSelection() {
        _selectedContactForStatement.value = null
        _statementTransactions.value = emptyList()
        _statementSummary.value = null
    }

    // Process secure online Stripe charge
    fun processStripeCheckout(amount: Double, cardNum: String, cvc: String, exp: String, name: String, csId: Int, boxId: Int) {
        viewModelScope.launch {
            _stripeProcessingState.value = StripeUiState.Loading
            val StripeResult = repository.processStripeCharge(amount, cardNum, cvc, exp, name)
            if (StripeResult.success) {
                _stripeProcessingState.value = StripeUiState.Success(StripeResult.chargeId, StripeResult.message)
                // Register the online cash inbound in local DB
                repository.addPayment(
                    csId = csId,
                    amount = amount,
                    description = "سداد إلكتروني معالج Stripe - معاملة ${StripeResult.chargeId}",
                    boxId = boxId,
                    txType = "Credit"
                )
                _notificationsList.value = listOf("الدفع الإلكتروني: تم تسوية $amount ج.م مع بنك Stripe بنجاح 🔒") + _notificationsList.value
            } else {
                _stripeProcessingState.value = StripeUiState.Error(StripeResult.message)
            }
        }
    }

    fun resetStripeState() {
        _stripeProcessingState.value = StripeUiState.Idle
    }

    fun dismissNotification(index: Int) {
        val current = _notificationsList.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _notificationsList.value = current
        }
    }

    fun triggerManualDemoSync() {
        _notificationsList.value = listOf("مزامنة السحابة (SignalR Hub): تم تحديث وتنشيط أرصدة الصناديق والمبيعات بنجاح 🌐") + _notificationsList.value
    }
}

// Security login record holding M3 permissions flags
data class UserState(
    val userId: Int = 0,
    val username: String = "",
    val fullName: String = "",
    val role: String = "",
    val isLoggedIn: Boolean = false,
    val permissions: Map<String, Boolean> = emptyMap()
)

sealed interface StripeUiState {
    object Idle : StripeUiState
    object Loading : StripeUiState
    data class Success(val chargeId: String, val message: String) : StripeUiState
    data class Error(val message: String) : StripeUiState
}
