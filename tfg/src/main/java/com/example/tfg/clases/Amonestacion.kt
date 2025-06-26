package com.example.tfg.clases

data class Amonestacion(
    val id: String? = null,
    val obraId: String = "",
    val autorId: String = "",
    val denuncianteId: String = "",
    val motivo: String = "",
    val estado: String = "pendiente", // "pendiente", "aprobada", "rechazada"
    val fecha: Long = System.currentTimeMillis()
)
