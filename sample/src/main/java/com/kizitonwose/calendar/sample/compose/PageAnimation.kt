@file:OptIn(ExperimentalAnimationApi::class)

package com.kizitonwose.calendar.sample.compose

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable

private const val animDurationMillis = 400
private typealias SlideDirection = AnimatedContentScope.SlideDirection

fun NavGraphBuilder.horizontallyAnimatedComposable(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        content = content,
        enterTransition = {
            slideIntoContainer(SlideDirection.Left, animationSpec = tween(animDurationMillis))
        },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Left, animationSpec = tween(animDurationMillis))
        },
        popEnterTransition = {
            slideIntoContainer(SlideDirection.Right, animationSpec = tween(animDurationMillis))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.Right, animationSpec = tween(animDurationMillis))
        },
    )
}

fun NavGraphBuilder.verticallyAnimatedComposable(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        content = content,
        enterTransition = {
            slideIntoContainer(SlideDirection.Up, animationSpec = tween(animDurationMillis))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(animDurationMillis))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(animDurationMillis))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.Down, animationSpec = tween(animDurationMillis))
        },
    )
}
