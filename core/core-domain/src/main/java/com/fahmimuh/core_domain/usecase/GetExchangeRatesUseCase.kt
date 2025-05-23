package com.fahmimuh.core_domain.usecase

import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.ExchangeRate
import com.fahmimuh.core_domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetExchangeRatesUseCase(
    private val tradeRepository: TradeRepository
) {
    operator fun invoke(
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Flow<Resource<Map<String, ExchangeRate>>> = flow {
        emit(Resource.Loading())
        val distinctTargetCurrencies = countryCurrencies.distinct().filter { it != homeCurrency }
        if (distinctTargetCurrencies.isEmpty()) {
            emit(Resource.Success(emptyMap()))
            return@flow
        }
        try {
            val result = tradeRepository.getExchangeRatesForCountries(homeCurrency, distinctTargetCurrencies)
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch exchange rates: ${e.localizedMessage}"))
        }
    }
}