package com.cookiedinner.boxanizer.core.data

import com.cookiedinner.boxanizer.core.database.Database
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.items.models.ItemAction
import com.cookiedinner.boxanizer.items.models.ItemListType

class DataProvider(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)

    @Throws(Exception::class)
    fun getBoxes(query: String = ""): List<Box> {
        return database.boxesSelectByQuery(query)
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
    fun getBoxItems(boxId: Long): Map<ItemListType, List<ItemInBox>> {
        val emptyItemsMap = mapOf<ItemListType, List<ItemInBox>>(
            ItemListType.REMOVED to emptyList(),
            ItemListType.IN_BOXES to emptyList()
        )
        return emptyItemsMap + database.itemsSelectByBoxId(boxId)
            .sortedWith(compareBy<ItemInBox> { it.amountRemovedFromBox == 0L }.thenByDescending { it.lastTimeMovedSections })
            .groupBy { if (it.amountRemovedFromBox > 0) ItemListType.REMOVED else ItemListType.IN_BOXES }
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
    fun getItems(query: String): Map<ItemListType, List<Item>> {
        val removed = database.itemsSelectRemovedFromBoxes(query)
        val inBoxes = database.itemsSelectInBoxes(query).filterNot { item -> removed.any { it.id == item.id } }
        val remaining = database.itemsSelectNotInBoxes(query).filterNot { item -> inBoxes.any { it.id == item.id } }
        return mapOf(
            ItemListType.REMOVED to removed,
            ItemListType.IN_BOXES to inBoxes,
            ItemListType.REMAINING to remaining
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

    @Throws(Exception::class)
    fun editItemInBox(
        itemId: Long,
        boxId: Long,
        action: ItemAction,
        item: ItemInBox
    ) {
        database.editItemInBox(itemId, boxId, action, item)
    }
}