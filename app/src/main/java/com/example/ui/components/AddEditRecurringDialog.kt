package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.CategoryItem
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.util.PaymentMethod
import java.util.UUID

data class RecurringPreset(
    val title: String,
    val categoryKey: String,
    val defaultMethod: String,
    val type: String
)

val EXPENSE_PRESETS = listOf(
    RecurringPreset("বাসা ভাড়া", "bill", "bank", "expense"),
    RecurringPreset("ইন্টারনেট বিল", "bill", "bkash", "expense"),
    RecurringPreset("বিদ্যুৎ বিল", "bill", "bkash", "expense"),
    RecurringPreset("গ্যাস বিল", "bill", "cash", "expense"),
    RecurringPreset("মোবাইল রিচার্জ", "bill", "bkash", "expense"),
    RecurringPreset("টিউশন ফি", "education", "bank", "expense"),
    RecurringPreset("জিম মেম্বারশিপ", "health", "cash", "expense"),
    RecurringPreset("দৈনিক যাতায়াত", "transport", "cash", "expense")
)

val INCOME_PRESETS = listOf(
    RecurringPreset("মাসিক বেতন", "salary", "bank", "income"),
    RecurringPreset("টিউশন আয়", "tuition", "bkash", "income"),
    RecurringPreset("বাড়ি ভাড়া প্রাপ্তি", "other_income", "bank", "income"),
    RecurringPreset("ফ্রিল্যান্সিং", "salary", "bank", "income"),
    RecurringPreset("ব্যবসা মুনাফা", "other_income", "cash", "income")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringDialog(
    userId: String,
    allCategories: List<CategoryItem>,
    existingItem: RecurringExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (RecurringExpenseEntity) -> Unit
) {
    var type by remember { mutableStateOf(existingItem?.type ?: "expense") }
    var title by remember { mutableStateOf(existingItem?.name ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (existingItem != null && existingItem.amount > 0)
                CurrencyFormatter.formatPaisaToTaka(existingItem.amount, includeSymbol = false).replace(",", "")
            else ""
        )
    }
    var selectedCategoryKey by remember {
        mutableStateOf(existingItem?.category ?: (if (type == "income") "salary" else "bill"))
    }
    var selectedMethod by remember { mutableStateOf(existingItem?.method ?: "cash") }
    var frequency by remember { mutableStateOf(existingItem?.frequency ?: "monthly") }
    var dayOfMonthSlider by remember { mutableFloatStateOf(existingItem?.dayOfMonth?.toFloat() ?: 1f) }
    var note by remember { mutableStateOf(existingItem?.note ?: "") }

    var isTitleError by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }

    val dayOfMonth = dayOfMonthSlider.toInt().coerceIn(1, 31)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingItem == null) "নতুন নিয়মিত শিডিউল" else "শিডিউল পরিবর্তন",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Type Switch (Expense vs Income)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Expense Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == "expense") ExpenseRed else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                type = "expense"
                                if (selectedCategoryKey == "salary" || selectedCategoryKey == "tuition") {
                                    selectedCategoryKey = "bill"
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "💸 নিয়মিত খরচ (Expense)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (type == "expense") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Income Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (type == "income") IncomeGreen else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                type = "income"
                                if (selectedCategoryKey == "bill" || selectedCategoryKey == "food") {
                                    selectedCategoryKey = "salary"
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "💰 নিয়মিত আয় (Income)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (type == "income") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Presets Quick Bar
                Column {
                    Text(
                        text = "দ্রুত টেমপ্লেট নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val presets = if (type == "expense") EXPENSE_PRESETS else INCOME_PRESETS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.clickable {
                                    title = preset.title
                                    selectedCategoryKey = preset.categoryKey
                                    selectedMethod = preset.defaultMethod
                                }
                            ) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("শিডিউলের নাম (যেমন: বাসা ভাড়া, বেতন)") },
                    singleLine = true,
                    isError = isTitleError,
                    supportingText = if (isTitleError) {
                        { Text("অনুগ্রহ করে একটি নাম দিন") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recurring_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // 4. Amount Input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() || ch == '.' }
                        amountText = filtered
                        if (filtered.isNotBlank()) isAmountError = false
                    },
                    label = { Text("টাকার পরিমাণ (৳)") },
                    placeholder = { Text("০.০০") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = isAmountError,
                    supportingText = if (isAmountError) {
                        { Text("সঠিক টাকার পরিমাণ লিখুন") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recurring_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // 5. Category Selection
                Column {
                    Text(
                        text = "ক্যাটাগরি নির্ধারণ করুন",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCategories) { cat ->
                            val isSelected = cat.key.equals(selectedCategoryKey, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryKey = cat.key },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (cat.icon != null) {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else cat.color
                                            )
                                        } else {
                                            Text(text = cat.emoji ?: "🏷️", fontSize = 14.sp)
                                        }
                                        Text(text = cat.label)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (type == "expense") ExpenseRed else IncomeGreen,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // 6. Payment Method Selection
                Column {
                    Text(
                        text = "লেনদেন পদ্ধতি",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(PaymentMethod.entries) { method ->
                            val isSelected = method.key.equals(selectedMethod, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) method.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) method.color else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedMethod = method.key }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = method.icon,
                                        contentDescription = null,
                                        tint = method.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = method.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Frequency & Day of Month
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                tint = PrimaryCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "প্রতি মাসের নির্ধারিত তারিখ:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryCoral.copy(alpha = 0.15f),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "${CurrencyFormatter.toBengaliNumerals(dayOfMonth.toString())} তারিখ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCoral,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Slider(
                        value = dayOfMonthSlider,
                        onValueChange = { dayOfMonthSlider = it },
                        valueRange = 1f..31f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryCoral,
                            activeTrackColor = PrimaryCoral
                        )
                    )
                }

                // 8. Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("মন্তব্য / নোট (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ফ্ল্যাট নং ৪বি, ডিপিএস ইত্যাদি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                        return@Button
                    }
                    val amountPaisa = CurrencyFormatter.takaToPaisa(amountText)
                    if (amountPaisa <= 0L) {
                        isAmountError = true
                        return@Button
                    }

                    val nextDue = DateHelper.calculateNextDueDate(dayOfMonth, frequency)

                    val scheduleEntity = RecurringExpenseEntity(
                        id = existingItem?.id ?: UUID.randomUUID().toString(),
                        userId = userId,
                        type = type,
                        name = title.trim(),
                        amount = amountPaisa,
                        category = selectedCategoryKey,
                        method = selectedMethod,
                        frequency = frequency,
                        dayOfMonth = dayOfMonth,
                        note = note.trim().ifEmpty { null },
                        active = existingItem?.active ?: true,
                        lastExecutedDate = existingItem?.lastExecutedDate,
                        nextDueDate = nextDue,
                        createdAt = existingItem?.createdAt ?: System.currentTimeMillis()
                    )

                    onSave(scheduleEntity)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "expense") ExpenseRed else IncomeGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_recurring_button")
            ) {
                Text(if (existingItem == null) "শিডিউল সংরক্ষণ" else "আপডেট করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
