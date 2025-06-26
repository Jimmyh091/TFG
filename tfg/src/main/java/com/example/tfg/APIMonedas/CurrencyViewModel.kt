package com.example.tfg.APIMonedas


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CurrencyViewModel : ViewModel() {

    private val _result = MutableStateFlow<String>("")
    val result = _result.asStateFlow()

    fun convert(from: String, to: String, amount: Double) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.convertCurrency(from, to, amount)
                if (response.success) {
                    _result.value = "${amount} $from = ${response.result} $to"
                } else {
                    _result.value = "Error en la conversión"
                }
            } catch (e: Exception) {
                _result.value = "Fallo de red o error: ${e.message}"
            }
        }
    }
}
