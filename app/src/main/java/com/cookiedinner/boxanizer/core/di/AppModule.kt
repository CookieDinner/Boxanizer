package com.cookiedinner.boxanizer.core.di

import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.main.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.main.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.main.viewmodels.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { DataProvider(DatabaseDriverFactory(androidApplication())) }
    single { DataStoreManager(androidApplication()) }
    single { Navigator() }

    viewModel { BoxesViewModel(get(), get()) }
    viewModel { MainViewModel() }
    viewModel { BoxDetailsViewModel(get(), get()) }
}