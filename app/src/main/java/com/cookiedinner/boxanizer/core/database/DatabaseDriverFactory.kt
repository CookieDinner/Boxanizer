package com.cookiedinner.boxanizer.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.cookiedinner.boxanizer.database.BoxanizerDb

class DatabaseDriverFactory(private val context: Context) {
    fun createDriver(): SqlDriver = AndroidSqliteDriver(BoxanizerDb.Schema, context, "boxanizer.db")
}