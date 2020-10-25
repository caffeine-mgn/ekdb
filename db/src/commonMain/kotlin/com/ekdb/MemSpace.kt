package com.ekdb

import pw.binom.ByteBuffer
import pw.binom.io.file.File
import pw.binom.io.file.write
import pw.binom.io.use
import pw.binom.writeByte
import pw.binom.writeInt
import pw.binom.writeLong

class MemSpace(
    val indexex: Array<MemIndex>
) {
    init {
        require(indexex.isNotEmpty())
        require(indexex[0].unique)
    }

    /**
     * Удаляет запись по индексу
     * @param index индекс по которому необходимо удалить данные
     * @param indexValues данные для удаления. Колонки по которым происходило индексирование
     *
     * @return true - удалось найти запись в текущем индексе, запись помечина удалённой
     * false - не удалось найти запись в текущем индексе. Требуется удалить запись по полным значениям
     */
    fun deleteByIndex(index: Int, indexValues: IndexValues): Boolean {
        require(index >= 0 && index < indexex.size)
        val r = indexex[index].selectOneByIndex(indexValues) ?: return false
        r.deleted = true
        return true
    }

    /**
     * Добавляет запись об удалении строки
     */
    fun delete(record: FullValues) {
        val r = Record(values = record, 0uL, true)
        indexex.forEach {
            it.insert(r)
        }
    }

    fun add(record: FullValues) {
        indexex.forEach {
            if (it.unique && it.selectOneByFull(record) != null) {
                throw RuntimeException("Value duplicated")
            }
        }
        val r = Record(record, 0uL, false)
        indexex.forEach {
            it.insert(r)
        }
    }

    fun selectOne(index: Int, value: IndexValues): FullValues? {
        val i = indexex[index]
        return i.selectOneByIndex(value)?.values
    }

    fun selectAll(
        index: Int,
        query: Array<Pair<Operator, Value?>>, func: (Record) -> Boolean,
        duplicateController: DuplicateController,
        callback: (FullValues) -> Boolean
    ) {
        val i = indexex[index]
        i.select(query) {
            if (duplicateController.add(it.values, it.deleted)) {
                callback(it.values)
            } else {
                false
            }
        }
    }


    fun save(file: File) {
        val buf = ByteBuffer.alloc(16)
        file.write().use { ch ->
            ch.writeLong(buf, indexex[0].size)
            indexex[0].forEach {
                it.address = ch.position
                ch.writeInt(buf, it.values.size)
                it.values.forEach {
                    if (it == null) {
                        ch.writeByte(buf, 0)
                    } else {
                        ch.writeByte(buf, 1)
                        it.write(buf, ch)
                    }
                }
            }
        }
    }
}