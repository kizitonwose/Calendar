package com.kizitonwose.calendarsample.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.dateRangeDisplayText
import kotlinx.coroutines.launch

enum class Page(val title: String, val subtitle: String, val showToolBar: Boolean) {
    List(
        title = "Calendar Compose Sample",
        subtitle = "",
        showToolBar = true
    ),
    Example1(
        title = "Example 1",
        subtitle = "Horizontal Calendar. Sticky header, paged scroll style, programmatic scrolling, multiple selection.",
        showToolBar = true
    ),
    Example2(
        title = "Example 2",
        subtitle = "Simple Calendar. Sticky header, paged scroll style, programmatic scrolling, single selection.",
        showToolBar = false
    ),
    Example3(
        title = "Example 3",
        subtitle = "Simple Calendar. Sticky header, paged scroll style, programmatic scrolling, single selection.",
        showToolBar = true
    );
}

val items = listOf(Page.Example1, Page.Example2)

class CalendarComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val primaryColor = colorResource(id = R.color.colorPrimary)
            var topBarTitle by remember { mutableStateOf("") }
            var toolbarVisible by remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val scaffoldState = rememberScaffoldState()
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val page = Page.valueOf(backStackEntry.destination.route ?: return@collect)
                    topBarTitle = page.title
                    toolbarVisible = page.showToolBar
                }
            }
            MaterialTheme(colors = MaterialTheme.colors.copy(primary = primaryColor)) {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        if (toolbarVisible) {
                            TopAppBar(title = { Text(text = topBarTitle) })
                        }
                    },
                    content = {
                        AppNavHost(
                            modifier = Modifier.padding(it),
                            navController = navController,
                            showSnack = { message ->
                                coroutineScope.launch {
                                    scaffoldState.snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                )
            }
        }
    }

    @Composable
    private fun AppNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        showSnack: (String) -> Unit = {},
    ) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = Page.List.name
        ) {
            composable(Page.List.name) { ListPage { page -> navController.navigate(page.name) } }
            composable(Page.Example1.name) { Example1Page() }
            composable(Page.Example2.name) {
                Example2Page(
                    close = { navController.popBackStack() },
                    dateSelected = { startDate, endDate ->
                        navController.popBackStack()
                        showSnack(dateRangeDisplayText(startDate, endDate))
                    })
            }
        }
    }

    @Composable
    private fun ListPage(navigate: (Page) -> Unit) {
        LazyColumn {
            items(items) { item ->
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clickable { navigate(item) }
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
}