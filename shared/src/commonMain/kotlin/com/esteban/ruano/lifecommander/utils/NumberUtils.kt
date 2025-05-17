package com.esteban.ruano.lifecommander.utils
fun Number.toCurrencyFormat(): String {
    val value = this.toDouble()
    val isNegative = value < 0
    val absValue = kotlin.math.abs(value)

    val whole = absValue.toInt()
    val cents = ((absValue - whole) * 100).toInt()

    val formattedWhole = whole
        .toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    val formattedCents = cents.toString().padStart(2, '0')
    val result = "$formattedWhole.$formattedCents"

    return if (isNegative) "-$$result" else "$$result"
}
