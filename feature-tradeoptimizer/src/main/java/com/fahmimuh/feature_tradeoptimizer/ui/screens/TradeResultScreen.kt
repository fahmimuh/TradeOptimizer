package com.fahmimuh.feature_tradeoptimizer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fahmimuh.core_common.utils.Resource
import com.fahmimuh.core_domain.model.TradeAction
import com.fahmimuh.core_domain.model.OptimizedTradeResult
import com.fahmimuh.feature_tradeoptimizer.viewmodel.TradeResultViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeResultScreen(
    navController: NavController,
    viewModel: TradeResultViewModel = hiltViewModel()
) {
    val resultState by viewModel.tradeResultState.collectAsState()
    val calculationDateString by viewModel.calculationDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trade Optimization Result") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (val resource = resultState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = "Error: ${resource.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is Resource.Success -> {
                    val tradeResult = resource.data
                    if (tradeResult != null) {
                        if (tradeResult.plan.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UsedParametersInfo(
                                    dateString = calculationDateString,
                                    homeCurrency = tradeResult.homeCurrencyCode
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No profitable trades found with the given input and constraints.")
                            }
                        } else {
                            OptimizedResultContent(
                                tradeResult,
                                dateString = calculationDateString)
                        }
                    } else {
                        Text(
                            "No data available for the trade result.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UsedParametersInfo(dateString: String?, homeCurrency: String) {
    val displayDate = remember(dateString) {
        if (dateString.isNullOrBlank()) {
            "Latest Rates"
        } else {
            try {
                LocalDate.parse(dateString).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            } catch (e: Exception) {
                dateString // Show raw string if parsing fails
            }
        }
    }
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            "Calculation Parameters:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        InfoRow("Rates Used:", displayDate)
        InfoRow("Home Currency:", homeCurrency)
        Spacer(modifier = Modifier.height(8.dp)) // Add some space before the plan title
    }
}

@Composable
fun OptimizedResultContent(result: OptimizedTradeResult, dateString: String?) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            UsedParametersInfo(dateString = dateString, homeCurrency = result.homeCurrencyCode)
        }
        item {
            Text("Optimal Trade Plan:", style = MaterialTheme.typography.headlineSmall)
        }
        if (result.plan.isNotEmpty()) {
            items(result.plan) { tradeAction ->
                TradeActionCard(
                    tradeAction,
                    homeCurrencyCode = result.homeCurrencyCode)
            }
        } else {
            item {
                Text("No actions in the plan.", style = MaterialTheme.typography.bodyLarge)
            }
        }

        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }

        item {
            SummarySection(
                label = "Total Cost (${result.homeCurrencyCode}):",
                value = String.format("%.2f", result.totalCostHomeCurrency)
            )
        }
        item {
            SummarySection(
                label = "Total Profit (${result.homeCurrencyCode}):",
                value = String.format("%.2f", result.totalProfitHomeCurrency),
                valueColor = if (result.totalProfitHomeCurrency >= 0) Color.Green.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TradeActionCard(action: TradeAction, homeCurrencyCode: String) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "${action.produceName} from ${action.countryName}",
                style = MaterialTheme.typography.titleMedium
            )
            InfoRow("Units Bought:", "${action.unitsBought}")
            InfoRow(
                label = "Cost (${action.localCurrencyCode}):",
                value = String.format("%.2f", action.costInLocalCurrency)
            )
            InfoRow(
                label = "Cost ($homeCurrencyCode):",
                value = String.format("%.2f", action.costInHomeCurrency)
            )
            InfoRow(
                label = "Profit ($homeCurrencyCode):",
                value = String.format("%.2f", action.profitInHomeCurrency),
                valueColor = if (action.profitInHomeCurrency >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error // Using theme colors
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f))
        Text(value, color = valueColor, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SummarySection(label: String, value: String, valueColor: Color = LocalContentColor.current) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), color = valueColor)
    }
}