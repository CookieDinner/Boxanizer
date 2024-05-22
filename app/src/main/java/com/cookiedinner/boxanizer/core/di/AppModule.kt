package com.cookiedinner.boxanizer.core.di

import com.cookiedinner.boxanizer.boxes.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.items.viewmodels.ItemDetailsViewModel
import com.cookiedinner.boxanizer.items.viewmodels.ItemsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { DataProvider(DatabaseDriverFactory(androidApplication())) }
    single { DataStoreManager(androidApplication()) }
    single { Navigator() }

    viewModel { MainViewModel(get(), get()) }

    viewModel { BoxesViewModel(get(), get()) }
    viewModel { BoxDetailsViewModel(get(), get()) }

    viewModel { ItemsViewModel(get(), get()) }
    viewModel { ItemDetailsViewModel(get(), get()) }

}