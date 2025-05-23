package com.fahmimuh.feature_tradeoptimizer.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.CountryData
import com.fahmimuh.core_domain.model.InvestmentInput
import com.fahmimuh.core_ui.composables.CurrencyDropdownMenu
import com.fahmimuh.feature_tradeoptimizer.uistate.UiCountryData
import com.fahmimuh.feature_tradeoptimizer.uistate.UiProduceItem
import com.fahmimuh.feature_tradeoptimizer.viewmodel.TradeInputViewModel
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeInputScreen(
    viewModel: TradeInputViewModel = hiltViewModel(),
    onCalculateClicked: (investment: InvestmentInput,
                         countries: List<CountryData>,
                         date: String?) -> Unit
) {
    val investmentAmount = viewModel.investmentAmount.value
    val homeCurrency = viewModel.homeCurrency.value
    val countries = viewModel.countries
    val selectedDateString = viewModel.selectedDate.value
    val showDatePickerDialog = remember { mutableStateOf(false) }

    var showValidationErrorDialog by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }

    val availableCurrenciesState by viewModel.availableCurrenciesState.collectAsState()

    if (showValidationErrorDialog) {
        AlertDialog(
            onDismissRequest = { showValidationErrorDialog = false },
            title = { Text("Input Error") },
            text = { Text(validationErrorMessage) },
            confirmButton = {
                Button(onClick = { showValidationErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Trade Optimizer Input") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val investmentInput = viewModel.getInvestmentInput()
                    val countriesData = viewModel.getCountriesData()
                    val dateForCalc = viewModel.selectedDate.value

                    if (investmentInput == null) {
                        validationErrorMessage = "Invalid investment amount or home currency (must be 3 letters)."
                        showValidationErrorDialog = true
                    } else if (countriesData == null || countriesData.isEmpty()) {
                        validationErrorMessage = "Please add valid country and produce information. Ensure all fields are filled correctly, prices/stock are valid numbers, and sell price is not less than buy price."
                        showValidationErrorDialog = true
                    } else {
                        onCalculateClicked(investmentInput, countriesData, dateForCalc)
                    }
                },
                icon = { Icon(Icons.Filled.AddCircle, "Calculate") },
                text = { Text("Calculate") }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DateSelectionSection(
                    selectedDateString = selectedDateString,
                    onDateIconClick = { showDatePickerDialog.value = true },
                    onDateClearClick = { viewModel.onDateCleared() }
                )
            }
            item {
                InvestmentInputSection(
                    investmentAmount = investmentAmount,
                    homeCurrency = homeCurrency,
                    onInvestmentChange = viewModel::onInvestmentAmountChange,
                    onCurrencyChange = viewModel::onHomeCurrencyChange,
                    availableCurrenciesResource = availableCurrenciesState
                )
            }

            itemsIndexed(countries, key = { _, country -> country.id }) { countryIndex, countryData ->
                CountryInputCard(
                    countryIndex = countryIndex,
                    countryData = countryData,
                    onCountryNameChange = { viewModel.onCountryNameChange(countryIndex, it) },
                    onCountryCurrencyChange = { viewModel.onCountryCurrencyChange(countryIndex, it) },
                    onAddProduce = { viewModel.addProduceItem(countryIndex) },
                    onRemoveCountry = { viewModel.removeCountry(countryIndex) },
                    onProduceChange = { produceIndex, field, value ->
                        when (field) {
                            "name" -> viewModel.onProduceNameChange(countryIndex, produceIndex, value)
                            "buyPrice" -> viewModel.onProduceBuyPriceChange(countryIndex, produceIndex, value)
                            "sellPrice" -> viewModel.onProduceSellPriceChange(countryIndex, produceIndex, value)
                            "stock" -> viewModel.onProduceStockChange(countryIndex, produceIndex, value)
                        }
                    },
                    onRemoveProduce = { produceIndex -> viewModel.removeProduceItem(countryIndex, produceIndex) },
                    canRemoveCountry = countries.size > 1,
                    availableCurrenciesResource = availableCurrenciesState
                )
            }

            item {
                Button(onClick = { viewModel.addCountry() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Add Another Country")
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        if (showDatePickerDialog.value) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDateString?.let {
                    LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                } ?: Instant.now().toEpochMilli(),
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis <= Instant.now().toEpochMilli()
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerDialog.value = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedLocalDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                                viewModel.onDateSelected(selectedLocalDate)
                            }
                            showDatePickerDialog.value = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    Button(onClick = { showDatePickerDialog.value = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

    }
}

@Composable
fun InvestmentInputSection(
    investmentAmount: String,
    homeCurrency: String,
    onInvestmentChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    availableCurrenciesResource: Resource<Map<String, String>>
) {
    Column {
        OutlinedTextField(
            value = investmentAmount,
            onValueChange = onInvestmentChange,
            label = { Text("Investment Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        val (currencies, isLoading, error) = when (availableCurrenciesResource) {
            is Resource.Success -> Triple(availableCurrenciesResource.data, false, null)
            is Resource.Loading -> Triple(null, true, null)
            is Resource.Error -> Triple(null, false, availableCurrenciesResource.message)
        }

        if (error != null) {
            Text("Error loading currencies: $error", color = MaterialTheme.colorScheme.error)
        }

        CurrencyDropdownMenu(
            label = "Home Currency",
            currencyResource = availableCurrenciesResource,
            selectedCurrencyCode = homeCurrency,
            onCurrencySelected = onCurrencyChange
        )
    }
}

@Composable
fun CountryInputCard(
    countryIndex: Int,
    countryData: UiCountryData,
    onCountryNameChange: (String) -> Unit,
    onCountryCurrencyChange: (String) -> Unit,
    availableCurrenciesResource: Resource<Map<String, String>>,
    onAddProduce: () -> Unit,
    onRemoveCountry: () -> Unit,
    onProduceChange: (produceIndex: Int, field: String, value: String) -> Unit,
    onRemoveProduce: (produceIndex: Int) -> Unit,
    canRemoveCountry: Boolean
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Country #${countryIndex + 1}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (canRemoveCountry) {
                    IconButton(onClick = onRemoveCountry) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Country")
                    }
                }
            }
            OutlinedTextField(
                value = countryData.name,
                onValueChange = {
                    onCountryNameChange(it)
                },
                label = { Text("Country Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            val (currencies, isLoading, error) = when (availableCurrenciesResource) {
                is Resource.Success -> Triple(availableCurrenciesResource.data, false, null)
                is Resource.Loading -> Triple(null, true, null)
                is Resource.Error -> Triple(null, false, availableCurrenciesResource.message)
            }
            if (error != null && countryIndex == 0) {
                Text("Error loading currencies for country selection: $error", color = MaterialTheme.colorScheme.error)
            }
            CurrencyDropdownMenu(
                label = "Currency Code",
                currencyResource = availableCurrenciesResource,
                selectedCurrencyCode = countryData.currency,
                onCurrencySelected = onCountryCurrencyChange
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Produce Items:", style = MaterialTheme.typography.titleSmall)
            countryData.produceItems.forEachIndexed { produceIndex, produceItem ->
                ProduceInputItem(
                    countryIndex = countryIndex,
                    produceIndex = produceIndex,
                    produceItemData = produceItem,
                    onProduceChange = onProduceChange,
                    onRemoveProduce = onRemoveProduce,
                    canRemoveProduce = countryData.produceItems.size > 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAddProduce, modifier = Modifier.fillMaxWidth()) {
                Text("Add Produce Item to this Country")
            }
        }
    }
}

@Composable
fun ProduceInputItem(
    countryIndex: Int,
    produceIndex: Int,
    produceItemData: UiProduceItem,
    onProduceChange: (produceIndex: Int, field: String, value: String) -> Unit,
    onRemoveProduce: (produceIndex: Int) -> Unit,
    canRemoveProduce: Boolean
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Produce #${produceIndex + 1}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            if (canRemoveProduce) {
                IconButton(onClick = { onRemoveProduce(produceIndex) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Produce Item")
                }
            }
        }
        OutlinedTextField(
            value = produceItemData.name,
            onValueChange = { onProduceChange(produceIndex, "name", it) },
            label = { Text("Produce Name (e.g., Apples)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = produceItemData.buyPrice,
                onValueChange = { onProduceChange(produceIndex, "buyPrice", it) },
                label = { Text("Buy Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = produceItemData.sellPrice,
                onValueChange = { onProduceChange(produceIndex, "sellPrice", it) },
                label = { Text("Sell Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(
            value = produceItemData.stock,
            onValueChange = { onProduceChange(produceIndex, "stock", it) },
            label = { Text("Available Stock") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DateSelectionSection(
    selectedDateString: String?,
    onDateIconClick: () -> Unit,
    onDateClearClick: () -> Unit
) {
    Column {
        Text(
            "Select Date (Optional, for historical rates):",
            style = MaterialTheme.typography.titleSmall,

        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = selectedDateString ?: "Latest (Click icon to change)",
            onValueChange = {},
            label = { Text(
                "Trade Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = "Select Date"
                ) {
                    onDateIconClick()
                },
            leadingIcon = {
                IconButton(onClick = onDateIconClick) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Select Date")
                }
            },
            trailingIcon = {
                if (selectedDateString != null) {
                    IconButton(onClick = onDateClearClick) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear Date")
                    }
                }
            },
        )
    }
}