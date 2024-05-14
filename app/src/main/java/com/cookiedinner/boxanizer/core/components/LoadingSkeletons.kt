package com.cookiedinner.boxanizer.core.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import org.koin.compose.koinInject

@Composable
fun BasicSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    val dataStoreManager: DataStoreManager = koinInject()
    val currentTheme = dataStoreManager.collectThemeWithLifecycle()
    val transition = rememberInfiniteTransition(label = "Infinite Shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = -200f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            RepeatMode.Restart
        ),
        label = "Shimmer"
    )
    val shimmerColors = if (currentTheme.value == DataStoreManager.ThemeChoice.DARK || (currentTheme.value == DataStoreManager.ThemeChoice.SYSTEM && isSystemInDarkTheme())) {
        listOf(
            Color.DarkGray.copy(alpha = 0.8f),
            Color.DarkGray.copy(alpha = 0.2f),
        )
    } else {
        listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.1f),
        )
    }
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation.value, translateAnimation.value),
        end = Offset(translateAnimation.value + 300f, translateAnimation.value + 300f),
        tileMode = TileMode.Mirror,
    )
    Box(modifier = modifier.background(brush, shape))
}

@Composable
fun ListSkeleton(
    itemHeight: Dp = 96.dp,
    itemShape: Shape = MaterialTheme.shapes.medium
) {
    LazyColumn(
        userScrollEnabled = false,
        contentPadding = PaddingValues(12.dp)
    ) {
        items(10) {
            BasicSkeleton(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .height(itemHeight),
                shape = itemShape
            )
        }
    }
}