package com.cookiedinner.boxanizer.core.data

import com.cookiedinner.boxanizer.core.database.Database
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory

class DataProvider(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)
}