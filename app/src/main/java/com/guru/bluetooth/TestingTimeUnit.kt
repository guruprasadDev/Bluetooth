package com.guru.bluetooth

import java.util.concurrent.TimeUnit

fun convertToMin() {
    val timeUnit = TimeUnit.MINUTES.convert(300, TimeUnit.SECONDS)
    println(timeUnit)
}

fun main() {
    convertToMin()
}