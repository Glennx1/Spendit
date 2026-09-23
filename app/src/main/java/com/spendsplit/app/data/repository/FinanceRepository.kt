package com.spendsplit.app.data.repository

import com.spendsplit.app.data.local.dao.CategoryDao
import com.spendsplit.app.data.local.dao.PersonDao
import com.spendsplit.app.data.local.dao.TransactionDao
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.PersonEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

data class PersonWithBalance(
    val person: PersonEntity,
    val balance: Double // Positive = they owe me, Negative = I owe them
)

data class IouSummary(
    val totalOwedToMe: Double,
    val totalIOwe: Double,
    val netBalance: Double
)

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val personDao: PersonDao,
    private val categoryDao: CategoryDao,
    private val userPreferences: UserPreferences
) {
    // Preferences
    val currencyFlow: Flow<String> = userPreferences.currencyFlow
    val themeFlow: Flow<String> = userPreferences.themeFlow

    suspend fun setCurrency(currency: String) = userPreferences.setCurrency(currency)
    suspend fun setTheme(theme: String) = userPreferences.setTheme(theme)

    // Transactions
    fun getAllTransactions(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByPerson(personId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByPerson(personId)

    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(categoryId)

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsBetween(startTime, endTime)

    fun getRecurringTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getRecurringTransactions()

    suspend fun searchPastDescriptions(query: String): List<String> =
        transactionDao.searchPastDescriptions(query)

    suspend fun getRecentTransactionByDescription(description: String): TransactionEntity? =
        transactionDao.getRecentTransactionByDescription(description)

    suspend fun countByDescription(description: String): Int =
        transactionDao.countByDescription(description)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insert(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.update(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.delete(transaction)

    suspend fun deleteTransactionById(id: Long) =
        transactionDao.deleteById(id)

    // Persons & Balances
    fun getAllPersons(): Flow<List<PersonEntity>> = personDao.getAllPersons()

    fun getPersonsWithBalances(): Flow<List<PersonWithBalance>> {
        return combine(personDao.getAllPersons(), transactionDao.getAllTransactions()) { persons, transactions ->
            persons.map { person ->
                val personTransactions = transactions.filter { it.personId == person.id }
                val balance = calculateBalance(personTransactions)
                PersonWithBalance(person, balance)
            }
        }
    }

    fun getIouSummary(): Flow<IouSummary> {
        return getPersonsWithBalances().map { list ->
            var owedToMe = 0.0
            var iOwe = 0.0
            for (item in list) {
                if (item.balance > 0) {
                    owedToMe += item.balance
                } else if (item.balance < 0) {
                    iOwe += kotlin.math.abs(item.balance)
                }
            }
            IouSummary(
                totalOwedToMe = owedToMe,
                totalIOwe = iOwe,
                netBalance = owedToMe - iOwe
            )
        }
    }

    suspend fun getOrCreatePerson(name: String): Long {
        val trimmed = name.trim()
        val existing = personDao.getPersonByName(trimmed)
        return existing?.id ?: personDao.insert(PersonEntity(name = trimmed))
    }

    suspend fun deletePerson(person: PersonEntity) {
        personDao.delete(person)
    }

    suspend fun settleUp(person: PersonEntity, currentBalance: Double): Long {
        if (currentBalance == 0.0) return -1
        // If currentBalance > 0 (they owe me 100), delta is -100 to bring to 0.
        // If currentBalance < 0 (I owe them 50), delta is +50 to bring to 0.
        val delta = -currentBalance
        val date = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val timeFormatted = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))

        // Get 'Other' category or default
        val otherCategory = categoryDao.getCategoryByName("Other")
        val categoryId = otherCategory?.id ?: 1L
        val categoryName = otherCategory?.name ?: "Other"

        val settlementTransaction = TransactionEntity(
            amount = kotlin.math.abs(currentBalance),
            date = date,
            time = timeFormatted,
            categoryId = categoryId,
            categoryName = categoryName,
            description = "Settlement with ${person.name}",
            type = "settlement",
            theirShare = delta,
            personId = person.id,
            personName = person.name,
            isRecurring = false
        )
        return transactionDao.insert(settlementTransaction)
    }

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)

    suspend fun deleteCategory(category: CategoryEntity) {
        // Reassign existing transactions with this category to 'Other'
        val otherCategory = categoryDao.getCategoryByName("Other")
        if (otherCategory != null) {
            transactionDao.reassignCategory(category.id, otherCategory.id, otherCategory.name)
        }
        categoryDao.delete(category)
    }

    companion object {
        fun calculateBalance(transactions: List<TransactionEntity>): Double {
            var balance = 0.0
            for (t in transactions) {
                when (t.type) {
                    "owed_to_me" -> balance += t.amount
                    "i_owe" -> balance -= t.amount
                    "split" -> balance += (t.theirShare ?: 0.0)
                    "settlement" -> balance += (t.theirShare ?: 0.0)
                }
            }
            return balance
        }
    }
}
