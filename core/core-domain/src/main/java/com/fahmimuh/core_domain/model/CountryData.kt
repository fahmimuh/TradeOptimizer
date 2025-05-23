package com.fahmimuh.core_domain.model

data class CountryData(
    val name: String,
    val currency: String,
    val produceItems: List<ProduceItem>
)