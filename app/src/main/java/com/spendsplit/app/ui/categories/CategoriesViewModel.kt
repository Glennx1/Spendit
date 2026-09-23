package com.spendsplit.app.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryWithSpend(
    val category: CategoryEntity,
    val thisMonthSpend: Double,
    val allTimeSpend: Double
)

class CategoriesViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val currency: StateFlow<String> = repository.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val categoriesWithSpend: StateFlow<List<CategoryWithSpend>> = combine(
        repository.getAllCategories(),
        repository.getAllTransactions()
    ) { categories, transactions ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfThisMonth = cal.timeInMillis

        // Personal spends (type personal or split my share)
        val personalSpends = transactions.filter { it.type == "personal" || it.type == "split" }

        categories.map { cat ->
            val catSpends = personalSpends.filter { it.categoryId == cat.id }
            val allTime = catSpends.sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }
            val thisMonth = catSpends
                .filter { it.date >= startOfThisMonth }
                .sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

            CategoryWithSpend(
                category = cat,
                thisMonthSpend = thisMonth,
                allTimeSpend = allTime
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _messageEvent = MutableSharedFlow<String>()
    val messageEvent: SharedFlow<String> = _messageEvent.asSharedFlow()

    fun addCategory(name: String, colorHex: String, iconName: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    isDefault = false
                )
            )
            _messageEvent.emit("Category '$name' created")
        }
    }

    fun updateCategory(category: CategoryEntity, name: String, colorHex: String, iconName: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateCategory(
                category.copy(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
            _messageEvent.emit("Category updated")
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (category.isDefault) {
            viewModelScope.launch {
                _messageEvent.emit("Default categories cannot be deleted")
            }
            return
        }
        viewModelScope.launch {
            repository.deleteCategory(category)
            _messageEvent.emit("Category '${category.name}' deleted")
        }
    }
}

class CategoriesViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CategoriesViewModel(repository) as T
    }
}
