package com.ekdb

import pw.binom.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.readInt
import pw.binom.readLong

/**
 * Индекс на базе хэш таблиц. Реализация для хранения в файле. [HashMemIndex] - реализация индекса хэш таблтиц в памяти
 */
class FileHashIndex(val file: File) : FileIndex, Closeable {
    private val channel = file.read()
    private val buf = ByteBuffer.alloc(8)
    private val bucketAddresses: Array<Bucket>

    class Bucket(val address: ULong, val count: ULong)

    init {
        val bucketCount = channel.readInt(buf)

        bucketAddresses = Array(bucketCount) {
            Bucket(
                address = channel.readLong(buf).toULong(),
                count = channel.readLong(buf).toULong(),
            )
        }
    }

    private fun find(hash: Int): ULong? {
        val bucketNum = hash % bucketAddresses.size
        val buket = bucketAddresses[bucketNum]
        channel.position = buket.address
        var cursor = 0uL
        while (cursor < buket.count) {
            val currentHash = channel.readInt(buf)
            val address = channel.readLong(buf).toULong()
            if (currentHash == hash) {
                return address
            }
            cursor++
        }
        return null
    }

    private fun findAll(hash: Int, callback: (ULong) -> Boolean) {
        val bucketNum = hash % bucketAddresses.size
        val buket = bucketAddresses[bucketNum]
        channel.position = buket.address
        var cursor = 0uL
        while (cursor < buket.count) {
            val currentHash = channel.readInt(buf)
            val address = channel.readLong(buf).toULong()
            if (currentHash == hash) {
                if (!callback(address)) {
                    break
                }
            }
            cursor++
        }
    }

    fun findAll(values: IndexValues, callback: (ULong) -> Boolean) {
        findAll(HashMemIndex.calcHash3(values), callback)
    }

    override fun find(values: IndexValues): ULong? =
        find(HashMemIndex.calcHash3(values))

    override fun close() {
        channel.close()
    }
}