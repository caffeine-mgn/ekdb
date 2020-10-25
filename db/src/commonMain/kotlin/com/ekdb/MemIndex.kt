package com.ekdb

import pw.binom.io.file.File

interface MemIndex {
    val unique: Boolean
    val size: Long

    fun select(query: Array<Pair<Operator, Value?>>, func: (Record) -> Boolean)
    fun selectOneByIndex(values: IndexValues): Record?
    fun selectOneByFull(values: FullValues): Record?
    fun insert(record: Record)
    fun save(file: File)
    fun forEach(func: (Record) -> Unit)
}