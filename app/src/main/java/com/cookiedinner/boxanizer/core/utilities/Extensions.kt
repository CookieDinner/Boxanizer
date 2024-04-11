package com.cookiedinner.boxanizer.core.utilities

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

fun getPackageInfo(context: Context, flags: Int = 0): VersionInfo {
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