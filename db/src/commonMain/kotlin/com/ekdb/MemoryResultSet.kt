package com.ekdb

class MemoryResultSet : ResultSet {
    val list = ArrayList<FullValues>()
    override fun addResult(values: FullValues) {
        list += values
    }
}