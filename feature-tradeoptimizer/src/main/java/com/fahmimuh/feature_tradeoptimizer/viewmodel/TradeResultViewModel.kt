package com.fahmimuh.feature_tradeoptimizer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.CountryData
import com.fahmimuh.core_domain.model.InvestmentInput
import com.fahmimuh.core_domain.model.OptimizedTradeResult
import com.fahmimuh.core_domain.usecase.CalculateOptimalTradeStrategyUseCase
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class TradeResultViewModel @Inject constructor(
    private val calculateOptimalTradeStrategyUseCase: CalculateOptimalTradeStrategyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _tradeResultState = MutableStateFlow<Resource<OptimizedTradeResult>>(Resource.Loading())
    val tradeResultState: StateFlow<Resource<OptimizedTradeResult>> = _tradeResultState.asStateFlow()

    init {
        val investmentJson = savedStateHandle.get<String>("investmentJson")
        val countriesJson = savedStateHandle.get<String>("countriesJson")
        val date = savedStateHandle.get<String>("date")

        if (investmentJson != null && countriesJson != null) {
            try {
                val investment = Gson().fromJson(URLDecoder.decode(investmentJson, "UTF-8"), InvestmentInput::class.java)
                val countriesListType = object : com.google.gson.reflect.TypeToken<List<CountryData>>() {}.type
                val countries: List<CountryData> = Gson().fromJson(URLDecoder.decode(countriesJson, "UTF-8"), countriesListType)

                calculateTradePlan(investment, countries, date?.ifBlank { null })
            } catch (e: Exception) {
                _tradeResultState.value = Resource.Error("Failed to decode input data: ${e.message}")
            }
        } else {
            _tradeResultState.value = Resource.Error("Missing input data for calculation.")
        }
    }

    private fun calculateTradePlan(
        investmentInput: InvestmentInput,
        countriesData: List<CountryData>,
        date: String?) {
        calculateOptimalTradeStrategyUseCase(investmentInput, countriesData, date)
            .onEach { result ->
                _tradeResultState.value = result
            }
            .launchIn(viewModelScope)
    }
}