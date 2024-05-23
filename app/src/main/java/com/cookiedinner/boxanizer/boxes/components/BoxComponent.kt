package com.cookiedinner.boxanizer.boxes.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxWithItem

@Composable
fun BoxComponent(
    modifier: Modifier = Modifier,
    box: Box,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    DeletableCard(
        modifier = modifier,
        onClick = onClick,
        onDelete = onDelete
    ) {
        BoxContent(box = box)
    }
}

@Composable
fun BoxComponent(
    modifier: Modifier = Modifier,
    boxWithItem: BoxWithItem,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxContent(
                box = Box(
                    id = boxWithItem.id,
                    code = boxWithItem.code,
                    name = boxWithItem.name,
                    description = boxWithItem.description,
                    image = boxWithItem.image
                )
            )
        }
    }
}

@Composable
private fun BoxContent(box: Box) {
    SubcomposeAsyncImage(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .then(
                if (box.image != null)
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
        modifier = Modifier.padding(start = 16.dp)
    ) {
        Text(
            text = box.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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