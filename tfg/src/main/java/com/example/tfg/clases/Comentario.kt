package com.example.tfg.clases

data class Comentario (
    val id: String = "ERROR",
    val autor: String = "ERROR",
    val texto: String = "ERROR",
    var fechaCreacion: Long = System.currentTimeMillis()
)