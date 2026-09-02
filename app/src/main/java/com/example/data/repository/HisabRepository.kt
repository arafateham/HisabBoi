package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.util.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID

class HisabRepository(
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    private val transactionDao = database.transactionDao()
    private val loanDao = database.loanDao()
    private val goalDao = database.goalDao()
    private val recurringDao = database.recurringDao()
    private val categoryDao = database.categoryDao()
    private val categoryBudgetDao = database.categoryBudgetDao()

    // Preferences
    val userSettingsFlow = userPreferences.userSettingsFlow

    suspend fun getUserId(): String {
        return userPreferences.userSettingsFlow.first().userId
    }

    // Transactions
    fun getTransactionsFlow(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getAllFlow(userId)
    }

    fun getTransactionsInRangeFlow(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getInRangeFlow(userId, startDate, endDate)
    }

    fun getRecentTransactionsFlow(userId: String, limit: Int = 5): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentFlow(userId, limit)
    }

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction.copy(synced = false))
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.update(transaction.copy(synced = false))
    }

    suspend fun softDeleteTransaction(id: String) {
        transactionDao.softDelete(id)
    }

    suspend fun restoreTransaction(id: String) {
        transactionDao.restore(id)
    }

    // Loans
    fun getLoansFlow(userId: String): Flow<List<LoanEntity>> {
        return loanDao.getAllFlow(userId)
    }

    fun getLoansByDirectionFlow(userId: String, direction: String): Flow<List<LoanEntity>> {
        return loanDao.getByDirectionFlow(userId, direction)
    }

    fun getLoanByIdFlow(id: String): Flow<LoanEntity?> {
        return loanDao.getByIdFlow(id)
    }

    suspend fun addLoan(loan: LoanEntity) {
        loanDao.insert(loan.copy(synced = false))
    }

    suspend fun updateLoan(loan: LoanEntity) {
        loanDao.update(loan.copy(synced = false, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteLoan(id: String) {
        loanDao.softDelete(id)
    }

    fun getLoanPaymentsFlow(loanId: String): Flow<List<LoanPaymentEntity>> {
        return loanDao.getPaymentsFlow(loanId)
    }

    suspend fun addLoanPayment(loanId: String, amountPaisa: Long, note: String? = null) {
        val payment = LoanPaymentEntity(
            id = UUID.randomUUID().toString(),
            loanId = loanId,
            amount = amountPaisa,
            note = note,
            date = System.currentTimeMillis()
        )
        loanDao.insertPayment(payment)
        
        // Update remaining on loan
        val loan = loanDao.getById(loanId)
        if (loan != null) {
            val newRemaining = (loan.remaining - amountPaisa).coerceAtLeast(0L)
            val newStatus = if (newRemaining <= 0L) "cleared" else "active"
            loanDao.update(loan.copy(remaining = newRemaining, status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    // Goals
    fun getGoalsFlow(userId: String): Flow<List<GoalEntity>> {
        return goalDao.getAllFlow(userId)
    }

    suspend fun addGoal(goal: GoalEntity) {
        goalDao.insert(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        goalDao.update(goal)
    }

    suspend fun deleteGoal(id: String) {
        goalDao.delete(id)
    }

    // Recurring
    fun getRecurringFlow(userId: String): Flow<List<RecurringExpenseEntity>> {
        return recurringDao.getAllFlow(userId)
    }

    suspend fun addRecurring(recurring: RecurringExpenseEntity) {
        recurringDao.insert(recurring)
    }

    suspend fun updateRecurring(recurring: RecurringExpenseEntity) {
        recurringDao.update(recurring)
    }

    suspend fun toggleRecurringActive(id: String, active: Boolean) {
        recurringDao.setActive(id, active)
    }

    suspend fun deleteRecurring(id: String) {
        recurringDao.delete(id)
    }

    suspend fun postRecurringTransaction(recurring: RecurringExpenseEntity): TransactionEntity {
        val now = System.currentTimeMillis()
        val tx = TransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = recurring.userId,
            type = recurring.type,
            amount = recurring.amount,
            category = recurring.category,
            method = recurring.method,
            source = if (recurring.type == "income") "self" else null,
            sourceName = if (recurring.type == "income") recurring.name else null,
            note = if (!recurring.note.isNullOrBlank()) "${recurring.name} (${recurring.note})" else "নিয়মিত: ${recurring.name}",
            date = now,
            createdAt = now,
            synced = false,
            deleted = false
        )
        transactionDao.insert(tx)

        val nextDue = DateHelper.calculateNextDueDate(recurring.dayOfMonth, recurring.frequency)
        val updated = recurring.copy(
            lastExecutedDate = now,
            nextDueDate = nextDue
        )
        recurringDao.update(updated)
        return tx
    }

    // Custom Categories
    fun getCustomCategoriesFlow(userId: String): Flow<List<com.example.data.local.entity.CategoryEntity>> {
        return categoryDao.getAllFlow(userId)
    }

    suspend fun addCustomCategory(category: com.example.data.local.entity.CategoryEntity) {
        categoryDao.insert(category)
    }

    suspend fun deleteCustomCategory(id: String) {
        categoryDao.deleteById(id)
    }

    // Category Budgets
    fun getCategoryBudgetsFlow(userId: String): Flow<List<com.example.data.local.entity.CategoryBudgetEntity>> {
        return categoryBudgetDao.getAllFlow(userId)
    }

    suspend fun setCategoryBudget(budget: com.example.data.local.entity.CategoryBudgetEntity) {
        categoryBudgetDao.setBudget(budget)
    }

    suspend fun deleteCategoryBudget(userId: String, categoryKey: String) {
        categoryBudgetDao.deleteBudget(userId, categoryKey)
    }

    // Cloud Sync
    suspend fun syncPending(): Boolean {
        return try {
            val userId = getUserId()
            val unsynced = transactionDao.getUnsynced(userId)
            // Mark items synced
            unsynced.forEach {
                transactionDao.update(it.copy(synced = true))
            }
            userPreferences.updateLastSyncTime(System.currentTimeMillis())
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun seedInitialDataIfEmpty(userId: String) {
        val recent = transactionDao.getRecentFlow(userId, 1).first()
        if (recent.isEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val initial = listOf(
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "income",
                    amount = 3500000L, // 35,000 Tk
                    category = "other",
                    method = "bank",
                    source = "self",
                    sourceName = "মাসিক বেতন",
                    note = "অফিস স্যালারি",
                    date = now - (dayMillis * 3),
                    synced = true
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "expense",
                    amount = 320000L, // 3,200 Tk
                    category = "grocery",
                    method = "bkash",
                    note = "সাপ্তাহিক বাজার",
                    date = now - (dayMillis * 2),
                    synced = true
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "expense",
                    amount = 45000L, // 450 Tk
                    category = "food",
                    method = "cash",
                    note = "দুপুরের খাবার",
                    date = now - dayMillis,
                    synced = true
                ),
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "expense",
                    amount = 18000L, // 180 Tk
                    category = "transport",
                    method = "cash",
                    note = "রিকশা ও বাস ভাড়া",
                    date = now,
                    synced = true
                )
            )
            transactionDao.insertAll(initial)

            // Seed initial loans
            loanDao.insert(
                LoanEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    person = "রাকিব হাসান",
                    direction = "they_owe_me",
                    originalAmount = 500000L, // 5000 Tk
                    remaining = 250000L,       // 2500 Tk
                    status = "active",
                    createdAt = now - (dayMillis * 5),
                    updatedAt = now - (dayMillis * 2),
                    synced = true
                )
            )

            // Seed goal
            goalDao.insert(
                GoalEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    name = "জরুরি ফান্ড",
                    targetAmount = 5000000L, // 50,000 Tk
                    savedAmount = 1500000L,  // 15,000 Tk
                    deadline = now + (dayMillis * 90)
                )
            )

            // Seed recurring income and expenses
            recurringDao.insert(
                RecurringExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "income",
                    name = "মাসিক বেতন",
                    amount = 4500000L, // 45,000 Tk
                    category = "salary",
                    method = "bank",
                    frequency = "monthly",
                    dayOfMonth = 1,
                    note = "অফিস বেতন",
                    active = true,
                    nextDueDate = DateHelper.calculateNextDueDate(1, "monthly")
                )
            )
            recurringDao.insert(
                RecurringExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "expense",
                    name = "বাসা ভাড়া",
                    amount = 1500000L, // 15,000 Tk
                    category = "bill",
                    method = "bank",
                    frequency = "monthly",
                    dayOfMonth = 5,
                    note = "বাড়িওয়ালাকে পরিশোধ",
                    active = true,
                    nextDueDate = DateHelper.calculateNextDueDate(5, "monthly")
                )
            )
            recurringDao.insert(
                RecurringExpenseEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "expense",
                    name = "ইন্টারনেট বিল",
                    amount = 105000L, // 1,050 Tk
                    category = "bill",
                    method = "bkash",
                    frequency = "monthly",
                    dayOfMonth = 10,
                    note = "ওয়াইফাই ব্রডব্যান্ড",
                    active = true,
                    nextDueDate = DateHelper.calculateNextDueDate(10, "monthly")
                )
            )
        }
    }

    suspend fun clearAllLocalData(userId: String) {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}
