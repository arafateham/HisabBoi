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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.LoanEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyFormatter

@Composable
fun LoanCard(
    loan: LoanEntity,
    onClick: () -> Unit,
    onFullPaidClick: () -> Unit,
    onPartialPaidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTheyOweMe = loan.direction == "they_owe_me"
    val themeColor = if (isTheyOweMe) IncomeGreen else ExpenseRed
    val directionIcon = if (isTheyOweMe) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward
    val directionLabel = if (isTheyOweMe) "পাওনা" else "দেনা"

    val paidAmount = (loan.originalAmount - loan.remaining).coerceAtLeast(0L)
    val progress = if (loan.originalAmount > 0) {
        paidAmount.toFloat() / loan.originalAmount.toFloat()
    } else 1f

    val isCleared = loan.remaining <= 0L || loan.status == "cleared"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("loan_item_${loan.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle with Initial
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(themeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = loan.person.firstOrNull()?.toString() ?: "U"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = themeColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.person,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "মূল: ${CurrencyFormatter.formatPaisaToTaka(loan.originalAmount)} | বাকি: ${CurrencyFormatter.formatPaisaToTaka(loan.remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(themeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCleared) Icons.Rounded.CheckCircle else directionIcon,
                        contentDescription = directionLabel,
                        tint = themeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = themeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!isCleared) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onFullPaidClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("full_paid_button_${loan.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = themeColor.copy(alpha = 0.15f),
                            contentColor = themeColor
                        )
                    ) {
                        Text(
                            text = if (isTheyOweMe) "ফেরত পেলাম" else "পরিশোধ করলাম",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedButton(
                        onClick = onPartialPaidClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("partial_paid_button_${loan.id}"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "আংশিক",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
