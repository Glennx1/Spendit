package com.spendsplit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class TimePeriod(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month")
}

data class BarChartDataPoint(
    val label: String,
    val amount: Double,
    val fullDateLabel: String = label
)

@Composable
fun SpendOverTimeChart(
    dataPoints: List<BarChartDataPoint>,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    currency: String,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(dataPoints, selectedPeriod) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val maxAmount = (dataPoints.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(1.0)
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spend Over Time",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Period Pill Toggle
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                        .padding(3.dp)
                ) {
                    TimePeriod.values().forEach { period ->
                        val isSelected = period == selectedPeriod
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { onPeriodSelected(period) }
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dataPoints.isEmpty() || dataPoints.all { it.amount == 0.0 }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No spending records for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val activePoint = selectedPointIndex?.let { dataPoints.getOrNull(it) }
                if (activePoint != null) {
                    Text(
                        text = "${activePoint.fullDateLabel}: ${CurrencyUtils.formatAmount(activePoint.amount, currency)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    Text(
                        text = "Tap a bar to inspect amount",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = dataPoints.size
                    if (barCount == 0) return@Canvas

                    val availableWidth = width - (barCount - 1) * 10.dp.toPx()
                    val barWidth = (availableWidth / barCount).coerceIn(12.dp.toPx(), 36.dp.toPx())
                    val spacing = if (barCount > 1) (width - (barWidth * barCount)) / (barCount - 1) else 0f

                    dataPoints.forEachIndexed { index, point ->
                        val x = index * (barWidth + spacing)
                        val barHeightFraction = (point.amount / maxAmount).toFloat() * animProgress.value
                        val barHeight = (height - 24.dp.toPx()) * barHeightFraction
                        val y = height - 24.dp.toPx() - barHeight

                        // Background track pillar
                        drawRoundRect(
                            color = trackColor,
                            topLeft = Offset(x, 0f),
                            size = Size(barWidth, height - 24.dp.toPx()),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Filled bar
                        val isHighlighted = selectedPointIndex == index || (selectedPointIndex == null && point.amount == maxAmount && point.amount > 0)
                        drawRoundRect(
                            color = if (isHighlighted) primaryColor else primaryColor.copy(alpha = 0.55f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight.coerceAtLeast(4.dp.toPx())),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.forEachIndexed { index, point ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    selectedPointIndex = if (selectedPointIndex == index) null else index
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedPointIndex == index) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
