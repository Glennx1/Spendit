package com.spendsplit.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spendsplit.app.data.local.dao.CategoryDao
import com.spendsplit.app.data.local.dao.PersonDao
import com.spendsplit.app.data.local.dao.TransactionDao
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.PersonEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        PersonEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SpendSplitDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun personDao(): PersonDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: SpendSplitDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SpendSplitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpendSplitDatabase::class.java,
                    "spendsplit_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }

            private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Food", colorHex = "#FF5722", iconName = "Food", isDefault = true),
                    CategoryEntity(name = "Travel", colorHex = "#2196F3", iconName = "Travel", isDefault = true),
                    CategoryEntity(name = "Rent", colorHex = "#9C27B0", iconName = "Rent", isDefault = true),
                    CategoryEntity(name = "Utilities", colorHex = "#FF9800", iconName = "Utilities", isDefault = true),
                    CategoryEntity(name = "Shopping", colorHex = "#E91E63", iconName = "Shopping", isDefault = true),
                    CategoryEntity(name = "Entertainment", colorHex = "#673AB7", iconName = "Entertainment", isDefault = true),
                    CategoryEntity(name = "Subscriptions", colorHex = "#009688", iconName = "Subscriptions", isDefault = true),
                    CategoryEntity(name = "Other", colorHex = "#607D8B", iconName = "Other", isDefault = true)
                )
                categoryDao.insertAll(defaultCategories)
            }
        }
    }
}
