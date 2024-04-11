package com.cookiedinner.boxanizer.core.database

import com.cookiedinner.boxanizer.BoxanizerDb

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = BoxanizerDb(
        driver = databaseDriverFactory.createDriver()
    )

    private val boxQueries = database.boxQueries
    private val itemQueries = database.itemQueries
    private val itemTagQueries = database.itemTagQueries
}