package com.spendsplit.app

import android.app.Application
import com.spendsplit.app.data.local.SpendSplitDatabase
import com.spendsplit.app.data.preferences.UserPreferences
import com.spendsplit.app.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SpendSplitApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { SpendSplitDatabase.getDatabase(this, applicationScope) }
    val userPreferences by lazy { UserPreferences(this) }
    val repository by lazy {
        FinanceRepository(
            transactionDao = database.transactionDao(),
            personDao = database.personDao(),
            categoryDao = database.categoryDao(),
            userPreferences = userPreferences
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SpendSplitApp
            private set
    }
}
