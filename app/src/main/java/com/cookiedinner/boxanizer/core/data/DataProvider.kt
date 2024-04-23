package com.cookiedinner.boxanizer.core.data

import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.Item
import com.cookiedinner.boxanizer.core.database.Database
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory

class DataProvider(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    @Throws(Exception::class)
    fun getBoxes(query: String = ""): List<Box> {
        return if (query.isEmpty())
            database.boxesSelectAll()
        else
            database.boxesSelectByQuery(query)
    }

    @Throws(Exception::class)
    fun getBoxDetails(boxId: Long): Box {
        return database.boxSelectById(boxId) ?: throw Exception()
    }

    @Throws(Exception::class)
    fun getBoxByCode(code: String): Box? {
        return database.boxSelectByCode(code)
    }

    @Throws(Exception::class)
    fun saveBox(box: Box): Box {
        return database.insertBox(box) ?: throw Exception()
    }

    @Throws(Exception::class)
    fun getItems(): List<Item> {
        return database.itemsSelectAll()
    }
}