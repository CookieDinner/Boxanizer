package com.cookiedinner.boxanizer.core.di

import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { DataProvider(DatabaseDriverFactory(androidApplication())) }
}