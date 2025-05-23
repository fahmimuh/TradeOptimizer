package com.fahmimuh.core_domain.model

data class OptimizedTradeResult(
    val plan: List<TradeAction>,
    val totalCostHomeCurrency: Double,
    val totalProfitHomeCurrency: Double,
    val homeCurrencyCode: String
)