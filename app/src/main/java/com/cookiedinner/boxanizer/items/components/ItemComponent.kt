package com.cookiedinner.boxanizer.items.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.components.DeletableCard
import com.cookiedinner.boxanizer.core.models.Digit
import com.cookiedinner.boxanizer.core.models.compareTo
import com.cookiedinner.boxanizer.core.utilities.trimNewLines
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox

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
    itemInBox: ItemInBox,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onAdded: () -> Unit,
    onRemoved: () -> Unit,
    onBorrowed: () -> Unit,
    onReturned: () -> Unit
) {
    DeletableCard(
        modifier = modifier,
        padding = PaddingValues(4.dp),
        onClick = onClick,
        onDelete = onDelete
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemContent(
                item = Item(
                    id = itemInBox.id,
                    name = itemInBox.name,
                    description = itemInBox.description,
                    image = itemInBox.image
                )
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var more by remember {
                        mutableLongStateOf(itemInBox.amountRemovedFromBox)
                    }
                    FilledIconButton(
                        onClick = { more-- },
                        enabled = more > 0,
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(imageVector = Icons.Default.SouthWest, contentDescription = "")
                    }
                    Row(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        more.toString()
                            .mapIndexed { index, char -> Digit(char, more, index) }
                            .forEach { listDigit ->
                                AnimatedContent(
                                    targetState = listDigit,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                        } else {
                                            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                        }
                                    }
                                ) { digit ->
                                    Text(
                                        modifier = Modifier.defaultMinSize(minWidth = 12.dp),
                                        text = digit.digitChar.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                    }
                    FilledIconButton(
                        onClick = { more++ },
                        enabled = more < itemInBox.amountInBox,
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(imageVector = Icons.Default.NorthEast, contentDescription = "")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { /*TODO*/ },
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "")
                    }
                    Text(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 12.dp)
                            .padding(horizontal = 8.dp),
                        text = itemInBox.amountInBox.toString(),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FilledIconButton(
                        onClick = { /*TODO*/ },
                        colors = IconButtonDefaults.filledTonalIconButtonColors()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "")
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemContent(item: Item) {
    Row {
        var hasImage by remember {
            mutableStateOf(false)
        }
        SubcomposeAsyncImage(
            modifier = Modifier
                .heightIn(0.dp, 48.dp)
                .width(48.dp)
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
                .data(item.image)
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build(),
            contentScale = ContentScale.FillBounds,
            onSuccess = {
                hasImage = true
            },
            error = {
                Icon(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Default.Construction,
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
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
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