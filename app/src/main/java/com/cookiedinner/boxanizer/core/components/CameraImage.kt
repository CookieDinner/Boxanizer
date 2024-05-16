package com.cookiedinner.boxanizer.core.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R

@Composable
fun CameraImage(
    image: ByteArray?,
    photoLoading: Boolean,
    onEditImage: () -> Unit,
    onDeleteImage: () -> Unit,
    addImageLabel: String = "",
    imageLabel: String = ""
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Surface(
        modifier = Modifier
            .clickable(!photoLoading) {
                if (image != null) {
                    expanded = !expanded
                } else {
                    onEditImage()
                }
            },
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        AnimatedContent(
            targetState = when {
                photoLoading -> 0
                image == null -> 1
                else -> 2
            }, label = ""
        ) {
            when (it) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(48.dp))
                    }
                }

                1 -> {
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = addImageLabel
                        )
                    }
                }

                else -> {
                    AnimatedContent(
                        targetState = expanded,
                        transitionSpec = {
                            if (targetState) {
                                (fadeIn(animationSpec = tween(220, 90)) +
                                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, 90)) +
                                        slideInVertically(tween(220, 90)) { -it / 2 })
                                    .togetherWith(fadeOut(animationSpec = tween(90)))
                            } else {
                                (fadeIn(animationSpec = tween(220, 90)) +
                                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, 90)) +
                                        slideInVertically(tween(220, 90)) { it / 2 })
                                    .togetherWith(fadeOut(animationSpec = tween(90)))
                            }
                        }, label = ""
                    ) {
                        Box(contentAlignment = Alignment.CenterEnd) {
                            SubcomposeAsyncImage(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .then(if (it) Modifier else Modifier.height(180.dp))
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.extraSmall),
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(image)
                                    .crossfade(true)
                                    .size(Size.ORIGINAL)
                                    .build(),
                                contentScale = ContentScale.FillWidth,
                                error = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(48.dp),
                                            imageVector = Icons.Filled.ImageNotSupported,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            contentDescription = stringResource(R.string.error)
                                        )
                                    }
                                },
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(Modifier.size(48.dp))
                                    }
                                },
                                contentDescription = imageLabel
                            )
                            Column(
                                modifier = Modifier.matchParentSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                OutlinedIconButton(
                                    modifier = Modifier.padding(4.dp),
                                    onClick = onDeleteImage,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = ""
                                    )
                                }
                                OutlinedIconButton(
                                    modifier = Modifier.padding(4.dp),
                                    onClick = {
                                        onEditImage()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.BorderColor,
                                        contentDescription = ""
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}