package com.example.ui.screens.loans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.LoanEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyFormatter
import com.example.util.HapticFeedbackHelper
import com.example.util.PaymentMethod
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanSheet(
    userId: String,
    onDismiss: () -> Unit,
    onSave: (LoanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    var selectedDirection by remember { mutableStateOf("they_owe_me") } // "they_owe_me" (পাওনা) | "i_owe_them" (দেনা)
    var personName by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var noteInput by remember { mutableStateOf("") }

    val amountPaisa = CurrencyFormatter.takaToPaisa(amountInput)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("add_loan_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "নতুন ধারের হিসাব যোগ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direction Toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_direction_toggle")
            ) {
                SegmentedButton(
                    selected = selectedDirection == "they_owe_me",
                    onClick = { selectedDirection = "they_owe_me" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        activeContentColor = IncomeGreen
                    )
                ) {
                    Text(
                        text = "আমি দিয়েছি (পাওনা)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                SegmentedButton(
                    selected = selectedDirection == "i_owe_them",
                    onClick = { selectedDirection = "i_owe_them" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        activeContentColor = ExpenseRed
                    )
                ) {
                    Text(
                        text = "আমি নিয়েছি (দেনা)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Person Name
            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                label = { Text("ব্যক্তির নাম") },
                placeholder = { Text("যেমন: রহিম সাহেব, বন্ধু ইত্যাদি") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_person_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount Input
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        amountInput = input
                    }
                },
                label = { Text("টাকার পরিমাণ") },
                placeholder = { Text("যেমন: 5000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("loan_amount_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Method Selector
            Text(
                text = "লেনদেনের মাধ্যম",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(PaymentMethod.entries) { method ->
                    val isSelected = selectedMethod == method
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) method.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, method.color) else null,
                        modifier = Modifier.clickable { selectedMethod = method }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = method.icon,
                                contentDescription = null,
                                tint = method.color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = method.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Note
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = {
                    if (personName.isNotBlank() && amountPaisa > 0L) {
                        HapticFeedbackHelper.vibrateSuccess(context)
                        val loan = LoanEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            person = personName.trim(),
                            direction = selectedDirection,
                            originalAmount = amountPaisa,
                            remaining = amountPaisa,
                            status = "active",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            synced = false
                        )
                        onSave(loan)
                    }
                },
                enabled = personName.isNotBlank() && amountPaisa > 0L,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_loan_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedDirection == "they_owe_me") IncomeGreen else ExpenseRed
                )
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সেভ করো",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
