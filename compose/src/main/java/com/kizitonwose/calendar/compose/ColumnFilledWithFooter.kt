package com.kizitonwose.calendar.compose

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine

/**
 * TODO: Doc
 */
@Composable
internal fun ColumnFilledWithFooter(
    modifier: Modifier,
    hasHeader: Boolean,
    content: @Composable ColumnScope.() -> Unit,
) = Layout(
    content = { NoOpColumnScopeInstance.content() },
    modifier = modifier,
) { measurables, constraints ->
    check(measurables.size == if (hasHeader) 3 else 2) {
        "You provided either a header or footer composable" +
            " with empty content, use `null` instead."
    }
    val headerPlaceable = if (hasHeader) {
        measurables.first().measure(constraints.copy(minHeight = 0))
    } else {
        null
    }
    val footerPlaceable = measurables.last().measure(constraints.copy(minHeight = 0))
    val bodyHeight = constraints.maxHeight - (headerPlaceable?.height ?: 0) - footerPlaceable.height
    val bodyConstraints = constraints.copy(minHeight = bodyHeight, maxHeight = bodyHeight)
    val bodyPlaceable = if (hasHeader) {
        measurables[1].measure(bodyConstraints)
    } else {
        measurables.first().measure(bodyConstraints)
    }
    val placeables = listOfNotNull(headerPlaceable, bodyPlaceable, footerPlaceable)
    val width = placeables.maxOf { it.width }
    val height = placeables.sumOf { it.height }
    layout(width, height) {
        var yPos = 0
        for (placeable in placeables) {
            placeable.place(0, yPos)
            yPos += placeable.height
        }
    }
}

private object NoOpColumnScopeInstance : ColumnScope {
    override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = this
    override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier = this
    override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier = this
    override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = this
}
