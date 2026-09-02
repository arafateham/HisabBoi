package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val type: String,           // "expense" | "income"
    val amount: Long,           // stored in paisa (x100)
    val category: String,
    val method: String,         // "cash" | "bkash" | "nagad" | "rocket" | "bank" | "card"
    val source: String? = null, // "self" | "baba" | "ma" | "other"
    val sourceName: String? = null,
    val note: String? = null,
    val photoUri: String? = null,
    val date: Long,             // epoch milliseconds
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val deleted: Boolean = false
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val person: String,
    val direction: String,      // "they_owe_me" | "i_owe_them"
    val originalAmount: Long,   // in paisa (x100)
    val remaining: Long,        // in paisa (x100)
    val status: String = "active", // "active" | "cleared" | "deleted"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    val type: String get() = direction
}

@Entity(tableName = "loan_payments")
data class LoanPaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val loanId: String,
    val amount: Long,           // in paisa (x100)
    val note: String? = null,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val name: String,
    val targetAmount: Long,     // in paisa (x100)
    val savedAmount: Long = 0L, // in paisa (x100)
    val deadline: Long? = null,
    val status: String = "active", // "active" | "completed"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val type: String = "expense", // "expense" | "income" (e.g. monthly rent vs salary)
    val name: String,
    val amount: Long,           // in paisa (x100)
    val category: String,
    val method: String = "cash", // "cash" | "bkash" | "nagad" | "rocket" | "bank" | "card"
    val frequency: String = "monthly", // "monthly" | "weekly" | "daily" | "yearly"
    val dayOfMonth: Int = 1,    // 1..31
    val note: String? = null,
    val active: Boolean = true,
    val lastExecutedDate: Long? = null,
    val nextDueDate: Long = System.currentTimeMillis(),
    val autoPost: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "person_aliases")
data class PersonAliasEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val alias: String,
    val canonical: String
)

@Entity(tableName = "custom_categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val name: String,
    val emoji: String = "🏷️",
    val colorHex: String = "#FF6B6B",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey val categoryKey: String, // "food", "transport", or custom category id
    val userId: String = "default_user",
    val monthlyBudget: Long,              // stored in paisa (x100)
    val updatedAt: Long = System.currentTimeMillis()
)

