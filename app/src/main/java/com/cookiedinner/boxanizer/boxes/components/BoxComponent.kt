package com.cookiedinner.boxanizer.boxes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.utilities.trimNewLines
import com.cookiedinner.boxanizer.database.Box

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun BoxComponent(
    modifier: Modifier = Modifier,
    box: Box,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable {
        mutableStateOf(false)
    }
    var deleted by rememberSaveable {
        mutableStateOf(false)
    }
    val lengthToExpand = with(LocalDensity.current) {
        72.dp.toPx()
    }
    val animatedExpandedLength by animateFloatAsState(
        targetValue = if (expanded) lengthToExpand else 0f,
        label = "Animated Expanded Length"
    )
    AnimatedVisibility(
        visible = !deleted,
        exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkOut() + fadeOut()
    ) {
        if (transition.currentState == EnterExitState.PostExit) {
            onDelete()
        }
        Box(
            modifier = modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        expanded = false
                        deleted = true
                    },
                color = MaterialTheme.colorScheme.error,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(24.dp)
                            .size(24.dp),
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Delete box"
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
                    var hasImage by remember {
                        mutableStateOf(false)
                    }
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .then(
                                if (hasImage)
                                    Modifier.border(
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                else
                                    Modifier
                            ),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(box.image)
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentScale = ContentScale.FillBounds,
                        onSuccess = {
                            hasImage = true
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(Modifier.size(36.dp))
                            }
                        },
                        contentDescription = stringResource(R.string.box_picture)
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = box.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (box.description != null) {
                            Text(
                                text = box.description.trimNewLines(),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}