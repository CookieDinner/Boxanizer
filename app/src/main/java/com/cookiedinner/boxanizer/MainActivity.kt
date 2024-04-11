package com.cookiedinner.boxanizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.navigation.AppNavigationScreen
import com.cookiedinner.boxanizer.core.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val dataStoreManager: DataStoreManager by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme = dataStoreManager.collectThemeWithLifecycle()
            val dynamicTheme = dataStoreManager.collectDynamicThemeWithLifecycle()
            AppTheme(
                darkTheme = when(theme.value) {
                    DataStoreManager.ThemeChoice.SYSTEM -> isSystemInDarkTheme()
                    DataStoreManager.ThemeChoice.DARK -> true
                    DataStoreManager.ThemeChoice.LIGHT -> false
                },
                dynamicColor = dynamicTheme.value
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    AppNavigationScreen()
}