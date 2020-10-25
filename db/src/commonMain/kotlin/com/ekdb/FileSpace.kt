package com.ekdb

import pw.binom.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.readByte
import pw.binom.readInt
import pw.binom.readLong

class FileSpace(val id: Int, file: File, val indexes: List<FileHashIndex>) : Closeable {
    val ptr = file.read()
    val buf = ByteBuffer.alloc(16)

    fun selectOne(index: Int, value: IndexValues): Array<Value?>? {
        val pos = indexes[index].find(value) ?: return null
        return getRecord(pos)
    }

    fun getRecord(location: ULong): Array<Value?> {
        ptr.position = location
        val valuesCount = ptr.readInt(buf)
        return Array(valuesCount) {
            if (ptr.readByte(buf) == 0.toByte()) {
                null
            } else {
                Value.read(buf, ptr)
            }
        }
    }

    val size = ptr.readLong(buf)

    override fun close() {
        ptr.close()
    }

}