package com.kizitonwose.calendarsample.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kizitonwose.calendarsample.R
import com.kizitonwose.calendarsample.dateRangeDisplayText
import kotlinx.coroutines.launch

class CalendarComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val primaryColor = colorResource(id = R.color.colorPrimary)
            var topBarTitle by remember { mutableStateOf("") }
            var toolbarVisible by remember { mutableStateOf(true) }
            var backButtonVisible by remember { mutableStateOf(true) }
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val scaffoldState = rememberScaffoldState()
            LaunchedEffect(navController) {
                navController.currentBackStackEntryFlow.collect { backStackEntry ->
                    val page = Page.valueOf(backStackEntry.destination.route ?: return@collect)
                    topBarTitle = page.title
                    toolbarVisible = page.showToolBar
                    backButtonVisible = page != Page.List
                }
            }
            MaterialTheme(colors = MaterialTheme.colors.copy(primary = primaryColor)) {
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        if (toolbarVisible) {
                            TopAppBar(
                                title = { Text(text = topBarTitle) },
                                navigationIcon = if (backButtonVisible) {
                                    { NavigationIcon { navController.popBackStack() } }
                                } else null,
                            )
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
}

@Composable
private fun NavigationIcon(onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .padding(4.dp)
        .clip(shape = CircleShape)
        .clickable(role = Role.Button, onClick = onClick)) {
        Icon(
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center),
            imageVector = Icons.Default.ArrowBack,
            contentDescription = null
        )
    }
}