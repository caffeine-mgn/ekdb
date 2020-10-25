package com.ekdb

import pw.binom.*

typealias FullValues = Array<Value?>
typealias IndexValues = Array<Value?>

sealed class Value {

    abstract fun toLong(): Long
    abstract fun compare(value: Value): Int

    fun write(buf: ByteBuffer, output: Output) {
        val u = when (this) {
            is StringValue -> {
                output.writeByte(buf, 0)
                output.writeUTF8String(buf, value)
            }
            is IntegerValue -> {
                output.writeByte(buf, 1)
                output.writeLong(buf, value)
            }
            is DoubleValue -> {
                output.writeByte(buf, 2)
                output.writeDouble(buf, value)
            }
            is UUIDValue -> {
                output.writeByte(buf, 3)
                output.writeLong(buf, v1)
                output.writeLong(buf, v2)
            }
        }
    }

    companion object {
        fun read(buf: ByteBuffer, input: Input): Value {
            val c = input.readByte(buf)
            return when (c) {
                0.toByte() -> StringValue(input.readUTF8String(buf))
                1.toByte() -> IntegerValue(input.readLong(buf))
                2.toByte() -> DoubleValue(input.readDouble(buf))
                4.toByte() -> UUIDValue(input.readLong(buf), input.readLong(buf))
                else -> TODO()
            }
        }
    }


    class StringValue(val value: String) : Value() {
        override fun toLong(): Long = hashCode().toLong()
        override fun compare(value: Value): Int {
            if (value !is StringValue) {
                TODO()
            }
            return hashCode() - value.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as StringValue

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String = value
    }

    class IntegerValue(val value: Long) : Value() {
        override fun toLong(): Long = value
        override fun compare(value: Value): Int {
            if (value !is IntegerValue) {
                TODO()
            }
            return when {
                this.value > value.value -> 1
                this.value < value.value -> -1
                else -> 0
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as IntegerValue

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }

    class DoubleValue(val value: Double) : Value() {
        override fun toLong(): Long = value.hashCode().toLong()

        override fun compare(value: Value): Int {
            if (value !is DoubleValue) {
                TODO()
            }
            return when {
                this.value > value.value -> 1
                this.value < value.value -> -1
                else -> 0
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as DoubleValue

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = value.toString()
    }

    class UUIDValue(val v1: Long, val v2: Long) : Value() {
        override fun toLong(): Long = v1 + v2

        override fun compare(value: Value): Int {
            if (value !is UUIDValue) {
                TODO()
            }
            when {
                this.v1 > value.v1 -> return 1
                this.v1 < value.v1 -> return -1
            }

            return when {
                this.v2 > value.v2 -> return 1
                this.v2 < value.v2 -> return -1
                else -> 0
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as UUIDValue

            if (v1 != other.v1) return false
            if (v2 != other.v2) return false

            return true
        }

        override fun hashCode(): Int = 31 * v1.hashCode() + v2.hashCode()
        override fun toString(): String = (UUID.create(v1, v2)).toString()

    }
}