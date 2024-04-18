package com.cookiedinner.boxanizer.core.data

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import java.util.Locale

class DataStoreManager(private val context: Context) {
    private val Context._dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context._dataStore

    private val themeKey = intPreferencesKey("theme")
    private val dynamicThemeKey = booleanPreferencesKey("dynamic_theme")

    enum class ThemeChoice {
        SYSTEM,
        DARK,
        LIGHT
    }

    enum class LanguageChoice(val code: String) {
        ENGLISH("en"),
        POLISH("pl")
    }

    @Composable
    fun collectThemeWithLifecycle() = dataStore.data.map { preferences ->
        ThemeChoice.entries[preferences[themeKey] ?: 0]
    }.collectAsStateWithLifecycle(initialValue = ThemeChoice.SYSTEM)

    suspend fun switchTheme(newTheme: ThemeChoice) {
        dataStore.edit { preferences ->
            preferences[themeKey] = newTheme.ordinal
        }
    }

    @Composable
    fun collectDynamicThemeWithLifecycle() = dataStore.data.map { preferences ->
        preferences[dynamicThemeKey] ?: false
    }.collectAsStateWithLifecycle(initialValue = false)

    suspend fun switchDynamicTheme() {
        dataStore.edit { preferences ->
            preferences[dynamicThemeKey] = !(preferences[dynamicThemeKey] ?: false)
        }
    }

    fun switchLanguage(newLanguage: LanguageChoice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(newLanguage.code)
        } else {
            val locale = Locale(newLanguage.code)
            Locale.setDefault(locale)

            val resources = context.resources
            val configuration = resources.configuration
            configuration.setLocale(locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
}