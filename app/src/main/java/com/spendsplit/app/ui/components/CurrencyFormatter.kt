package com.spendsplit.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatAmount(amount: Double, currency: String = "₹", showDecimals: Boolean = true): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern(if (showDecimals) "#,##0.00" else "#,##0")
        return "$currency${formatter.format(amount)}"
    }

    fun formatSignedAmount(amount: Double, currency: String = "₹"): String {
        val absAmount = kotlin.math.abs(amount)
        val formatted = formatAmount(absAmount, currency)
        return when {
            amount > 0 -> "+$formatted"
            amount < 0 -> "-$formatted"
            else -> formatted
        }
    }
}

@Composable
fun CurrencyText(
    amount: Double,
    currency: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    showSign: Boolean = false
) {
    val text = if (showSign) {
        CurrencyUtils.formatSignedAmount(amount, currency)
    } else {
        CurrencyUtils.formatAmount(amount, currency)
    }
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color
    )
}
