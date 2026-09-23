package com.spendsplit.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.PersonEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TransactionType(val label: String) {
    JUST_MY_SPEND("Just my spend"),
    SOMEONE_PAID_FOR_ME("Someone paid for me"),
    I_PAID_FOR_SOMEONE("I paid for someone"),
    SPLIT_EXPENSE("Split expense")
}

data class SplitParticipant(
    val person: PersonEntity,
    val shareText: String
)

data class AddTransactionUiState(
    val amountText: String = "",
    val totalAmountText: String = "",
    val myShareText: String = "",
    val splitParticipants: List<SplitParticipant> = emptyList(),
    val isSplitEqually: Boolean = true,
    val description: String = "",
    val suggestions: List<String> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val selectedPerson: PersonEntity? = null,
    val dateEpoch: Long = System.currentTimeMillis(),
    val timeFormatted: String = "",
    val transactionType: TransactionType = TransactionType.JUST_MY_SPEND,
    val isRecurring: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class AddTransactionViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val persons: StateFlow<List<PersonEntity>> = repository.getAllPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currency: StateFlow<String> = repository.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        _uiState.value = _uiState.value.copy(
            timeFormatted = timeFormat.format(calendar.time)
        )
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amountText = amount)
    }

    fun onTotalAmountChanged(total: String) {
        _uiState.value = _uiState.value.copy(totalAmountText = total)
        if (_uiState.value.isSplitEqually) {
            recalculateEqualShares(total, _uiState.value.splitParticipants)
        }
    }

    fun onMyShareChanged(myShare: String) {
        _uiState.value = _uiState.value.copy(myShareText = myShare, isSplitEqually = false)
    }

    fun onParticipantShareChanged(personId: Long, shareText: String) {
        val updated = _uiState.value.splitParticipants.map {
            if (it.person.id == personId) it.copy(shareText = shareText) else it
        }
        _uiState.value = _uiState.value.copy(splitParticipants = updated, isSplitEqually = false)
    }

    fun addSplitParticipant(person: PersonEntity) {
        val current = _uiState.value.splitParticipants
        if (current.any { it.person.id == person.id }) return

        val newParticipants = current + SplitParticipant(person = person, shareText = "")
        _uiState.value = _uiState.value.copy(splitParticipants = newParticipants)

        if (_uiState.value.isSplitEqually) {
            recalculateEqualShares(_uiState.value.totalAmountText, newParticipants)
        }
    }

    fun removeSplitParticipant(personId: Long) {
        val newParticipants = _uiState.value.splitParticipants.filterNot { it.person.id == personId }
        _uiState.value = _uiState.value.copy(splitParticipants = newParticipants)

        if (_uiState.value.isSplitEqually) {
            recalculateEqualShares(_uiState.value.totalAmountText, newParticipants)
        }
    }

    fun toggleSplitEqually(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isSplitEqually = enabled)
        if (enabled) {
            recalculateEqualShares(_uiState.value.totalAmountText, _uiState.value.splitParticipants)
        }
    }

    fun allocateRemainingToMe() {
        val total = _uiState.value.totalAmountText.toDoubleOrNull() ?: return
        val othersTotal = _uiState.value.splitParticipants.sumOf { it.shareText.toDoubleOrNull() ?: 0.0 }
        val remaining = (total - othersTotal).coerceAtLeast(0.0)
        _uiState.value = _uiState.value.copy(
            myShareText = String.format(Locale.US, "%.2f", remaining),
            isSplitEqually = false
        )
    }

    private fun recalculateEqualShares(totalStr: String, participants: List<SplitParticipant>) {
        val total = totalStr.toDoubleOrNull() ?: 0.0
        val totalPeople = participants.size + 1 // Everyone + You
        if (total > 0 && totalPeople > 0) {
            val share = total / totalPeople
            val shareFormatted = String.format(Locale.US, "%.2f", share)
            val updatedParticipants = participants.map { it.copy(shareText = shareFormatted) }
            _uiState.value = _uiState.value.copy(
                myShareText = shareFormatted,
                splitParticipants = updatedParticipants
            )
        }
    }

    fun onDescriptionChanged(query: String) {
        _uiState.value = _uiState.value.copy(description = query)
        if (query.trim().length >= 2) {
            viewModelScope.launch {
                val matches = repository.searchPastDescriptions(query.trim())
                _uiState.value = _uiState.value.copy(suggestions = matches)
            }
        } else {
            _uiState.value = _uiState.value.copy(suggestions = emptyList())
        }
    }

    fun onSuggestionSelected(suggestion: String) {
        viewModelScope.launch {
            val recent = repository.getRecentTransactionByDescription(suggestion)
            val pastCount = repository.countByDescription(suggestion)
            val category = categories.value.firstOrNull { it.id == recent?.categoryId }

            _uiState.value = _uiState.value.copy(
                description = suggestion,
                suggestions = emptyList(),
                amountText = recent?.amount?.let { String.format(Locale.US, "%.2f", it) } ?: _uiState.value.amountText,
                selectedCategory = category ?: _uiState.value.selectedCategory,
                isRecurring = pastCount >= 2
            )
        }
    }

    fun onCategorySelected(category: CategoryEntity) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onPersonSelected(person: PersonEntity) {
        _uiState.value = _uiState.value.copy(selectedPerson = person)
    }

    fun onTransactionTypeSelected(type: TransactionType) {
        _uiState.value = _uiState.value.copy(transactionType = type)
    }

    fun onDateSelected(epoch: Long) {
        _uiState.value = _uiState.value.copy(dateEpoch = epoch)
    }

    fun onTimeSelected(timeFormatted: String) {
        _uiState.value = _uiState.value.copy(timeFormatted = timeFormatted)
    }

    fun onRecurringToggled(isRecurring: Boolean) {
        _uiState.value = _uiState.value.copy(isRecurring = isRecurring)
    }

    fun createNewCategoryInline(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val newId = repository.insertCategory(
                CategoryEntity(
                    name = name,
                    colorHex = colorHex,
                    iconName = iconName,
                    isDefault = false
                )
            )
            val newCategory = CategoryEntity(id = newId, name = name, colorHex = colorHex, iconName = iconName)
            _uiState.value = _uiState.value.copy(selectedCategory = newCategory)
        }
    }

    fun createNewPersonInline(name: String, addToSplit: Boolean = false) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.getOrCreatePerson(name)
            val newPerson = PersonEntity(id = newId, name = name.trim())
            if (addToSplit) {
                addSplitParticipant(newPerson)
            } else {
                _uiState.value = _uiState.value.copy(selectedPerson = newPerson)
            }
        }
    }

    fun saveTransaction() {
        val state = _uiState.value

        val category = state.selectedCategory ?: categories.value.firstOrNull()
        if (category == null) {
            _uiState.value = state.copy(errorMessage = "Please select or create a category")
            return
        }

        when (state.transactionType) {
            TransactionType.JUST_MY_SPEND -> {
                val amount = state.amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = state.copy(errorMessage = "Please enter a valid amount")
                    return
                }

                saveInternal(
                    TransactionEntity(
                        amount = amount,
                        date = state.dateEpoch,
                        time = state.timeFormatted,
                        categoryId = category.id,
                        categoryName = category.name,
                        description = state.description.ifBlank { category.name },
                        type = "personal",
                        isRecurring = state.isRecurring
                    )
                )
            }

            TransactionType.SOMEONE_PAID_FOR_ME -> {
                val person = state.selectedPerson
                if (person == null) {
                    _uiState.value = state.copy(errorMessage = "Please select who paid for you")
                    return
                }
                val amount = state.amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = state.copy(errorMessage = "Please enter a valid amount")
                    return
                }

                saveInternal(
                    TransactionEntity(
                        amount = amount,
                        date = state.dateEpoch,
                        time = state.timeFormatted,
                        categoryId = category.id,
                        categoryName = category.name,
                        description = state.description.ifBlank { "${person.name} paid for me" },
                        type = "i_owe",
                        personId = person.id,
                        personName = person.name,
                        isRecurring = state.isRecurring
                    )
                )
            }

            TransactionType.I_PAID_FOR_SOMEONE -> {
                val person = state.selectedPerson
                if (person == null) {
                    _uiState.value = state.copy(errorMessage = "Please select who you paid for")
                    return
                }
                val amount = state.amountText.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.value = state.copy(errorMessage = "Please enter a valid amount")
                    return
                }

                saveInternal(
                    TransactionEntity(
                        amount = amount,
                        date = state.dateEpoch,
                        time = state.timeFormatted,
                        categoryId = category.id,
                        categoryName = category.name,
                        description = state.description.ifBlank { "Paid for ${person.name}" },
                        type = "owed_to_me",
                        personId = person.id,
                        personName = person.name,
                        isRecurring = state.isRecurring
                    )
                )
            }

            TransactionType.SPLIT_EXPENSE -> {
                val totalAmount = state.totalAmountText.toDoubleOrNull()
                if (totalAmount == null || totalAmount <= 0) {
                    _uiState.value = state.copy(errorMessage = "Please enter total bill amount")
                    return
                }

                if (state.splitParticipants.isEmpty()) {
                    _uiState.value = state.copy(errorMessage = "Please add at least one person to split with")
                    return
                }

                val myShare = state.myShareText.toDoubleOrNull()
                if (myShare == null || myShare < 0) {
                    _uiState.value = state.copy(errorMessage = "Please enter a valid share for yourself")
                    return
                }

                val participantPairs = mutableListOf<Pair<PersonEntity, Double>>()
                for (p in state.splitParticipants) {
                    val share = p.shareText.toDoubleOrNull()
                    if (share == null || share <= 0) {
                        _uiState.value = state.copy(errorMessage = "Please enter a valid share for ${p.person.name}")
                        return
                    }
                    participantPairs.add(p.person to share)
                }

                val sumOfShares = myShare + participantPairs.sumOf { it.second }
                // Allow a small penny difference due to equal split division
                if (kotlin.math.abs(sumOfShares - totalAmount) > 0.08) {
                    _uiState.value = state.copy(
                        errorMessage = "Shares sum (${String.format(Locale.US, "%.2f", sumOfShares)}) must equal Total Bill (${String.format(Locale.US, "%.2f", totalAmount)})"
                    )
                    return
                }

                viewModelScope.launch {
                    _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
                    repository.insertMultiPersonSplit(
                        totalAmount = totalAmount,
                        myShare = myShare,
                        participants = participantPairs,
                        dateEpoch = state.dateEpoch,
                        timeFormatted = state.timeFormatted,
                        categoryId = category.id,
                        categoryName = category.name,
                        description = state.description.ifBlank { "Split bill" },
                        isRecurring = state.isRecurring
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _saveSuccessEvent.emit(Unit)
                    resetForm()
                }
            }
        }
    }

    private fun saveInternal(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            repository.insertTransaction(transaction)
            _uiState.value = _uiState.value.copy(isSaving = false)
            _saveSuccessEvent.emit(Unit)
            resetForm()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun resetForm() {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        _uiState.value = AddTransactionUiState(
            timeFormatted = timeFormat.format(calendar.time),
            selectedCategory = categories.value.firstOrNull()
        )
    }
}

class AddTransactionViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddTransactionViewModel(repository) as T
    }
}
