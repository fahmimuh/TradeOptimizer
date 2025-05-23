package com.fahmimuh.core_domain.model

data class ProduceItem(
    val name: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int
) {
    val profitPerUnitLocal: Double
        get() = if (sellPrice >= buyPrice) sellPrice - buyPrice else 0.0
}