package com.spendsplit.app.ui.people

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsplit.app.ui.components.CurrencyUtils
import com.spendsplit.app.ui.theme.AccentBlack
import com.spendsplit.app.ui.theme.BeigeBackground
import com.spendsplit.app.ui.theme.CardBorder
import com.spendsplit.app.ui.theme.CharcoalSecondary
import com.spendsplit.app.ui.theme.GreenPositive
import com.spendsplit.app.ui.theme.GreenPositiveBg
import com.spendsplit.app.ui.theme.MutedSurface
import com.spendsplit.app.ui.theme.ObsidianBlack
import com.spendsplit.app.ui.theme.RedNegative
import com.spendsplit.app.ui.theme.RedNegativeBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel
) {
    val currency by viewModel.currency.collectAsState()
    val people by viewModel.peopleWithBalances.collectAsState()
    val summary by viewModel.iouSummary.collectAsState()
    val selectedPerson by viewModel.selectedPerson.collectAsState()
    val personTransactions by viewModel.selectedPersonTransactions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddPersonDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.settleSuccessEvent.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "People & IOUs",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPersonDialog = true },
                containerColor = AccentBlack,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Contact")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BeigeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net IOU Summary Card
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
                            text = "NET IOU POSITION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CharcoalSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val netSign = if (summary.netBalance > 0) "+ " else if (summary.netBalance < 0) "- " else ""
                        val netColor = if (summary.netBalance > 0) GreenPositive else if (summary.netBalance < 0) RedNegative else ObsidianBlack
                        Text(
                            text = "$netSign${CurrencyUtils.formatAmount(kotlin.math.abs(summary.netBalance), currency)}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = netColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // You are owed
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GreenPositiveBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = GreenPositive,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "You are owed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CharcoalSecondary
                                        )
                                        Text(
                                            CurrencyUtils.formatAmount(summary.totalOwedToMe, currency),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GreenPositive
                                        )
                                    }
                                }
                            }

                            // You owe
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = RedNegativeBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = RedNegative,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "You owe",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CharcoalSecondary
                                        )
                                        Text(
                                            CurrencyUtils.formatAmount(summary.totalIOwe, currency),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = RedNegative
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // People List Header
            item {
                Text(
                    text = "Contacts (${people.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ObsidianBlack
                    )
                )
            }

            if (people.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = CharcoalSecondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "No contacts added yet",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = CharcoalSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to add a contact or split a spend",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalSecondary
                            )
                        }
                    }
                }
            } else {
                items(people, key = { it.person.id }) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPerson(item) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MutedSurface)
                                    .border(1.dp, CardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.person.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianBlack
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.person.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianBlack
                                    )
                                )
                                val status = when {
                                    item.balance > 0 -> "Owes you"
                                    item.balance < 0 -> "You owe"
                                    else -> "All settled"
                                }
                                Text(
                                    text = status,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = when {
                                        item.balance > 0 -> GreenPositive
                                        item.balance < 0 -> RedNegative
                                        else -> CharcoalSecondary
                                    }
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val amountColor = when {
                                    item.balance > 0 -> GreenPositive
                                    item.balance < 0 -> RedNegative
                                    else -> CharcoalSecondary
                                }
                                Text(
                                    text = CurrencyUtils.formatAmount(kotlin.math.abs(item.balance), currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = amountColor
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = CharcoalSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Person Ledger Sheet
    selectedPerson?.let { person ->
        PersonDetailSheet(
            personWithBalance = person,
            transactions = personTransactions,
            currency = currency,
            onDismiss = { viewModel.selectPerson(null) },
            onSettleUp = { viewModel.settleUp(person.person, person.balance) },
            onDeletePerson = { viewModel.deletePerson(person.person) }
        )
    }

    // Add Person Dialog
    if (showAddPersonDialog) {
        var personNameInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPersonDialog = false },
            title = { Text("New Contact", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = {
                OutlinedTextField(
                    value = personNameInput,
                    onValueChange = { personNameInput = it },
                    label = { Text("Full Name or Nickname") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (personNameInput.isNotBlank()) {
                            viewModel.addPerson(personNameInput.trim())
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
