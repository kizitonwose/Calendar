import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val primaryColor = Colors.primary
            var toolBarTitle by remember { mutableStateOf("") }
            var toolBarVisible by remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val page = Page.valueOf(backStackEntry.destination.route ?: return@collect)
                    toolBarTitle = page.title
                    toolBarVisible = page.showToolBar
                }
            }
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = primaryColor)) {
                Scaffold(
                    modifier = Modifier.widthIn(max = 600.dp),
                    topBar = {
                        if (toolBarVisible) {
                            AppToolBar(title = toolBarTitle, navController)
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                    content = {
                        AppNavHost(
                            modifier = Modifier.padding(it),
                            navController = navController,
                            showSnack = { message ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppToolBar(title: String, navController: NavHostController) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(),
        title = { Text(text = title) },
        navigationIcon = navIcon@{
            val destination = navController.currentBackStackEntry?.destination
            val page = Page.valueOf(destination?.route ?: return@navIcon)
            if (page == Page.List) {
                Unit
            } else {
                NavigationIcon {
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
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Page.List.name,
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
                },
            )
        }
        verticallyAnimatedComposable(Page.Example3.name) { Example3Page() }
        horizontallyAnimatedComposable(Page.Example4.name) { Example4Page() }
        horizontallyAnimatedComposable(Page.Example5.name) { Example5Page { navController.popBackStack() } }
        horizontallyAnimatedComposable(Page.Example6.name) { Example6Page() }
        horizontallyAnimatedComposable(Page.Example7.name) { Example7Page() }
        horizontallyAnimatedComposable(Page.Example9.name) { Example9Page() }
    }
}
