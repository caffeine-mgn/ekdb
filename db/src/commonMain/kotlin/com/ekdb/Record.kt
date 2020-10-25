package com.ekdb

class Record(
    var values: Array<Value?>,
    var address: ULong,
    var deleted: Boolean
)