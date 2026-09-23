package com.spendsplit.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.data.repository.FinanceRepository
import com.spendsplit.app.ui.components.BarChartDataPoint
import com.spendsplit.app.ui.components.CategorySpendItem
import com.spendsplit.app.ui.components.TimePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RecurringSpendSummary(
    val description: String,
    val averageAmount: Double,
    val count: Int,
    val lastDateEpoch: Long,
    val categoryName: String,
    val categoryColorHex: String,
    val iconName: String
)

enum class DateFilter(val label: String) {
    THIS_MONTH("This Month"),
    LAST_30_DAYS("Last 30 Days"),
    ALL_TIME("All Time")
}

data class DashboardUiState(
    val thisMonthSpent: Double = 0.0,
    val prevMonthSpent: Double = 0.0,
    val monthlyDeltaPercent: Double? = null,
    val categorySpendItems: List<CategorySpendItem> = emptyList(),
    val timeChartPoints: List<BarChartDataPoint> = emptyList(),
    val recurringSpends: List<RecurringSpendSummary> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedTimePeriod: TimePeriod = TimePeriod.DAY,
    val selectedDateFilter: DateFilter = DateFilter.THIS_MONTH,
    val searchQuery: String = ""
)

class DashboardViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val currency: StateFlow<String> = repository.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "₹")

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    private val _selectedTimePeriod = MutableStateFlow(TimePeriod.DAY)
    private val _selectedDateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    private val _searchQuery = MutableStateFlow("")

    private data class FilterConfig(
        val categoryId: Long?,
        val timePeriod: TimePeriod,
        val dateFilter: DateFilter,
        val query: String
    )

    private val filterConfig = combine(
        _selectedCategoryId,
        _selectedTimePeriod,
        _selectedDateFilter,
        _searchQuery
    ) { categoryId, timePeriod, dateFilter, query ->
        FilterConfig(categoryId, timePeriod, dateFilter, query)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllTransactions(),
        repository.getAllCategories(),
        filterConfig
    ) { transactions, categories, config ->
        calculateDashboardState(
            transactions = transactions,
            categories = categories,
            selectedCatId = config.categoryId,
            timePeriod = config.timePeriod,
            dateFilter = config.dateFilter,
            query = config.query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun selectTimePeriod(period: TimePeriod) {
        _selectedTimePeriod.value = period
    }

    fun selectDateFilter(filter: DateFilter) {
        _selectedDateFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    private fun calculateDashboardState(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        selectedCatId: Long?,
        timePeriod: TimePeriod,
        dateFilter: DateFilter,
        query: String
    ): DashboardUiState {
        val now = Calendar.getInstance()

        // Filter transactions representing personal expenditure
        // personal spend = type "personal" (amount) OR type "split" (myShare)
        val personalSpends = transactions.filter {
            it.type == "personal" || it.type == "split"
        }

        // Current Month Range
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfThisMonth = cal.timeInMillis

        // Prev Month Range
        val calPrev = Calendar.getInstance()
        calPrev.timeInMillis = startOfThisMonth
        calPrev.add(Calendar.MONTH, -1)
        val startOfPrevMonth = calPrev.timeInMillis
        val endOfPrevMonth = startOfThisMonth - 1

        val thisMonthTotal = personalSpends
            .filter { it.date >= startOfThisMonth }
            .sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

        val prevMonthTotal = personalSpends
            .filter { it.date in startOfPrevMonth..endOfPrevMonth }
            .sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

        val deltaPercent = if (prevMonthTotal > 0) {
            ((thisMonthTotal - prevMonthTotal) / prevMonthTotal) * 100.0
        } else null

        // Category Breakdown (for this month or selected date filter)
        val filteredForCategoryChart = when (dateFilter) {
            DateFilter.THIS_MONTH -> personalSpends.filter { it.date >= startOfThisMonth }
            DateFilter.LAST_30_DAYS -> {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                personalSpends.filter { it.date >= thirtyDaysAgo }
            }
            DateFilter.ALL_TIME -> personalSpends
        }

        val categoryChartSpendTotal = filteredForCategoryChart.sumOf {
            if (it.type == "split") (it.myShare ?: it.amount) else it.amount
        }

        val categorySpendMap = filteredForCategoryChart.groupBy { it.categoryId }
        val categoryItems = categories.mapNotNull { cat ->
            val catSpends = categorySpendMap[cat.id] ?: emptyList()
            val sum = catSpends.sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }
            if (sum > 0) {
                val pct = if (categoryChartSpendTotal > 0) ((sum / categoryChartSpendTotal) * 100f).toFloat() else 0f
                CategorySpendItem(
                    categoryId = cat.id,
                    name = cat.name,
                    amount = sum,
                    colorHex = cat.colorHex,
                    percentage = pct
                )
            } else null
        }.sortedByDescending { it.amount }

        // Time Chart Data Points
        val chartPoints = generateTimeChartPoints(personalSpends, timePeriod)

        // Recurring Spends Summary
        val recurringList = transactions.filter { it.isRecurring }
            .groupBy { it.description.lowercase().trim() }
            .map { (_, group) ->
                val avg = group.map { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }.average()
                val latest = group.maxByOrNull { it.date } ?: group.first()
                val cat = categories.firstOrNull { it.id == latest.categoryId }
                RecurringSpendSummary(
                    description = latest.description,
                    averageAmount = avg,
                    count = group.size,
                    lastDateEpoch = latest.date,
                    categoryName = latest.categoryName,
                    categoryColorHex = cat?.colorHex ?: "#10B981",
                    iconName = cat?.iconName ?: "Other"
                )
            }.sortedByDescending { it.lastDateEpoch }

        // Transaction List filtered by DateFilter + CategoryId + SearchQuery
        val filteredList = transactions.filter { t ->
            val matchesCat = selectedCatId == null || t.categoryId == selectedCatId
            val matchesDate = when (dateFilter) {
                DateFilter.THIS_MONTH -> t.date >= startOfThisMonth
                DateFilter.LAST_30_DAYS -> t.date >= (System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000)
                DateFilter.ALL_TIME -> true
            }
            val matchesQuery = query.isBlank() ||
                    t.description.contains(query, ignoreCase = true) ||
                    t.categoryName.contains(query, ignoreCase = true) ||
                    (t.personName != null && t.personName.contains(query, ignoreCase = true))

            matchesCat && matchesDate && matchesQuery
        }

        return DashboardUiState(
            thisMonthSpent = thisMonthTotal,
            prevMonthSpent = prevMonthTotal,
            monthlyDeltaPercent = deltaPercent,
            categorySpendItems = categoryItems,
            timeChartPoints = chartPoints,
            recurringSpends = recurringList,
            filteredTransactions = filteredList,
            selectedCategoryId = selectedCatId,
            selectedTimePeriod = timePeriod,
            selectedDateFilter = dateFilter,
            searchQuery = query
        )
    }

    private fun generateTimeChartPoints(
        personalSpends: List<TransactionEntity>,
        timePeriod: TimePeriod
    ): List<BarChartDataPoint> {
        val points = mutableListOf<BarChartDataPoint>()
        when (timePeriod) {
            TimePeriod.DAY -> {
                // Last 7 days
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val fullSdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    val end = start + (24 * 60 * 60 * 1000) - 1

                    val daySpends = personalSpends.filter { it.date in start..end }
                    val amount = daySpends.sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

                    points.add(
                        BarChartDataPoint(
                            label = sdf.format(Date(start)),
                            amount = amount,
                            fullDateLabel = fullSdf.format(Date(start))
                        )
                    )
                }
            }
            TimePeriod.WEEK -> {
                // Last 4 weeks
                for (i in 3 downTo 0) {
                    val calStart = Calendar.getInstance()
                    calStart.add(Calendar.WEEK_OF_YEAR, -i)
                    calStart.set(Calendar.DAY_OF_WEEK, calStart.firstDayOfWeek)
                    calStart.set(Calendar.HOUR_OF_DAY, 0)
                    calStart.set(Calendar.MINUTE, 0)
                    val start = calStart.timeInMillis

                    val calEnd = Calendar.getInstance()
                    calEnd.timeInMillis = start
                    calEnd.add(Calendar.DAY_OF_WEEK, 6)
                    calEnd.set(Calendar.HOUR_OF_DAY, 23)
                    calEnd.set(Calendar.MINUTE, 59)
                    val end = calEnd.timeInMillis

                    val weekSpends = personalSpends.filter { it.date in start..end }
                    val amount = weekSpends.sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

                    points.add(
                        BarChartDataPoint(
                            label = "W${4 - i}",
                            amount = amount,
                            fullDateLabel = "Week ${4 - i}"
                        )
                    )
                }
            }
            TimePeriod.MONTH -> {
                // Last 6 months
                val monthSdf = SimpleDateFormat("MMM", Locale.getDefault())
                val yearMonthSdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                for (i in 5 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, -i)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    val start = cal.timeInMillis

                    val nextCal = Calendar.getInstance()
                    nextCal.timeInMillis = start
                    nextCal.add(Calendar.MONTH, 1)
                    val end = nextCal.timeInMillis - 1

                    val monthSpends = personalSpends.filter { it.date in start..end }
                    val amount = monthSpends.sumOf { if (it.type == "split") (it.myShare ?: it.amount) else it.amount }

                    points.add(
                        BarChartDataPoint(
                            label = monthSdf.format(Date(start)),
                            amount = amount,
                            fullDateLabel = yearMonthSdf.format(Date(start))
                        )
                    )
                }
            }
        }
        return points
    }
}

class DashboardViewModelFactory(
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}
