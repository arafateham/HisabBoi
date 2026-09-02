package com.example.ui.screens.loans

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.LoanEntity
import com.example.ui.components.LoanCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.PrimaryCoral
import com.example.util.CurrencyFormatter
import com.example.viewmodel.HisabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanScreen(
    viewModel: HisabViewModel,
    onNavigateToLoanDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: পাওনা, 1: দেনা
    val theyOweMeLoans by viewModel.theyOweMeLoans.collectAsStateWithLifecycle()
    val iOweThemLoans by viewModel.iOweThemLoans.collectAsStateWithLifecycle()
    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()

    var showAddLoanSheet by remember { mutableStateOf(false) }
    var partialLoanTarget by remember { mutableStateOf<LoanEntity?>(null) }
    var partialAmountInput by remember { mutableStateOf("") }
    var partialNoteInput by remember { mutableStateOf("") }

    val totalTheyOweMe = remember(theyOweMeLoans) { theyOweMeLoans.sumOf { it.remaining } }
    val totalIOweThem = remember(iOweThemLoans) { iOweThemLoans.sumOf { it.remaining } }

    val currentLoans = if (selectedTabIndex == 0) theyOweMeLoans else iOweThemLoans

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ধার ও দেনা-পাওনা",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddLoanSheet = true },
                containerColor = PrimaryCoral,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_loan")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Loan",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Cards Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "মোট পাওনা",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisaToTaka(totalTheyOweMe),
                            style = MaterialTheme.typography.titleMedium,
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "মোট দেনা",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatPaisaToTaka(totalIOweThem),
                            style = MaterialTheme.typography.titleMedium,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs: [আমার পাওনা] [আমার দেনা]
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "আমার পাওনা (${theyOweMeLoans.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "আমার দেনা (${iOweThemLoans.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loans List or Empty State
            if (currentLoans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryCoral.copy(alpha = 0.15f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Handshake,
                                    contentDescription = null,
                                    tint = PrimaryCoral,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTabIndex == 0) "কোনো পাওনা হিসাব নেই" else "কোনো দেনা হিসাব নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নতুন ধারের হিসাব যোগ করতে নিচের + বাটনে চাপুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentLoans, key = { it.id }) { loan ->
                        LoanCard(
                            loan = loan,
                            onClick = { onNavigateToLoanDetail(loan.id) },
                            onFullPaidClick = { viewModel.markLoanFullyPaid(loan) },
                            onPartialPaidClick = {
                                partialLoanTarget = loan
                                partialAmountInput = ""
                                partialNoteInput = ""
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Loan Bottom Sheet
    if (showAddLoanSheet) {
        AddLoanSheet(
            userId = userSettings.userId,
            onDismiss = { showAddLoanSheet = false },
            onSave = { newLoan ->
                viewModel.addLoan(newLoan)
                showAddLoanSheet = false
            }
        )
    }

    // Partial Payment Dialog
    if (partialLoanTarget != null) {
        val loan = partialLoanTarget!!
        AlertDialog(
            onDismissRequest = { partialLoanTarget = null },
            title = { Text("${loan.person} - আংশিক পরিশোধ") },
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
                        label = { Text("পরিশোধিত টাকা") },
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
                        val amtPaisa = CurrencyFormatter.takaToPaisa(partialAmountInput)
                        if (amtPaisa > 0L) {
                            viewModel.recordPartialPayment(loan, amtPaisa, partialNoteInput)
                            partialLoanTarget = null
                        }
                    }
                ) {
                    Text("সেভ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { partialLoanTarget = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
