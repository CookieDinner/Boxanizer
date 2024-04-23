package com.cookiedinner.boxanizer.core.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.SharedFlow
import org.koin.androidx.compose.koinViewModel

fun getPackageInfo(
    context: Context,
    flags: Int = 0
): VersionInfo {
    val packageManager = context.packageManager
    val packageName = context.packageName
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        VersionInfo(packageInfo.versionName, packageInfo.longVersionCode)
    } else {
        val packageInfo = packageManager.getPackageInfo(packageName, flags)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            VersionInfo(packageInfo.versionName, packageInfo.longVersionCode)
        } else {
            @Suppress("DEPRECATION") VersionInfo(packageInfo.versionName, packageInfo.versionCode.toLong())
        }
    }
}

data class VersionInfo(
    val versionName: String,
    val versionCode: Long
)

suspend fun SnackbarHostState.safelyShowSnackbar(
    message: String,
    withDismissAction: Boolean = true,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult? {
    return if (this.currentSnackbarData == null) {
        this.showSnackbar(
            message = message,
            withDismissAction = withDismissAction,
            actionLabel = actionLabel,
            duration = duration
        )
    } else null
}

fun Context.getActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> throw Exception("Failed casting context to activity")
}

@Composable
inline fun <reified T : androidx.lifecycle.ViewModel> koinActivityViewModel() = koinViewModel<T>(
    viewModelStoreOwner = LocalContext.current.getActivity()
)

@Composable
fun <T> FlowObserver(
    flow: SharedFlow<T>,
    onCollect: suspend (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect {
                onCollect(it)
            }
        }
    }
}

fun String.trimNewLines() = this.replace("\n", " ").replace("\\s{2,}".toRegex(), " ")