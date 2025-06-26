package com.example.tfg.subastas

data class Puja(
    val id: String? = null,
    val subastaId: String = "",
    val pujadorId: String = "",
    val cantidad: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
