package com.kizitonwose.calendarsample.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Page(val title: String, val subtitle: String, val showToolBar: Boolean) {
    List(
        title = "Calendar Compose Sample",
        subtitle = "",
        showToolBar = true
    ),
    Example1(
        title = "Example 1",
        subtitle = "Horizontal Calendar. month header, paged scroll style, programmatic scrolling, multiple selection.",
        showToolBar = true
    ),
    Example2(
        title = "Example 2",
        subtitle = "Vertical Calendar. Sticky header, continuous selection within one month and across multiple months, " +
                "dates older than the current day are disabled. Similar to what is in the Airbnb app.",
        showToolBar = false
    ),
    Example3(
        title = "Example 3",
        subtitle = "Simple Calendar. Sticky header, paged scroll style, programmatic scrolling, single selection.",
        showToolBar = true
    );
}

val items = listOf(Page.Example1, Page.Example2)

@Composable
fun ListPage(click: (Page) -> Unit) {
    LazyColumn {
        items(items) { item ->
            Column(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .clickable { click(item) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val titleStyle = MaterialTheme.typography.subtitle1
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Medium,
                    style = titleStyle.copy(
                        fontSize = 22.sp,
                        color = titleStyle.color.copy(alpha = ContentAlpha.high)),
                )
                val subtitleStyle = MaterialTheme.typography.body2
                Text(
                    text = item.subtitle,
                    style = subtitleStyle.copy(
                        fontSize = 16.sp,
                        color = subtitleStyle.color.copy(alpha = ContentAlpha.medium)),
                )
            }
            Divider()
        }
    }
}