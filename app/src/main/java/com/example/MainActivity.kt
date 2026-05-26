package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.AppDatabase
import com.example.data.SalesRepository
import com.example.ui.SalesAppUi
import com.example.ui.SalesViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize local SQLite Database instance
        val database = AppDatabase.getDatabase(applicationContext)
        
        // 2. Extract relative database access objects
        val contactDao = database.contactDao()
        val invoiceDao = database.invoiceDao()
        val cashBoxDao = database.cashBoxDao()
        val transactionDao = database.transactionDao()
        val itemDao = database.itemDao()
        
        // 3. Setup unified business repository logic and state syncing
        val repository = SalesRepository(
            contactDao = contactDao,
            invoiceDao = invoiceDao,
            cashBoxDao = cashBoxDao,
            transactionDao = transactionDao,
            itemDao = itemDao
        )
        
        // 4. Initialize ViewModel representation
        val viewModel = SalesViewModel(repository)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SalesAppUi(viewModel = viewModel)
            }
        }
    }
}
