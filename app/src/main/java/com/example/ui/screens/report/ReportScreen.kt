package com.example.ui.screens.report

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CategoryBarChart
import com.example.ui.components.CategoryBudgetSection
import com.example.ui.components.HeatmapCalendar
import com.example.ui.components.IncomeExpenseTrendChart
import com.example.ui.components.PaymentMethodDonutChart
import com.example.ui.components.SmartInsightsCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.Category
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.util.PDFExporter
import com.example.viewmodel.DateFilterType
import com.example.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: HisabViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allLoans by viewModel.allLoans.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.categoryBudgetProgressList.collectAsStateWithLifecycle()

    var selectedRangeType by remember { mutableStateOf(DateFilterType.THIS_MONTH) }
    var customRange by remember { mutableStateOf<Pair<Long, Long>?>(null) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    val dateRange = when (selectedRangeType) {
        DateFilterType.TODAY -> DateHelper.getTodayStartAndEnd()
        DateFilterType.THIS_WEEK -> DateHelper.getThisWeekStartAndEnd()
        DateFilterType.THIS_MONTH -> DateHelper.getThisMonthStartAndEnd()
        DateFilterType.CUSTOM -> customRange ?: DateHelper.getThisMonthStartAndEnd()
    }

    val filteredTransactions = remember(allTransactions, dateRange) {
        allTransactions.filter { it.date in dateRange.first..dateRange.second }
    }

    val totalIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    }
    val totalExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
    }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "আর্থিক রিপোর্ট ও বিশ্লেষণ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            PDFExporter.generateAndShareReport(
                                context = context,
                                transactions = filteredTransactions,
                                loans = allLoans,
                                dateRangeTitle = selectedRangeType.label
                            )
                        },
                        modifier = Modifier.testTag("top_pdf_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = PrimaryCoral
                        )
                    }
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
            // 1. Date Range Segmented Selector
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_range_selector")
                ) {
                    DateFilterType.entries.forEachIndexed { index, df ->
                        SegmentedButton(
                            selected = selectedRangeType == df,
                            onClick = {
                                selectedRangeType = df
                                if (df == DateFilterType.CUSTOM) {
                                    showCustomDateDialog = true
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = DateFilterType.entries.size
                            )
                        ) {
                            Text(df.label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 2. Summary 3 Cards Row (মোট আয়, মোট খরচ, ব্যালেন্স)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Income Card
                    SummaryCard(
                        title = "মোট আয়",
                        amount = totalIncome,
                        icon = Icons.Rounded.ArrowDownward,
                        color = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )

                    // Total Expense Card
                    SummaryCard(
                        title = "মোট খরচ",
                        amount = totalExpense,
                        icon = Icons.Rounded.ArrowUpward,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )

                    // Net Balance Card
                    SummaryCard(
                        title = "ব্যালেন্স",
                        amount = netBalance,
                        icon = Icons.Rounded.AccountBalanceWallet,
                        color = PrimaryCoral,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Smart Insights Card
            item {
                SmartInsightsCard(
                    transactions = filteredTransactions,
                    monthlyBudget = userSettings.monthlyBudget
                )
            }

            // 3.5 Category Budgets & Utilization (Current Month)
            if (budgetProgressList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_budget_progress_card"),
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
                                Text(
                                    text = "ক্যাটাগরি বাজেট ও খরচের অবস্থা",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "চলতি মাস",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            budgetProgressList.forEach { item ->
                                val progress = (item.percentage / 100f).coerceIn(0f, 1f)
                                val progressColor = when {
                                    item.isOverBudget -> ExpenseRed
                                    item.isWarning -> Color(0xFFFF9F43)
                                    else -> item.categoryItem.color
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(item.categoryItem.color.copy(alpha = 0.2f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (item.categoryItem.icon != null) {
                                                    Icon(
                                                        imageVector = item.categoryItem.icon,
                                                        contentDescription = null,
                                                        tint = item.categoryItem.color,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                } else {
                                                    Text(text = item.categoryItem.emoji ?: "🏷️", fontSize = 14.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item.categoryItem.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (item.isOverBudget) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = ExpenseRed.copy(alpha = 0.15f),
                                                    modifier = Modifier.padding(end = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "বাজেট পার!",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ExpenseRed,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            } else if (item.isWarning) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFFF9F43).copy(alpha = 0.15f),
                                                    modifier = Modifier.padding(end = 6.dp)
                                                ) {
                                                    Text(
                                                        text = "⚠️ ৮০% সতর্কীকরণ",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFFFF9F43),
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "${CurrencyFormatter.formatPaisaToTaka(item.spentPaisa)}${if (item.budgetPaisa > 0L) " / ${CurrencyFormatter.formatPaisaToTaka(item.budgetPaisa)}" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    if (item.budgetPaisa > 0L) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        androidx.compose.material3.LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = progressColor,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "${item.percentage.toInt()}% ব্যবহৃত",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = if (item.isWarning || item.isOverBudget) progressColor else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Category Bar Chart
            item {
                CategoryBarChart(transactions = filteredTransactions)
            }

            // 5. Income vs Expense Trend Chart
            item {
                IncomeExpenseTrendChart(transactions = filteredTransactions)
            }

            // 6. Heatmap Calendar
            item {
                HeatmapCalendar(transactions = allTransactions)
            }

            // 7. Cash vs Digital Payment Donut Chart
            item {
                PaymentMethodDonutChart(transactions = filteredTransactions)
            }

            // 8. PDF Export Button
            item {
                Button(
                    onClick = {
                        PDFExporter.generateAndShareReport(
                            context = context,
                            transactions = filteredTransactions,
                            loans = allLoans,
                            dateRangeTitle = selectedRangeType.label
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("download_pdf_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PDF রিপোর্ট ডাউনলোড ও শেয়ার করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Custom Date Range Picker
    if (showCustomDateDialog) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showCustomDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            customRange = Pair(start, end)
                        }
                        showCustomDateDialog = false
                    }
                ) {
                    Text("নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDateDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { Text("রিপোর্টের সময়কাল নির্বাচন করুন", modifier = Modifier.padding(16.dp)) },
                modifier = Modifier.height(420.dp)
            )
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = CurrencyFormatter.formatPaisaToTaka(amount),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
