package com.spendsplit.app.ui.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsplit.app.data.local.entity.PersonEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.data.repository.FinanceRepository
import com.spendsplit.app.data.repository.IouSummary
import com.spendsplit.app.data.repository.PersonWithBalance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PeopleViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val currency: StateFlow<String> = repository.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val peopleWithBalances: StateFlow<List<PersonWithBalance>> = repository.getPersonsWithBalances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val iouSummary: StateFlow<IouSummary> = repository.getIouSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IouSummary(0.0, 0.0, 0.0))

    private val _selectedPerson = MutableStateFlow<PersonWithBalance?>(null)
    val selectedPerson: StateFlow<PersonWithBalance?> = _selectedPerson.asStateFlow()

    val selectedPersonTransactions: StateFlow<List<TransactionEntity>> = _selectedPerson
        .flatMapLatest { personWithBalance ->
            if (personWithBalance != null) {
                repository.getTransactionsByPerson(personWithBalance.person.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settleSuccessEvent = MutableSharedFlow<String>()
    val settleSuccessEvent: SharedFlow<String> = _settleSuccessEvent.asSharedFlow()

    fun selectPerson(personWithBalance: PersonWithBalance?) {
        _selectedPerson.value = personWithBalance
    }

    fun addPerson(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.getOrCreatePerson(name)
        }
    }

    fun settleUp(person: PersonEntity, currentBalance: Double) {
        viewModelScope.launch {
            repository.settleUp(person, currentBalance)
            _settleSuccessEvent.emit("Settled up with ${person.name}!")
            // Update selected person balance to 0
            _selectedPerson.value = _selectedPerson.value?.copy(balance = 0.0)
        }
    }

    fun deletePerson(person: PersonEntity) {
        viewModelScope.launch {
            repository.deletePerson(person)
            if (_selectedPerson.value?.person?.id == person.id) {
                _selectedPerson.value = null
            }
        }
    }
}

class PeopleViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PeopleViewModel(repository) as T
    }
}
