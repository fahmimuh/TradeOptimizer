package com.fahmimuh.feature_tradeoptimizer.uistate

data class UiProduceItem(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var buyPrice: String = "",
    var sellPrice: String = "",
    var stock: String = ""
)

data class UiCountryData(
    var id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "",
    var currency: String = "",
    var produceItems: MutableList<UiProduceItem> = mutableListOf(UiProduceItem())
)

data class TradeInputState(
    val investmentAmount: String = "",
    val homeCurrency: String = "",
    val countries: MutableList<UiCountryData> = mutableListOf(UiCountryData())
)