package com.fahmimuh.core_domain.usecase

import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.repository.TradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAvailableCurrenciesUseCase(
    private val tradeRepository: TradeRepository
) {
    operator fun invoke(): Flow<Resource<Map<String, String>>> = flow {
        emit(Resource.Loading())
        try {
            val result = tradeRepository.getAvailableCurrencies()
            emit(result)
        } catch (e: Exception) {
            emit(Resource.Error("Failed to fetch available currencies: ${e.localizedMessage}"))
        }
    }
}