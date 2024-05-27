package com.cookiedinner.boxanizer.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun CustomBadgedBox(
    modifier: Modifier = Modifier,
    percentageMoved: Float = 0.5f,
    badge: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    var badgeSize by remember {
        mutableStateOf(IntSize(0, 0))
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopEnd
    ) {
        content()
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    badgeSize = it.size
                }
                .graphicsLayer {
                    this.translationY = -(badgeSize.height * percentageMoved)
                    this.translationX = (badgeSize.width * percentageMoved)
                }
        ) {
            badge()
        }
    }
}
@Composable
fun CustomBadge(
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .height(18.dp)
            .clip(CircleShape)
            .background(color)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColorFor(backgroundColor = color)) {
            content()
        }
    }
}