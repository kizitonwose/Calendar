package com.kizitonwose.calendar.sample.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedContentScope.SlideDirection.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.*
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.kizitonwose.calendar.sample.R
import com.kizitonwose.calendar.sample.dateRangeDisplayText
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
class CalendarComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val primaryColor = colorResource(id = R.color.colorPrimary)
            var toolBarTitle by remember { mutableStateOf("") }
            var toolBarVisible by remember { mutableStateOf(true) }
            val navController = rememberAnimatedNavController()
            val coroutineScope = rememberCoroutineScope()
            val scaffoldState = rememberScaffoldState()
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val page = Page.valueOf(backStackEntry.destination.route ?: return@collect)
                    toolBarTitle = page.title
                    toolBarVisible = page.showToolBar
                }
            }
            MaterialTheme(colors = MaterialTheme.colors.copy(primary = primaryColor)) {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        if (toolBarVisible) {
                            AppToolBar(title = toolBarTitle, navController)
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
    private fun AppToolBar(title: String, navController: NavHostController) {
        TopAppBar(
            title = { Text(text = title) },
            navigationIcon = {
                NavigationIcon icon@{
                    val destination =
                        navController.currentBackStackEntry?.destination
                    val page = Page.valueOf(destination?.route ?: return@icon)
                    if (page == Page.List) {
                        finishAfterTransition()
                    } else {
                        navController.popBackStack()
                    }
                }
            },
        )
    }

    @Composable
    private fun AppNavHost(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        showSnack: (String) -> Unit = {},
    ) {
        AnimatedNavHost(
            modifier = modifier,
            navController = navController,
            startDestination = Page.List.name
        ) {
            composable(Page.List.name) {
                ListPage { page -> navController.navigate(page.name) }
            }
            horizontallyAnimatedComposable(Page.Example1.name) { Example1Page() }
            verticallyAnimatedComposable(Page.Example2.name) {
                Example2Page(
                    close = { navController.popBackStack() },
                    dateSelected = { startDate, endDate ->
                        navController.popBackStack()
                        showSnack(dateRangeDisplayText(startDate, endDate))
                    }
                )
            }
            verticallyAnimatedComposable(Page.Example3.name) { Example3Page() }
            horizontallyAnimatedComposable(Page.Example4.name) { Example4Page() }
            horizontallyAnimatedComposable(Page.Example5.name) { Example5Page { navController.popBackStack() } }
            horizontallyAnimatedComposable(Page.Example6.name) { Example6Page() }
            horizontallyAnimatedComposable(Page.Example7.name) { Example7Page() }
        }
    }
}
