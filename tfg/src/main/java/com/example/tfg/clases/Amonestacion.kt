package com.example.tfg.clases

data class Amonestacion(
    var id: String? = null,
    val obraId: String = "",
    val autorId: String = "",
    val denuncianteId: String = "",
    val motivo: String = "",
    val estado: String = "pendiente",
    val fecha: Long = System.currentTimeMillis()
)
