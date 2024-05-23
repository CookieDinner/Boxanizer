package com.cookiedinner.boxanizer.core.models

data class Digit(
    val digitChar: Char,
    val fullNumber: Long,
    val place: Int
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Digit -> digitChar == other.digitChar
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = digitChar.hashCode()
        result = 31 * result + fullNumber.hashCode()
        result = 31 * result + place
        return result
    }
}

operator fun Digit.compareTo(other: Digit): Int {
    return fullNumber.compareTo(other.fullNumber)
}