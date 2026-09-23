package com.spendsplit.app.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsplit.app.data.local.entity.TransactionEntity
import com.spendsplit.app.ui.components.CategoryIconBadge
import com.spendsplit.app.ui.components.CurrencyText
import com.spendsplit.app.ui.components.CurrencyUtils
import com.spendsplit.app.ui.components.DonutChart
import com.spendsplit.app.ui.components.SpendOverTimeChart
import com.spendsplit.app.ui.theme.AccentBlack
import com.spendsplit.app.ui.theme.BeigeBackground
import com.spendsplit.app.ui.theme.CardBorder
import com.spendsplit.app.ui.theme.CardBorderSubtle
import com.spendsplit.app.ui.theme.CharcoalSecondary
import com.spendsplit.app.ui.theme.GreenPositive
import com.spendsplit.app.ui.theme.GreenPositiveBg
import com.spendsplit.app.ui.theme.MutedSurface
import com.spendsplit.app.ui.theme.ObsidianBlack
import com.spendsplit.app.ui.theme.RedNegative
import com.spendsplit.app.ui.theme.RedNegativeBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenSettings: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "SpendSplit",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MutedSurface,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = currency,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = ObsidianBlack)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ObsidianBlack)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeigeBackground
                )
            )
        },
        containerColor = BeigeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expandable Search Bar
            if (isSearchVisible) {
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Search transactions, notes, people...", color = CharcoalSecondary.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ObsidianBlack) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = CharcoalSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObsidianBlack,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }

            // Minimalist Spend Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "TOTAL SPENT THIS MONTH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CharcoalSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        CurrencyText(
                            amount = uiState.thisMonthSpent,
                            currency = currency,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Delta Comparison Pill
                        val delta = uiState.monthlyDeltaPercent
                        if (delta != null) {
                            val isHigher = delta > 0
                            val deltaColor = if (isHigher) RedNegative else GreenPositive
                            val deltaBg = if (isHigher) RedNegativeBg else GreenPositiveBg
                            Surface(
                                shape = CircleShape,
                                color = deltaBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isHigher) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = deltaColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", kotlin.math.abs(delta))}% vs last month (${CurrencyUtils.formatAmount(uiState.prevMonthSpent, currency)})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = deltaColor
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Previous month: ${CurrencyUtils.formatAmount(uiState.prevMonthSpent, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalSecondary
                            )
                        }
                    }
                }
            }

            // Quick Date Range Filter Pills
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilter.values().forEach { filter ->
                        val isSelected = uiState.selectedDateFilter == filter
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) AccentBlack else MutedSurface,
                            border = BorderStroke(1.dp, if (isSelected) AccentBlack else CardBorder),
                            modifier = Modifier.clickable { viewModel.selectDateFilter(filter) }
                        ) {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else ObsidianBlack
                                ),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Category Spend Card (Donut Chart)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spend by Category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (uiState.selectedCategoryId != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = MutedSurface,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.clickable { viewModel.selectCategory(null) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Clear filter", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (uiState.categorySpendItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No category spends recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CharcoalSecondary
                                )
                            }
                        } else {
                            DonutChart(
                                items = uiState.categorySpendItems,
                                currency = currency,
                                selectedCategoryId = uiState.selectedCategoryId,
                                onCategoryClick = viewModel::selectCategory
                            )
                        }
                    }
                }
            }

            // Spend Over Time Card (Bar Chart)
            item {
                SpendOverTimeChart(
                    dataPoints = uiState.timeChartPoints,
                    selectedPeriod = uiState.selectedTimePeriod,
                    onPeriodSelected = viewModel::selectTimePeriod,
                    currency = currency
                )
            }

            // Recurring Spends Card
            if (uiState.recurringSpends.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Repeat, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recurring Spends",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.recurringSpends) { rec ->
                                    val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MutedSurface.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, CardBorder),
                                        modifier = Modifier.width(160.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CategoryIconBadge(
                                                    iconName = rec.iconName,
                                                    colorHex = rec.categoryColorHex,
                                                    size = 26.dp,
                                                    iconSize = 14.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = rec.description,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Avg: ${CurrencyUtils.formatAmount(rec.averageAmount, currency)}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                                            )
                                            Text(
                                                text = "${rec.count} entries • Last ${dateFmt.format(Date(rec.lastDateEpoch))}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CharcoalSecondary)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions (${uiState.filteredTransactions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Transactions List
            if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CharcoalSecondary
                        )
                    }
                }
            } else {
                items(uiState.filteredTransactions, key = { it.id }) { transaction ->
                    val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val dateFormatted = dateFmt.format(Date(transaction.date))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(
                                iconName = transaction.categoryName,
                                colorHex = "#18181B",
                                size = 40.dp,
                                iconSize = 20.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = transaction.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$dateFormatted • ${transaction.time}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CharcoalSecondary
                                    )
                                    if (transaction.personName != null) {
                                        Text(
                                            text = " • with ${transaction.personName}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                            color = ObsidianBlack
                                        )
                                    }
                                    if (transaction.isRecurring) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            Icons.Default.Repeat,
                                            contentDescription = "Recurring",
                                            modifier = Modifier.size(11.dp),
                                            tint = CharcoalSecondary
                                        )
                                    }
                                }
                                if (!transaction.splitDetails.isNullOrBlank()) {
                                    Text(
                                        text = transaction.splitDetails,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = CharcoalSecondary),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Amount & Type Tag
                            Column(horizontalAlignment = Alignment.End) {
                                val displayAmount = when (transaction.type) {
                                    "split" -> transaction.myShare ?: transaction.amount
                                    else -> transaction.amount
                                }
                                CurrencyText(
                                    amount = displayAmount,
                                    currency = currency,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                                )

                                val typeLabel = when (transaction.type) {
                                    "split" -> "Split (My Share)"
                                    "owed_to_me" -> "They Owe"
                                    "i_owe" -> "I Owe"
                                    "settlement" -> "Settled"
                                    else -> "Personal"
                                }
                                Text(
                                    text = typeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = when (transaction.type) {
                                        "owed_to_me" -> GreenPositive
                                        "i_owe" -> RedNegative
                                        else -> CharcoalSecondary
                                    }
                                )
                            }

                            IconButton(
                                onClick = { transactionToDelete = transaction },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CharcoalSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Delete Entry?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("Are you sure you want to remove '${tx.description}'? If this was a split bill, all associated shares will also be removed.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("Delete", color = RedNegative, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel", color = CharcoalSecondary)
                }
            }
        )
    }
}
