import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
@Preview
fun App() {
    MaterialTheme(SampleColorScheme) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (maxWidth >= 600.dp) {
                val widthPx = maxWidth.value.roundToInt()
                val count = if (widthPx in 650..800) 2 else widthPx / 400
                Row {
                    repeat(count) { index ->
                        Demo(modifier = Modifier.weight(1f))
                        if (index < count - 1) {
                            VerticalDivider()
                        }
                    }
                }
            } else {
                Demo(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Demo(modifier: Modifier = Modifier) {
    var toolBarTitle by remember { mutableStateOf("") }
    var toolBarVisible by remember { mutableStateOf(true) }
    var toolBarBackButtonVisible by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val page = Page.valueOf(backStackEntry.destination.route ?: return@collect)
            toolBarTitle = page.title
            toolBarVisible = page.showToolBar
            toolBarBackButtonVisible = page != Page.List
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            if (toolBarVisible) {
                Column {
                    ExampleToolbar(
                        title = toolBarTitle,
                        colors = if (isMobile()) blueToolbar else whiteToolbar,
                        navigationIcon = navIcon@{
                            if (toolBarBackButtonVisible) {
                                NavigationIcon(
                                    tint = if (isMobile()) Color.White else Color.Black,
                                ) {
                                    navController.popBackStack()
                                }
                            }
                        },
                    )
                    // Add divider to separate the white toolbar.
                    if (!isMobile()) {
                        HorizontalDivider()
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = {
            CompositionLocalProvider(LocalScaffoldPaddingValues provides it) {
                AppNavHost(
                    navController = navController,
                    showSnack = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExampleToolbar(
    title: String,
    colors: TopAppBarColors = blueToolbar,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
) = TopAppBar(
    modifier = modifier,
    colors = colors,
    title = {
        Text(text = title)
    },
    navigationIcon = navigationIcon,
)

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
        verticallyAnimatedComposable(Page.Example3.name) { Example3Page { navController.popBackStack() } }
        horizontallyAnimatedComposable(Page.Example4.name) { Example4Page() }
        horizontallyAnimatedComposable(Page.Example5.name) { Example5Page { navController.popBackStack() } }
        horizontallyAnimatedComposable(Page.Example6.name) { Example6Page() }
        horizontallyAnimatedComposable(Page.Example7.name) { Example7Page() }
        horizontallyAnimatedComposable(Page.Example8.name) { Example8Page { navController.popBackStack() } }
        horizontallyAnimatedComposable(Page.Example9.name) { Example9Page() }
        horizontallyAnimatedComposable(Page.Example10.name) { Example10Page() }
        horizontallyAnimatedComposable(Page.Example11.name) { Example11Page() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val blueToolbar
    @Composable
    get() = TopAppBarDefaults.topAppBarColors(
        containerColor = Colors.primary,
        titleContentColor = Color.White,
    )

@OptIn(ExperimentalMaterial3Api::class)
private val whiteToolbar
    @Composable
    get() = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.White,
        titleContentColor = Color.Black,
    )
