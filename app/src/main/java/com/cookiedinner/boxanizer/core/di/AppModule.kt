package com.cookiedinner.boxanizer.core.di

import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.core.navigation.Navigator
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { DataProvider(DatabaseDriverFactory(androidApplication())) }
    single { DataStoreManager(androidApplication()) }
    single { Navigator() }
}