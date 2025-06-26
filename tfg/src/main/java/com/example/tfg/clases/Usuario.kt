package com.example.tfg.clases

data class Usuario(
    var id_firebase: String = "",
    var admin: Boolean = false,
    var nombre: String = "",
    var correo: String = "",
    var contrasenia: String = "",
    var imagen: String = "",
    var dinero: Float = 0f,
    var fechaCreacion: Long = System.currentTimeMillis(),
    var obrasFavoritas: MutableList<String> = mutableListOf(),
    var obrasCompradas: MutableList<String> = mutableListOf(),
    var obrasCreadas: MutableList<String> = mutableListOf()
)
