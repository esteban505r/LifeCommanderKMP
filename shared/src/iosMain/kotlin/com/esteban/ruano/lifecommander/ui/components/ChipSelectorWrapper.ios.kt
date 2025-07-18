package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ChipSelectorWrapper(modifier: Modifier,
                               scrollState: LazyListState,
                               content: @Composable (() -> Unit)
) {
    content()
}