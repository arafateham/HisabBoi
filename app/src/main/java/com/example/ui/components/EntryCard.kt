package com.example.ui.components

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
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.Category
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.util.PaymentMethod

@Composable
fun EntryCard(
    transaction: TransactionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customCategories: List<com.example.data.local.entity.CategoryEntity> = emptyList()
) {
    val categoryItem = Category.getCategoryItem(transaction.category, customCategories)
    val method = PaymentMethod.fromKey(transaction.method)
    val isExpense = transaction.type == "expense"
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val amountPrefix = if (isExpense) "-" else "+"

    val titleText = if (isExpense) {
        categoryItem.label
    } else {
        if (!transaction.sourceName.isNullOrBlank()) {
            transaction.sourceName
        } else {
            "আয় (${categoryItem.label})"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("entry_item_${transaction.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon/Emoji Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(categoryItem.color.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (categoryItem.icon != null) {
                    Icon(
                        imageVector = categoryItem.icon,
                        contentDescription = categoryItem.label,
                        tint = categoryItem.color,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = categoryItem.emoji ?: "🏷️",
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = DateHelper.formatRelativeBengali(transaction.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!transaction.note.isNullOrBlank()) {
                        Text(
                            text = "• ${transaction.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Method Badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$amountPrefix${CurrencyFormatter.formatPaisaToTaka(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = method.color.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = method.icon,
                            contentDescription = null,
                            tint = method.color,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = method.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = method.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
