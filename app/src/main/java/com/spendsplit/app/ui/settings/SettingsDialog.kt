package com.spendsplit.app.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsplit.app.data.repository.FinanceRepository
import com.spendsplit.app.ui.theme.AccentBlack
import com.spendsplit.app.ui.theme.CardBorder
import com.spendsplit.app.ui.theme.CardBorderSubtle
import com.spendsplit.app.ui.theme.CharcoalSecondary
import com.spendsplit.app.ui.theme.MutedSurface
import com.spendsplit.app.ui.theme.ObsidianBlack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    repository: FinanceRepository,
    onDismiss: () -> Unit
) {
    val currentCurrency by repository.currencyFlow.collectAsState(initial = "₹")
    val currentTheme by repository.themeFlow.collectAsState(initial = "SYSTEM")

    val supportedCurrencies = listOf(
        "₹" to "Indian Rupee (INR)",
        "$" to "US Dollar (USD)",
        "€" to "Euro (EUR)",
        "£" to "British Pound (GBP)",
        "¥" to "Japanese Yen (JPY)",
        "₩" to "Korean Won (KRW)",
        "C$" to "Canadian Dollar (CAD)",
        "A$" to "Australian Dollar (AUD)"
    )

    val themes = listOf(
        "SYSTEM" to "System default",
        "LIGHT" to "Light",
        "DARK" to "Dark"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Preferences",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBlack
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Currency selection
                Text(
                    text = "CURRENCY SYMBOL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CharcoalSecondary
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    supportedCurrencies.take(6).forEach { (symbol, label) ->
                        val isSelected = currentCurrency == symbol
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MutedSurface else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, CardBorder) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.setCurrency(symbol)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = symbol,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ObsidianBlack
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isSelected) ObsidianBlack else CharcoalSecondary
                                        )
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ObsidianBlack
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = CardBorderSubtle)

                // Theme selection
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CharcoalSecondary
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    themes.forEach { (code, label) ->
                        val isSelected = currentTheme == code
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MutedSurface else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, CardBorder) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        repository.setTheme(code)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isSelected) ObsidianBlack else CharcoalSecondary
                                    )
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ObsidianBlack
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = CardBorderSubtle)

                // About section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = CharcoalSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Column {
                        Text(
                            text = "SpendSplit",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ObsidianBlack)
                        )
                        Text(
                            text = "100% offline personal finance & group IOU engine.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = CharcoalSecondary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontWeight = FontWeight.Bold, color = ObsidianBlack)
            }
        }
    )
}
