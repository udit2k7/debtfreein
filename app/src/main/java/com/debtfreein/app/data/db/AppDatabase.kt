package com.debtfreein.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.debtfreein.app.data.dao.CardDao
import com.debtfreein.app.data.dao.ExpenseDao
import com.debtfreein.app.data.dao.InvestmentDao
import com.debtfreein.app.data.dao.SystemLogDao
import com.debtfreein.app.data.dao.TokenSpendDao
import com.debtfreein.app.data.model.CreditCard
import com.debtfreein.app.data.model.Expense
import com.debtfreein.app.data.model.Investment
import com.debtfreein.app.data.model.SystemLog
import com.debtfreein.app.data.model.TokenSpend
import com.debtfreein.app.data.logging.FileLogger

@Database(
    entities = [CreditCard::class, Expense::class, Investment::class, SystemLog::class, TokenSpend::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun systemLogDao(): SystemLogDao
    abstract fun tokenSpendDao(): TokenSpendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                FileLogger.log("APP_DATABASE", "Initializing Room database instance: debt_free_database", context)
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "debt_free_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
