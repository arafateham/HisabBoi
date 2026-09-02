package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CategoryBudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserSettings
import com.example.data.repository.HisabRepository
import com.example.util.Category
import com.example.util.CategoryItem
import com.example.util.DateHelper
import com.example.util.NotificationHelper
import com.example.util.toCategoryItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SyncState {
    SYNCED, PENDING, OFFLINE
}

enum class DateFilterType(val label: String) {
    TODAY("আজ"),
    THIS_WEEK("এই সপ্তাহ"),
    THIS_MONTH("এই মাস"),
    CUSTOM("কাস্টম")
}

data class MethodBalance(
    val cash: Long = 0L,
    val bkash: Long = 0L,
    val nagad: Long = 0L,
    val rocket: Long = 0L,
    val bank: Long = 0L,
    val card: Long = 0L
)

data class SummaryStats(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val netBalance: Long = 0L,
    val todayIncome: Long = 0L,
    val todayExpense: Long = 0L,
    val methodBalance: MethodBalance = MethodBalance()
)

data class RecurringSummary(
    val totalMonthlyIncome: Long = 0L,
    val totalMonthlyExpense: Long = 0L,
    val netMonthly: Long = 0L,
    val activeCount: Int = 0
)

data class CategoryBudgetProgress(
    val categoryItem: CategoryItem,
    val spentPaisa: Long,
    val budgetPaisa: Long,
    val percentage: Float,
    val isOverBudget: Boolean,
    val isWarning: Boolean // >= 80%
)

class HisabViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val database = AppDatabase.getDatabase(application)
    val repository = HisabRepository(database, userPreferences)

    val userSettings: StateFlow<UserSettings> = userPreferences.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _syncState = MutableStateFlow(SyncState.SYNCED)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Filter states for Entries screen
    val searchQuery = MutableStateFlow("")
    val typeFilter = MutableStateFlow("all") // "all", "expense", "income", "loan"
    val dateFilterType = MutableStateFlow(DateFilterType.THIS_MONTH)
    val customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val categoryFilter = MutableStateFlow<String?>(null)

    // Last deleted item for Undo
    private val _lastDeletedTransaction = MutableStateFlow<TransactionEntity?>(null)
    val lastDeletedTransaction: StateFlow<TransactionEntity?> = _lastDeletedTransaction.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTransactions: StateFlow<List<TransactionEntity>> = userSettings
        .flatMapLatest { settings -> repository.getTransactionsFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentTransactions: StateFlow<List<TransactionEntity>> = userSettings
        .flatMapLatest { settings -> repository.getRecentTransactionsFlow(settings.userId, 5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLoans: StateFlow<List<LoanEntity>> = userSettings
        .flatMapLatest { settings -> repository.getLoansFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val theyOweMeLoans: StateFlow<List<LoanEntity>> = allLoans.mapState(viewModelScope) { list ->
        list.filter { it.direction == "they_owe_me" || it.direction == "given" }
    }

    val iOweThemLoans: StateFlow<List<LoanEntity>> = allLoans.mapState(viewModelScope) { list ->
        list.filter { it.direction == "i_owe_them" || it.direction == "taken" }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<GoalEntity>> = userSettings
        .flatMapLatest { settings -> repository.getGoalsFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val allRecurringSchedules: StateFlow<List<RecurringExpenseEntity>> = userSettings
        .flatMapLatest { settings -> repository.getRecurringFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringExpenses: StateFlow<List<RecurringExpenseEntity>> = allRecurringSchedules.mapState(viewModelScope) { list ->
        list.filter { it.type == "expense" }
    }

    val recurringIncomes: StateFlow<List<RecurringExpenseEntity>> = allRecurringSchedules.mapState(viewModelScope) { list ->
        list.filter { it.type == "income" }
    }

    val recurringSummary: StateFlow<RecurringSummary> = allRecurringSchedules.mapState(viewModelScope) { list ->
        val activeList = list.filter { it.active }
        val income = activeList.filter { it.type == "income" }.sumOf { it.amount }
        val expense = activeList.filter { it.type == "expense" }.sumOf { it.amount }
        RecurringSummary(
            totalMonthlyIncome = income,
            totalMonthlyExpense = expense,
            netMonthly = income - expense,
            activeCount = activeList.size
        )
    }

    // Custom Categories
    @OptIn(ExperimentalCoroutinesApi::class)
    val customCategories: StateFlow<List<CategoryEntity>> = userSettings
        .flatMapLatest { settings -> repository.getCustomCategoriesFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Budgets
    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryBudgets: StateFlow<List<CategoryBudgetEntity>> = userSettings
        .flatMapLatest { settings -> repository.getCategoryBudgetsFlow(settings.userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Category Items (Default + Custom)
    val allCategoryItems: StateFlow<List<CategoryItem>> = customCategories.mapState(viewModelScope) { customs ->
        val defaultItems = Category.entries.map { it.toCategoryItem() }
        val customItems = customs.map { it.toCategoryItem() }
        defaultItems + customItems
    }

    // Category Budget Progress List for current month
    val categoryBudgetProgressList: StateFlow<List<CategoryBudgetProgress>> = combine(
        allTransactions,
        categoryBudgets,
        allCategoryItems
    ) { transactions, budgets, categories ->
        val thisMonth = DateHelper.getThisMonthStartAndEnd()
        val thisMonthExpenses = transactions.filter {
            it.type == "expense" && it.date in thisMonth.first..thisMonth.second
        }

        val budgetMap = budgets.associateBy { it.categoryKey.lowercase() }
        val spentMap = thisMonthExpenses.groupBy { it.category.lowercase() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val relevantCategoryKeys = (budgetMap.keys + spentMap.keys).toSet()

        relevantCategoryKeys.mapNotNull { catKey ->
            val catItem = categories.firstOrNull { it.key.equals(catKey, ignoreCase = true) }
                ?: Category.OTHER.toCategoryItem().copy(key = catKey, label = catKey)
            val budget = budgetMap[catKey]?.monthlyBudget ?: 0L
            val spent = spentMap[catKey] ?: 0L

            if (budget > 0L || spent > 0L) {
                val percentage = if (budget > 0L) (spent.toFloat() / budget.toFloat()) * 100f else 0f
                CategoryBudgetProgress(
                    categoryItem = catItem,
                    spentPaisa = spent,
                    budgetPaisa = budget,
                    percentage = percentage,
                    isOverBudget = budget > 0L && spent > budget,
                    isWarning = budget > 0L && percentage >= 80f
                )
            } else null
        }.sortedByDescending { it.spentPaisa }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered transactions for Entries screen
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        combine(
            allTransactions,
            searchQuery,
            typeFilter,
            dateFilterType,
            customDateRange
        ) { list, query, type, dFilter, customRange ->
            FilterBundle(list, query, type, dFilter, customRange)
        },
        categoryFilter
    ) { bundle, cat ->
        val range = when (bundle.dFilter) {
            DateFilterType.TODAY -> DateHelper.getTodayStartAndEnd()
            DateFilterType.THIS_WEEK -> DateHelper.getThisWeekStartAndEnd()
            DateFilterType.THIS_MONTH -> DateHelper.getThisMonthStartAndEnd()
            DateFilterType.CUSTOM -> bundle.customRange ?: DateHelper.getThisMonthStartAndEnd()
        }

        bundle.list.filter { item ->
            // Date filter
            val inDate = item.date in range.first..range.second

            // Type filter
            val matchType = when (bundle.type) {
                "expense" -> item.type == "expense"
                "income" -> item.type == "income"
                else -> true
            }

            // Category filter
            val matchCat = if (cat.isNullOrBlank()) true else item.category.equals(cat, ignoreCase = true)

            // Search query
            val matchQuery = if (bundle.query.isBlank()) true else {
                (item.note?.contains(bundle.query, ignoreCase = true) == true) ||
                        item.category.contains(bundle.query, ignoreCase = true) ||
                        (item.sourceName?.contains(bundle.query, ignoreCase = true) == true)
            }

            inDate && matchType && matchCat && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Statistics
    val summaryStats: StateFlow<SummaryStats> = allTransactions.mapState(viewModelScope) { list ->
        var totalIncome = 0L
        var totalExpense = 0L
        var todayIncome = 0L
        var todayExpense = 0L

        var cash = 0L
        var bkash = 0L
        var nagad = 0L
        var rocket = 0L
        var bank = 0L
        var card = 0L

        val todayRange = DateHelper.getTodayStartAndEnd()

        list.forEach { tx ->
            if (tx.type == "income") {
                totalIncome += tx.amount
                when (tx.method.lowercase()) {
                    "cash" -> cash += tx.amount
                    "bkash" -> bkash += tx.amount
                    "nagad" -> nagad += tx.amount
                    "rocket" -> rocket += tx.amount
                    "bank" -> bank += tx.amount
                    "card" -> card += tx.amount
                }
                if (tx.date in todayRange.first..todayRange.second) {
                    todayIncome += tx.amount
                }
            } else if (tx.type == "expense") {
                totalExpense += tx.amount
                when (tx.method.lowercase()) {
                    "cash" -> cash -= tx.amount
                    "bkash" -> bkash -= tx.amount
                    "nagad" -> nagad -= tx.amount
                    "rocket" -> rocket -= tx.amount
                    "bank" -> bank -= tx.amount
                    "card" -> card -= tx.amount
                }
                if (tx.date in todayRange.first..todayRange.second) {
                    todayExpense += tx.amount
                }
            }
        }

        SummaryStats(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = totalIncome - totalExpense,
            todayIncome = todayIncome,
            todayExpense = todayExpense,
            methodBalance = MethodBalance(
                cash = cash.coerceAtLeast(0L),
                bkash = bkash.coerceAtLeast(0L),
                nagad = nagad.coerceAtLeast(0L),
                rocket = rocket.coerceAtLeast(0L),
                bank = bank.coerceAtLeast(0L),
                card = card.coerceAtLeast(0L)
            )
        )
    }

    init {
        viewModelScope.launch {
            val uid = userSettings.value.userId
            repository.seedInitialDataIfEmpty(uid)
        }
    }

    fun addTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(tx)
            _syncState.value = SyncState.PENDING
            if (tx.type == "expense") {
                checkCategoryBudgetUtilization(tx.category)
            }
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
            _syncState.value = SyncState.PENDING
            if (tx.type == "expense") {
                checkCategoryBudgetUtilization(tx.category)
            }
        }
    }

    private fun checkCategoryBudgetUtilization(categoryKey: String) {
        viewModelScope.launch {
            val budgets = categoryBudgets.value
            val budgetEntity = budgets.firstOrNull { it.categoryKey.equals(categoryKey, ignoreCase = true) }
            if (budgetEntity != null && budgetEntity.monthlyBudget > 0L) {
                val thisMonth = DateHelper.getThisMonthStartAndEnd()
                val monthExpenses = allTransactions.value.filter {
                    it.type == "expense" &&
                            it.category.equals(categoryKey, ignoreCase = true) &&
                            it.date in thisMonth.first..thisMonth.second
                }.sumOf { it.amount }

                val pct = (monthExpenses.toFloat() / budgetEntity.monthlyBudget.toFloat()) * 100f
                if (pct >= 80f) {
                    val catItem = allCategoryItems.value.firstOrNull { it.key.equals(categoryKey, ignoreCase = true) }
                    val label = catItem?.label ?: categoryKey
                    NotificationHelper.showCategoryBudgetWarning(
                        context = getApplication(),
                        categoryName = label,
                        percent = pct.toInt(),
                        spentPaisa = monthExpenses,
                        budgetPaisa = budgetEntity.monthlyBudget
                    )
                }
            }
        }
    }

    // Custom Categories
    fun addCustomCategory(name: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            val uid = userSettings.value.userId
            val entity = CategoryEntity(
                name = name.trim(),
                emoji = emoji.ifBlank { "🏷️" },
                colorHex = colorHex.ifBlank { "#FF6B6B" },
                userId = uid
            )
            repository.addCustomCategory(entity)
        }
    }

    fun deleteCustomCategory(id: String) {
        viewModelScope.launch {
            val uid = userSettings.value.userId
            repository.deleteCustomCategory(id)
            repository.deleteCategoryBudget(uid, id)
        }
    }

    // Category Budgets
    fun setCategoryBudget(categoryKey: String, monthlyBudgetPaisa: Long) {
        viewModelScope.launch {
            val uid = userSettings.value.userId
            val budget = CategoryBudgetEntity(
                categoryKey = categoryKey,
                userId = uid,
                monthlyBudget = monthlyBudgetPaisa
            )
            repository.setCategoryBudget(budget)
            checkCategoryBudgetUtilization(categoryKey)
        }
    }

    fun deleteCategoryBudget(categoryKey: String) {
        viewModelScope.launch {
            val uid = userSettings.value.userId
            repository.deleteCategoryBudget(uid, categoryKey)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            _lastDeletedTransaction.value = tx
            repository.softDeleteTransaction(tx.id)
            _syncState.value = SyncState.PENDING
        }
    }

    fun undoDelete() {
        val tx = _lastDeletedTransaction.value ?: return
        viewModelScope.launch {
            repository.restoreTransaction(tx.id)
            _lastDeletedTransaction.value = null
        }
    }

    fun addLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.addLoan(loan)
            _syncState.value = SyncState.PENDING
        }
    }

    fun markLoanFullyPaid(loan: LoanEntity) {
        viewModelScope.launch {
            val remaining = loan.remaining
            if (remaining > 0L) {
                repository.addLoanPayment(loan.id, remaining, "সম্পূর্ণ পরিশোধিত")
            }
            _syncState.value = SyncState.PENDING
        }
    }

    fun recordPartialPayment(loan: LoanEntity, amountPaisa: Long, note: String?) {
        viewModelScope.launch {
            repository.addLoanPayment(loan.id, amountPaisa, note)
            _syncState.value = SyncState.PENDING
        }
    }

    fun deleteLoan(loan: LoanEntity) {
        viewModelScope.launch {
            repository.deleteLoan(loan.id)
            _syncState.value = SyncState.PENDING
        }
    }

    fun deleteLoan(loanId: String) {
        viewModelScope.launch {
            repository.deleteLoan(loanId)
            _syncState.value = SyncState.PENDING
        }
    }

    fun getLoanPayments(loanId: String) = repository.getLoanPaymentsFlow(loanId)

    fun addGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.addGoal(goal)
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun addRecurring(recurring: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.addRecurring(recurring)
        }
    }

    fun updateRecurring(recurring: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.updateRecurring(recurring)
        }
    }

    fun toggleRecurringActive(id: String, active: Boolean) {
        viewModelScope.launch {
            repository.toggleRecurringActive(id, active)
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            repository.deleteRecurring(id)
        }
    }

    fun postRecurringNow(recurring: RecurringExpenseEntity, onComplete: ((TransactionEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val tx = repository.postRecurringTransaction(recurring)
            _syncState.value = SyncState.PENDING
            if (tx.type == "expense") {
                checkCategoryBudgetUtilization(tx.category)
            }
            onComplete?.invoke(tx)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _syncState.value = SyncState.PENDING
            val success = repository.syncPending()
            _syncState.value = if (success) SyncState.SYNCED else SyncState.OFFLINE
        }
    }

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateDarkMode(enabled)
        }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateNightReminder(userSettings.value.nightReminderTime, enabled)
        }
    }

    fun updateMonthlyBudget(budgetPaisa: Long) {
        viewModelScope.launch {
            userPreferences.updateMonthlyBudget(budgetPaisa)
        }
    }

    fun updateMorningReminder(time: String, enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateMorningReminder(time, enabled)
        }
    }

    fun updateNightReminder(time: String, enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.updateNightReminder(time, enabled)
        }
    }

    fun updateUserProfile(id: String, name: String, email: String) {
        viewModelScope.launch {
            userPreferences.updateUserData(id, name, email, true)
        }
    }

    fun loginDemoUser(name: String, email: String) {
        viewModelScope.launch {
            userPreferences.updateUserData("user_${System.currentTimeMillis()}", name, email, true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.updateUserData("default_user", "ইউজার", "user@hisabboi.app", false)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllLocalData(userSettings.value.userId)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(userSettings.value.userId)
        }
    }
}

private data class FilterBundle(
    val list: List<TransactionEntity>,
    val query: String,
    val type: String,
    val dFilter: DateFilterType,
    val customRange: Pair<Long, Long>?
)

// Helper extension for mapping StateFlow
inline fun <T, R> StateFlow<T>.mapState(
    scope: kotlinx.coroutines.CoroutineScope,
    crossinline transform: (T) -> R
): StateFlow<R> {
    return this.map { transform(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), transform(value))
}
