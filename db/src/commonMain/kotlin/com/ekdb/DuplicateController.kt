package com.ekdb

import pw.binom.io.Closeable

/**
 * Используется чтобы отсечь дублирующиеся значения
 */
class DuplicateController(val columns: IntArray) : Closeable {

    private val _data = HashMap<Item, Boolean>()
    fun asSequence() = _data.asSequence().filter { !it.value }.map { it.key.values }

    private inner class Item(val values: FullValues) {

        override fun hashCode(): Int = HashMemIndex.calcHash(columns, values)
        override fun equals(other: Any?): Boolean {
            val array = (other as? FullValues) ?: return false
            columns.indices.forEach {
                if (values[it] != array[it]) {
                    return false
                }
            }
            return true
        }
    }

    fun add(values: FullValues, deleted: Boolean): Boolean {
        val i = Item(values)
        if (_data[i] == true) {
            return false
        }
        _data[i] = deleted
        return true
    }

    override fun close() {

    }
}