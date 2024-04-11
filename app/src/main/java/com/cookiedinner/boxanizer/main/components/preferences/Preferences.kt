package com.cookiedinner.boxanizer.main.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextPreference(
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.82f)
                .padding(
                    horizontal = 8.dp,
                    vertical = 16.dp
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            if (description != null) {
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = description,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Normal
                    ),
                )
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    description: String,
    currentValue: Boolean,
    onSwitched: (Boolean) -> Unit
) {
    Box(contentAlignment = Alignment.CenterEnd) {
        TextPreference(
            title = title,
            description = description
        ) {
            onSwitched(!currentValue)
        }
        Box(modifier = Modifier.fillMaxWidth(0.15f)) {
            Switch(
                modifier = Modifier
                    .scale(0.8f)
                    .padding(end = 8.dp),
                checked = currentValue,
                onCheckedChange = {
                    onSwitched(!currentValue)
                }
            )
        }
    }

}