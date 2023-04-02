package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Page(val title: String, val subtitle: String, val showToolBar: Boolean) {
    List(
        title = "Calendar Compose Sample",
        subtitle = "",
        showToolBar = true,
    ),
    Example1(
        title = "Example 1",
        subtitle = "Horizontal Calendar - Month header, paged scroll style, programmatic scrolling, multiple selection.",
        showToolBar = true,
    ),
    Example2(
        title = "Example 2",
        subtitle = "Vertical Calendar - Sticky header, continuous selection within one month and across multiple months, " +
            "dates older than the current day are disabled. Similar to what is in the Airbnb app.",
        showToolBar = false,
    ),
    Example3(
        title = "Example 3",
        subtitle = "Horizontal Calendar - Single selection, shows the \"EndOfGrid\" implementation of \"OutDateStyle\" property. A flight schedule calendar.",
        showToolBar = false,
    ),
    Example4(
        title = "Example 4",
        subtitle = "Horizontal Calendar - Custom date width and height, custom month container and content backgrounds, continuous horizontal scroll style.",
        showToolBar = true,
    ),
    Example5(
        title = "Example 5",
        subtitle = "Week Calendar - Single selection, paged scroll and visible item observation.",
        showToolBar = false,
    ),
    Example6(
        title = "Example 6",
        subtitle = "HeatMap Calendar - Dynamic month header, continuous scroll. Similar to GitHub's contributions chart.",
        showToolBar = true,
    ),
    Example7(
        title = "Example 7",
        subtitle = "Week Calendar - Continuous scroll, custom day content width, single selection.",
        showToolBar = true,
    ),
    Example8(
        title = "Example 8",
        subtitle = "Fullscreen Horizontal Calendar - Month header and footer, paged horizontal scrolling. Shows the \"Fill\" option of ContentHeightMode property.",
        showToolBar = false,
    ),
    Example9(
        title = "Example 9",
        subtitle = "Month and week calendar toggle with animations.",
        showToolBar = true,
    ),
}

@Composable
fun ListPage(click: (Page) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        items(Page.values().drop(1)) { item ->
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { click(item) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val titleStyle = MaterialTheme.typography.subtitle1
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium,
                    style = titleStyle.copy(
                        fontSize = 20.sp,
                        color = titleStyle.color.copy(alpha = ContentAlpha.high),
                    ),
                )
                val subtitleStyle = MaterialTheme.typography.body2
                Text(
                    text = item.subtitle,
                    style = subtitleStyle.copy(
                        fontSize = 16.sp,
                        color = subtitleStyle.color.copy(alpha = ContentAlpha.medium),
                    ),
                )
            }
            Divider()
        }
    }
}
