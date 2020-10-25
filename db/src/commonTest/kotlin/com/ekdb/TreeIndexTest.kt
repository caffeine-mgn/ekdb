package com.ekdb

import kotlin.test.Test
import kotlin.test.assertEquals

class TreeIndexTest {

    @Test
    fun test() {
        val indexColumns = intArrayOf(0, 1, 2)
        val c = MemTreeIndex(indexColumns, false)
        c.insert(Record(arrayOf(Value.IntegerValue(10), Value.IntegerValue(11), Value.IntegerValue(12)), 0uL, false))
        val rr = MemoryResultSet()
        c.select(arrayOf(
            Operator.EQ to Value.IntegerValue(10),
            Operator.EQ to Value.IntegerValue(11),
            Operator.EQ to Value.IntegerValue(12)
        )){
            rr.addResult(it.values)
            true
        }
        val result = rr.list
        assertEquals(1, result.size)
        result[0].also {
            assertEquals(Value.IntegerValue(10), it[0])
            assertEquals(Value.IntegerValue(11), it[1])
            assertEquals(Value.IntegerValue(12), it[2])
        }
    }

    @Test
    fun test2() {
        val indexColumns = intArrayOf(0)
        val c = MemTreeIndex(indexColumns, false)
        c.insert(Record(arrayOf(Value.IntegerValue(9), Value.IntegerValue(11), Value.IntegerValue(12)), 0uL, false))
        c.insert(Record(arrayOf(Value.IntegerValue(10), Value.IntegerValue(11), Value.IntegerValue(12)), 0uL, false))
        c.insert(Record(arrayOf(Value.IntegerValue(11), Value.IntegerValue(11), Value.IntegerValue(12)), 0uL, false))
        val rr = MemoryResultSet()
        c.select(arrayOf(
            Operator.GE to Value.IntegerValue(10)
        )){
            rr.addResult(it.values)
            true
        }
        val result = rr.list
        assertEquals(2, result.size)
        result[0].also {
            assertEquals(Value.IntegerValue(10), it[0])
            assertEquals(Value.IntegerValue(11), it[1])
            assertEquals(Value.IntegerValue(12), it[2])
        }
        result[1].also {
            assertEquals(Value.IntegerValue(11), it[0])
            assertEquals(Value.IntegerValue(11), it[1])
            assertEquals(Value.IntegerValue(12), it[2])
        }
    }
}