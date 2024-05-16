package com.cookiedinner.boxanizer.core.data

import com.cookiedinner.boxanizer.core.database.Database
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.models.ItemListType

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
    fun deleteBox(boxId: Long) {
        database.deleteBox(boxId)
    }

    @Throws(Exception::class)
    fun saveItem(item: Item): Item {
        return database.insertItem(item) ?: throw Exception()
    }

    @Throws(Exception::class)
    fun getItems(): Map<ItemListType, List<Item>> {
        return mapOf(
            ItemListType.REMOVED to database.itemsSelectRemovedFromBoxes(),
            ItemListType.IN_BOXES to database.itemsSelectInBoxes(),
            ItemListType.REMAINING to database.itemsSelectNotInBoxes()
        )
    }

    @Throws(Exception::class)
    fun getItemDetails(itemId: Long): Item {
        return database.itemSelectById(itemId) ?: throw Exception()
    }

    @Throws(Exception::class)
    fun deleteItem(itemId: Long) {
        database.deleteItem(itemId)
    }
}