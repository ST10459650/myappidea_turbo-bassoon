package com.example.pillarpocket.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pillarpocket.ui.theme.PillarAmber
import com.example.pillarpocket.ui.theme.PillarRed
import com.example.pillarpocket.viewmodel.CategorySpendingItem

/*
 * Formats a Double amount into a short label for chart axes.
 * Values >= 1000 are shown as e.g. "1.5k", others as whole numbers.
 */
fun formatChartAmount(value: Double): String = when {
    value >= 1000 -> "${"%.1f".format(value / 1000)}k"
    else -> "%.0f".format(value)
}

/*
 * A horizontally scrollable custom bar chart that displays spending
 * per category for a selected month, with optional min/max goal lines.
 *
 * @param items          List of [CategorySpendingItem] to render as bars.
 * @param minGoal        Optional minimum budget goal — drawn as an amber dashed line.
 * @param maxGoal        Optional maximum budget goal — drawn as a red dashed line.
 * @param chartHeight    Height of the chart canvas area.
 */
@Composable
fun SpendingBarChart(
    items: List<CategorySpendingItem>,
    minGoal: Double?,
    maxGoal: Double?,
    chartHeight: Dp = 300.dp
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current

    // Determine the highest Y value so all elements fit within the chart
    val maxValue = maxOf(
        items.maxOfOrNull { it.totalAmount } ?: 1.0,
        maxGoal ?: 0.0,
        minGoal ?: 0.0
    ) * 1.25

    // Minimum width allocated per category bar slot
    val barSlotWidthDp = 90.dp
    val totalChartWidth = barSlotWidthDp * items.size + 80.dp

    // Parse each category's colour from its hex string
    val barColors = items.map { item ->
        runCatching {
            Color(android.graphics.Color.parseColor(item.category.colorHex))
        }.getOrDefault(Color.Gray)
    }

    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Canvas(
            modifier = Modifier
                .width(totalChartWidth)
                .height(chartHeight)
        ) {
            val leftPad  = 68.dp.toPx()
            val bottomPad = 64.dp.toPx()
            val topPad   = 24.dp.toPx()
            val rightPad = 16.dp.toPx()

            val chartW = size.width - leftPad - rightPad
            val chartH = size.height - topPad - bottomPad

            val barSlotW = chartW / items.size
            val barW     = barSlotW * 0.55f

            // Gridlines & Y-axis labels
            val gridLines = 5
            for (i in 0..gridLines) {
                val ratio = i.toFloat() / gridLines
                val y = topPad + chartH - chartH * ratio
                val value = maxValue * ratio

                // Horizontal grid line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(leftPad, y),
                    end   = Offset(size.width - rightPad, y),
                    strokeWidth = with(density) { 1.dp.toPx() }
                )

                // Y-axis value label
                drawContext.canvas.nativeCanvas.drawText(
                    "R${formatChartAmount(value)}",
                    leftPad - with(density) { 6.dp.toPx() },
                    y + with(density) { 4.dp.toPx() },
                    android.graphics.Paint().apply {
                        color     = android.graphics.Color.GRAY
                        textSize  = with(density) { 9.dp.toPx() }
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                )
            }

            //Bars
            items.forEachIndexed { index, item ->
                val barH  = if (maxValue > 0)
                    (item.totalAmount / maxValue * chartH).toFloat()
                else 0f

                val barX = leftPad + index * barSlotW + (barSlotW - barW) / 2
                val barY = topPad + chartH - barH

                // Bar rectangle
                drawRoundRect(
                    color      = barColors[index],
                    topLeft    = Offset(barX, barY),
                    size       = Size(barW, barH),
                    cornerRadius = CornerRadius(with(density) { 6.dp.toPx() })
                )

                // Amount label on top of bar
                if (item.totalAmount > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        "R${formatChartAmount(item.totalAmount)}",
                        barX + barW / 2,
                        barY - with(density) { 5.dp.toPx() },
                        android.graphics.Paint().apply {
                            color     = android.graphics.Color.DKGRAY
                            textSize  = with(density) { 9.dp.toPx() }
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                    )
                }

                // Category name below X-axis
                val labelText = item.category.name.let {
                    if (it.length > 9) it.take(8) + "…" else it
                }
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    barX + barW / 2,
                    topPad + chartH + with(density) { 16.dp.toPx() },
                    android.graphics.Paint().apply {
                        color     = android.graphics.Color.DKGRAY
                        textSize  = with(density) { 9.dp.toPx() }
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }

            //Min Goal dashed line
            if (minGoal != null && minGoal > 0 && maxValue > 0) {
                val y = topPad + chartH - (minGoal / maxValue * chartH).toFloat()
                drawGoalLine(
                    y          = y,
                    leftPad    = leftPad,
                    rightPad   = rightPad,
                    color      = PillarAmber,
                    label      = "Min  R${formatChartAmount(minGoal)}",
                    density    = density
                )
            }

            // Max Goal dashed line
            if (maxGoal != null && maxGoal > 0 && maxValue > 0) {
                val y = topPad + chartH - (maxGoal / maxValue * chartH).toFloat()
                drawGoalLine(
                    y          = y,
                    leftPad    = leftPad,
                    rightPad   = rightPad,
                    color      = PillarRed,
                    label      = "Max  R${formatChartAmount(maxGoal)}",
                    density    = density
                )
            }

            // Axes
            // Y-axis
            drawLine(
                color       = Color.Gray,
                start       = Offset(leftPad, topPad),
                end         = Offset(leftPad, topPad + chartH),
                strokeWidth = with(density) { 1.5.dp.toPx() }
            )
            // X-axis
            drawLine(
                color       = Color.Gray,
                start       = Offset(leftPad, topPad + chartH),
                end         = Offset(size.width - rightPad, topPad + chartH),
                strokeWidth = with(density) { 1.5.dp.toPx() }
            )
        }
    }
}

 // Draws a dashed horizontal goal line across the chart with a label.
private fun DrawScope.drawGoalLine(
    y: Float,
    leftPad: Float,
    rightPad: Float,
    color: Color,
    label: String,
    density: androidx.compose.ui.unit.Density
) {
    drawLine(
        color       = color,
        start       = Offset(leftPad, y),
        end         = Offset(size.width - rightPad, y),
        strokeWidth = with(density) { 2.dp.toPx() },
        pathEffect  = PathEffect.dashPathEffect(floatArrayOf(14f, 7f))
    )
    drawContext.canvas.nativeCanvas.drawText(
        label,
        leftPad + with(density) { 4.dp.toPx() },
        y - with(density) { 5.dp.toPx() },
        android.graphics.Paint().apply {
            this.color     = color.toArgb()
            textSize       = with(density) { 9.dp.toPx() }
            isFakeBoldText = true
            isAntiAlias    = true
        }
    )
}