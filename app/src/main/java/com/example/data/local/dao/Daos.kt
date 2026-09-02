package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND deleted = 0 ORDER BY date DESC")
    fun getAllFlow(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND deleted = 0 AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getInRangeFlow(userId: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND deleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getRecentFlow(userId: String, limit: Int = 5): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND synced = 0")
    suspend fun getUnsynced(userId: String): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("UPDATE transactions SET deleted = 1, synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    @Query("UPDATE transactions SET deleted = 0, synced = 0 WHERE id = :id")
    suspend fun restore(id: String)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDelete(id: String)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE userId = :userId AND status != 'deleted' ORDER BY updatedAt DESC")
    fun getAllFlow(userId: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE userId = :userId AND direction = :direction AND status != 'deleted' ORDER BY updatedAt DESC")
    fun getByDirectionFlow(userId: String, direction: String): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): LoanEntity?

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<LoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: LoanEntity)

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("UPDATE loans SET status = 'deleted', synced = 0 WHERE id = :id")
    suspend fun softDelete(id: String)

    // Payments
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY date DESC")
    fun getPaymentsFlow(loanId: String): Flow<List<LoanPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: LoanPaymentEntity)

    @Query("DELETE FROM loan_payments WHERE id = :id")
    suspend fun deletePayment(id: String)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId ORDER BY dayOfMonth ASC, createdAt DESC")
    fun getAllFlow(userId: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId AND type = :type ORDER BY dayOfMonth ASC")
    fun getByTypeFlow(userId: String, type: String): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RecurringExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringExpenseEntity)

    @Update
    suspend fun update(recurring: RecurringExpenseEntity)

    @Query("UPDATE recurring_expenses SET active = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM custom_categories WHERE userId = :userId ORDER BY createdAt ASC")
    fun getAllFlow(userId: String): Flow<List<com.example.data.local.entity.CategoryEntity>>

    @Query("SELECT * FROM custom_categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): com.example.data.local.entity.CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: com.example.data.local.entity.CategoryEntity)

    @Update
    suspend fun update(category: com.example.data.local.entity.CategoryEntity)

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets WHERE userId = :userId")
    fun getAllFlow(userId: String): Flow<List<com.example.data.local.entity.CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budgets WHERE userId = :userId AND categoryKey = :categoryKey LIMIT 1")
    suspend fun getByCategory(userId: String, categoryKey: String): com.example.data.local.entity.CategoryBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: com.example.data.local.entity.CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE userId = :userId AND categoryKey = :categoryKey")
    suspend fun deleteBudget(userId: String, categoryKey: String)
}

