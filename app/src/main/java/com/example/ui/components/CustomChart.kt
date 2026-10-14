package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocalPerformancePoint
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
fun CustomPerformanceChart(
    points: List<LocalPerformancePoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "📈",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "No performance data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Add some performance records to view trends.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return;
    }

    // Colors
    val readingColor = Color(0xFF1E3C72) // Deep Navy
    val writingColor = Color(0xFF10B981) // Green
    val speakingColor = Color(0xFFF59E0B) // Amber Gold
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val inverseSurface = MaterialTheme.colorScheme.inverseSurface
    val inverseOnSurface = MaterialTheme.colorScheme.inverseOnSurface

    // Sort points by date chronological order (or take raw list)
    val sortedPoints = remember(points) {
        // Simple sort helper (assuming date format like YYYY-MM-DD or MM/DD/YYYY)
        points.takeLast(7) // Display max last 7 entries for crisp visualization
    }

    // Animation entry
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(sortedPoints) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    // Gesture hover selection
    var selectedPointIndex by remember { mutableStateOf(-1) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }

    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subject Performance Core State",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legends
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendItem("Reading", readingColor)
                    LegendItem("Writing", writingColor)
                    LegendItem("Speaking", speakingColor)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(sortedPoints) {
                            detectTapGestures(
                                onPress = { offset ->
                                    touchPosition = offset
                                    // Calculate closest point on X coordinate
                                    val canvasWidth = size.width
                                    val paddingLeft = 100f
                                    val paddingRight = 40f
                                    val usableWidth = canvasWidth - paddingLeft - paddingRight
                                    val stepX = usableWidth / (sortedPoints.size - 1).coerceAtLeast(1)

                                    var closestIndex = -1
                                    var minDistance = Float.MAX_VALUE
                                    for (i in sortedPoints.indices) {
                                        val x = paddingLeft + i * stepX
                                        val distance = abs(offset.x - x)
                                        if (distance < minDistance) {
                                            minDistance = distance
                                            closestIndex = i
                                        }
                                    }
                                    if (minDistance < stepX / 2f) {
                                        selectedPointIndex = closestIndex
                                    } else {
                                        selectedPointIndex = -1
                                    }
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingTop = 40f
                    val paddingBottom = 80f
                    val paddingLeft = 100f
                    val paddingRight = 40f

                    val usableWidth = width - paddingLeft - paddingRight
                    val usableHeight = height - paddingTop - paddingBottom

                    // Draw Horizontal Grid lines & Left Labels (Y-axis)
                    val yLines = 5
                    for (i in 0 until yLines) {
                        val percentage = i.toFloat() / (yLines - 1)
                        val y = paddingTop + (usableHeight * (1f - percentage))
                        
                        // Line
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Label
                        val labelText = (percentage * 100).toInt().toString()
                        drawText(
                            textMeasurer = textMeasurer,
                            text = labelText,
                            topLeft = Offset(20f, y - 24f),
                            style = TextStyle(
                                color = textLabelColor,
                                fontSize = 11.sp
                            )
                        )
                    }

                    if (sortedPoints.size < 1) return@Canvas

                    // Coordinates helper
                    val stepX = usableWidth / (sortedPoints.size - 1).coerceAtLeast(1)

                    fun getCoordinates(index: Int, score: Float): Offset {
                        val x = paddingLeft + (index * stepX)
                        val normalizedScore = (score.coerceIn(0f, 100f) / 100f)
                        val y = paddingTop + usableHeight * (1f - normalizedScore)
                        return Offset(x, y)
                    }

                    // Draw Paths
                    fun drawLinePath(
                        scores: List<Float>,
                        borderColor: Color,
                        gradientColor: Color
                    ) {
                        val path = Path()
                        val fillPath = Path()

                        for (i in scores.indices) {
                            val coord = getCoordinates(i, scores[i] * animationProgress.value)
                            if (i == 0) {
                                path.moveTo(coord.x, coord.y)
                                fillPath.moveTo(coord.x, height - paddingBottom)
                                fillPath.lineTo(coord.x, coord.y)
                            } else {
                                // Draw smoothly using bezier or standard line
                                val prevCoord = getCoordinates(i - 1, scores[i - 1] * animationProgress.value)
                                val controlPoint1 = Offset(prevCoord.x + stepX / 2, prevCoord.y)
                                val controlPoint2 = Offset(coord.x - stepX / 2, coord.y)
                                path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, coord.x, coord.y)
                                fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, coord.x, coord.y)
                            }

                            if (i == scores.lastIndex) {
                                fillPath.lineTo(coord.x, height - paddingBottom)
                                fillPath.close()
                            }
                        }

                        // Gradient Area fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = paddingTop,
                                endY = height - paddingBottom
                            )
                        )

                        // Border Stroke
                        drawPath(
                            path = path,
                            color = borderColor,
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Map score lists
                    val readingScores = sortedPoints.map { it.reading.toFloat() }
                    val writingScores = sortedPoints.map { it.writing.toFloat() }
                    val speakingScores = sortedPoints.map { it.speaking.toFloat() }

                    // Perform drawings
                    drawLinePath(readingScores, readingColor, readingColor)
                    drawLinePath(writingScores, writingColor, writingColor)
                    drawLinePath(speakingScores, speakingColor, speakingColor)

                    // Draw Bottom Labels (X-axis) and Data point ticks
                    for (i in sortedPoints.indices) {
                        val point = sortedPoints[i]
                        val x = paddingLeft + (i * stepX)
                        val dateLabel = point.date.substringAfter("-").substringBeforeLast("-").ifEmpty { point.date } // strip down to MM/DD if long

                        // Tick Label
                        val cleanDate = if (dateLabel.length > 5) dateLabel.take(5) else dateLabel
                        drawText(
                            textMeasurer = textMeasurer,
                            text = cleanDate,
                            topLeft = Offset(x - 30f, height - paddingBottom + 12f),
                            style = TextStyle(
                                color = textLabelColor,
                                fontSize = 11.sp
                            )
                        )

                        // Data nodes circle markers
                        listOf(
                            readingScores[i] to readingColor,
                            writingScores[i] to writingColor,
                            speakingScores[i] to speakingColor
                        ).forEach { (score, col) ->
                            val circleLoc = getCoordinates(i, score * animationProgress.value)
                            drawCircle(
                                color = Color.White,
                                radius = 5.dp.toPx(),
                                center = circleLoc
                            )
                            drawCircle(
                                color = col,
                                radius = 3.dp.toPx(),
                                center = circleLoc,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        }
                    }

                    // Draw Touch Detail HUD vertical guide bar and popup
                    if (selectedPointIndex in sortedPoints.indices) {
                        val i = selectedPointIndex
                        val x = paddingLeft + (i * stepX)
                        val point = sortedPoints[i]

                        // Guide Line
                        drawLine(
                            color = primaryColor.copy(alpha = 0.4f),
                            start = Offset(x, paddingTop),
                            end = Offset(x, height - paddingBottom),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Draw detail info tooltip as background overlay
                        val tooltipText = buildAnnotatedString {
                            append("R: ${point.reading}  ")
                            pushStyle(SpanStyle(color = writingColor))
                            append("W: ${point.writing}  ")
                            pop()
                            pushStyle(SpanStyle(color = speakingColor))
                            append("S: ${point.speaking}")
                        }

                        val dialogSize = textMeasurer.measure(tooltipText)
                        val popupOffset = Offset(
                            x = (x - dialogSize.size.width / 2).coerceIn(paddingLeft, width - paddingRight - dialogSize.size.width),
                            y = paddingTop - 30f
                        )

                        // Simple tooltip shadow/background
                        drawRect(
                            color = inverseSurface,
                            topLeft = Offset(popupOffset.x - 8f, popupOffset.y - 4f),
                            size = Size(dialogSize.size.width.toFloat() + 16f, dialogSize.size.height.toFloat() + 8f)
                        )

                        drawText(
                            textMeasurer = textMeasurer,
                            text = tooltipText,
                            topLeft = popupOffset,
                            style = TextStyle(
                                color = inverseOnSurface,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}
