package com.cookiedinner.boxanizer.main.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.utilities.trimNewLines

@Composable
fun BoxComponent(
    modifier: Modifier = Modifier,
    box: Box,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
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