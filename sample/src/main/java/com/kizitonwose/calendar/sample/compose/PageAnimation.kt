package com.kizitonwose.calendar.sample.compose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

private const val ANIM_DURATION_MILLIS = 400

fun NavGraphBuilder.horizontallyAnimatedComposable(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        content = content,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
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
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION_MILLIS))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION_MILLIS))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(ANIM_DURATION_MILLIS),
            )
        },
    )
}
