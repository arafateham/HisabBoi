package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.util.CategoryItem
import com.example.util.CurrencyFormatter

@Composable
fun SetCategoryBudgetDialog(
    categoryItem: CategoryItem,
    currentBudgetPaisa: Long,
    onDismiss: () -> Unit,
    onSave: (monthlyBudgetPaisa: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var budgetInput by remember {
        mutableStateOf(
            if (currentBudgetPaisa > 0L)
                CurrencyFormatter.formatPaisaToTaka(currentBudgetPaisa, includeSymbol = false).replace(",", "")
            else ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ক্যাটাগরি বাজেট নির্ধারণ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (currentBudgetPaisa > 0L && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Remove budget",
                            tint = ExpenseRed
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Header
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = categoryItem.color.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(categoryItem.color.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (categoryItem.icon != null) {
                                Icon(
                                    imageVector = categoryItem.icon,
                                    contentDescription = null,
                                    tint = categoryItem.color,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = categoryItem.emoji ?: "🏷️",
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.size(12.dp))

                        Column {
                            Text(
                                text = categoryItem.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentBudgetPaisa > 0L)
                                    "বর্তমান বাজেট: ${CurrencyFormatter.formatPaisaToTaka(currentBudgetPaisa)}"
                                else "কোনো বাজেট নির্ধারণ করা নেই",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text(
                    text = "এই ক্যাটাগরিতে প্রতি মাসে সর্বোচ্চ খরচের সীমা নির্ধারণ করুন। ৮০% খরচ হলে আপনাকে সতর্কতা পাঠানো হবে:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() || it == '.' }) {
                            budgetInput = input
                        }
                    },
                    label = { Text("বাজেটের পরিমাণ (টাকা)") },
                    placeholder = { Text("যেমন: ৫০০০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val paisa = CurrencyFormatter.takaToPaisa(budgetInput)
                    onSave(paisa)
                    onDismiss()
                }
            ) {
                Text("সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
