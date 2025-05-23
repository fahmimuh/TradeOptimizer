package com.fahmimuh.core_data.mappers

import com.fahmimuh.core_data.network.dto.ExchangeRateResponseDto
import com.fahmimuh.core_domain.model.ExchangeRate

fun ExchangeRateResponseDto.toDomainExchangeRatesMap(): Map<String, ExchangeRate> {
    val domainRates = mutableMapOf<String, ExchangeRate>()
    val fromCurrency = this.base ?: return emptyMap()

    this.rates?.forEach { (toCurrencyCode, rateValue) ->
        domainRates[toCurrencyCode] = ExchangeRate(
            fromCurrency = fromCurrency,
            toCurrency = toCurrencyCode,
            rate = rateValue
        )
    }
    return domainRates
}

fun ExchangeRateResponseDto.toDomainExchangeRate(targetCurrency: String): ExchangeRate? {
    val fromCurrency = this.base ?: return null
    val rateValue = this.rates?.get(targetCurrency) ?: return null
    return ExchangeRate(
        fromCurrency = fromCurrency,
        toCurrency = targetCurrency,
        rate = rateValue
    )
}