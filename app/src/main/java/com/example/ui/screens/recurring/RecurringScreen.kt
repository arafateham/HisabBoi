package com.example.ui.screens.recurring

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.ui.components.AddEditRecurringDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.Category
import com.example.util.CategoryItem
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.util.PaymentMethod
import com.example.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    viewModel: HisabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val allSchedules by viewModel.allRecurringSchedules.collectAsStateWithLifecycle()
    val summary by viewModel.recurringSummary.collectAsStateWithLifecycle()
    val allCategoryItems by viewModel.allCategoryItems.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Expenses, 2: Incomes
    var showAddEditDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    var itemToPostConfirm by remember { mutableStateOf<RecurringExpenseEntity?>(null) }

    val filteredList = when (selectedTabIndex) {
        1 -> allSchedules.filter { it.type == "expense" }
        2 -> allSchedules.filter { it.type == "income" }
        else -> allSchedules
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "নিয়মিত আয় ও ব্যয় শিডিউল",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showAddEditDialog = true
                },
                containerColor = PrimaryCoral,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_recurring")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Recurring"
                    )
                    Text(
                        text = "নতুন শিডিউল",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
            // 1. Summary Header Card
            item {
                RecurringOverviewCard(
                    totalIncome = summary.totalMonthlyIncome,
                    totalExpense = summary.totalMonthlyExpense,
                    netCommit = summary.netMonthly,
                    activeCount = summary.activeCount
                )
            }

            // 2. Tab Row (All, Expenses, Incomes)
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = PrimaryCoral,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("সব (${allSchedules.size})", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = {
                                Text(
                                    "খরচ (${allSchedules.count { it.type == "expense" }})",
                                    fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == 1) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = { selectedTabIndex = 2 },
                            text = {
                                Text(
                                    "আয় (${allSchedules.count { it.type == "income" }})",
                                    fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == 2) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // 3. List of Items
            if (filteredList.isEmpty()) {
                item {
                    EmptyRecurringCard(
                        onAddClick = {
                            itemToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    RecurringItemCard(
                        item = item,
                        allCategories = allCategoryItems,
                        onToggleActive = { active ->
                            viewModel.toggleRecurringActive(item.id, active)
                        },
                        onEdit = {
                            itemToEdit = item
                            showAddEditDialog = true
                        },
                        onDelete = {
                            itemToDelete = item
                        },
                        onPostNow = {
                            itemToPostConfirm = item
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialog: Add / Edit Recurring
    if (showAddEditDialog) {
        AddEditRecurringDialog(
            userId = userSettings.userId,
            allCategories = allCategoryItems,
            existingItem = itemToEdit,
            onDismiss = {
                showAddEditDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                if (itemToEdit != null) {
                    viewModel.updateRecurring(savedItem)
                    Toast.makeText(context, "শিডিউল আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addRecurring(savedItem)
                    Toast.makeText(context, "নতুন শিডিউল যোগ হয়েছে", Toast.LENGTH_SHORT).show()
                }
                showAddEditDialog = false
                itemToEdit = null
            }
        )
    }

    // Dialog: Confirm Delete
    if (itemToDelete != null) {
        val item = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("শিডিউল মুছে ফেলবেন?") },
            text = { Text("'${item.name}' শিডিউলটি মুছে ফেলতে চান? এটি আর স্বয়ংক্রিয় বা নিয়মিত তালিকায় থাকবে না।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecurring(item.id)
                        Toast.makeText(context, "মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Confirm Post to Transactions
    if (itemToPostConfirm != null) {
        val item = itemToPostConfirm!!
        val isIncome = item.type == "income"
        AlertDialog(
            onDismissRequest = { itemToPostConfirm = null },
            title = { Text(if (isIncome) "আয় হিসাবভুক্ত করুন" else "খরচ হিসাবভুক্ত করুন") },
            text = {
                Column {
                    Text("আজকের তারিখে '${item.name}' (${CurrencyFormatter.formatPaisaToTaka(item.amount)}) হিসাব এন্ট্রিতে যোগ করতে চান?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "পদ্ধতি: ${PaymentMethod.fromKey(item.method).label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.postRecurringNow(item) {
                            Toast.makeText(context, "সফলভাবে হিসাবভুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                        itemToPostConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) IncomeGreen else PrimaryCoral
                    )
                ) {
                    Text("হিসাবভুক্ত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToPostConfirm = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun RecurringOverviewCard(
    totalIncome: Long,
    totalExpense: Long,
    netCommit: Long,
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryCoral.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = null,
                                tint = PrimaryCoral,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "মাসিক নিয়মিত প্রতিশ্রুতি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সক্রিয় শিডিউল: ${CurrencyFormatter.toBengaliNumerals(activeCount.toString())} টি",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monthly Recurring Income
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = IncomeGreen.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = IncomeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "নিয়মিত আয়",
                                style = MaterialTheme.typography.labelSmall,
                                color = IncomeGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisaToTaka(totalIncome),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }

                // Monthly Recurring Expense
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ExpenseRed.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.TrendingDown,
                                contentDescription = null,
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "নিয়মিত খরচ",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisaToTaka(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net balance after commitments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "প্রতিশ্রুতি বাদে অবশিষ্ট থাকবে:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatPaisaToTaka(netCommit),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (netCommit >= 0) MaterialTheme.colorScheme.onSurface else ExpenseRed
                )
            }
        }
    }
}

@Composable
fun RecurringItemCard(
    item: RecurringExpenseEntity,
    allCategories: List<CategoryItem>,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPostNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncome = item.type == "income"
    val catItem = allCategories.firstOrNull { it.key.equals(item.category, ignoreCase = true) }
        ?: (if (isIncome) Category.SALARY.toCategoryItem() else Category.BILL.toCategoryItem())
    val method = PaymentMethod.fromKey(item.method)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recurring_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.active) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                color = if (isIncome) IncomeGreen.copy(alpha = 0.15f) else catItem.color.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (catItem.icon != null) {
                            Icon(
                                imageVector = catItem.icon,
                                contentDescription = null,
                                tint = if (isIncome) IncomeGreen else catItem.color,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(text = catItem.emoji ?: "🏷️", fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isIncome) "আয়" else "খরচ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIncome) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = DateHelper.formatRecurringScheduleText(item.dayOfMonth, item.frequency),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryCoral,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.formatPaisaToTaka(item.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) IncomeGreen else ExpenseRed
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = method.icon,
                            contentDescription = null,
                            tint = method.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = method.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!item.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "নোট: ${item.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.lastExecutedDate != null && item.lastExecutedDate > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "সর্বশেষ হিসাবভুক্ত: ${DateHelper.formatDayMonth(item.lastExecutedDate)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Post Now Button
                FilledTonalButton(
                    onClick = onPostNow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isIncome) IncomeGreen.copy(alpha = 0.15f) else PrimaryCoral.copy(alpha = 0.15f),
                        contentColor = if (isIncome) IncomeGreen else PrimaryCoral
                    ),
                    modifier = Modifier.testTag("post_recurring_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "এখনই হিসাবভুক্ত করুন",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Active Switch
                    Switch(
                        checked = item.active,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryCoral,
                            checkedTrackColor = PrimaryCoral.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.size(width = 40.dp, height = 24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyRecurringCard(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = PrimaryCoral.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = null,
                        tint = PrimaryCoral,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "কোনো শিডিউল পাওয়া যায়নি",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "মাসিক বাড়ি ভাড়া, বেতন, ওয়াইফাই বা টিউশন ফির মতো নিয়মিত আয় ও ব্যয়ের শিডিউল তৈরি করুন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("শিডিউল তৈরি করুন")
            }
        }
    }
}
