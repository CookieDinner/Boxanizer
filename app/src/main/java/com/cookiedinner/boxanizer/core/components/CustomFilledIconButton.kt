package com.cookiedinner.boxanizer.core.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cookiedinner.boxanizer.core.utilities.getContainerColor
import com.cookiedinner.boxanizer.core.utilities.getContentColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFilledIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    enabled: Boolean = true,
    shape: Shape = IconButtonDefaults.filledShape,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) = Surface(
    modifier = modifier
        .minimumInteractiveComponentSize()
        .semantics { role = Role.Button }
        .clip(shape)
        .combinedClickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = onClick,
            onLongClick = onLongClick
        ),
    shape = shape,
    color = colors.getContainerColor(enabled),
    contentColor = colors.getContentColor(enabled),
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}