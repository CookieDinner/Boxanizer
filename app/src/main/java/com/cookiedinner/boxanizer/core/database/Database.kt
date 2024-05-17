package com.cookiedinner.boxanizer.core.database

import android.util.Log
import app.cash.sqldelight.Query
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxanizerDb
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = BoxanizerDb(
        driver = databaseDriverFactory.createDriver()
    )

    private val boxQueries = database.boxQueries
    private val itemQueries = database.itemQueries
    private val itemTagQueries = database.itemTagQueries

    private fun <T: Any> buildListFromQuery(
        searchQuery: String,
        databaseFunction: (String) -> Query<T>,
        comparisonField: (T) -> Any
    ): List<T> {
        val fragmentedQuery = searchQuery.split(" ").filter { it.isNotBlank() }
        val finalList = mutableListOf<T>()
        if (fragmentedQuery.isEmpty()) {
            finalList.addAll(databaseFunction(searchQuery).executeAsList())
        } else {
            fragmentedQuery.forEachIndexed { index, value ->
                if (index == 0)
                    finalList.addAll(databaseFunction(value).executeAsList())
                else
                    finalList.retainAll { item -> databaseFunction(value).executeAsList().any { comparisonField(it) == comparisonField(item) } }
            }
        }
        return finalList
    }
    fun boxesSelectByQuery(query: String): List<Box> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = boxQueries::searchByQuery,
            comparisonField = { it.id },
        )
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

    fun itemsSelectRemovedFromBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectRemovedFromBoxes,
            comparisonField = { it.id }
        )
    }

    fun itemsSelectInBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectInBoxes,
            comparisonField = { it.id }
        )
    }

    fun itemsSelectNotInBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectNotInBoxes,
            comparisonField = { it.id }
        )
    }

    fun itemSelectById(id: Long): Item? {
        return itemQueries.selectById(id).executeAsOneOrNull()
    }

    fun itemsSelectByBoxId(boxId: Long): List<ItemInBox> {
        return itemQueries.itemInBox(boxId).executeAsList()
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