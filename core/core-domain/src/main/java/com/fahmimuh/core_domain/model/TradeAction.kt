package com.fahmimuh.core_domain.model

data class TradeAction(
    val countryName: String,
    val produceName: String,
    val unitsBought: Int,
    val costInLocalCurrency: Double,
    val costInHomeCurrency: Double,
    val localCurrencyCode: String,
    val profitInHomeCurrency: Double
)