package com.cookiedinner.boxanizer.items.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.components.DeletableCard
import com.cookiedinner.boxanizer.core.utilities.trimNewLines
import com.cookiedinner.boxanizer.database.Item

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