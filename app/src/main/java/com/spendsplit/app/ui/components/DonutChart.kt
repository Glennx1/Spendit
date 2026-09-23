package com.spendsplit.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

data class CategorySpendItem(
    val categoryId: Long,
    val name: String,
    val amount: Double,
    val colorHex: String,
    val percentage: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DonutChart(
    items: List<CategorySpendItem>,
    currency: String,
    modifier: Modifier = Modifier,
    chartSize: Dp = 190.dp,
    strokeWidth: Dp = 20.dp,
    selectedCategoryId: Long? = null,
    onCategoryClick: (Long?) -> Unit = {}
) {
    val totalSpend = items.sumOf { it.amount }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(items) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(chartSize)
                    .pointerInput(items) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            val adjustedAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            var clickedId: Long? = null
                            for (item in items) {
                                val sweep = (item.percentage / 100f) * 360f
                                if (adjustedAngle >= currentAngle && adjustedAngle <= currentAngle + sweep) {
                                    clickedId = item.categoryId
                                    break
                                }
                                currentAngle += sweep
                            }
                            if (clickedId == selectedCategoryId) {
                                onCategoryClick(null)
                            } else {
                                onCategoryClick(clickedId)
                            }
                        }
                    }
            ) {
                val strokePx = strokeWidth.toPx()
                val radius = (size.minDimension - strokePx) / 2f
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                val chartAreaSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartAreaSize,
                    style = Stroke(width = strokePx)
                )

                if (totalSpend > 0) {
                    var currentStartAngle = -90f
                    for (item in items) {
                        val sweep = (item.percentage / 100f) * 360f * animationProgress.value
                        val itemColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        val isSelected = selectedCategoryId == null || selectedCategoryId == item.categoryId
                        val effectiveColor = if (isSelected) itemColor else itemColor.copy(alpha = 0.25f)
                        val effectiveStroke = if (selectedCategoryId == item.categoryId) strokePx * 1.15f else strokePx

                        drawArc(
                            color = effectiveColor,
                            startAngle = currentStartAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = chartAreaSize,
                            style = Stroke(width = effectiveStroke, cap = StrokeCap.Butt)
                        )
                        currentStartAngle += sweep
                    }
                }
            }

            // Center Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                val selectedItem = items.firstOrNull { it.categoryId == selectedCategoryId }
                if (selectedItem != null) {
                    Text(
                        text = selectedItem.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(selectedItem.amount, currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", selectedItem.percentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatAmount(totalSpend, currency),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend chips / grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedCategoryId == item.categoryId
                val color = try {
                    Color(android.graphics.Color.parseColor(item.colorHex))
                } catch (e: Exception) {
                    Color.Gray
                }

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline),
                    modifier = Modifier.clickable {
                        onCategoryClick(if (isSelected) null else item.categoryId)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.0f", item.percentage)}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
