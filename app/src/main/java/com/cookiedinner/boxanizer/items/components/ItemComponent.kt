package com.cookiedinner.boxanizer.items.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.components.AnimatedCounter
import com.cookiedinner.boxanizer.core.components.CustomBadge
import com.cookiedinner.boxanizer.core.components.CustomBadgedBox
import com.cookiedinner.boxanizer.core.components.DeletableCard
import com.cookiedinner.boxanizer.core.models.Direction
import com.cookiedinner.boxanizer.core.utilities.trimNewLines
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.items.models.ItemAction

@Composable
fun ItemComponent(
    modifier: Modifier = Modifier,
    item: Item,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    DeletableCard(
        modifier = modifier,
        onClick = onClick,
        onDelete = onDelete
    ) {
        ItemContent(item = item)
    }
}

@Composable
fun ItemComponent(
    modifier: Modifier = Modifier,
    item: Item,
    onClick: () -> Unit
) {
    DeletableCard(
        modifier = modifier,
        onClick = onClick,
        onDelete = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemContent(
                modifier = Modifier.weight(1f),
                item = item
            )
            if (item.id == -1L) {
                Icon(imageVector = Icons.Default.NewReleases, contentDescription = "")
            }
        }
    }
}

@Composable
fun ItemComponent(
    modifier: Modifier = Modifier,
    itemInBox: ItemInBox,
    onClick: () -> Unit,
    onAction: (ItemAction, () -> Unit) -> Unit,
) {
    var interactable by remember {
        mutableStateOf(true)
    }
    var cardExpanded by remember {
        mutableStateOf(false)
    }
    DeletableCard(
        modifier = modifier,
        onClick = onClick,
        onDelete = {
            onAction(ItemAction.DELETE) {}
        },
        onExpanded = {
            cardExpanded = it
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemContent(
                modifier = Modifier.weight(1f),
                item = Item(
                    id = itemInBox.id,
                    name = itemInBox.name,
                    description = itemInBox.description,
                    image = itemInBox.image,
                    consumable = itemInBox.consumable
                )
            )
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                FilledIconButton(
                    onClick = {
                        interactable = false
                        if (cardExpanded) {
                            onAction(ItemAction.REMOVE) {
                                interactable = true
                            }
                        } else {
                            onAction(ItemAction.RETURN) {
                                interactable = true
                            }
                        }
                    },
                    enabled = interactable && if (cardExpanded)
                        itemInBox.amountInBox > 1 && itemInBox.amountInBox > itemInBox.amountRemovedFromBox
                    else
                        itemInBox.amountRemovedFromBox > 0,
                    colors = IconButtonDefaults.filledTonalIconButtonColors()
                ) {
                    Icon(
                        modifier = Modifier.rotate(if (cardExpanded) 0f else 90f),
                        imageVector = if (cardExpanded) Icons.Default.Remove else Icons.Default.SubdirectoryArrowRight,
                        contentDescription = ""
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedVisibility(visible = itemInBox.amountRemovedFromBox > 0) {
                        Icon(
                            modifier = Modifier
                                .padding(start = 14.dp, bottom = 12.dp)
                                .rotate(180f),
                            imageVector = Icons.Default.SubdirectoryArrowLeft,
                            contentDescription = ""
                        )
                    }
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        AnimatedCounter(
                            modifier = Modifier
                                .padding(horizontal = 14.dp)
                                .defaultMinSize(minWidth = (9 * itemInBox.amountInBox.toString().length).dp, minHeight = 36.dp),
                            number = itemInBox.amountInBox - itemInBox.amountRemovedFromBox,
                            incrementDirection = Direction.DOWN
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = itemInBox.amountRemovedFromBox > 0) {
                        AnimatedCounter(
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .defaultMinSize(minWidth = 24.dp, minHeight = 12.dp),
                            number = itemInBox.amountRemovedFromBox,
                            incrementDirection = Direction.RIGHT
                        )
                    }
                    FilledIconButton(
                        onClick = {
                            interactable = false
                            if (cardExpanded) {
                                onAction(ItemAction.ADD) {
                                    interactable = true
                                }
                            } else {
                                onAction(ItemAction.BORROW) {
                                    interactable = true
                                }
                            }
                        },
                        enabled = interactable && (cardExpanded || itemInBox.amountRemovedFromBox < itemInBox.amountInBox),
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(
                            imageVector = if (cardExpanded) Icons.Default.Add else Icons.Default.NorthEast,
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemContent(
    modifier: Modifier = Modifier,
    item: Item
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomBadgedBox(
            percentageMoved = 0.3f,
            badge = {
                if (item.image != null) {
                    CustomBadge(
                        color = if (item.consumable) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (item.consumable) Icons.Default.Science else Icons.Default.Construction,
                            contentDescription = ""
                        )
                    }
                }
            }
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .heightIn(0.dp, 48.dp)
                    .width(48.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .then(
                        if (item.image != null)
                            Modifier.border(
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                        else
                            Modifier
                    ),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.image)
                    .crossfade(true)
                    .size(Size.ORIGINAL)
                    .build(),
                contentScale = ContentScale.FillBounds,
                error = {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = if (item.consumable) Icons.Default.Science else Icons.Default.Construction,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    }
                },
                contentDescription = stringResource(R.string.box_picture)
            )
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (item.description != null) {
                Text(
                    text = item.description.trimNewLines(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}