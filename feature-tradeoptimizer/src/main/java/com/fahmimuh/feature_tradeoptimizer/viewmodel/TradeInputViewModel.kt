package com.fahmimuh.feature_tradeoptimizer.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.CountryData
import com.fahmimuh.core_domain.model.InvestmentInput
import com.fahmimuh.core_domain.model.ProduceItem
import com.fahmimuh.core_domain.usecase.GetAvailableCurrenciesUseCase
import com.fahmimuh.feature_tradeoptimizer.uistate.UiCountryData
import com.fahmimuh.feature_tradeoptimizer.uistate.UiProduceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TradeInputViewModel @Inject constructor(
    private val getAvailableCurrenciesUseCase: GetAvailableCurrenciesUseCase
) : ViewModel() {

    val investmentAmount = mutableStateOf("")
    val homeCurrency = mutableStateOf("USD")
    val selectedDate = mutableStateOf<String?>(null)

    private val _availableCurrenciesState = MutableStateFlow<Resource<Map<String, String>>>(Resource.Loading())
    val availableCurrenciesState: StateFlow<Resource<Map<String, String>>> = _availableCurrenciesState.asStateFlow()

    val countries = mutableStateListOf(UiCountryData(currency = "EUR"))

    init {
        fetchAvailableCurrencies()
    }

    private fun fetchAvailableCurrencies() {
        getAvailableCurrenciesUseCase().onEach { result ->
            _availableCurrenciesState.value = result
            if (result is Resource.Success && result.data?.containsKey(homeCurrency.value) == false) {
                homeCurrency.value = result.data!!.keys.firstOrNull() ?: "USD"
            }
            if (result is Resource.Success && countries.isNotEmpty() && result.data?.containsKey(countries[0].currency) == false) {
                val firstAvailableCurrency = result.data!!.keys.firstOrNull()
                if (firstAvailableCurrency != null) {
                    onCountryCurrencyChange(0, firstAvailableCurrency)
                }
            }

        }.launchIn(viewModelScope)
    }

    fun onDateSelected(date: LocalDate?) {
        selectedDate.value = date?.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun onDateCleared() {
        selectedDate.value = null
    }

    fun onInvestmentAmountChange(newValue: String) {
        investmentAmount.value = newValue.filter { it.isDigit() || it == '.' }
    }

    fun onHomeCurrencyChange(newValue: String) {
        homeCurrency.value = newValue.uppercase().filter { it.isLetter() }
    }

    fun addCountry() {
        countries.add(UiCountryData())
    }

    fun removeCountry(index: Int) {
        if (countries.size > 1) {
            countries.removeAt(index)
        }
    }

    fun onCountryNameChange(countryIndex: Int, newName: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val currentCountry = countries[countryIndex]
            countries[countryIndex] = currentCountry.copy(name = newName)
        }
    }

    fun onCountryCurrencyChange(countryIndex: Int, newCurrency: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            countries[countryIndex] = countries[countryIndex].copy(currency = newCurrency)
        }
    }

    fun addProduceItem(countryIndex: Int) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val currentCountry = countries[countryIndex]
            val updatedProduceItems = currentCountry.produceItems.toMutableList()
            updatedProduceItems.add(UiProduceItem())
            countries[countryIndex] = currentCountry.copy(produceItems = updatedProduceItems)
        }
    }

    fun removeProduceItem(countryIndex: Int, produceIndex: Int) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val currentCountry = countries[countryIndex]
            if (produceIndex >= 0 && produceIndex < currentCountry.produceItems.size) {
                if (currentCountry.produceItems.size > 1) {
                    val updatedProduceItems = currentCountry.produceItems.toMutableList()
                    updatedProduceItems.removeAt(produceIndex)
                    countries[countryIndex] = currentCountry.copy(produceItems = updatedProduceItems)
                }
            }
        }
    }

    fun onProduceNameChange(countryIndex: Int, produceIndex: Int, newName: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val country = countries[countryIndex]
            if (produceIndex >= 0 && produceIndex < country.produceItems.size) {
                val updatedProduceItems = country.produceItems.toMutableList()
                updatedProduceItems[produceIndex] = updatedProduceItems[produceIndex].copy(name = newName)
                countries[countryIndex] = country.copy(produceItems = updatedProduceItems)
            }
        }
    }

    fun onProduceBuyPriceChange(countryIndex: Int, produceIndex: Int, newPrice: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val country = countries[countryIndex]
            if (produceIndex >= 0 && produceIndex < country.produceItems.size) {
                val updatedProduceItems = country.produceItems.toMutableList()
                updatedProduceItems[produceIndex] = updatedProduceItems[produceIndex].copy(buyPrice = newPrice.filter { it.isDigit() || it == '.' })
                countries[countryIndex] = country.copy(produceItems = updatedProduceItems)
            }
        }
    }

    fun onProduceSellPriceChange(countryIndex: Int, produceIndex: Int, newPrice: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val country = countries[countryIndex]
            if (produceIndex >= 0 && produceIndex < country.produceItems.size) {
                val updatedProduceItems = country.produceItems.toMutableList()
                updatedProduceItems[produceIndex] = updatedProduceItems[produceIndex].copy(sellPrice = newPrice.filter { it.isDigit() || it == '.' })
                countries[countryIndex] = country.copy(produceItems = updatedProduceItems)
            }
        }
    }

    fun onProduceStockChange(countryIndex: Int, produceIndex: Int, newStock: String) {
        if (countryIndex >= 0 && countryIndex < countries.size) {
            val country = countries[countryIndex]
            if (produceIndex >= 0 && produceIndex < country.produceItems.size) {
                val updatedProduceItems = country.produceItems.toMutableList()
                updatedProduceItems[produceIndex] = updatedProduceItems[produceIndex].copy(stock = newStock.filter { it.isDigit() })
                countries[countryIndex] = country.copy(produceItems = updatedProduceItems)
            }
        }
    }

    fun getInvestmentInput(): InvestmentInput? {
        val amount = investmentAmount.value.toDoubleOrNull()
        return if (amount != null && homeCurrency.value.isNotBlank() && homeCurrency.value.length == 3) {
            InvestmentInput(amount, homeCurrency.value)
        } else {
            null
        }
    }

    fun getCountriesData(): List<CountryData>? {
        val domainCountries = mutableListOf<CountryData>()
        for (uiCountry in countries) {
            if (uiCountry.name.isBlank() || uiCountry.currency.isBlank() || uiCountry.currency.length != 3) return null

            val domainProduceItems = mutableListOf<ProduceItem>()
            for (uiProduce in uiCountry.produceItems) {
                val buyPrice = uiProduce.buyPrice.toDoubleOrNull()
                val sellPrice = uiProduce.sellPrice.toDoubleOrNull()
                val stock = uiProduce.stock.toIntOrNull()

                if (uiProduce.name.isBlank() || buyPrice == null || sellPrice == null || stock == null || buyPrice < 0 || sellPrice < buyPrice || stock < 0) {
                    return null
                }
                domainProduceItems.add(
                    ProduceItem(
                        name = uiProduce.name,
                        buyPrice = buyPrice,
                        sellPrice = sellPrice,
                        stock = stock
                    )
                )
            }
            if (domainProduceItems.isEmpty()) return null

            domainCountries.add(
                CountryData(
                    name = uiCountry.name,
                    currency = uiCountry.currency,
                    produceItems = domainProduceItems
                )
            )
        }
        return if (domainCountries.isNotEmpty()) domainCountries else null
    }
}