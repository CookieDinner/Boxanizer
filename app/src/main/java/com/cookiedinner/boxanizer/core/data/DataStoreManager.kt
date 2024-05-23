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
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.map
import java.util.Locale

class DataStoreManager(private val context: Context) {
    private val Context._dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val dataStore = context._dataStore

    private val themeKey = intPreferencesKey("theme")
    private val dynamicThemeKey = booleanPreferencesKey("dynamic_theme")
    private val barcodesKey = stringSetPreferencesKey("barcodes")

    enum class ThemeChoice {
        SYSTEM,
        DARK,
        LIGHT
    }

    enum class LanguageChoice(val code: String) {
        ENGLISH("en"),
        POLISH("pl")
    }

    enum class BarcodeTypes(
        val type: String,
        val code: Int
    ) {
        FORMAT_CODE_128("Code 128", Barcode.FORMAT_CODE_128),
        FORMAT_CODE_39("Code 39", Barcode.FORMAT_CODE_39),
        FORMAT_CODE_93("Code 93", Barcode.FORMAT_CODE_93),
        FORMAT_CODABAR("Codabar", Barcode.FORMAT_CODABAR),
        FORMAT_EAN_13("EAN-13", Barcode.FORMAT_EAN_13),
        FORMAT_EAN_8("EAN-8", Barcode.FORMAT_EAN_8),
        FORMAT_ITF("ITF", Barcode.FORMAT_ITF),
        FORMAT_UPC_A("UPC-A", Barcode.FORMAT_UPC_A),
        FORMAT_UPC_E("UPC-E", Barcode.FORMAT_UPC_E),
        FORMAT_QR_CODE("QR", Barcode.FORMAT_QR_CODE),
        FORMAT_PDF417("PDF417", Barcode.FORMAT_PDF417),
        FORMAT_AZTEC("Aztec", Barcode.FORMAT_AZTEC),
        FORMAT_DATA_MATRIX("Data Matrix", Barcode.FORMAT_DATA_MATRIX)
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

    @Composable
    fun collectBarcodeTypesWithLifecycle() = dataStore.data.map { preferences ->
        preferences[barcodesKey]?.map { enumValueOf<BarcodeTypes>(it) }?.toSet() ?: BarcodeTypes.entries.toSet()
    }.collectAsStateWithLifecycle(initialValue = emptySet())

    suspend fun changeBarcodeTypes(barcodes: Set<BarcodeTypes>?) {
        dataStore.edit { preferences ->
            if (barcodes != null) {
                preferences[barcodesKey] = barcodes.map { it.name }.toSet()
            } else {
                preferences.remove(barcodesKey)
            }
        }
    }
}