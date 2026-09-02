package com.example.ui.screens.profile

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CreateCategoryDialog
import com.example.ui.components.SetCategoryBudgetDialog
import com.example.ui.components.SyncStatusIcon
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.Category
import com.example.util.CategoryItem
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: HisabViewModel,
    onNavigateToAuth: () -> Unit,
    onNavigateToRecurring: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val recurringSummary by viewModel.recurringSummary.collectAsStateWithLifecycle()
    val allSchedules by viewModel.allRecurringSchedules.collectAsStateWithLifecycle()
    val customCategories by viewModel.customCategories.collectAsStateWithLifecycle()
    val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
    val allCategoryItems by viewModel.allCategoryItems.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.categoryBudgetProgressList.collectAsStateWithLifecycle()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var budgetInput by remember {
        mutableStateOf(if (userSettings.monthlyBudget > 0L) CurrencyFormatter.formatPaisaToTaka(userSettings.monthlyBudget, includeSymbol = false).replace(",", "") else "")
    }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryForBudget by remember { mutableStateOf<CategoryItem?>(null) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "প্রোফাইল ও সেটিংস",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. User Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("user_profile_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryCoral.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = "Avatar",
                                    tint = PrimaryCoral,
                                    modifier = Modifier.size(54.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userSettings.userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = userSettings.userEmail.ifBlank { "অফলাইন মোড (গেস্ট ব্যবহারকারী)" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (userSettings.userEmail.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logout()
                                    onNavigateToAuth()
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("লগআউট করুন")
                            }
                        } else {
                            Button(
                                onClick = onNavigateToAuth,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                            ) {
                                Icon(imageVector = Icons.Rounded.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Google দিয়ে সাইন ইন করুন", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 2. Cloud Sync Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudSync,
                                    contentDescription = null,
                                    tint = PrimaryCoral
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ক্লাউড সিঙ্ক ও ব্যাকআপ",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "সর্বশেষ সিঙ্ক: ${if (userSettings.lastSyncTime > 0) DateHelper.formatRelativeBengali(userSettings.lastSyncTime) else "এখনো হয়নি"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.triggerSync()
                                    Toast.makeText(context, "ক্লাউড সিঙ্ক সম্পন্ন হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("সিঙ্ক করো")
                            }
                        }
                    }
                }
            }

            // 2.1 Recurring Incomes & Expenses Schedule Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRecurring() }
                        .testTag("recurring_schedule_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryCoral.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Rounded.Paid,
                                            contentDescription = null,
                                            tint = PrimaryCoral,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "নিয়মিত আয় ও ব্যয় শিডিউল",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "মাসিক ভাড়া, বেতন ও বিলের তালিকা (${CurrencyFormatter.toBengaliNumerals(allSchedules.size.toString())}টি)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = onNavigateToRecurring,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ম্যানেজ")
                            }
                        }

                        if (allSchedules.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "নিয়মিত আয়: +${CurrencyFormatter.formatPaisaToTaka(recurringSummary.totalMonthlyIncome)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "নিয়মিত খরচ: -${CurrencyFormatter.formatPaisaToTaka(recurringSummary.totalMonthlyExpense)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 3. Financial Settings (Monthly Budget & Category Budgets)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "আর্থিক লক্ষ্যমাত্রা ও বাজেট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Total Monthly Budget
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(PrimaryCoral.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Savings,
                                        contentDescription = null,
                                        tint = PrimaryCoral,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "মোট মাসিক বাজেট",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (userSettings.monthlyBudget > 0L) CurrencyFormatter.formatPaisaToTaka(userSettings.monthlyBudget) else "নির্ধারণ করা নেই",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            TextButton(onClick = { showBudgetDialog = true }) {
                                Text("পরিবর্তন")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Category-specific Budgets Title
                        Text(
                            text = "ক্যাটাগরি ভিত্তিক বাজেট",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "প্রতিটি খাতে খরচের সর্বোচ্চ সীমা নির্ধারণ করুন (৮০% খরচ হলে নোটিফিকেশন পাবেন)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        allCategoryItems.forEach { catItem ->
                            val budget = categoryBudgets.firstOrNull { it.categoryKey.equals(catItem.key, ignoreCase = true) }
                            val budgetPaisa = budget?.monthlyBudget ?: 0L

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { selectedCategoryForBudget = catItem },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(catItem.color.copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (catItem.icon != null) {
                                            Icon(
                                                imageVector = catItem.icon,
                                                contentDescription = null,
                                                tint = catItem.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(text = catItem.emoji ?: "🏷️", fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = catItem.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (budgetPaisa > 0L)
                                                "বাজেট: ${CurrencyFormatter.formatPaisaToTaka(budgetPaisa)}"
                                            else "বাজেট নেই",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (budgetPaisa > 0L) PrimaryCoral else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                TextButton(onClick = { selectedCategoryForBudget = catItem }) {
                                    Text(if (budgetPaisa > 0L) "এডিট" else "সেট করুন")
                                }
                            }
                        }
                    }
                }
            }

            // 4. Custom Categories Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "কাস্টম ক্যাটাগরি",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "আপনার পছন্দমতো নতুন ক্যাটাগরি যুক্ত করুন",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            FilledTonalButton(
                                onClick = { showCreateCategoryDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ নতুন")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (customCategories.isEmpty()) {
                            Text(
                                text = "এখনো কোনো কাস্টম ক্যাটাগরি তৈরি করা হয়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            customCategories.forEach { customCat ->
                                val color = Category.parseHexColor(customCat.colorHex)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(color.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = customCat.emoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = customCat.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCustomCategory(customCat.id)
                                            Toast.makeText(context, "ক্যাটাগরি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteForever,
                                            contentDescription = "Delete Category",
                                            tint = ExpenseRed.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Preferences & Notifications
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "অ্যাপ সেটিংস ও নোটিফিকেশন",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Reminder Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("দৈনিক খরচের রিমাইন্ডার", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("প্রতিদিন রাত ${userSettings.reminderHour}:${if (userSettings.reminderMinute < 10) "0" else ""}${userSettings.reminderMinute}-এ নোটিফিকেশন", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = userSettings.dailyReminderEnabled,
                                onCheckedChange = { viewModel.toggleDailyReminder(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (userSettings.isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ডার্ক মোড", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(if (userSettings.isDarkTheme) "ডার্ক থিম সক্রিয়" else "লাইট থিম সক্রিয়", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = userSettings.isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme(it) }
                            )
                        }
                    }
                }
            }

            // 5. Data Management (Sample Data / Clear Data)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "তথ্য ব্যবস্থাপনা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FilledTonalButton(
                            onClick = {
                                viewModel.seedSampleData()
                                Toast.makeText(context, "নমুনা তথ্য যোগ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("নমুনা তথ্য যোগ করুন (ডেমো ডাটা)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
                        ) {
                            Icon(imageVector = Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("সব হিসাব মুছে ফেলুন")
                        }
                    }
                }
            }

            // 6. About App
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HisabBoi (হিসাববই)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCoral
                        )
                        Text(
                            text = "তোমার হিসাব, তোমার হাতে • ভার্সন ১.০.০",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Monthly Budget Dialog
    if (showBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("মাসিক বাজেট নির্ধারণ") },
            text = {
                Column {
                    Text(
                        text = "আপনার প্রতি মাসের খরচের সর্বোচ্চ সীমা নির্ধারণ করুন:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                budgetInput = input
                            }
                        },
                        label = { Text("বাজেটের পরিমাণ (টাকা)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val paisa = CurrencyFormatter.takaToPaisa(budgetInput)
                        viewModel.updateMonthlyBudget(paisa)
                        showBudgetDialog = false
                    }
                ) {
                    Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("সকল ডাটা মুছে ফেলবেন?") },
            text = { Text("আপনি কি নিশ্চিতভাবে আপনার সকল হিসাব, লেনদেন ও ধারের তথ্য স্থায়ীভাবে মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "সকল তথ্য মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("সব মুছুন", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Create Custom Category Dialog
    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { name, emoji, colorHex ->
                viewModel.addCustomCategory(name, emoji, colorHex)
                Toast.makeText(context, "ক্যাটাগরি তৈরি সফল হয়েছে", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Set Category Budget Dialog
    selectedCategoryForBudget?.let { catItem ->
        val existingBudget = categoryBudgets.firstOrNull { it.categoryKey.equals(catItem.key, ignoreCase = true) }?.monthlyBudget ?: 0L
        SetCategoryBudgetDialog(
            categoryItem = catItem,
            currentBudgetPaisa = existingBudget,
            onDismiss = { selectedCategoryForBudget = null },
            onSave = { newBudgetPaisa ->
                viewModel.setCategoryBudget(catItem.key, newBudgetPaisa)
                Toast.makeText(context, "${catItem.label} বাজেট সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
            },
            onDelete = if (existingBudget > 0L) {
                {
                    viewModel.deleteCategoryBudget(catItem.key)
                    Toast.makeText(context, "${catItem.label} বাজেট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            } else null
        )
    }
}
