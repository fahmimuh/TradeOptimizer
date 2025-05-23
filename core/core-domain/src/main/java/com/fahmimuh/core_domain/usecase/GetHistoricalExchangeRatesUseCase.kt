package com.fahmimuh.core_domain.usecase

import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.ExchangeRate
import com.fahmimuh.core_domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class GetHistoricalExchangeRatesUseCase(
    private val tradeRepository: TradeRepository
) {
    operator fun invoke(
        date: String,
        homeCurrency: String,
        countryCurrencies: List<String>
    ): Flow<Resource<Map<String, ExchangeRate>>> = flow {
        emit(Resource.Loading())

        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            emit(Resource.Error("Invalid date format. Please use YYYY-MM-DD."))
            return@flow
        }

        val distinctTargetCurrencies = countryCurrencies.distinct().filter { it != homeCurrency }
        if (distinctTargetCurrencies.isEmpty() && countryCurrencies.any { it == homeCurrency }) {
            emit(Resource.Success(emptyMap()))
            return@flow
        }
        if (distinctTargetCurrencies.isEmpty()) {
            emit(Resource.Success(emptyMap()))
            return@flow
        }

        try {
            val result = tradeRepository.getHistoricalExchangeRates(date, homeCurrency, distinctTargetCurrencies)
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch historical exchange rates: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }
}