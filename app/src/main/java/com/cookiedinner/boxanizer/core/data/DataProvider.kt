package com.cookiedinner.boxanizer.core.data

import com.cookiedinner.boxanizer.boxes.models.BoxListType
import com.cookiedinner.boxanizer.core.api.ApiClient
import com.cookiedinner.boxanizer.core.api.ReleaseInfo
import com.cookiedinner.boxanizer.core.database.Database
import com.cookiedinner.boxanizer.core.database.DatabaseDriverFactory
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxWithItem
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.database.ItemTag
import com.cookiedinner.boxanizer.items.models.ItemAction
import com.cookiedinner.boxanizer.items.models.ItemForQueryInBox
import com.cookiedinner.boxanizer.items.models.ItemListType

class DataProvider(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = Database(databaseDriverFactory)
    private val apiClient = ApiClient()

    @Throws(Exception::class)
    suspend fun checkForUpdates(): ReleaseInfo? {
        val releaseInfoList = apiClient.checkForUpdates()
        return releaseInfoList.maxByOrNull { it.name }
    }

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
        return emptyItemsMap + database.itemsSelectByBoxId(boxId).groupBy { if (it.amountRemovedFromBox > 0) ItemListType.REMOVED else ItemListType.IN_BOXES }
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
    fun getItemsForQueryInBox(
        query: String,
        boxId: Long
    ): List<ItemForQueryInBox> {
        return database.itemsSelectForQueryInBox(query, boxId)
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
        item: ItemInBox,
        boxId: Long,
        action: ItemAction,
        customAmount: Long
    ) {
        database.editItemInBox(item, boxId, action, customAmount)
    }

    @Throws(Exception::class)
    fun getBoxesForItem(itemId: Long): Map<BoxListType, List<BoxWithItem>> {
        val emptyMap = mapOf<BoxListType, List<BoxWithItem>>(
            BoxListType.REMOVED_FROM to emptyList(),
            BoxListType.INSIDE to emptyList()
        )
        return emptyMap + database.boxesSelectByItemId(itemId).groupBy { if (it.amountRemovedFromBox > 0) BoxListType.REMOVED_FROM else BoxListType.INSIDE }
    }

    @Throws(Exception::class)
    fun getTagsForItem(itemId: Long): List<ItemTag> {
        return database.selectItemTags(itemId)
    }

    @Throws(Exception::class)
    fun addTag(
        itemId: Long,
        name: String
    ) {
        database.insertTag(itemId, name)
    }

    @Throws(Exception::class)
    fun addTag(tag: ItemTag) {
        addTag(tag.itemId, tag.name)
    }

    @Throws(Exception::class)
    fun deleteTag(
        itemId: Long,
        name: String
    ) {
        database.deleteTag(itemId, name)
    }

    @Throws(Exception::class)
    fun addItemToBox(
        boxId: Long,
        item: Item
    ) {
        if (item.id == -1L) {
            val insertedItem = database.insertItem(item) ?: throw Exception("Somehow the inserted item returned null")
            database.addItemToBox(boxId, insertedItem.id)
        } else {
            database.addItemToBox(boxId, item.id)
        }
    }
}