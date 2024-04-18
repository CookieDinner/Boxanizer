package com.cookiedinner.boxanizer.main.screens

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.utilities.getPackageInfo
import com.cookiedinner.boxanizer.main.components.preferences.PreferenceGroup
import com.cookiedinner.boxanizer.main.components.preferences.SwitchPreference
import com.cookiedinner.boxanizer.main.components.preferences.TextPreference
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun SettingsScreen(
    dataStoreManager: DataStoreManager = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()

    val dynamicTheme = dataStoreManager.collectDynamicThemeWithLifecycle()
    val currentTheme = dataStoreManager.collectThemeWithLifecycle()

    SettingsScreenContent(
        currentTheme = currentTheme.value,
        onThemeChoice = {
            coroutineScope.launch {
                dataStoreManager.switchTheme(it)
            }
        },
        dynamicTheme = dynamicTheme.value,
        onDynamicThemeClick = {
            coroutineScope.launch {
                dataStoreManager.switchDynamicTheme()
            }
        },
        onLanguageClick = {
            coroutineScope.launch {
                dataStoreManager.switchLanguage(it)
            }
        }
    )
}

@Composable
private fun SettingsScreenContent(
    currentTheme: DataStoreManager.ThemeChoice,
    onThemeChoice: (DataStoreManager.ThemeChoice) -> Unit,
    dynamicTheme: Boolean,
    onDynamicThemeClick: (Boolean) -> Unit,
    onLanguageClick: (DataStoreManager.LanguageChoice) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {
        item {
            PreferenceGroup(title = stringResource(R.string.personalization)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SwitchPreference(
                        title = stringResource(R.string.pref_dynamic_colors),
                        description = stringResource(R.string.pref_dynamic_colors_description),
                        currentValue = dynamicTheme,
                        onSwitched = onDynamicThemeClick
                    )
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    var themeDropdownVisible by remember {
                        mutableStateOf(false)
                    }
                    TextPreference(
                        title = stringResource(R.string.pref_theme),
                        description = when (currentTheme) {
                            DataStoreManager.ThemeChoice.SYSTEM -> stringResource(R.string.system_theme)
                            DataStoreManager.ThemeChoice.DARK -> stringResource(R.string.dark_theme)
                            DataStoreManager.ThemeChoice.LIGHT -> stringResource(R.string.light_theme)
                        }
                    ) {
                        themeDropdownVisible = true
                    }
                    Box {
                        DropdownMenu(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            expanded = themeDropdownVisible,
                            onDismissRequest = { themeDropdownVisible = false }
                        ) {
                            DataStoreManager.ThemeChoice.entries.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            modifier = Modifier.padding(6.dp),
                                            text = when (it) {
                                                DataStoreManager.ThemeChoice.SYSTEM -> stringResource(R.string.system_theme)
                                                DataStoreManager.ThemeChoice.DARK -> stringResource(R.string.dark_theme)
                                                DataStoreManager.ThemeChoice.LIGHT -> stringResource(R.string.light_theme)
                                            }
                                        )
                                    },
                                    onClick = {
                                        themeDropdownVisible = false
                                        onThemeChoice(it)
                                    }
                                )
                            }
                        }
                    }
                }
                Box(contentAlignment = Alignment.TopEnd) {
                    var languageDropdownVisible by remember {
                        mutableStateOf(false)
                    }
                    TextPreference(
                        title = stringResource(R.string.language),
                        description = when (Locale.current.language) {
                            DataStoreManager.LanguageChoice.POLISH.code -> stringResource(R.string.language_pl)
                            else -> stringResource(R.string.language_en)
                        }
                    ) {
                        languageDropdownVisible = true
                    }
                    Box {
                        DropdownMenu(
                            modifier = Modifier.fillMaxWidth(0.5f),
                            expanded = languageDropdownVisible,
                            onDismissRequest = { languageDropdownVisible = false }
                        ) {
                            DataStoreManager.LanguageChoice.entries.forEach {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            modifier = Modifier.padding(6.dp),
                                            text = java.util.Locale(it.code).displayName.replaceFirstChar { it.titlecase() }
                                        )
                                    },
                                    onClick = {
                                        languageDropdownVisible = false
                                        onLanguageClick(it)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            PreferenceGroup(title = stringResource(R.string.about)) {
                TextPreference(
                    title = stringResource(R.string.app_version),
                    description = getPackageInfo(context, 0).versionName
                )
                TextPreference(
                    title = stringResource(R.string.check_for_updates)
                ) {

                }
            }
        }
    }
}