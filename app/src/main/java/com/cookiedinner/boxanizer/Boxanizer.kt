package com.cookiedinner.boxanizer

import android.app.Application
import com.cookiedinner.boxanizer.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Boxanizer : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Boxanizer)
            modules(appModule)
        }
    }
}