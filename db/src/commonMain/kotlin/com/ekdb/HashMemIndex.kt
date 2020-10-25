package com.ekdb

import pw.binom.ByteBuffer
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.writeInt
import pw.binom.writeLong

class HashMemIndex(val columns: IntArray, override val unique: Boolean, bucketSize: Int) : MemIndex {

    init {
        require(columns.isNotEmpty())
    }

    private class Item(var next: Item?, var record: Record, val hash: Int)
    private class Bucket {
        var size: Long = 0L
        var next: Item? = null
    }

    private val buckets = Array(bucketSize) { Bucket() }
    override val size: Long
        get() {
            var out = 0L
            buckets.forEach {
                out += it.size
            }
            return out
        }

    override fun select(query: Array<Pair<Operator, Value?>>, func: (Record) -> Boolean) {
        if (query.any { it.first != Operator.EQ }) {
            return
        }
        val hh = calcHash4(query)
        val i = hh % buckets.size
        val b = buckets[i].next
        var r = b
        while (r != null) {
            if (calcHash(r.record.values) == hh && equals(r.record.values, query)) {
                func(r.record)
                if (unique) {
                    break
                }
            }
            r = r.next
        }
    }

    override fun selectOneByIndex(values: IndexValues): Record? {
        TODO("Not yet implemented")
    }

    override fun selectOneByFull(values: FullValues): Record? {
        TODO("Not yet implemented")
    }

    override fun insert(record: Record) {
        val h = calcHash(record.values)
        val i = h % buckets.size
        buckets[i].next = Item(buckets[i].next, record, h)
        buckets[i].size++
    }

    companion object {

        fun calcHash4(values: Array<Pair<Operator, Value?>>): Int {
            var c = 0
            values.forEach {
                c += it.second.hashCode()
            }
            return c
        }

        fun calcHash3(values: Array<Value?>): Int {
            var c = 0
            values.forEach {
                c += it.hashCode()
            }
            return c
        }

        fun calcHash(columns: IntArray, values: Array<Value?>): Int {
            var c = 0
            columns.forEach {
                c += values[it].hashCode()
            }
            return c
        }
    }

    private fun calcHash(values: Array<Value?>): Int =
        calcHash(columns, values)

    private fun calcHash2(values: Array<Value?>): Int {
        require(values.size == columns.size)
        return calcHash3(values)
    }

    private fun equals(a: Array<Value?>, b: Array<Value?>): Boolean {
        columns.forEach {
            if (a[it] != b[it]) {
                return false
            }
        }
        return true
    }

    private fun equals(a: Array<Value?>, b: Array<Pair<Operator, Value?>>): Boolean {
        columns.forEach {
            if (a[it] != b[it].second) {
                return false
            }
        }
        return true
    }


//    fun add(record: Record) {
//
//    }

    fun replace(oldValues: Record, newValues: Array<Value?>) {
        val deleted = deleteRecord(oldValues) != null
        val h = calcHash(newValues)
        val i = h % buckets.size
        buckets[i].next = Item(buckets[i].next, oldValues, h)
        if (!deleted) {
            buckets[i].size++
        }
    }

    fun deleteByIndexColumns(values: Array<Value?>): Record? {
        require(columns.size == values.size)
        val h = calcHash2(values)
        val i = h % buckets.size
        val b = buckets[i].next
        var p: Item? = null
        var r = b
        while (r != null) {
            if (r.hash == h && equals(r.record.values, values)) {
                val rr = r.record
                if (p == null) {
                    buckets[i].next = null
                } else {
                    p.next = r.next
                }
                return rr
            }
            p = r
            r = r.next
        }
        return null
    }

    private fun deleteRecord(values: Record): Record? {
        require(values.values.size >= columns.size)
        columns.forEach { indexColumn ->
            require(indexColumn < values.values.size)
        }
        val h = calcHash(values.values)
        val i = h % buckets.size
        val b = buckets[i].next
        var p: Item? = null
        var r = b
        while (r != null) {
            if (r.record == values) {
                val rr = r.record
                if (p == null) {
                    buckets[i].next = null
                } else {
                    p.next = r.next
                }
                return rr
            }
            p = r
            r = r.next
        }
        return null
    }

    fun deleteByAllColumns(values: Array<Value?>): Record? {
        require(values.size >= columns.size)
        columns.forEach { indexColumn ->
            require(indexColumn < values.size)
        }
        val h = calcHash(values)
        val i = h % buckets.size
        val b = buckets[i].next
        var p: Item? = null
        var r = b
        while (r != null) {
            if (r.hash == h && equals(r.record.values, values)) {
                val rr = r.record
                if (p == null) {
                    buckets[i].next = null
                } else {
                    p.next = r.next
                }
                return rr
            }
            p = r
            r = r.next
        }
        return null
    }

//    fun find(values: IndexValues): Record? {
//        val i = calcHash(values) % buckets.size
//        val b = buckets[i].next
//        var r = b
//        while (r != null) {
//            if (calcHash(r.record.values) == calcHash(values) && equals(r.record.values, values)) {
//                return r.record
//            }
//            r = r.next
//        }
//        return null
//    }

//    fun findAll(values: IndexValues, callback: (Record) -> Boolean) {
//        val i = calcHash(values) % buckets.size
//        val b = buckets[i].next
//        var r = b
//        val hh = calcHash(values)
//        while (r != null) {
//            if (calcHash(r.record.values) == hh && equals(r.record.values, values)) {
//                if (!callback(r.record)) {
//                    break
//                }
//                if (unique) {
//                    break
//                }
//            }
//            r = r.next
//        }
//    }

    override fun forEach(func: (Record) -> Unit) {
        buckets.forEach {
            var r = it.next
            while (r != null) {
                func(r.record)
                r = r.next
            }
        }
    }

//    fun select(query: Array<Pair<Operator, Value?>>, duplicateController: DuplicateController) {
//        if (query.any { it.first != Operator.EQ }) {
//            return
//        }
//        val hh = calcHash4(query)
//        val i = hh % buckets.size
//        val b = buckets[i].next
//        var r = b
//        while (r != null) {
//            if (calcHash(r.record.values) == hh && equals(r.record.values, query)) {
//                duplicateController.add(r.record.values, r.record.deleted)
//                if (unique) {
//                    break
//                }
//            }
//            r = r.next
//        }
//    }

    override fun save(file: File) {
        val buf = ByteBuffer.alloc(8)
        file.write().use { ch ->
            ch.writeInt(buf, buckets.size)

            buckets.forEach {
                ch.writeLong(buf, 0)
                ch.writeLong(buf, it.size)
            }
            buckets.forEachIndexed { index, bucket ->
                val pos = ch.position
                ch.position = (Int.SIZE_BYTES + Long.SIZE_BYTES * 2 * index).toULong()
                ch.writeLong(buf, pos.toLong())
                ch.position = pos
                var r = bucket.next
                while (r != null) {
                    ch.writeInt(buf, r.hash)
                    ch.writeLong(buf, r.record.address.toLong())
                    r = r.next
                }
            }
        }
    }
}