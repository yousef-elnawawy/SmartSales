package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random
import com.example.data.Contact
import com.example.data.CashBox
import com.example.data.SalesItem
import com.example.data.Transaction
import com.example.data.Invoice
import java.util.*

// Visual palette variables
val DeepNavy = Color(0xFF1E40AF) // Professional Polish: Blue 800 Primary Brand Blue
val SolidDark = Color(0xFF1E293B) // Dark elements / slate dark
val LightSlate = Color(0xFFF3F4F6) // Warm light grey canvas / slate-50
val MediumSlate = Color(0xFF64748B) // Slate text & border
val ActiveCyan = Color(0xFF3B82F6) // Active blue-500 accent
val AlertGold = Color(0xFFF59E0B) // Warn yellow
val SuccessTeal = Color(0xFF10B981) // Success emerald
val ErrorCrimson = Color(0xFFEF4444) // Error red

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SalesAppUi(viewModel: SalesViewModel) {
    val userState by viewModel.currentUserState.collectAsState()
    
    // Always enforce RTL layout direction for authentic Arabic enterprise software feel
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (userState.isLoggedIn) LightSlate else DeepNavy
        ) {
            AnimatedContent(
                targetState = userState.isLoggedIn,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "auth_transition"
            ) { isLoggedIn ->
                if (isLoggedIn) {
                    MainHubScreen(viewModel = viewModel)
                } else {
                    LoginScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: SalesViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    val sqlIp by viewModel.sqlIp.collectAsState()
    val sqlPort by viewModel.sqlPort.collectAsState()
    val sqlDb by viewModel.sqlDatabase.collectAsState()
    val sqlUser by viewModel.sqlUsername.collectAsState()
    val sqlPass by viewModel.sqlPassword.collectAsState()

    var editIp by remember { mutableStateOf(sqlIp) }
    var editPort by remember { mutableStateOf(sqlPort) }
    var editDb by remember { mutableStateOf(sqlDb) }
    var editUser by remember { mutableStateOf(sqlUser) }
    var editPass by remember { mutableStateOf(sqlPass) }

    var showConnectionConfig by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepNavy, Color(0xFF020617))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Logo Header
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ActiveCyan, SuccessTeal))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(45.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SmartSales PRO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "نظام المبيعات والحسابات المتكامل دوت نت ١٠",
                fontSize = 13.sp,
                color = ActiveCyan,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Connection Configuration Toggle Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ActiveCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudSync,
                                contentDescription = "Syncing server",
                                tint = ActiveCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "الخادم النشط: ${sqlIp}:${sqlPort}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "قاعدة البيانات: $sqlDb",
                                    fontSize = 10.sp,
                                    color = LightSlate.copy(alpha = 0.7f)
                                )
                            }
                        }
                        TextButton(
                            onClick = { showConnectionConfig = !showConnectionConfig },
                            colors = ButtonDefaults.textButtonColors(contentColor = ActiveCyan)
                        ) {
                            Text(
                                if (showConnectionConfig) "إغلاق 🙈" else "تعديل الشبكة ⚙️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (showConnectionConfig) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editIp,
                            onValueChange = { editIp = it },
                            label = { Text("عنوان IP اللابتوب", color = LightSlate) },
                            placeholder = { Text("مثال: 192.168.8.6", color = MediumSlate) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ActiveCyan,
                                unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPort,
                                onValueChange = { editPort = it },
                                label = { Text("المنفذ (Port)", color = LightSlate) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActiveCyan,
                                    unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editDb,
                                onValueChange = { editDb = it },
                                label = { Text("قاعدة البيانات", color = LightSlate) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActiveCyan,
                                    unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editUser,
                                onValueChange = { editUser = it },
                                label = { Text("مستخدم SQL", color = LightSlate) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActiveCyan,
                                    unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editPass,
                                onValueChange = { editPass = it },
                                label = { Text("كلمة المرور", color = LightSlate) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ActiveCyan,
                                    unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.updateSqlConfigs(editIp, editPort, editDb, editUser, editPass)
                                showConnectionConfig = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ إعدادات الاتصال والمنفذ", fontSize = 11.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inputs panel
            Card(
                colors = CardDefaults.cardColors(containerColor = SolidDark),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "تسجيل الدخول للنظام",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Username entry
                    Text(
                        text = "اسم المستخدم (من جدول Users)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightSlate.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        placeholder = { Text("أدخل اسم المستخدم", color = MediumSlate) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = ActiveCyan) },
                        isError = errorMessage != null || loginError != null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActiveCyan,
                            unfocusedBorderColor = MediumSlate.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password entry
                    Text(
                        text = "كلمة المرور",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightSlate.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        placeholder = { Text("أدخل رمز المرور السري", color = MediumSlate) },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = ActiveCyan) },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorMessage != null || loginError != null,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActiveCyan,
                            unfocusedBorderColor = MediumSlate.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val displayError = loginError ?: errorMessage
                    if (displayError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = displayError,
                            color = ErrorCrimson,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "يرجى تعبئة كافة حقول المدخلات لتسجيل الدخول."
                            } else {
                                viewModel.performLoginAsync(username, password) { success ->
                                    if (!success) {
                                        errorMessage = null // Let ViewModel's error stream handle it
                                    }
                                }
                            }
                        },
                        enabled = !isLoggingIn,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoggingIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = DeepNavy
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("جاري التحقق وصلاحية الدخول...", fontSize = 14.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = "تسجيل الدخول الآمن",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Helpful demo credentials
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFB74D).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 معلومات الدخول التجريبي للنظام الموحد:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AlertGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• حساب المدير (صلاحيات كاملة): admin / 123123",
                        fontSize = 11.sp,
                        color = LightSlate
                    )
                    Text(
                        text = "• حساب الكاشير (صلاحيات مبيعات محدودة): cashier / 1122",
                        fontSize = 11.sp,
                        color = LightSlate
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MainHubScreen(viewModel: SalesViewModel) {
    val userState by viewModel.currentUserState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            SalesAppHeader(userState = userState, onLogout = { viewModel.performLogout() })
        },
        bottomBar = {
            SalesAppBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                userState = userState
            )
        },
        containerColor = LightSlate
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardTab(viewModel, onNavigateTab = { selectedTab = it })
                1 -> PosInvoiceTab(viewModel)
                2 -> LedgerTab(viewModel)
                3 -> StripeTab(viewModel)
            }
        }
    }
}

@Composable
fun SalesAppHeader(userState: UserState, onLogout: () -> Unit) {
    Surface(
        color = DeepNavy,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userState.fullName.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = userState.fullName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (userState.role == "Administrator") "مدير النظام (SmartSales PRO)" else "كاشير المبيعات",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            IconButton(
                onClick = onLogout,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "خروج",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SalesAppBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit, userState: UserState) {
    // Check if user has permission for Stripe or Statement tab
    val canAccessLedger = userState.permissions["A4_ACCOUNT_STATEMENT"] ?: true
    val canAccessStripe = userState.permissions["A6_STRIPE_PAYMENT"] ?: true

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("الرئيسية", fontSize = 11.sp) },
            icon = { Icon(if (selectedTab == 0) Icons.Filled.Home else Icons.Filled.Home, contentDescription = "الرئيسية") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepNavy,
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = DeepNavy,
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFFDBEAFE)
            )
        )
        
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("فاتورة مبيعات", fontSize = 11.sp) },
            icon = { Icon(Icons.Filled.Receipt, contentDescription = "فاتورة مبيعات") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DeepNavy,
                unselectedIconColor = Color(0xFF94A3B8),
                selectedTextColor = DeepNavy,
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFFDBEAFE)
            )
        )

        if (canAccessLedger) {
            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                label = { Text("كشف الحساب", fontSize = 11.sp) },
                icon = { Icon(Icons.Filled.People, contentDescription = "كشف الحساب") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepNavy,
                    unselectedIconColor = Color(0xFF94A3B8),
                    selectedTextColor = DeepNavy,
                    unselectedTextColor = Color(0xFF94A3B8),
                    indicatorColor = Color(0xFFDBEAFE)
                )
            )
        }

        if (canAccessStripe) {
            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                label = { Text("دفع Stripe", fontSize = 11.sp) },
                icon = { Icon(Icons.Filled.CreditCard, contentDescription = "دفع Stripe") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepNavy,
                    unselectedIconColor = Color(0xFF94A3B8),
                    selectedTextColor = DeepNavy,
                    unselectedTextColor = Color(0xFF94A3B8),
                    indicatorColor = Color(0xFFDBEAFE)
                )
            )
        }
    }
}

