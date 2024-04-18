package com.cookiedinner.boxanizer.core.database

import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.BoxanizerDb

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = BoxanizerDb(
        driver = databaseDriverFactory.createDriver()
    )

    private val boxQueries = database.boxQueries
    private val itemQueries = database.itemQueries
    private val itemTagQueries = database.itemTagQueries

    fun boxesSelectAll(): List<Box> {
        return boxQueries.selectAll().executeAsList()
    }

    fun boxesSelectByQuery(query: String): List<Box> {
        return boxQueries.selectByItemNames(query).executeAsList()
    }

    fun boxSelectById(id: Long): Box? {
        return boxQueries.selectById(id).executeAsOneOrNull()
    }

    fun boxSelectByCode(code: String): Box? {
        return boxQueries.selectByCode(code).executeAsOneOrNull()
    }

    @Throws(Exception::class)
    fun insertBox(box: Box): Box? {
        var insertedBox: Box? = null
        database.transaction {
            val codeBox = boxQueries.selectByCode(box.code).executeAsOneOrNull()
            if (codeBox != null && box.id != codeBox.id)
                throw Exception()
            boxQueries.insert(
                id = if (box.id == -1L) null else box.id,
                code = box.code,
                name = box.name,
                description = box.description?.ifBlank { null },
                image = box.image
            )
            insertedBox = boxQueries.selectLastBox().executeAsOneOrNull()
        }
        return insertedBox
    }
}