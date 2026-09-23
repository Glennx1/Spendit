package com.spendsplit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object CategoryIcons {
    val AVAILABLE_ICONS = listOf(
        "Food" to Icons.Default.Restaurant,
        "Travel" to Icons.Default.Flight,
        "Rent" to Icons.Default.Home,
        "Utilities" to Icons.Default.LocalGasStation,
        "Shopping" to Icons.Default.ShoppingCart,
        "Entertainment" to Icons.Default.Movie,
        "Subscriptions" to Icons.Default.Subscriptions,
        "Health" to Icons.Default.FitnessCenter,
        "Car" to Icons.Default.DirectionsCar,
        "Work" to Icons.Default.Work,
        "Education" to Icons.Default.School,
        "Bills" to Icons.Default.ReceiptLong,
        "Other" to Icons.Default.Category
    )

    fun getIcon(name: String): ImageVector {
        return AVAILABLE_ICONS.firstOrNull { it.first.equals(name, ignoreCase = true) }?.second
            ?: Icons.Default.Category
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    val bg = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF607D8B)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIcons.getIcon(iconName),
            contentDescription = iconName,
            tint = bg,
            modifier = Modifier.size(iconSize)
        )
    }
}
