package com.cookiedinner.boxanizer.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
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
    padding: PaddingValues = PaddingValues(20.dp),
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onExpanded: (Boolean) -> Unit = {},
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
            onDelete?.invoke()
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
                        onExpanded(false)
                        visible.targetState = false
                    },
                color = MaterialTheme.colorScheme.error,
                shape = MaterialTheme.shapes.large,
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
            val highlightedElevation = animateDpAsState(targetValue = if (highlighted) 12.dp else 0.dp)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        this.translationX = -animatedExpandedLength
                    },
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(highlightedElevation.value)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (onClick != null || onDelete != null) {
                                Modifier.combinedClickable(
                                    onClick = {
                                        if (!expanded)
                                            onClick?.invoke()
                                        else {
                                            expanded = false
                                            onExpanded(false)
                                        }
                                    },
                                    onLongClick = {
                                        if (onDelete != null) {
                                            expanded = !expanded
                                            onExpanded(expanded)
                                        }
                                    }
                                )
                            } else Modifier

                        )
                        .padding(padding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content()
                }
            }
        }
    }
}