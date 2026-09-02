package com.example.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.CreateCategoryDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.Category
import com.example.util.CategoryItem
import com.example.util.CurrencyFormatter
import com.example.util.HapticFeedbackHelper
import com.example.util.IncomeSource
import com.example.util.PaymentMethod
import com.example.util.toCategoryItem
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEntrySheet(
    userId: String,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    allCategories: List<CategoryItem> = emptyList(),
    onCreateCustomCategory: (name: String, emoji: String, colorHex: String) -> Unit = { _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val categoryList = if (allCategories.isNotEmpty()) allCategories else Category.entries.map { it.toCategoryItem() }

    var selectedType by remember { mutableStateOf("expense") } // "expense" | "income"
    var amountInput by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf(categoryList.firstOrNull()?.key ?: Category.FOOD.key) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var selectedSource by remember { mutableStateOf(IncomeSource.SELF) }
    var sourceNameInput by remember { mutableStateOf("") }
    var selectedReasonChip by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var photoUriString by remember { mutableStateOf<String?>(null) }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUriString = uri?.toString()
    }

    val amountPaisa = CurrencyFormatter.takaToPaisa(amountInput)

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onConfirm = { name, emoji, colorHex ->
                onCreateCustomCategory(name, emoji, colorHex)
                showCreateCategoryDialog = false
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("add_entry_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "নতুন হিসাব যোগ করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Type Segmented Toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("entry_type_toggle")
            ) {
                SegmentedButton(
                    selected = selectedType == "expense",
                    onClick = { selectedType = "expense" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        activeContentColor = ExpenseRed
                    )
                ) {
                    Text(
                        text = "খরচ",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                SegmentedButton(
                    selected = selectedType == "income",
                    onClick = { selectedType = "income" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        activeContentColor = IncomeGreen
                    )
                ) {
                    Text(
                        text = "আয়",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Amount Display & Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "পরিমাণ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val displayAmount = if (amountInput.isEmpty()) "০" else amountInput
                    Text(
                        text = "৳ $displayAmount",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (selectedType == "expense") ExpenseRed else IncomeGreen,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset Quick Add Chips (+50, +100, +500, +1000)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(50, 100, 500, 1000).forEach { addVal ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    val current = amountInput.toDoubleOrNull() ?: 0.0
                                    amountInput = (current + addVal).toInt().toString()
                                }
                            ) {
                                Text(
                                    text = "+৳$addVal",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Clear Button
                        if (amountInput.isNotEmpty()) {
                            IconButton(
                                onClick = { amountInput = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Backspace,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                amountInput = input
                            }
                        },
                        placeholder = { Text("টাকার পরিমাণ লিখুন") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Category (if expense)
            if (selectedType == "expense") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ক্যাটাগরি নির্বাচন করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryCoral.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clickable { showCreateCategoryDialog = true }
                            .testTag("add_custom_category_button")
                    ) {
                        Text(
                            text = "+ নতুন ক্যাটাগরি",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCoral,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryList) { cat ->
                        val isSelected = selectedCategoryKey.equals(cat.key, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) cat.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, cat.color) else null,
                            modifier = Modifier
                                .testTag("category_chip_${cat.key}")
                                .clickable { selectedCategoryKey = cat.key }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(cat.color.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cat.icon != null) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = cat.label,
                                            tint = cat.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = cat.emoji ?: "🏷️",
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cat.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                // Step 3 & 5 (Income Source)
                Text(
                    text = "আয়ের উৎস",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IncomeSource.entries.forEach { src ->
                        val isSelected = selectedSource == src
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSource = src },
                            label = { Text(src.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                                selectedLabelColor = IncomeGreen
                            )
                        )
                    }
                }

                if (selectedSource == IncomeSource.OTHER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sourceNameInput,
                        onValueChange = { sourceNameInput = it },
                        label = { Text("নাম লিখুন") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "আয়ের কারণ:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("আগের ধার ফেরত", "কাজের টাকা", "উপহার", "ধার দিলো").forEach { reason ->
                            FilterChip(
                                selected = selectedReasonChip == reason,
                                onClick = {
                                    selectedReasonChip = reason
                                    noteInput = reason
                                },
                                label = { Text(reason) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 4: Payment Method Selector
            Text(
                text = "লেনদেনের মাধ্যম",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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
                        modifier = Modifier
                            .testTag("method_chip_${method.key}")
                            .clickable { selectedMethod = method }
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 6: Note + Photo
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("নোট লিখুন (ঐচ্ছিক)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photoUriString != null) "ছবি সংযুক্ত আছে" else "রসিদের ছবি")
                }

                if (photoUriString != null) {
                    IconButton(onClick = { photoUriString = null }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Remove photo",
                            tint = ExpenseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = {
                    if (amountPaisa > 0L) {
                        HapticFeedbackHelper.vibrateSuccess(context)
                        val transaction = TransactionEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            type = selectedType,
                            amount = amountPaisa,
                            category = if (selectedType == "expense") selectedCategoryKey else "other",
                            method = selectedMethod.key,
                            source = if (selectedType == "income") selectedSource.key else null,
                            sourceName = if (selectedType == "income") {
                                if (selectedSource == IncomeSource.OTHER && sourceNameInput.isNotBlank()) {
                                    sourceNameInput
                                } else {
                                    selectedSource.label
                                }
                            } else null,
                            note = noteInput.ifBlank { null },
                            photoUri = photoUriString,
                            date = System.currentTimeMillis()
                        )
                        onSave(transaction)
                    }
                },
                enabled = amountPaisa > 0L,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_entry_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == "expense") ExpenseRed else IncomeGreen
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

