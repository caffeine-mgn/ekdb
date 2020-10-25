package com.ekdb

import pw.binom.io.file.*
import pw.binom.io.use

/**
 * Область
 */
class Space(val dir: File, indexes: Array<IndexDefine>) {
    interface Index {

        fun select(query: Array<Pair<Operator, Value?>>, duplicateController: DuplicateController)
        fun selectOne(values: IndexValues): FullValues?
        fun delete(values: IndexValues)
    }


    inner class IndexImpl(val unique: Boolean, val columns: IntArray, val num: Int) : Index {

        override fun selectOne(values: IndexValues): FullValues? {
            mem.selectOne(num, values)
            for (i in journal.size - 1 downTo 0) {
                val j = journal[i]
                val r = j.selectOne(num, values)
                if (r != null) {
                    return r
                }
            }
            return null
        }

        override fun select(query: Array<Pair<Operator, Value?>>, duplicateController: DuplicateController) {
            TODO("Not yet implemented")
        }

        override fun delete(values: IndexValues) {
            if (!mem.deleteByIndex(num, values)) {
                val r = selectOne(values) ?: return
                mem.delete(r)
            }
        }
    }

    val indexes = indexes.mapIndexed { index, it ->
        IndexImpl(
            unique = it.unique,
            columns = it.columns,
            num = index
        )
    }

    fun insert(values: FullValues) {
        mem.add(values)
    }

//    fun upsert(values: FullValues) {
//        mem.upsert(values)
//    }

    private val journal = ArrayList<FileSpace>()

    class IndexDefine(val unique: Boolean, val columns: IntArray)

    init {
        require(dir.isDirectory || !dir.isExist)
        dir.mkdirs()

        dir.iterator().use {
            it.forEach {
                if (it.extension.toLowerCase() == "space") {
                    val spaceName = it.nameWithoutExtension.toInt()

                    val indexes = indexes.indices.map { index ->
                        FileHashIndex(File(it, "$spaceName.$index.index"))
                    }

                    journal += FileSpace(spaceName, it, indexes)
                }
            }
        }
        journal.sortBy { it.id }
    }

    /**
     * Сохраняет текущее состояние в памяти на диск, освобождая память
     */
    fun dump() {
        if (mem.indexex[0].size == 0L) {
            return
        }
        val id = journal.maxOfOrNull { it.id }?.let { it + 1 } ?: 1
        val f = File(dir, "$id.space")
        mem.save(f)
        val inds = mem.indexex.mapIndexed { index, hashMemIndex ->
            val f = File(dir, "$id.$index.index")
            hashMemIndex.save(f)
            FileHashIndex(f)
        }

        journal += FileSpace(id, f, inds)

        mem = MemSpace(
            Array(this.indexes.size) {
                HashMemIndex(
                    columns = this.indexes[it].columns,
                    unique = this.indexes[it].unique,
                    bucketSize = 32
                )
            }
        )
    }

    private var mem = MemSpace(
        Array<MemIndex>(this.indexes.size) {
            HashMemIndex(
                columns = this.indexes[it].columns,
                unique = this.indexes[it].unique,
                bucketSize = 32
            )
        }
    )
}