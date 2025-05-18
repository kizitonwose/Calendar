package com.kizitonwose.calendar.sample.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

val LocalScaffoldPaddingValues = compositionLocalOf { PaddingValues() }

@Composable
fun Modifier.applyScaffoldHorizontalPaddings() =
    padding(
        start = LocalScaffoldPaddingValues.current.calculateStartPadding(LocalLayoutDirection.current),
        end = LocalScaffoldPaddingValues.current.calculateEndPadding(LocalLayoutDirection.current),
    )

@Composable
fun Modifier.applyScaffoldTopPadding() =
    padding(top = LocalScaffoldPaddingValues.current.calculateTopPadding())

@Composable
fun Modifier.applyScaffoldBottomPadding() =
    padding(bottom = LocalScaffoldPaddingValues.current.calculateBottomPadding())
