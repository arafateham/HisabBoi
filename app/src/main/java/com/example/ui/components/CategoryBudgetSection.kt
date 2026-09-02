package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WarningAmber
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.CurrencyFormatter
import com.example.viewmodel.CategoryBudgetProgress

@Composable
fun CategoryBudgetSection(
    budgetProgressList: List<CategoryBudgetProgress>,
    onCategoryClick: ((CategoryBudgetProgress) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_budget_section_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryCoral.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PieChart,
                            contentDescription = null,
                            tint = PrimaryCoral,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ক্যাটাগরি বাজেট অগ্রগতি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "এই মাসের ক্যাটাগরি ভিত্তিক খরচের হিসাব",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (budgetProgressList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "কোনো ক্যাটাগরির বাজেট সেট করা নেই",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রোফাইল স্ক্রিন থেকে ক্যাটাগরি অনুযায়ী মাসিক বাজেট নির্ধারণ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    budgetProgressList.forEach { item ->
                        CategoryBudgetItemRow(
                            item = item,
                            onClick = { onCategoryClick?.invoke(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetItemRow(
    item: CategoryBudgetProgress,
    onClick: () -> Unit
) {
    val progressAnimated by animateFloatAsState(
        targetValue = (item.percentage / 100f).coerceIn(0f, 1f),
        label = "category_budget_progress"
    )

    val progressColor = when {
        item.isOverBudget -> ExpenseRed
        item.isWarning -> Color(0xFFFF9F43) // Warning Amber
        else -> item.categoryItem.color
    }

    val hasBudget = item.budgetPaisa > 0L

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_budget_item_${item.categoryItem.key}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Category info & badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(item.categoryItem.color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.categoryItem.icon != null) {
                            Icon(
                                imageVector = item.categoryItem.icon,
                                contentDescription = null,
                                tint = item.categoryItem.color,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = item.categoryItem.emoji ?: "🏷️",
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.categoryItem.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (hasBudget) {
                            Text(
                                text = "বাজেট: ${CurrencyFormatter.formatPaisaToTaka(item.budgetPaisa)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "বাজেট নির্ধারণ করা নেই",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Spent amount & status badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatPaisaToTaka(item.spentPaisa),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isOverBudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                    )

                    if (hasBudget) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = progressColor.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.isOverBudget) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.percentage.toInt()}% (অতিক্রম)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = ExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else if (item.isWarning) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9F43),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.percentage.toInt()}% (সতর্কতা)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFFFF9F43),
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${item.percentage.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = IncomeGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (hasBudget) {
                Spacer(modifier = Modifier.height(8.dp))

                // Progress Indicator Bar
                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Remaining balance text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val remainingPaisa = item.budgetPaisa - item.spentPaisa
                    if (remainingPaisa >= 0) {
                        Text(
                            text = "অবশিষ্ট: ${CurrencyFormatter.formatPaisaToTaka(remainingPaisa)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = IncomeGreen
                        )
                    } else {
                        Text(
                            text = "অতিরিক্ত খরচ: ${CurrencyFormatter.formatPaisaToTaka(Math.abs(remainingPaisa))}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "ব্যবহার: ${item.percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
