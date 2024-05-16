package com.cookiedinner.boxanizer.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cookiedinner.boxanizer.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeletableCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    val visible = remember {
        MutableTransitionState(true)
    }
    val lengthToExpand = with(LocalDensity.current) {
        72.dp.toPx()
    }
    val animatedExpandedLength by animateFloatAsState(
        targetValue = if (expanded) lengthToExpand else 0f,
        label = "Animated Expanded Length"
    )
    LaunchedEffect(visible.currentState) {
        if (!visible.currentState) {
            onDelete()
        }
    }
    AnimatedVisibility(
        visibleState = visible,
        exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkOut() + fadeOut()
    ) {
        Box(
            modifier = modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .matchParentSize()
                    .clickable {
                        expanded = false
                        visible.targetState = false
                    },
                color = MaterialTheme.colorScheme.error,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight(0.9f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(end = 24.dp),
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.translationX = -animatedExpandedLength
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .combinedClickable(
                            onClick = {
                                if (!expanded)
                                    onClick()
                                else
                                    expanded = false
                            },
                            onLongClick = {
                                expanded = !expanded
                            }
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                }
            }
        }
    }
}