package com.cookiedinner.boxanizer.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroup(
    title: String,
    preferences: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 12.dp, bottom = 20.dp)
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            ),
        )
        preferences()
    }
}