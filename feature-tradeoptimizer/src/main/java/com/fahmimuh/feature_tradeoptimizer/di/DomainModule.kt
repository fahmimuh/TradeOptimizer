package com.fahmimuh.feature_tradeoptimizer.di

import com.fahmimuh.core_domain.repository.TradeRepository
import com.fahmimuh.core_domain.usecase.CalculateOptimalTradeStrategyUseCase
import com.fahmimuh.core_domain.usecase.GetAvailableCurrenciesUseCase
import com.fahmimuh.core_domain.usecase.GetExchangeRatesUseCase
import com.fahmimuh.core_domain.usecase.GetHistoricalExchangeRatesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DomainModule {

    @Provides
    @ViewModelScoped
    fun provideGetExchangeRatesUseCase(tradeRepository: TradeRepository): GetExchangeRatesUseCase {
        return GetExchangeRatesUseCase(tradeRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetHistoricalExchangeRatesUseCase(tradeRepository: TradeRepository): GetHistoricalExchangeRatesUseCase {
        return GetHistoricalExchangeRatesUseCase(tradeRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCalculateOptimalTradeStrategyUseCase(
        getExchangeRatesUseCase: GetExchangeRatesUseCase,
        getHistoricalExchangeRatesUseCase: GetHistoricalExchangeRatesUseCase
    ): CalculateOptimalTradeStrategyUseCase {
        return CalculateOptimalTradeStrategyUseCase(getExchangeRatesUseCase,
            getHistoricalExchangeRatesUseCase)

    }

    @Provides
    @ViewModelScoped
    fun provideGetAvailableCurrenciesUseCase(tradeRepository: TradeRepository): GetAvailableCurrenciesUseCase {
        return GetAvailableCurrenciesUseCase(tradeRepository)
    }
}