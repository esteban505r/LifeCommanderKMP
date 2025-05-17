package com.esteban.ruano.lifecommander.ui.components

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun ChipSelectorWrapper(modifier:Modifier, scrollState: LazyListState, content: @Composable () -> Unit) {
    Box{
        content()
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
        )
    }
}