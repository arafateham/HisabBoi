package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentMethod(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
) {
    CASH("cash", "ক্যাশ", Icons.Rounded.Payments, Color(0xFF2ED573)),
    BKASH("bkash", "বিকাশ", Icons.Rounded.AccountBalanceWallet, Color(0xFFE2136E)),
    NAGAD("nagad", "নগদ", Icons.Rounded.FlashOn, Color(0xFFF7941D)),
    ROCKET("rocket", "রকেট", Icons.Rounded.RocketLaunch, Color(0xFF8C3494)),
    BANK("bank", "ব্যাংক", Icons.Rounded.AccountBalance, Color(0xFF1E88E5)),
    CARD("card", "কার্ড", Icons.Rounded.CreditCard, Color(0xFF00B894));

    companion object {
        fun fromKey(key: String): PaymentMethod {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: CASH
        }
    }
}

enum class IncomeSource(
    val key: String,
    val label: String
) {
    SELF("self", "নিজের"),
    BABA("baba", "বাবা"),
    MA("ma", "মা"),
    OTHER("other", "অন্য কেউ");

    companion object {
        fun fromKey(key: String?): IncomeSource {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SELF
        }
    }
}
