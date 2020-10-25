package com.ekdb

import pw.binom.io.file.File

interface TreeColumn
abstract class TreeColumnWithNext : RedBlackTree<Value?, TreeColumn>(), TreeColumn {
    override fun compare(a: Value?, b: Value?): Int {

        when {
            a == null -> return 1
            b == null -> return -1
        }
//        return a.hashCode()-b.hashCode()
        return a!!.compare(b!!)
    }

    fun find(op: Operator, value: Value?, func: (TreeColumn) -> Boolean) {
        findNode {
            if (op == Operator.ALL) {
                return@findNode BOTH
            }
            val db = it.key
            if (db == null || value == null) {
                when {
                    db == value && op == Operator.EQ -> func(it.value)
                    db != value && op == Operator.NE -> func(it.value)
                }
                return@findNode STOP
            }
            if (op == Operator.EQ) {
                if (value == db) {
                    func(it.value)
                }
                return@findNode STOP
            }
            if (op == Operator.NE) {
                if (value != db) {
                    func(it.value)
                }
                return@findNode STOP
            }
            val com = value.compare(db)
            when {
                op == Operator.LE && com >= 0 -> {
                    return@findNode if (!func(it.value))
                        STOP
                    else
                        LEFT
                }
                op == Operator.LT && com > 0 -> {
                    return@findNode if (!func(it.value))
                        STOP
                    else
                        LEFT
                }
                op == Operator.GE && com <= 0 -> {
                    return@findNode if (!func(it.value))
                        STOP
                    else
                        RIGHT

                }
                op == Operator.GT && com < 0 -> {
                    return@findNode if (!func(it.value))
                        STOP
                    else
                        RIGHT
                }
            }
            STOP
        }
    }
}

class NotUniqueColumn(value: Record) : TreeColumn {
    val list = ArrayList<Record>()

    init {
        list.add(value)
    }
}

class UniqueColumn(var value: Record) : TreeColumn

class CommonColumn(value: Value?) : TreeColumnWithNext()

class MemTreeIndex(val columns: IntArray, override val unique: Boolean) : TreeColumnWithNext(), MemIndex {
    private var _size = 0L
    override val size: Long
        get() = _size

    override fun select(query: Array<Pair<Operator, Value?>>, func: (Record) -> Boolean) {
        require(query.size == columns.size)
        var searchList = ArrayList<TreeColumnWithNext>()
        searchList.add(this)
        var stop = false
        query.forEach { q ->
            val ss = ArrayList<TreeColumnWithNext>()
            searchList.forEach {
                if (stop) {
                    return
                }
                it.find(q.first, q.second) {
                    println("! it=$it")
                    when (it) {
                        is TreeColumnWithNext -> ss += it
                        is UniqueColumn -> if (!func(it.value)) {
                            stop = true
                            return@find false
                        }
                        is NotUniqueColumn -> {
                            it.list.forEach {
                                if (!func(it)) {
                                    stop = true
                                    return@find false
                                }
                            }
                        }
                        else -> TODO()
                    }
                    true
                }
            }
            searchList = ss
        }
    }

    override fun selectOneByIndex(values: IndexValues): Record? {
        var r: Record? = null
        select(Array(columns.size) {
            Operator.EQ to values[it]
        }) {
            r = it
            false
        }
        return r
    }

    override fun selectOneByFull(values: FullValues): Record? {
        var r: Record? = null
        select(Array(columns.size) {
            Operator.EQ to values[columns[it]]
        }) {
            r = it
            false
        }
        return r
    }

    override fun insert(record: Record) {
        var c: TreeColumnWithNext = this
        columns.forEachIndexed { index, i ->
            val it = c.findNode(record.values[i])
            when (it) {
                is UniqueColumn -> {
                    TODO()
                }
                is NotUniqueColumn -> {
                    it.list.add(record)
                    _size++
                }
                is TreeColumnWithNext -> c = it
                null -> {
                    if (index == columns.lastIndex) {
                        if (unique) {
                            c.put(record.values[i], UniqueColumn(record))
                        } else {
                            c.put(record.values[i], NotUniqueColumn(record))
                        }
                        _size++
                    } else {
                        val n = CommonColumn(record.values[i])
                        c.put(record.values[i], n)
                        c = n
                    }
                }
                else -> TODO()
            }
        }
    }

    override fun save(file: File) {
        TODO("Not yet implemented")
    }

    override fun forEach(func: (Record) -> Unit) {
        TODO("Not yet implemented")
    }
}