// ======================== TAB 1: DASHBOARD ========================
@Composable
fun DashboardTab(viewModel: SalesViewModel, onNavigateTab: (Int) -> Unit) {
    val cashBoxes by viewModel.cashBoxes.collectAsState()
    val notifications by viewModel.notificationsList.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    var showSqlServerSyncDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // SignalR Connection Status Badge (Professional Polish HTML Spec)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)), // bg-emerald-50
                border = BorderStroke(1.dp, Color(0xFFD1FAE5)), // border-emerald-100
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Soft green pulsing bulb indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "بث SignalR المباشر: متصل بنجاح 🟢",
                            color = Color(0xFF065F46), // text-emerald-800
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "v10.0.1 PRO",
                        color = Color(0xFF059669), // text-emerald-600
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Welcoming Card with Dynamic SignalR alert banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "لوحة المتابعة الشاملة",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SuccessTeal)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بث SignalR مباشر",
                                    color = SuccessTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // SignalR Live Notifications Ticker
                    if (notifications.isNotEmpty()) {
                        Text(
                            text = "أحدث إشعارات الخادم اللحظية:",
                            fontSize = 11.sp,
                            color = ActiveCyan,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        notifications.take(3).forEachIndexed { idx, notifyMsg ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SolidDark)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.NotificationsActive,
                                            contentDescription = "Notification",
                                            tint = AlertGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = notifyMsg,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.dismissNotification(idx) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "dismiss",
                                            tint = MediumSlate,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Treasury (CashBoxes) Horizontal view
        item {
            Text(
                text = "صناديق الحسابات والخزائن 💰",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cashBoxes.forEach { box ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .width(260.dp)
                            .shadow(2.dp, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = box.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = if (box.name.contains("Stripe")) Icons.Filled.CreditCard else Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = ActiveCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "رصيد الصندوق الحالي:",
                                fontSize = 11.sp,
                                color = MediumSlate
                            )
                            Text(
                                text = "${String.format("%,.2f", box.currentBalance)} ج.م",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessTeal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = box.description,
                                fontSize = 10.sp,
                                color = MediumSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        // Quick Action Grid (Professional Polish HTML Spec)
        item {
            Text(
                text = "الوصول السريع والعمليات اللحظية ⚡",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Action 1: New Sale
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(1) }
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)), // light blue
                            contentAlignment = Alignment.Center
                        ) {
                            Text("➕", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "فاتورة جديدة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                // Action 2: Invoices
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(2) }
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEEF2F6)), // light grey
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📋", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "كشف الحساب",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                // Action 3: Payments (Stripe)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateTab(3) }
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFECFDF5)), // light emerald
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💳", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "بوابة الدفع",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                // Action 4: SQL Server Sync
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { 
                            showSqlServerSyncDialog = true
                        }
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFFBEB)), // light amber
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💻", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تزامن SQL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }
            }
        }
        
        // Fast statistics widget
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("إجمالي الفواتير", fontSize = 11.sp, color = MediumSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${invoices.size} فاتورة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("مبيعات الخادم الكلية", fontSize = 11.sp, color = MediumSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        val salesTotal = invoices.sumOf { it.netInvoice }
                        Text(
                            text = "${String.format("%,.0f", salesTotal)} ج.م",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActiveCyan
                        )
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).shadow(2.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الديون الملعقة", fontSize = 11.sp, color = MediumSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        val remDebt = invoices.sumOf { it.remainingAmount }
                        Text(
                            text = "${String.format("%,.0f", remDebt)} ج.م",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorCrimson
                        )
                    }
                }
            }
        }
        
        // Recent transactional operations logs
        item {
            Text(
                text = "آخر الحركات المالية المصدقة 📋",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا يوجد قيود حركات مالية مسجلة حالياً.", color = MediumSlate)
                }
            }
        } else {
            items(transactions.take(10)) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (tx.transactionType == "Credit") SuccessTeal.copy(alpha = 0.15f)
                                        else ErrorCrimson.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.transactionType == "Credit") Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (tx.transactionType == "Credit") SuccessTeal else ErrorCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy
                                )
                                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                Text(
                                    text = formatter.format(Date(tx.transactionDate)),
                                    fontSize = 10.sp,
                                    color = MediumSlate
                                )
                            }
                        }
                        
                        Text(
                            text = "${if (tx.transactionType == "Credit") "+" else "-"}${String.format("%,.1f", tx.amount)} ج.م",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.transactionType == "Credit") SuccessTeal else ErrorCrimson
                        )
                    }
                }
            }
        }
    }

    if (showSqlServerSyncDialog) {
        val ip by viewModel.sqlIp.collectAsState()
        val port by viewModel.sqlPort.collectAsState()
        val db by viewModel.sqlDatabase.collectAsState()
        val user by viewModel.sqlUsername.collectAsState()
        val pass by viewModel.sqlPassword.collectAsState()
        val connectionStatus by viewModel.sqlConnectionStatus.collectAsState()
        val syncProgress by viewModel.sqlSyncProgress.collectAsState()
        val isSyncing by viewModel.sqlIsSyncing.collectAsState()

        var editIp by remember { mutableStateOf(ip) }
        var editPort by remember { mutableStateOf(port) }
        var editDb by remember { mutableStateOf(db) }
        var editUser by remember { mutableStateOf(user) }
        var editPass by remember { mutableStateOf(pass) }
        var showSettings by remember { mutableStateOf(false) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { if (!isSyncing) showSqlServerSyncDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بوابة ربط خادم MS SQL Server 💻",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        IconButton(
                            onClick = { if (!isSyncing) showSqlServerSyncDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("❌", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يتصل هذا التطبيق مباشرة بقاعدة بيانات SmartSalesSystemPRO على لابتوبك الشخصي المتواجد على نفس الشبكة المحلية اللاسلكية.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Connection Status Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                connectionStatus.contains("🟢") -> Color(0xFFECFDF5)
                                connectionStatus.contains("🔴") -> Color(0xFFFEF2F2)
                                connectionStatus.contains("🟡") -> Color(0xFFFFFBEB)
                                else -> Color(0xFFF3F4F6)
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "حالة الاتصال والخدمة الحالية:",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = connectionStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick status or inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الخادم المستهدف: ${ip}:${port}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MediumSlate
                        )
                        TextButton(
                            onClick = { showSettings = !showSettings }
                        ) {
                            Text(
                                text = if (showSettings) "إخفاء التفاصيل 🙈" else "تعديل الإعدادات والشبكة ⚙️",
                                fontSize = 11.sp,
                                color = DeepNavy
                            )
                        }
                    }

                    if (showSettings) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editIp,
                            onValueChange = { editIp = it },
                            label = { Text("عنوان IP اللابتوب") },
                            placeholder = { Text("مثال: 192.168.8.6") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editPort,
                                onValueChange = { editPort = it },
                                label = { Text("المنفذ (Port)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editDb,
                                onValueChange = { editDb = it },
                                label = { Text("اسم قاعدة البيانات") },
                                singleLine = true,
                                modifier = Modifier.weight(2f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = editUser,
                                onValueChange = { editUser = it },
                                label = { Text("المستخدم (User)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editPass,
                                onValueChange = { editPass = it },
                                label = { Text("كلمة المرور") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                viewModel.updateSqlConfigs(editIp, editPort, editDb, editUser, editPass)
                                showSettings = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ وتحديث الإعدادات 💾", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testSqlServerConnection() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("اختبار الاتصال ⚡", fontSize = 11.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pull Data
                        Button(
                            onClick = { viewModel.syncPullFromSqlServer() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("سحب البيانات ⬇️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Push Data
                        Button(
                            onClick = { viewModel.syncPushToSqlServer() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ترحيل الفواتير ⬆️", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isSyncing || syncProgress.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SolidDark), // Terminal slate
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "مخرجات المزامنة الحية:",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = SuccessTeal
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = syncProgress,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================== TAB 2: INVOICE POS ========================
@Composable
fun PosInvoiceTab(viewModel: SalesViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val cashBoxes by viewModel.cashBoxes.collectAsState()
    val scope = rememberCoroutineScope()
    
    // POS Form state
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var selectedBox by remember { mutableStateOf<CashBox?>(null) }
    val cart = remember { mutableStateListOf<Pair<SalesItem, Int>>() } // Item and Quantity
    
    var discountInput by remember { mutableStateOf("") }
    var paidInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var successAlert by remember { mutableStateOf<String?>(null) }
    
    // Compute total sum
    val subtotal = cart.sumOf { it.first.saleUnitPrice * it.second }
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val totalWithDiscount = if (subtotal > discount) subtotal - discount else 0.0
    val taxRatio = 0.14 // 14% Value Added Tax Egyptian Standard
    val vatAmount = totalWithDiscount * taxRatio
    val finalTotal = totalWithDiscount + vatAmount
    val paidAmount = if (paidInput.isNotEmpty()) (paidInput.toDoubleOrNull() ?: 0.0) else finalTotal
    val remaining = if (finalTotal > paidAmount) finalTotal - paidAmount else 0.0
    
    // Automatically set default Treasury on first load
    if (selectedBox == null && cashBoxes.isNotEmpty()) {
        selectedBox = cashBoxes.first()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "صياغة فاتورة مبيعات جديدة 📄",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
            Text(
                text = "أدخل بيانات المعاملة لترسيبها وتطبيق القيود في المحاسبة مباشرة",
                fontSize = 12.sp,
                color = MediumSlate
            )
        }
        
        // Step 1: Select Customer
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "١. اختيار العميل والمحاسب مالياً",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    if (contacts.isEmpty()) {
                        Text("الرجاء إضافة عملاء من صفحة دليل الحسابات أولاً.", color = ErrorCrimson, fontSize = 12.sp)
                    } else {
                        // Horizontal selection of Customers
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            contacts.filter { it.type == "Customer" }.forEach { client ->
                                val isSelected = selectedContact?.id == client.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) ActiveCyan else LightSlate)
                                        .clickable { selectedContact = client }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = client.name,
                                        color = if (isSelected) DeepNavy else DeepNavy.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Step 2: Select Items to POS cart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "٢. بنود وأصناف الفاتورة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Display products list available for clicking and adding
                    Text(
                        text = "انقر على الصنف لإضافته لسلة المبيعات والتسعير:",
                        fontSize = 11.sp,
                        color = MediumSlate
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val productsList by viewModel.productsList.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        productsList.forEach { pItem ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MediumSlate.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .clickable {
                                        val existingIndex = cart.indexOfFirst { it.first.id == pItem.id }
                                        if (existingIndex >= 0) {
                                            val currentQty = cart[existingIndex].second
                                            cart[existingIndex] = Pair(pItem, currentQty + 1)
                                        } else {
                                            cart.add(Pair(pItem, 1))
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(pItem.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy, maxLines = 1)
                                    Text("${pItem.saleUnitPrice} ج.م", fontSize = 10.sp, color = SuccessTeal, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show current active Cart
                    if (cart.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(LightSlate, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("السلة فارغة. الرجاء اختيار أصناف أعلاه.", color = MediumSlate, fontSize = 12.sp)
                        }
                    } else {
                        Text("قائمة الأصناف المختارة والكميات:", fontSize = 11.sp, color = DeepNavy, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        cart.forEachIndexed { index, pair ->
                            val (prod, qty) = pair
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LightSlate)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                    Text("${prod.saleUnitPrice} ج.م × $qty = ${prod.saleUnitPrice * qty} ج.م", fontSize = 10.sp, color = MediumSlate)
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (qty > 1) {
                                                cart[index] = Pair(prod, qty - 1)
                                            } else {
                                                cart.removeAt(index)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "decrease", tint = ErrorCrimson, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(qty.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = {
                                            cart[index] = Pair(prod, qty + 1)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.AddCircleOutline, contentDescription = "increase", tint = SuccessTeal, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { cart.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "delete", tint = ErrorCrimson, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Step 3: Prices and discounts Calculations
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "٣. الحسابات المالية والتسوية",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("خصم نقدي مباشر", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ActiveCyan,
                                unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = paidInput,
                            onValueChange = { paidInput = it },
                            label = { Text("المبلغ المسدد", fontSize = 11.sp) },
                            placeholder = { Text("اتركه فارغاً للسداد الكلي") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ActiveCyan,
                                unfocusedBorderColor = MediumSlate.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Choose destination Cash box
                    Text("صندوق ترحيل النقدية ومستودع التحصيل:", fontSize = 11.sp, color = MediumSlate)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cashBoxes.forEach { box ->
                            val isSelected = selectedBox?.id == box.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ActiveCyan else LightSlate)
                                    .clickable { selectedBox = box }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = box.name,
                                    fontSize = 11.sp,
                                    color = if (isSelected) DeepNavy else DeepNavy.copy(alpha = 0.8f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Price Receipt calculations mockup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightSlate, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مجموع أصناف السلة:", fontSize = 12.sp, color = MediumSlate)
                            Text("${String.format("%,.2f", subtotal)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الخصم المسموح به:", fontSize = 12.sp, color = MediumSlate)
                            Text("- ${String.format("%,.2f", discount)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ErrorCrimson)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ضريبة المبيعات المقررة (14%):", fontSize = 12.sp, color = MediumSlate)
                            Text("+ ${String.format("%,.2f", vatAmount)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AlertGold)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MediumSlate.copy(alpha = 0.2f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("صافي المطالبة بالفاتورة الكلي:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Text("${String.format("%,.2f", finalTotal)} ج.م", fontSize = 16.sp, fontWeight = FontWeight.Black, color = SuccessTeal)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المسدد للفرع نقداً:", fontSize = 12.sp, color = MediumSlate)
                            Text("${String.format("%,.2f", paidAmount)} ج.م", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                        val remaining = if (finalTotal > paidAmount) finalTotal - paidAmount else 0.0
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الرصيد/الدين المتبقي للعميل (آجل):", fontSize = 12.sp, color = MediumSlate)
                            Text("${String.format("%,.2f", remaining)} ج.م", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (remaining > 0) ErrorCrimson else SuccessTeal)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Final submission button
                    Button(
                        onClick = {
                            val contact = selectedContact
                            val box = selectedBox
                            if (contact == null) {
                                successAlert = "الرجاء تحديد العميل المرتبط بالفاتورة أولاً ⚠️"
                            } else if (cart.isEmpty()) {
                                successAlert = "الرجاء تفعيل صنف واحد على الأقل داخل السلة لإصدار الفاتورة ⚠️"
                            } else {
                                val destinationBoxId = box?.id ?: 1
                                val payTypeStr = if (remaining > 0) "آجل" else "نقدي"
                                
                                viewModel.createSaleInvoice(
                                    csId = contact.id,
                                    total = subtotal,
                                    discount = discount,
                                    paid = paidAmount,
                                    payType = payTypeStr,
                                    boxId = destinationBoxId
                                )
                                
                                successAlert = "تمت معالجة وترحيل الفاتورة رقم INV-#${Random.nextInt(5000, 9999)} بنجاح وربطها بنظام SmartSalesSystemPRO العام! 🌍✅"
                                
                                // Reset fields
                                cart.clear()
                                discountInput = ""
                                paidInput = ""
                                note = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "حفظ وترحيل الفاتورة الكترونياً 🧾",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    if (successAlert != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.OfflinePin, contentDescription = null, tint = SuccessTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successAlert ?: "",
                                    fontSize = 11.sp,
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { successAlert = null }) {
                                    Icon(Icons.Filled.Close, contentDescription = "close", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}

// ======================== TAB 3: ACCOUNT LEDGER ========================
@Composable
fun LedgerTab(viewModel: SalesViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val cashBoxes by viewModel.cashBoxes.collectAsState()
    
    // Detailed account screen states
    val selectedContactForStatement by viewModel.selectedContactForStatement.collectAsState()
    val statementTransactions by viewModel.statementTransactions.collectAsState()
    val statementSummary by viewModel.statementSummary.collectAsState()
    
    // Register custom payments inputs
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentDescInput by remember { mutableStateOf("") }
    var selectedPaymentBox by remember { mutableStateOf<CashBox?>(null) }
    var manualPayType by remember { mutableStateOf("Credit") } // "Credit" (تحصيل منه) or "Debit" (دفعنا له)
    
    var alertMsg by remember { mutableStateOf<String?>(null) }
    var exportCompletedState by remember { mutableStateOf(false) }
    var excelExportProgress by remember { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()
    
    if (selectedPaymentBox == null && cashBoxes.isNotEmpty()) {
        selectedPaymentBox = cashBoxes.first()
    }
    
    if (selectedContactForStatement != null) {
        // RENDER 3.2: ACCOUNT STATEMENT VIEW (كشف حساب عميل مفصل)
        val customer = selectedContactForStatement!!
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.clearContactStatementSelection() },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("رجوع للقائمة", fontSize = 12.sp)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ActiveCyan.copy(alpha = 0.12f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (customer.type == "Customer") "كشف حساب عميل" else "كشف حساب مورد",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ActiveCyan
                        )
                    }
                }
            }
            
            // Customer Header Info card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(customer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text("الهاتف: ${customer.contactNumber}", fontSize = 12.sp, color = MediumSlate)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("العنوان: ${customer.address}", fontSize = 12.sp, color = MediumSlate)
                        }
                        
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MediumSlate.copy(alpha = 0.2f))
                        
                        // Summary numbers block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LightSlate),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المستحقات عليه (Debit)", fontSize = 10.sp, color = MediumSlate)
                                    Text("${String.format("%,.1f", statementSummary?.totalDebit ?: 0.0)} ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LightSlate),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("المسدد منه (Credit)", fontSize = 10.sp, color = MediumSlate)
                                    Text("${String.format("%,.1f", statementSummary?.totalCredit ?: 0.0)}  ج.م", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Balance Card indicator
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if ((statementSummary?.currentBalance ?: 0.0) > 0) ErrorCrimson.copy(alpha = 0.1f) else SuccessTeal.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                val isCustomer = customer.type == "Customer"
                                val balanceText = if (isCustomer) "صافي الرصيد المستحق بذمته (مدين):" else "صافي مستحقات المورد لديننا (دائن):"
                                Text(balanceText, fontSize = 11.sp, color = DeepNavy)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${String.format("%,.2f", statementSummary?.currentBalance ?: 0.0)} ج.م",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if ((statementSummary?.currentBalance ?: 0.0) > 0) ErrorCrimson else SuccessTeal
                                )
                            }
                        }
                    }
                }
            }
            
            // QuestPDF and ClosedXML Simulated Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                excelExportProgress = 0f
                                delay(400)
                                excelExportProgress = 0.4f
                                delay(500)
                                excelExportProgress = 0.8f
                                delay(300)
                                excelExportProgress = 1.0f
                                delay(300)
                                excelExportProgress = null
                                exportCompletedState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير Excel 📊", fontSize = 11.sp)
                        }
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                excelExportProgress = 0f
                                delay(600)
                                excelExportProgress = 1.0f
                                delay(300)
                                excelExportProgress = null
                                exportCompletedState = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("QuestPDF تفصيلي 🖨️", fontSize = 11.sp)
                        }
                    }
                }
                
                if (excelExportProgress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("جاري معالجة وتوليد كشف الحساب من خادم QuestPDF...", fontSize = 11.sp, color = MediumSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(progress = excelExportProgress!!, color = ActiveCyan, modifier = Modifier.fillMaxWidth())
                    }
                }
                
                if (exportCompletedState) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SuccessTeal.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = SuccessTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تم توليد وتنزيل كشف الحساب بنجاح في مجلد Downloads/ 📂", color = DeepNavy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { exportCompletedState = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
            
            // Quick Accounts Movement Logger Form (سند قبض / صرف نقدية يدوي)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تحرير سند مالي مباشر (تحصيل نقدية / سداد)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Radio button choices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (manualPayType == "Credit") SuccessTeal.copy(alpha = 0.15f) else LightSlate)
                                    .clickable { manualPayType = "Credit" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "تحصيل / مستلم منه (Credit)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (manualPayType == "Credit") SuccessTeal else MediumSlate
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (manualPayType == "Debit") ErrorCrimson.copy(alpha = 0.15f) else LightSlate)
                                    .clickable { manualPayType = "Debit" }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "صرف / مدفوع له (Debit)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (manualPayType == "Debit") ErrorCrimson else MediumSlate
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = paymentAmountInput,
                                onValueChange = { paymentAmountInput = it },
                                label = { Text("المبلغ النقدي", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan),
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = paymentDescInput,
                                onValueChange = { paymentDescInput = it },
                                label = { Text("البيان والوصف", fontSize = 11.sp) },
                                placeholder = { Text("مثال: سداد نقدي تحت الحساب") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan),
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text("خزينة الترحيل الفني للحركة:", fontSize = 11.sp, color = MediumSlate)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cashBoxes.forEach { box ->
                                val isSel = selectedPaymentBox?.id == box.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ActiveCyan else LightSlate)
                                        .clickable { selectedPaymentBox = box }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(box.name, fontSize = 10.sp, color = if (isSel) DeepNavy else DeepNavy.copy(alpha = 0.8f))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Button(
                            onClick = {
                                val amt = paymentAmountInput.toDoubleOrNull()
                                if (amt == null || amt <= 0) {
                                    alertMsg = "يرجى كتابة مبلغ نقدي صالح وصحيح."
                                } else if (paymentDescInput.isBlank()) {
                                    alertMsg = "الرجاء كتابة بيان السند لإرفاقه بالحركة المحاسبية."
                                } else {
                                    viewModel.registerManualPayment(
                                        csId = customer.id,
                                        amount = amt,
                                        desc = paymentDescInput,
                                        boxId = selectedPaymentBox?.id ?: 1,
                                        type = manualPayType
                                    )
                                    paymentAmountInput = ""
                                    paymentDescInput = ""
                                    alertMsg = "تم إدراج الحركة المالية للسجل وتعديل رصيد الخزينة بنجاح!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ترحيل تسوية السند لـ SmartSalesSystemPRO ✅", fontSize = 12.sp)
                        }
                        
                        if (alertMsg != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(alertMsg!!, fontSize = 11.sp, color = ActiveCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Transactions Ledger table block
            item {
                Text(
                    text = "سجل كشوفات القيود المسجلة بالتاريخ 📂",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            }
            
            if (statementTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(10.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا يوجد قيود حركات مالية مسجلة لهذا الحساب.", color = MediumSlate, fontSize = 12.sp)
                    }
                }
            } else {
                items(statementTransactions) { tx ->
                    val sign = if (tx.transactionType == "Credit") "دائن (-)" else "مدين (+)"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.description, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fm = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    Text(fm.format(Date(tx.transactionDate)), fontSize = 11.sp, color = MediumSlate)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (tx.transactionType == "Credit") SuccessTeal.copy(alpha = 0.15f)
                                                else ErrorCrimson.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            sign,
                                            fontSize = 9.sp,
                                            color = if (tx.transactionType == "Credit") SuccessTeal else ErrorCrimson,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            
                            Text(
                                text = "${String.format("%,.1f", tx.amount)} ج.م",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.transactionType == "Credit") SuccessTeal else ErrorCrimson
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    } else {
        // RENDER 3.1: DIRECTORY OF CONTACTS LIST (دليل العملاء والموردين)
        var clientSearchQuery by remember { mutableStateOf("") }
        
        // Add new customer inputs state
        var showAddCustomerForm by remember { mutableStateOf(false) }
        var newCustomerName by remember { mutableStateOf("") }
        var newCustomerPhone by remember { mutableStateOf("") }
        var newCustomerEmail by remember { mutableStateOf("") }
        var newCustomerAddress by remember { mutableStateOf("") }
        var newCustomerType by remember { mutableStateOf("Customer") } // "Customer" or "Vendor"
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الحسابات المالية (العملاء والموردين) 🤝",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = "انقر فوق الاسم لاستعراض كشف الحساب المحاسبي التفصيلي",
                            fontSize = 11.sp,
                            color = MediumSlate
                        )
                    }
                    
                    IconButton(
                        onClick = { showAddCustomerForm = !showAddCustomerForm },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = ActiveCyan)
                    ) {
                        Icon(
                            imageVector = if (showAddCustomerForm) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "add customer",
                            tint = DeepNavy
                        )
                    }
                }
            }
            
            // Inline add customer form details
            if (showAddCustomerForm) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("تسجيل حساب مالي جديد بقاعدة البيانات", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            OutlinedTextField(
                                value = newCustomerName,
                                onValueChange = { newCustomerName = it },
                                label = { Text("الاسم الكامل") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = newCustomerPhone,
                                onValueChange = { newCustomerPhone = it },
                                label = { Text("رقم الهاتف") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newCustomerEmail,
                                    onValueChange = { newCustomerEmail = it },
                                    label = { Text("البريد الإلكتروني") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                                )
                                OutlinedTextField(
                                    value = newCustomerAddress,
                                    onValueChange = { newCustomerAddress = it },
                                    label = { Text("العنوان السكني") },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Type choices
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (newCustomerType == "Customer") ActiveCyan else LightSlate)
                                        .clickable { newCustomerType = "Customer" }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("عميل (Customer)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (newCustomerType == "Vendor") ActiveCyan else LightSlate)
                                        .clickable { newCustomerType = "Vendor" }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("مورد (Vendor)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    if (newCustomerName.isBlank() || newCustomerPhone.isBlank()) {
                                        // Simple alert
                                    } else {
                                        viewModel.addNewContact(
                                            name = newCustomerName,
                                            phone = newCustomerPhone,
                                            email = newCustomerEmail,
                                            address = newCustomerAddress,
                                            type = newCustomerType
                                        )
                                        // Reset fields
                                        newCustomerName = ""
                                        newCustomerPhone = ""
                                        newCustomerEmail = ""
                                        newCustomerAddress = ""
                                        showAddCustomerForm = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إدراج المالي لـ SmartSalesSystemPRO 🛡️", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
            
            // Search Query text field
            item {
                OutlinedTextField(
                    value = clientSearchQuery,
                    onValueChange = { clientSearchQuery = it },
                    placeholder = { Text("ابحث باسم العميل أو الشركة...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MediumSlate) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = ActiveCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Group Contacts Lists
            val filteredContacts = contacts.filter {
                it.name.contains(clientSearchQuery, ignoreCase = true) ||
                it.contactNumber.contains(clientSearchQuery)
            }
            
            if (filteredContacts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("عفواً، لا يوجد مطابقة للبحث حالياً.", color = MediumSlate)
                    }
                }
            } else {
                items(filteredContacts) { contact ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .clickable { viewModel.loadContactStatement(contact) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (contact.type == "Customer") ActiveCyan.copy(alpha = 0.15f) else AlertGold.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (contact.type == "Customer") "ع" else "م",
                                        fontWeight = FontWeight.Bold,
                                        color = if (contact.type == "Customer") ActiveCyan else AlertGold,
                                        fontSize = 15.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(contact.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                                    Text("${contact.contactNumber} • ${contact.address}", fontSize = 10.sp, color = MediumSlate)
                                }
                            }
                            
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "statement view", tint = MediumSlate)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

// ======================== TAB 4: STRIPE TERMINAL ========================
@Composable
fun StripeTab(viewModel: SalesViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val stripeState by viewModel.stripeProcessingState.collectAsState()
    
    var selectedContactUser by remember { mutableStateOf<Contact?>(null) }
    var inputAmountStr by remember { mutableStateOf("") }
    
    // Credit card visualization properties
    var stripeCardNo by remember { mutableStateOf("") }
    var stripeHolderName by remember { mutableStateOf("") }
    var stripeExpiry by remember { mutableStateOf("") }
    var stripeCvv by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "محطة الدفع الإلكتروني Stripe.net 💳",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
            Text(
                text = "تسوية الفواتير مباشرة مع العملاء من خلال معالج بطاقة الائتمان الآمن.",
                fontSize = 12.sp,
                color = MediumSlate
            )
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("١. اختيار العميل والمبلغ المطلوب ترحيله", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Client Selector horizontal scroll
                    Text("العميل المعني بالسداد:", fontSize = 11.sp, color = MediumSlate)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        contacts.filter { it.type == "Customer" }.forEach { cl ->
                            val isChosen = selectedContactUser?.id == cl.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) ActiveCyan else LightSlate)
                                    .clickable { 
                                        selectedContactUser = cl 
                                        stripeHolderName = cl.name.split(" ").take(2).joinToString(" ")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    cl.name, 
                                    fontSize = 11.sp, 
                                    color = if (isChosen) DeepNavy else DeepNavy.copy(alpha = 0.8f),
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = inputAmountStr,
                        onValueChange = { inputAmountStr = it },
                        label = { Text("مبلغ السداد المستحق (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Beautiful Stylized Credit Card Mockup drawing
        item {
            val cardDisplay = if (stripeCardNo.isNotEmpty()) stripeCardNo else "•••• •••• •••• ••••"
            val holderDisplay = if (stripeHolderName.isNotEmpty()) stripeHolderName else "اسم حامل البطاقة"
            val expiryDisplay = if (stripeExpiry.isNotEmpty()) stripeExpiry else "MM/YY"
            val cvvDisplay = if (stripeCvv.isNotEmpty()) stripeCvv else "•••"
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(DeepNavy, Color(0xFF1E1B4B), SolidDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Stripe Secure Gate", color = ActiveCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.CreditCard, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                    
                    Text(
                        text = cardDisplay,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Card Holder", fontSize = 9.sp, color = MediumSlate)
                        Text(holderDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row {
                        Column(modifier = Modifier.padding(end = 16.dp)) {
                            Text("Expires", fontSize = 9.sp, color = MediumSlate)
                            Text(expiryDisplay, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("CVV", fontSize = 9.sp, color = MediumSlate)
                            Text(cvvDisplay, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
        
        // Card Inputs form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("٢. تفاصيل البطاقة وبوابة التحويل الإلكتروني", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = stripeCardNo,
                        onValueChange = { if (it.length <= 16) stripeCardNo = it },
                        label = { Text("رقم البطاقة الائتمانية (16 رقم)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stripeExpiry,
                            onValueChange = { stripeExpiry = it },
                            label = { Text("تاريخ الانتهاء (MM/YY)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                        )
                        OutlinedTextField(
                            value = stripeCvv,
                            onValueChange = { if (it.length <= 4) stripeCvv = it },
                            label = { Text("الرمز السري (CVV)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActiveCyan)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    // Manage states
                    when (stripeState) {
                        is StripeUiState.Idle -> {
                            Button(
                                onClick = {
                                    val contact = selectedContactUser
                                    val finalAmt = inputAmountStr.toDoubleOrNull()
                                    if (contact == null) {
                                        // Empty user error
                                    } else if (finalAmt == null || finalAmt <= 0) {
                                        // Invalid amount
                                    } else {
                                        viewModel.processStripeCheckout(
                                            amount = finalAmt,
                                            cardNum = stripeCardNo,
                                            cvc = stripeCvv,
                                            exp = stripeExpiry,
                                            name = stripeHolderName,
                                            csId = contact.id,
                                            boxId = 2 // Stripe Electronic cashbox destinations
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessTeal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("تفويض وسحب الدفعة نقداً 🔒", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        is StripeUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = ActiveCyan)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("جاري تشفير الاتصال والتحصيل بنسق Stripe.net API 🔒", fontSize = 11.sp, color = MediumSlate)
                                }
                            }
                        }
                        is StripeUiState.Success -> {
                            val successData = stripeState as StripeUiState.Success
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SuccessTeal.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("معاملة مالية ناجحة! 🥳💳", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SuccessTeal)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(successData.message, fontSize = 12.sp, color = DeepNavy, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            viewModel.resetStripeState()
                                            stripeCardNo = ""
                                            stripeCvv = ""
                                            stripeExpiry = ""
                                            inputAmountStr = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                                    ) {
                                        Text("معاملة دفع أخرى", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        is StripeUiState.Error -> {
                            val errorData = stripeState as StripeUiState.Error
                            Card(
                                colors = CardDefaults.cardColors(containerColor = ErrorCrimson.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("فشلت عملية الدفع الإلكتروني ⚠️", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorCrimson)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(errorData.message, fontSize = 12.sp, color = DeepNavy, textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { viewModel.resetStripeState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                                    ) {
                                        Text("إعادة المحاولة", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(40.dp)) }
    }
}
