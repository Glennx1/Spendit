package com.spendsplit.app.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsplit.app.data.local.entity.CategoryEntity
import com.spendsplit.app.data.local.entity.PersonEntity
import com.spendsplit.app.ui.components.CategoryIconBadge
import com.spendsplit.app.ui.components.CategoryIcons
import com.spendsplit.app.ui.components.CurrencyUtils
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val persons by viewModel.persons.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var isAddingToSplit by remember { mutableStateOf(false) }
    var singlePersonDropdownExpanded by remember { mutableStateOf(false) }
    var addSplitPersonDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccessEvent.collect {
            snackbarHostState.showSnackbar("Transaction logged successfully")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Entry",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeigeBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BeigeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type Pill Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = uiState.transactionType == type
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) AccentBlack else MutedSurface,
                        border = BorderStroke(1.dp, if (isSelected) AccentBlack else CardBorder),
                        modifier = Modifier.clickable { viewModel.onTransactionTypeSelected(type) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CharcoalSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Amount / Multi-Person Split Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (uiState.transactionType == TransactionType.SPLIT_EXPENSE) {
                        // Header
                        Text(
                            text = "TOTAL BILL AMOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CharcoalSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.totalAmountText,
                            onValueChange = viewModel::onTotalAmountChanged,
                            placeholder = { Text("0.00", color = CharcoalSecondary.copy(alpha = 0.5f)) },
                            prefix = {
                                Text(
                                    "$currency ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObsidianBlack,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = MutedSurface.copy(alpha = 0.3f),
                                unfocusedContainerColor = MutedSurface.copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = CardBorderSubtle)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Split Mode: Equal vs Custom Pill Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Split Allocation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier
                                    .background(MutedSurface, CircleShape)
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (uiState.isSplitEqually) AccentBlack else Color.Transparent)
                                        .clickable { viewModel.toggleSplitEqually(true) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "Equally",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (uiState.isSplitEqually) FontWeight.Bold else FontWeight.Medium,
                                            color = if (uiState.isSplitEqually) Color.White else CharcoalSecondary
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (!uiState.isSplitEqually) AccentBlack else Color.Transparent)
                                        .clickable { viewModel.toggleSplitEqually(false) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "Custom",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (!uiState.isSplitEqually) FontWeight.Bold else FontWeight.Medium,
                                            color = if (!uiState.isSplitEqually) Color.White else CharcoalSecondary
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Participants list header
                        Text(
                            text = "Participants (${uiState.splitParticipants.size + 1})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CharcoalSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. "You" Participant Row
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MutedSurface.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(AccentBlack),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("You", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("You (My share)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Logs as personal spend", style = MaterialTheme.typography.labelSmall.copy(color = CharcoalSecondary, fontSize = 10.sp))
                                    }
                                }

                                OutlinedTextField(
                                    value = uiState.myShareText,
                                    onValueChange = viewModel::onMyShareChanged,
                                    placeholder = { Text("0.00") },
                                    prefix = { Text(currency, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.width(120.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ObsidianBlack,
                                        unfocusedBorderColor = CardBorder,
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2. Added Participants Rows
                        uiState.splitParticipants.forEach { participant ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MutedSurface.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MutedSurface)
                                                .border(1.dp, CardBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                participant.person.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(participant.person.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                            Text("Owes you this share", style = MaterialTheme.typography.labelSmall.copy(color = GreenPositive, fontSize = 10.sp))
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = participant.shareText,
                                            onValueChange = { viewModel.onParticipantShareChanged(participant.person.id, it) },
                                            placeholder = { Text("0.00") },
                                            prefix = { Text(currency, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.width(120.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ObsidianBlack,
                                                unfocusedBorderColor = CardBorder,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeSplitParticipant(participant.person.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = CharcoalSecondary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add Person To Split Button
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { addSplitPersonDropdownExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "+ Add Friend to Split",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = addSplitPersonDropdownExpanded,
                                onDismissRequest = { addSplitPersonDropdownExpanded = false }
                            ) {
                                val availablePersons = persons.filterNot { p ->
                                    uiState.splitParticipants.any { it.person.id == p.id }
                                }

                                if (availablePersons.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No contacts available", color = CharcoalSecondary) },
                                        onClick = {}
                                    )
                                } else {
                                    availablePersons.forEach { person ->
                                        DropdownMenuItem(
                                            text = { Text(person.name, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                viewModel.addSplitParticipant(person)
                                                addSplitPersonDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = ObsidianBlack)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("+ Create New Contact", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        addSplitPersonDropdownExpanded = false
                                        isAddingToSplit = true
                                        showAddPersonDialog = true
                                    }
                                )
                            }
                        }

                        // Allocation summary status pill
                        val totalVal = uiState.totalAmountText.toDoubleOrNull() ?: 0.0
                        val myShareVal = uiState.myShareText.toDoubleOrNull() ?: 0.0
                        val othersTotal = uiState.splitParticipants.sumOf { it.shareText.toDoubleOrNull() ?: 0.0 }
                        val currentSum = myShareVal + othersTotal
                        val diff = totalVal - currentSum

                        if (totalVal > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (kotlin.math.abs(diff) < 0.05) {
                                    Surface(shape = CircleShape, color = GreenPositiveBg) {
                                        Text(
                                            "✓ Fully allocated",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = GreenPositive,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                } else if (diff > 0) {
                                    Surface(shape = CircleShape, color = MutedSurface) {
                                        Text(
                                            "${CurrencyUtils.formatAmount(diff, currency)} unallocated",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = CharcoalSecondary,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    TextButton(onClick = viewModel::allocateRemainingToMe) {
                                        Text("Add rest to me", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack))
                                    }
                                } else {
                                    Surface(shape = CircleShape, color = RedNegativeBg) {
                                        Text(
                                            "${CurrencyUtils.formatAmount(kotlin.math.abs(diff), currency)} over total bill",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = RedNegative,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Standard Single Spend Amount Input
                        val labelText = when (uiState.transactionType) {
                            TransactionType.JUST_MY_SPEND -> "AMOUNT SPENT"
                            TransactionType.SOMEONE_PAID_FOR_ME -> "AMOUNT THEY COVERED"
                            TransactionType.I_PAID_FOR_SOMEONE -> "AMOUNT YOU PAID"
                            else -> "AMOUNT"
                        }
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CharcoalSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.amountText,
                            onValueChange = viewModel::onAmountChanged,
                            placeholder = { Text("0.00", color = CharcoalSecondary.copy(alpha = 0.5f)) },
                            prefix = {
                                Text(
                                    "$currency ",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBlack
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ObsidianBlack,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = MutedSurface.copy(alpha = 0.3f),
                                unfocusedContainerColor = MutedSurface.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            // Person Selector (Single Person Modes: Someone paid for me / I paid for someone)
            if (uiState.transactionType == TransactionType.SOMEONE_PAID_FOR_ME || uiState.transactionType == TransactionType.I_PAID_FOR_SOMEONE) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        val personPrompt = if (uiState.transactionType == TransactionType.SOMEONE_PAID_FOR_ME) {
                            "Who paid for you?"
                        } else {
                            "Who did you pay for?"
                        }

                        Text(
                            text = personPrompt,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CharcoalSecondary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.selectedPerson?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select contact") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = ObsidianBlack)
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.ExpandMore, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = ObsidianBlack,
                                    disabledBorderColor = CardBorder,
                                    disabledLeadingIconColor = ObsidianBlack,
                                    disabledTrailingIconColor = CharcoalSecondary,
                                    disabledContainerColor = MutedSurface.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { singlePersonDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = singlePersonDropdownExpanded,
                                onDismissRequest = { singlePersonDropdownExpanded = false }
                            ) {
                                persons.forEach { person ->
                                    DropdownMenuItem(
                                        text = { Text(person.name, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            viewModel.onPersonSelected(person)
                                            singlePersonDropdownExpanded = false
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = ObsidianBlack)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("+ Create New Contact", fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        singlePersonDropdownExpanded = false
                                        isAddingToSplit = false
                                        showAddPersonDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Description / Note with Autocomplete
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "NOTE / DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = CharcoalSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        placeholder = { Text("e.g. Dinner with team, Flight booking", color = CharcoalSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ObsidianBlack,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = MutedSurface.copy(alpha = 0.3f),
                            unfocusedContainerColor = MutedSurface.copy(alpha = 0.2f)
                        )
                    )

                    // Autocomplete suggestions
                    AnimatedVisibility(visible = uiState.suggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .background(MutedSurface, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Past matches (tap to auto-fill):",
                                style = MaterialTheme.typography.labelSmall.copy(color = CharcoalSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            uiState.suggestions.forEach { suggestion ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onSuggestionSelected(suggestion) }
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = ObsidianBlack
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Recurring toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Repeat, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Recurring expense?", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Summarized in Dashboard", style = MaterialTheme.typography.labelSmall.copy(color = CharcoalSecondary, fontSize = 11.sp))
                            }
                        }
                        Switch(
                            checked = uiState.isRecurring,
                            onCheckedChange = viewModel::onRecurringToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ObsidianBlack,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MutedSurface
                            )
                        )
                    }
                }
            }

            // Category Picker
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CharcoalSecondary
                            )
                        )
                        TextButton(onClick = { showAddCategoryDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = ObsidianBlack)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = uiState.selectedCategory?.id == category.id
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) AccentBlack else MutedSurface,
                                border = BorderStroke(1.dp, if (isSelected) AccentBlack else CardBorder),
                                modifier = Modifier.clickable { viewModel.onCategorySelected(category) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIconBadge(
                                        iconName = category.iconName,
                                        colorHex = if (isSelected) "#FFFFFF" else category.colorHex,
                                        size = 22.dp,
                                        iconSize = 13.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else ObsidianBlack
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Date & Time
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    val dateString = remember(uiState.dateEpoch) { dateFormat.format(Date(uiState.dateEpoch)) }

                    // Date Pill
                    Surface(
                        shape = CircleShape,
                        color = MutedSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val cal = Calendar.getInstance().apply { timeInMillis = uiState.dateEpoch }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply { set(y, m, d) }
                                        viewModel.onDateSelected(newCal.timeInMillis)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp), tint = ObsidianBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateString, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                        }
                    }

                    // Time Pill
                    Surface(
                        shape = CircleShape,
                        color = MutedSurface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                val parts = uiState.timeFormatted.split(":")
                                val initHour = parts.getOrNull(0)?.toIntOrNull() ?: 12
                                val initMin = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        val formatted = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                                        viewModel.onTimeSelected(formatted)
                                    },
                                    initHour,
                                    initMin,
                                    true
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp), tint = ObsidianBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.timeFormatted.ifEmpty { "Time" }, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }

            // Save Action Button
            Button(
                onClick = viewModel::saveTransaction,
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlack,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        "Log Spend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Inline Add Category Dialog
    if (showAddCategoryDialog) {
        var catName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#18181B") }
        var selectedIcon by remember { mutableStateOf("Food") }

        val palette = listOf(
            "#18181B", "#52525B", "#2563EB", "#059669",
            "#D97706", "#DC2626", "#7C3AED", "#DB2777"
        )

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("New Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Color", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        palette.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = hex }
                                    .padding(2.dp)
                            ) {
                                if (selectedColor == hex) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    Text("Icon", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryIcons.AVAILABLE_ICONS.take(8).forEach { (iconKey, _) ->
                            val isChosen = selectedIcon == iconKey
                            Surface(
                                shape = CircleShape,
                                color = if (isChosen) MutedSurface else Color.Transparent,
                                border = if (isChosen) BorderStroke(1.5.dp, ObsidianBlack) else null,
                                modifier = Modifier.clickable { selectedIcon = iconKey }
                            ) {
                                CategoryIconBadge(iconName = iconKey, colorHex = selectedColor, size = 32.dp, iconSize = 18.dp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (catName.isNotBlank()) {
                            viewModel.createNewCategoryInline(catName.trim(), selectedColor, selectedIcon)
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, color = ObsidianBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel", color = CharcoalSecondary)
                }
            }
        )
    }

    // Inline Add Person Dialog
    if (showAddPersonDialog) {
        var personNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("New Contact", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                OutlinedTextField(
                    value = personNameInput,
                    onValueChange = { personNameInput = it },
                    label = { Text("Name or Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (personNameInput.isNotBlank()) {
                            viewModel.createNewPersonInline(personNameInput, addToSplit = isAddingToSplit)
                            showAddPersonDialog = false
                        }
                    }
                ) {
                    Text("Add", fontWeight = FontWeight.Bold, color = ObsidianBlack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPersonDialog = false }) {
                    Text("Cancel", color = CharcoalSecondary)
                }
            }
        )
    }
}
