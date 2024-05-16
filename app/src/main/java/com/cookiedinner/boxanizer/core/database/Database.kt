package com.cookiedinner.boxanizer.core.database

import android.util.Log
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxanizerDb
import com.cookiedinner.boxanizer.database.Item

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
            insertedBox = boxQueries.selectLastInsertedBox().executeAsOneOrNull()
        }
        return insertedBox
    }

    @Throws(Exception::class)
    fun deleteBox(boxId: Long) {
        database.transaction {
            boxQueries.delete(boxId)
            boxQueries.deleteItemLinks(boxId)
        }
    }

    fun itemsSelectAll(): List<Item> {
        return itemQueries.selectAll().executeAsList()
    }

    fun itemsSelectRemovedFromBoxes(): List<Item> {
        return itemQueries.selectRemovedFromBoxes().executeAsList()
    }

    fun itemsSelectInBoxes(): List<Item> {
        return itemQueries.selectInBoxes().executeAsList()
    }

    fun itemsSelectNotInBoxes(): List<Item> {
        return itemQueries.selectNotInBoxes().executeAsList()
    }

    fun itemSelectById(id: Long): Item? {
        return itemQueries.selectById(id).executeAsOneOrNull()
    }

    @Throws(Exception::class)
    fun insertItem(item: Item): Item? {
        var insertedItem: Item? = null
        database.transaction {
            itemQueries.insert(
                id = if (item.id == -1L) null else item.id,
                name = item.name,
                description = item.description?.ifBlank { null },
                image = item.image
            )
            insertedItem = itemQueries.selectLastInsertedItem().executeAsOneOrNull()
        }
        return insertedItem
    }

    @Throws(Exception::class)
    fun deleteItem(itemId: Long) {
        database.transaction {
            itemQueries.delete(itemId)
            itemQueries.deleteBoxLinks(itemId)
            itemQueries.deleteTagLinks(itemId)
        }
    }
}