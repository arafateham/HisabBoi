package com.example.ui.screens.loans

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.LoanEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.CurrencyFormatter
import com.example.util.DateHelper
import com.example.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailScreen(
    loanId: String,
    viewModel: HisabViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loans by viewModel.allLoans.collectAsStateWithLifecycle()
    val loan = loans.firstOrNull { it.id == loanId }

    var showPartialDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var partialAmountInput by remember { mutableStateOf("") }
    var partialNoteInput by remember { mutableStateOf("") }

    if (loan == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("ধারের বিস্তারিত") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("ধারের হিসাব খুঁজে পাওয়া যায়নি")
            }
        }
        return
    }

    val isTheyOweMe = loan.direction == "they_owe_me"
    val themeColor = if (isTheyOweMe) IncomeGreen else ExpenseRed
    val directionLabel = if (isTheyOweMe) "আমি পাব (পাওনা)" else "আমাকে দিতে হবে (দেনা)"
    val isCleared = loan.remaining <= 0L || loan.status == "cleared"
    val paidAmount = (loan.originalAmount - loan.remaining).coerceAtLeast(0L)
    val progress = if (loan.originalAmount > 0) paidAmount.toFloat() / loan.originalAmount.toFloat() else 1f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = loan.person,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("delete_loan_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed
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
            // Main Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(themeColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = loan.person.firstOrNull()?.toString() ?: "U",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = themeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = directionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = themeColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = loan.person,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "শুরু: ${DateHelper.formatDate(loan.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Stats Grid (মূল, পরিশোধ, বাকি)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "মূল পরিমাণ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatPaisaToTaka(loan.originalAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "পরিশোধিত",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatPaisaToTaka(paidAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column {
                                Text(
                                    text = "অবশিষ্ট বাকি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatPaisaToTaka(loan.remaining),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isCleared) IncomeGreen else ExpenseRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = themeColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        if (isCleared) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সম্পূর্ণ পরিশোধিত হয়েছে",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions (if not cleared)
            if (!isCleared) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markLoanFullyPaid(loan) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mark_full_paid_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                        ) {
                            Text(
                                text = if (isTheyOweMe) "সব ফেরত পেলাম" else "সব শোধ করলাম",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        FilledTonalButton(
                            onClick = { showPartialDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("mark_partial_paid_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "আংশিক পরিশোধ",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Loan Information Note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "লেনদেনের হিসাব বিবরণী",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সর্বশেষ আপডেট: ${DateHelper.formatDate(loan.updatedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isTheyOweMe) "উনার কাছ থেকে ধারের টাকা আদায় হলে 'ফেরত পেলাম' বা 'আংশিক' চাপুন।" else "উনাকে ধারের টাকা পরিশোধ করলে 'সব শোধ করলাম' বা 'আংশিক' চাপুন।",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Partial Payment Dialog
    if (showPartialDialog) {
        AlertDialog(
            onDismissRequest = { showPartialDialog = false },
            title = { Text("আংশিক পরিশোধ যোগ করুন") },
            text = {
                Column {
                    Text(
                        text = "সর্বোচ্চ বাকি: ${CurrencyFormatter.formatPaisaToTaka(loan.remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = partialAmountInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                partialAmountInput = input
                            }
                        },
                        label = { Text("পরিশোধিত টাকার পরিমাণ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = partialNoteInput,
                        onValueChange = { partialNoteInput = it },
                        label = { Text("নোট (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amountPaisa = CurrencyFormatter.takaToPaisa(partialAmountInput)
                        if (amountPaisa > 0L) {
                            viewModel.recordPartialPayment(loan, amountPaisa, partialNoteInput)
                            showPartialDialog = false
                            partialAmountInput = ""
                            partialNoteInput = ""
                        }
                    }
                ) {
                    Text("সেভ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPartialDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Delete Loan Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ধারের হিসাব মুছে ফেলবেন?") },
            text = { Text("আপনি কি নিশ্চিতভাবে ${loan.person}-এর ধারের হিসাব মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLoan(loan)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("মুছে ফেলুন", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
