package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BankColor
import com.example.ui.theme.BkashColor
import com.example.ui.theme.CardColor
import com.example.ui.theme.CashColor
import com.example.ui.theme.NagadColor
import com.example.ui.theme.PrimaryCoral
import com.example.ui.theme.RocketColor
import com.example.util.CurrencyFormatter
import com.example.viewmodel.SummaryStats

@Composable
fun BalanceSection(
    stats: SummaryStats,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Total Balance Card
        item {
            TotalBalanceCard(
                balancePaisa = stats.netBalance,
                totalIncome = stats.totalIncome,
                totalExpense = stats.totalExpense
            )
        }

        // 2. Cash Card
        item {
            AccountWalletCard(
                title = "ক্যাশ",
                amountPaisa = stats.methodBalance.cash,
                icon = Icons.Rounded.Payments,
                themeColor = CashColor
            )
        }

        // 3. bKash Card
        item {
            AccountWalletCard(
                title = "বিকাশ",
                amountPaisa = stats.methodBalance.bkash,
                icon = Icons.Rounded.AccountBalanceWallet,
                themeColor = BkashColor
            )
        }

        // 4. Nagad Card
        item {
            AccountWalletCard(
                title = "নগদ",
                amountPaisa = stats.methodBalance.nagad,
                icon = Icons.Rounded.FlashOn,
                themeColor = NagadColor
            )
        }

        // 5. Rocket Card
        item {
            AccountWalletCard(
                title = "রকেট",
                amountPaisa = stats.methodBalance.rocket,
                icon = Icons.Rounded.RocketLaunch,
                themeColor = RocketColor
            )
        }

        // 6. Bank Card
        item {
            AccountWalletCard(
                title = "ব্যাংক",
                amountPaisa = stats.methodBalance.bank,
                icon = Icons.Rounded.AccountBalance,
                themeColor = BankColor
            )
        }

        // 7. Card Card
        item {
            AccountWalletCard(
                title = "কার্ড",
                amountPaisa = stats.methodBalance.card,
                icon = Icons.Rounded.CreditCard,
                themeColor = CardColor
            )
        }
    }
}

@Composable
fun TotalBalanceCard(
    balancePaisa: Long,
    totalIncome: Long,
    totalExpense: Long,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .height(130.dp)
            .testTag("total_balance_card"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryCoral,
                            Color(0xFFC82A45)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মোট ব্যালেন্স",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = CurrencyFormatter.formatPaisaToTaka(balancePaisa),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "আয়: ${CurrencyFormatter.formatPaisaToTaka(totalIncome)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "খরচ: ${CurrencyFormatter.formatPaisaToTaka(totalExpense)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun AccountWalletCard(
    title: String,
    amountPaisa: Long,
    icon: ImageVector,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(130.dp)
            .testTag("wallet_card_${title}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(themeColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = themeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatPaisaToTaka(amountPaisa),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
