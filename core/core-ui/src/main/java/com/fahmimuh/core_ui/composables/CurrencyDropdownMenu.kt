package com.fahmimuh.core_ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahmimuh.core_common.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdownMenu(
    label: String,
    currencyResource: Resource<Map<String, String>>,
    selectedCurrencyCode: String,
    onCurrencySelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val (availableCurrencies, isLoading, errorMessage) = when (currencyResource) {
        is Resource.Success -> Triple(currencyResource.data, false, null)
        is Resource.Loading -> Triple(null, true, null)
        is Resource.Error -> Triple(null, false, currencyResource.message)
    }

    val currencyList = remember(availableCurrencies) { availableCurrencies?.toList() ?: emptyList() }
    val currentSelectionDisplay = if (isLoading) {
        "Loading currencies..."
    } else if (errorMessage != null) {
        "Error"
    } else {
        "${selectedCurrencyCode} (${availableCurrencies?.get(selectedCurrencyCode) ?: "Select"})"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded && enabled && !isLoading && errorMessage == null,
            onExpandedChange = {
                if (enabled && !isLoading && errorMessage == null && currencyList.isNotEmpty()) {
                    expanded = !expanded
                }
            },
        ) {
            OutlinedTextField(
                value = currentSelectionDisplay,
                onValueChange = {},
                label = { Text(label) },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                readOnly = true,
                enabled = enabled && !isLoading,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                isError = errorMessage != null
            )

            if (errorMessage == null && !isLoading) {
                ExposedDropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { expanded = false }
                ) {
                    if (currencyList.isNotEmpty()) {
                        currencyList.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text("$code ($name)") },
                                onClick = {
                                    onCurrencySelected(code)
                                    expanded = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("No currencies available") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }
        }
        if (errorMessage != null && !isLoading) {
            Text(
                text = "Could not load currencies: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}