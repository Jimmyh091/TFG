package com.example.tfg.APIMonedas

import retrofit2.http.GET
import retrofit2.http.Query

data class ConvertResponse(
    val result: Double,
    val success: Boolean
)

interface ExchangeRateApi {
    @GET("convert")
    suspend fun convertCurrency(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double
    ): ConvertResponse
}

