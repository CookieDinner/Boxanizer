package com.cookiedinner.boxanizer.items.models

data class ItemWithAmount(
    val id: Long,
    val name: String,
    val description: String?,
    val image: ByteArray?,
    val amountInBox: Int,
    val amountRemovedFromBox: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemWithAmount

        if (id != other.id) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}