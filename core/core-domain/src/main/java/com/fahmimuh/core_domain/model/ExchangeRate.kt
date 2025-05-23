package com.fahmimuh.core_domain.model

data class ExchangeRate(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double
)