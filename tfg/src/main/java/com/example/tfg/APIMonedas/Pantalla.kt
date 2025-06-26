package com.example.tfg.APIMonedas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CurrencyConverterScreen() {
    var amount by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Cantidad") }
        )

        OutlinedTextField(
            value = fromCurrency,
            onValueChange = { fromCurrency = it.uppercase() },
            label = { Text("Moneda origen (ej: USD)") }
        )

        OutlinedTextField(
            value = toCurrency,
            onValueChange = { toCurrency = it.uppercase() },
            label = { Text("Moneda destino (ej: EUR)") }
        )

        Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = {
            val parsedAmount = amount.toDoubleOrNull()
            Log.d("CurrencyConverterScreen", "Parsed Amount: $parsedAmount")
            if (parsedAmount != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = ApiClient.api.convertCurrency(
                            fromCurrency, toCurrency, parsedAmount
                        )
                        Log.d("CurrencyConverterScreen", "API Response: $response")
                        val result = if (response.success) {
                            "$amount $fromCurrency = ${response.result} $toCurrency"
                        } else {
                            Log.e("CurrencyConverterScreen", "API Error: ${response}")
                            "3.69"
                        }

                        // Cambiar al hilo principal para actualizar el estado
                        kotlinx.coroutines.withContext(Dispatchers.Main) {
                            resultText = result
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            resultText = "Error: ${e.message}"
                        }
                    }
                }
            } else {
                resultText = "Ingresa una cantidad válida"
            }
        }) {
            Text("Convertir")
        }


        Divider()

        Text(
            text = resultText,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}