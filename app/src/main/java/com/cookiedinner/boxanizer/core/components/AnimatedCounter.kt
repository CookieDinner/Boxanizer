package com.cookiedinner.boxanizer.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cookiedinner.boxanizer.core.models.Digit
import com.cookiedinner.boxanizer.core.models.Direction
import com.cookiedinner.boxanizer.core.models.compareTo

@Composable
fun AnimatedCounter(
    modifier: Modifier = Modifier,
    number: Long,
    incrementDirection: Direction = Direction.UP
) {
    Row(
        modifier = Modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = number,
            transitionSpec = {
                if (targetState > initialState) {
                    when (incrementDirection) {
                        Direction.UP -> slideInVertically { it }
                        Direction.DOWN -> slideInVertically { -it }
                        Direction.LEFT -> slideInHorizontally { it * 2 }
                        Direction.RIGHT -> slideInHorizontally { -it * 2 }
                    } + fadeIn(tween(200)) togetherWith
                            when (incrementDirection) {
                                Direction.UP -> slideOutVertically { -it }
                                Direction.DOWN -> slideOutVertically { it }
                                Direction.LEFT -> slideOutHorizontally { -it * 2 }
                                Direction.RIGHT -> slideOutHorizontally { it * 2 }
                            } + fadeOut(tween(150))
                } else {
                    when (incrementDirection) {
                        Direction.UP -> slideInVertically { -it }
                        Direction.DOWN -> slideInVertically { it }
                        Direction.LEFT -> slideInHorizontally { -it * 2 }
                        Direction.RIGHT -> slideInHorizontally { it * 2 }
                    } + fadeIn(tween(200)) togetherWith
                            when (incrementDirection) {
                                Direction.UP -> slideOutVertically { it }
                                Direction.DOWN -> slideOutVertically { -it }
                                Direction.LEFT -> slideOutHorizontally { it * 2 }
                                Direction.RIGHT -> slideOutHorizontally { -it * 2 }
                            } + fadeOut(tween(150))
                }
            }
        ) { number ->
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}