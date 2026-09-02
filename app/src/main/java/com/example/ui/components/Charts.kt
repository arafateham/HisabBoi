package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.Category
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.util.PaymentMethod
import java.util.Calendar

/**
 * 1. Category Bar Chart with progress bars, amounts & percentages
 */
@Composable
fun CategoryBarChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val expenseList = transactions.filter { it.type == "expense" }
    val totalExpense = expenseList.sumOf { it.amount }

    val categorySums = expenseList.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_bar_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "ক্যাটাগরি ভিত্তিক খরচ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (categorySums.isEmpty() || totalExpense == 0L) {
                Text(
                    text = "এই সময়ে কোনো খরচের তথ্য পাওয়া যায়নি",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categorySums.take(6).forEach { (catKey, amount) ->
                    val cat = Category.fromKey(catKey)
                    val percentage = (amount.toFloat() / totalExpense.toFloat()) * 100f

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(cat.color.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = null,
                                        tint = cat.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "${CurrencyFormatter.formatPaisaToTaka(amount)} (${String.format("%.1f", percentage)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = { (percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = cat.color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Income vs Expense Trend Chart (Dual Line Canvas Chart)
 */
@Composable
fun IncomeExpenseTrendChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trend_line_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আয় বনাম খরচ ট্রেন্ড",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(IncomeGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "আয়",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(ExpenseRed, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "খরচ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Build daily buckets for the last 7 intervals
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val intervals = (6 downTo 0).map { i ->
                val end = now - (i * dayMillis)
                val start = end - dayMillis
                val inc = transactions.filter { it.type == "income" && it.date in start..end }.sumOf { it.amount }
                val exp = transactions.filter { it.type == "expense" && it.date in start..end }.sumOf { it.amount }
                Triple(DateHelper.formatDayMonth(start), inc, exp)
            }

            val maxVal = intervals.maxOfOrNull { maxOf(it.second, it.third) }?.coerceAtLeast(100000L) ?: 100000L

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val width = size.width
                val height = size.height
                val stepX = if (intervals.size > 1) width / (intervals.size - 1) else width

                val incomePath = Path()
                val expensePath = Path()

                intervals.forEachIndexed { index, item ->
                    val x = index * stepX
                    val incY = height - (item.second.toFloat() / maxVal.toFloat() * (height - 30f)) - 15f
                    val expY = height - (item.third.toFloat() / maxVal.toFloat() * (height - 30f)) - 15f

                    if (index == 0) {
                        incomePath.moveTo(x, incY)
                        expensePath.moveTo(x, expY)
                    } else {
                        incomePath.lineTo(x, incY)
                        expensePath.lineTo(x, expY)
                    }

                    // Draw points
                    drawCircle(
                        color = IncomeGreen,
                        radius = 4.dp.toPx(),
                        center = Offset(x, incY)
                    )
                    drawCircle(
                        color = ExpenseRed,
                        radius = 4.dp.toPx(),
                        center = Offset(x, expY)
                    )
                }

                drawPath(
                    path = incomePath,
                    color = IncomeGreen,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = expensePath,
                    color = ExpenseRed,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                intervals.forEach { (label, _, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 3. Heatmap Calendar (Custom Compose calendar grid showing daily spending intensity)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeatmapCalendar(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance()
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)

    // Calculate daily expenses for the current month
    val dailyExpenses = (1..maxDays).associateWith { day ->
        val c = Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
        }
        val start = c.timeInMillis
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        val end = c.timeInMillis

        transactions.filter { it.type == "expense" && it.date in start..end }.sumOf { it.amount }
    }

    val maxSpending = dailyExpenses.values.maxOrNull()?.coerceAtLeast(100000L) ?: 100000L

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("heatmap_calendar_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "দৈনিক খরচের হিটম্যাপ (চলতি মাস)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "কম",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(0.15f, 0.4f, 0.7f, 1f).forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(IncomeGreen.copy(alpha = alpha), RoundedCornerShape(2.dp))
                        )
                    }
                    Text(
                        text = "বেশি",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Columns Grid for days
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 7,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..maxDays).forEach { day ->
                    val expense = dailyExpenses[day] ?: 0L
                    val intensity = if (expense == 0L) 0.08f else {
                        (expense.toFloat() / maxSpending.toFloat()).coerceIn(0.2f, 1f)
                    }
                    val boxColor = if (expense == 0L) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        IncomeGreen.copy(alpha = intensity)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(boxColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (expense > 0L) FontWeight.Bold else FontWeight.Normal,
                                color = if (expense > 0L && intensity > 0.6f) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 4. Cash vs Digital Payment Donut / Pie Chart
 */
@Composable
fun PaymentMethodDonutChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val expenseList = transactions.filter { it.type == "expense" }
    val total = expenseList.sumOf { it.amount }

    val cashAmount = expenseList.filter { it.method == "cash" }.sumOf { it.amount }
    val digitalAmount = total - cashAmount

    val cashPercent = if (total > 0) (cashAmount.toFloat() / total.toFloat()) * 100f else 50f
    val digitalPercent = if (total > 0) (digitalAmount.toFloat() / total.toFloat()) * 100f else 50f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payment_donut_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ক্যাশ বনাম ডিজিটাল লেনদেন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val stroke = 18.dp.toPx()
                        val sweepCash = (cashPercent / 100f) * 360f
                        val sweepDigital = 360f - sweepCash

                        drawArc(
                            color = Color(0xFF2ED573),
                            startAngle = -90f,
                            sweepAngle = sweepCash,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        drawArc(
                            color = Color(0xFF54A0FF),
                            startAngle = -90f + sweepCash,
                            sweepAngle = sweepDigital,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                    }
                    Text(
                        text = "খরচ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF2ED573), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ক্যাশ (${String.format("%.1f", cashPercent)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyFormatter.formatPaisaToTaka(cashAmount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF54A0FF), RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ডিজিটাল / ব্যাংক (${String.format("%.1f", digitalPercent)}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = CurrencyFormatter.formatPaisaToTaka(digitalAmount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. Smart Insights Card (Rule-based financial insights)
 */
@Composable
fun SmartInsightsCard(
    transactions: List<TransactionEntity>,
    monthlyBudget: Long,
    modifier: Modifier = Modifier
) {
    val expenseList = transactions.filter { it.type == "expense" }
    val totalExpense = expenseList.sumOf { it.amount }

    val topCategory = expenseList.groupBy { it.category }
        .maxByOrNull { entry -> entry.value.sumOf { it.amount } }

    val insights = mutableListOf<String>()

    if (topCategory != null && totalExpense > 0) {
        val cat = Category.fromKey(topCategory.key)
        val catAmount = topCategory.value.sumOf { it.amount }
        val pct = (catAmount.toFloat() / totalExpense.toFloat()) * 100f
        insights.add("এই সময়ে সবচেয়ে বেশি খরচ হয়েছে ${cat.label} খাতে (${String.format("%.0f", pct)}%)।")
    }

    if (monthlyBudget > 0) {
        val budgetUsedPct = (totalExpense.toFloat() / monthlyBudget.toFloat()) * 100f
        if (budgetUsedPct >= 80f) {
            insights.add("সতর্কতা: আপনার মাসিক বাজেটের ${String.format("%.0f", budgetUsedPct)}% ইতোমধ্যে শেষ হয়েছে!")
        } else {
            insights.add("আপনার খরচের গতি নিয়ন্ত্রণে আছে (বাজেটের ${String.format("%.0f", budgetUsedPct)}% ব্যবহৃত)।")
        }
    } else {
        insights.add("সঞ্চয় বাড়াতে প্রোফাইল থেকে মাসিক বাজেট নির্ধারণ করতে পারেন।")
    }

    if (expenseList.size >= 5) {
        val cashExpense = expenseList.filter { it.method == "cash" }.sumOf { it.amount }
        if (cashExpense > (totalExpense * 0.6)) {
            insights.add("আপনার খরচের বেশিরভাগ ক্যাশে হচ্ছে, নগদ খরচের রসিদ সংরক্ষণ করুন।")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_insights_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryCoral.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = "Insights",
                        tint = PrimaryCoral,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "স্মার্ট বিশ্লেষণ ও পরামর্শ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            insights.take(3).forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCoral
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
