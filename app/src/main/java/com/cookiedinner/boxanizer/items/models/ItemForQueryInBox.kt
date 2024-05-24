package com.cookiedinner.boxanizer.items.models

data class ItemForQueryInBox(
    val id: Long,
    val name: String,
    val description: String?,
    val image: ByteArray?,
    val alreadyInBox: Boolean
)