package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CategoryBudgetDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.LoanDao
import com.example.data.local.dao.RecurringDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.CategoryBudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.LoanEntity
import com.example.data.local.entity.LoanPaymentEntity
import com.example.data.local.entity.PersonAliasEntity
import com.example.data.local.entity.RecurringExpenseEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        GoalEntity::class,
        RecurringExpenseEntity::class,
        PersonAliasEntity::class,
        CategoryEntity::class,
        CategoryBudgetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun loanDao(): LoanDao
    abstract fun goalDao(): GoalDao
    abstract fun recurringDao(): RecurringDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisabboi_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): AppDatabase = getDatabase(context)
    }
}

