package com.ekdb

import pw.binom.io.Closeable

interface FileIndex : Closeable {
    fun find(values: IndexValues): ULong?
}