package com.cookiedinner.boxanizer.core.database

import app.cash.sqldelight.Query
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxWithItem
import com.cookiedinner.boxanizer.database.BoxanizerDb
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.database.ItemTag
import com.cookiedinner.boxanizer.items.models.ItemAction

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = BoxanizerDb(
        driver = databaseDriverFactory.createDriver()
    )

    private val boxQueries = database.boxQueries
    private val itemQueries = database.itemQueries
    private val itemTagQueries = database.itemTagQueries

    private fun <T : Any> buildListFromQuery(
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

    fun itemsSelectRemovedFromBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectRemovedFromBoxes,
            comparisonField = { it.id }
        ).distinctBy { it.id }
    }

    fun itemsSelectInBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectInBoxes,
            comparisonField = { it.id }
        ).distinctBy { it.id }
    }

    fun itemsSelectNotInBoxes(query: String): List<Item> {
        return buildListFromQuery(
            searchQuery = query,
            databaseFunction = itemQueries::selectNotInBoxes,
            comparisonField = { it.id }
        ).distinctBy { it.id }
    }

    fun itemSelectById(id: Long): Item? {
        return itemQueries.selectById(id).executeAsOneOrNull()
    }

    fun itemsSelectByBoxId(boxId: Long): List<ItemInBox> {
        return itemQueries.itemInBox(boxId).executeAsList()
    }

    fun boxesSelectByItemId(itemId: Long): List<BoxWithItem> {
        return boxQueries.boxWithItem(itemId).executeAsList()
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

    @Throws(Exception::class)
    fun editItemInBox(
        itemId: Long,
        boxId: Long,
        action: ItemAction,
        item: ItemInBox
    ) {
        when (action) {
            ItemAction.BORROW, ItemAction.RETURN -> {
                if ((action == ItemAction.BORROW && item.amountRemovedFromBox == 0L) || (action == ItemAction.RETURN && item.amountRemovedFromBox == 1L)) {
                    itemQueries.reinsertAmountRemovedInBox(
                        boxId = boxId,
                        itemId = itemId,
                        amountRemovedFromBox = if (action == ItemAction.BORROW) 1 else 0
                    )
                } else {
                    itemQueries.editAmountRemovedInBox(
                        newAmount = item.amountRemovedFromBox + if (action == ItemAction.BORROW) 1 else -1,
                        itemId = itemId,
                        boxId = boxId
                    )
                }
            }

            ItemAction.ADD, ItemAction.REMOVE -> {
                if (action == ItemAction.REMOVE) {
                    if (item.amountInBox > 1L) {
                        itemQueries.editAmountInBox(
                            newAmount = item.amountInBox - 1,
                            itemId = itemId,
                            boxId = boxId
                        )
                    }
                } else {
                    itemQueries.editAmountInBox(
                        newAmount = item.amountInBox + 1,
                        itemId = itemId,
                        boxId = boxId
                    )
                }
            }

            ItemAction.DELETE -> {
                itemQueries.deleteFromBox(boxId, itemId)
            }
        }
    }

    @Throws(Exception::class)
    fun selectItemTags(itemId: Long): List<ItemTag> {
        return itemTagQueries.selectByItemId(itemId).executeAsList()
    }

    @Throws(Exception::class)
    fun insertTag(
        itemId: Long,
        name: String
    ) {
        itemTagQueries.insert(itemId, name)
    }

    @Throws(Exception::class)
    fun deleteTag(
        itemId: Long,
        name: String
    ) {
        itemTagQueries.delete(itemId, name)
    }
}