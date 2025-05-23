package com.fahmimuh.core_domain.usecase

import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.CountryData
import com.fahmimuh.core_domain.model.ExchangeRate
import com.fahmimuh.core_domain.model.InvestmentInput
import com.fahmimuh.core_domain.model.OptimizedTradeResult
import com.fahmimuh.core_domain.model.ProduceItem
import com.fahmimuh.core_domain.model.TradeAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.floor

class CalculateOptimalTradeStrategyUseCase(
    private val getExchangeRatesUseCase: GetExchangeRatesUseCase,
    private val getHistoricalExchangeRatesUseCase: GetHistoricalExchangeRatesUseCase
) {
    operator fun invoke(
        investmentInput: InvestmentInput,
        countriesData: List<CountryData>,
        date: String? = null
    ): Flow<Resource<OptimizedTradeResult>> = flow {
        emit(Resource.Loading())

        if (investmentInput.amount <= 0) {
            emit(Resource.Success(OptimizedTradeResult(
                emptyList(), 0.0, 0.0,
                homeCurrencyCode = ""
            )))
            return@flow
        }

        val countryCurrencies = countriesData.map { it.currency }.distinct()
        val exchangeRatesFlow: Flow<Resource<Map<String, ExchangeRate>>> = if (date != null) {
            getHistoricalExchangeRatesUseCase(date, investmentInput.currency, countryCurrencies)
        } else {
            getExchangeRatesUseCase(investmentInput.currency, countryCurrencies)
        }
        var ratesMap: Map<String, ExchangeRate>? = null

        exchangeRatesFlow.collect { resource ->
            when (resource) {
                is Resource.Loading -> emit(Resource.Loading())
                is Resource.Success -> {
                    ratesMap = resource.data
                    if (ratesMap != null) {
                        try {
                            val result = calculateStrategy(investmentInput, countriesData, ratesMap!!)
                            emit(Resource.Success(result))
                        } catch (e: Exception) {
                            emit(Resource.Error("Error calculating strategy: ${e.localizedMessage}"))
                        }
                    } else if (countryCurrencies.all { it == investmentInput.currency } ) {
                        val dummyRates = countryCurrencies.associateWith {
                            ExchangeRate(investmentInput.currency, it, 1.0)
                        }
                        try {
                            val result = calculateStrategy(investmentInput, countriesData, dummyRates)
                            emit(Resource.Success(result))
                        } catch (e: Exception) {
                            emit(Resource.Error("Error calculating strategy: ${e.localizedMessage}"))
                        }
                    } else {
                        emit(Resource.Error("Exchange rates successfully fetched but no relevant data found."))
                    }
                }
                is Resource.Error -> {
                    emit(Resource.Error(resource.message ?: "Failed to fetch critical exchange rates.")) // CORRECT
                    return@collect
                }
            }
        }
    }


    private fun calculateStrategy(
        investmentInput: InvestmentInput,
        countriesData: List<CountryData>,
        exchangeRates: Map<String, ExchangeRate>
    ): OptimizedTradeResult {
        val tradeActions = mutableListOf<TradeAction>()
        var remainingInvestmentHomeCurrency = investmentInput.amount
        
        val potentialTrades = mutableListOf<Triple<ProduceItem, CountryData, Double>>()

        countriesData.forEach { country ->
            val toLocalRate = exchangeRates[country.currency]?.rate ?:
            if (country.currency == investmentInput.currency) 1.0 else return OptimizedTradeResult(
                emptyList(), 0.0, 0.0,
                homeCurrencyCode = ""
            ) 

            country.produceItems.forEach { item ->
                if (item.profitPerUnitLocal > 0) {
                    val profitPerUnitHome = item.profitPerUnitLocal / toLocalRate
                    potentialTrades.add(Triple(item, country, profitPerUnitHome))
                }
            }
        }

        potentialTrades.sortByDescending { it.third }

        for ((item, country, profitPerUnitHome) in potentialTrades) {
            if (remainingInvestmentHomeCurrency <= 0) break

            val toLocalRate = exchangeRates[country.currency]?.rate ?:
            if (country.currency == investmentInput.currency) 1.0 else continue

            val itemCostLocal = item.buyPrice
            val itemCostHome = itemCostLocal / toLocalRate

            if (itemCostHome <= 0) continue 

            var unitsToBuy = item.stock
            if (itemCostHome > 0) { 
                val maxUnitsByBudget = floor(remainingInvestmentHomeCurrency / itemCostHome).toInt()
                unitsToBuy = minOf(unitsToBuy, maxUnitsByBudget)
            } else if (remainingInvestmentHomeCurrency > 0) { 
                unitsToBuy = item.stock
            } else {
                unitsToBuy = 0
            }


            if (unitsToBuy > 0) {
                val totalCostForUnitsLocal = unitsToBuy * item.buyPrice
                val totalCostForUnitsHome = unitsToBuy * itemCostHome
                val totalProfitForUnitsHome = unitsToBuy * profitPerUnitHome

                tradeActions.add(
                    TradeAction(
                        countryName = country.name,
                        produceName = item.name,
                        unitsBought = unitsToBuy,
                        costInLocalCurrency = totalCostForUnitsLocal,
                        costInHomeCurrency = totalCostForUnitsHome,
                        profitInHomeCurrency = totalProfitForUnitsHome,
                        localCurrencyCode = country.currency,
                    )
                )
                remainingInvestmentHomeCurrency -= totalCostForUnitsHome
            }
        }

        val totalCost = tradeActions.sumOf { it.costInHomeCurrency }
        val totalProfit = tradeActions.sumOf { it.profitInHomeCurrency }

        return OptimizedTradeResult(tradeActions, totalCost, totalProfit, investmentInput.currency)
    }
}