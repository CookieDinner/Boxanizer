package com.cookiedinner.boxanizer.settings.viewmodels

import android.app.Application
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.utilities.getPackageInfo
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    fun checkForUpdates(openBrowserForUrl: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestRelease = dataProvider.checkForUpdates()
                if (latestRelease == null) {
                    snackbarHostState.safelyShowSnackbar(context.getString(R.string.no_other_version_available))
                    return@launch
                }
                val currentVersion = getPackageInfo(context, 0).versionName
                if (latestRelease.name > currentVersion) {
                    val result = snackbarHostState.safelyShowSnackbar(
                        message = context.getString(R.string.newer_version_available),
                        actionLabel = context.getString(R.string.open),
                        duration = SnackbarDuration.Indefinite
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed -> {
                            withContext(Dispatchers.Main) {
                                openBrowserForUrl(latestRelease.html_url)
                            }
                        }

                        null -> {}
                    }
                } else {
                    snackbarHostState.safelyShowSnackbar(context.getString(R.string.you_have_the_newest_version))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.failed_to_check_for_updates))
            }
        }
    }
}