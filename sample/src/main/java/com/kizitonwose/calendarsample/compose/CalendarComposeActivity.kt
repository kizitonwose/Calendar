package com.kizitonwose.calendarsample.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kizitonwose.calendarsample.R

enum class Page {
    List, Example1, Example2, Example3
}

data class ListInfo(val page: Page, val title: String, val subtitle: String)

val items = listOf(
    ListInfo(
        Page.Example1,
        title = "Example 1",
        subtitle = "Simple Calendar. Sticky header, paged scroll style, programmatic scrolling, single selection."
    ),
    ListInfo(Page.Example2, title = "Example 2", subtitle = "Events Calendar")
)

class CalendarComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val primaryColor = colorResource(id = R.color.colorPrimary)
            MaterialTheme(colors = MaterialTheme.colors.copy(primary = primaryColor)) {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text(text = "Calendar Compose Sample") })
                    },
                    content = {
                        AppNavHost(Modifier.padding(it))
                    })
            }
        }
    }

    @Composable
    private fun AppNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = Page.List.name
        ) {
            composable(Page.List.name) { ListPage { page -> navController.navigate(page.name) } }
            composable(Page.Example1.name) { Example1Page() }
            composable(Page.Example2.name) { Example1Page() }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun ListPage(navigate: (Page) -> Unit) {
        LazyColumn {
            items(items) { item ->
                ListItem(
                    modifier = Modifier.clickable { navigate(item.page) },
                    text = {
                        Text(
                            text = item.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    secondaryText = { Text(text = item.subtitle) },
                )
            }
        }
    }
}