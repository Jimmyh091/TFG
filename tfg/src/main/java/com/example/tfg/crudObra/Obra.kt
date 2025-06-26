package com.example.tfg.crudObra

import android.net.Uri
import com.example.tfg.clases.Comentario
import java.io.Serializable

data class Obra(
    val id_firebase: String? = "ERROR",
    val autor: String? = "ERROR",
    val titulo: String? = "ERROR",
    val descripcion: String? = "ERROR",
    val genero: String? = "ERROR",
    val precio: Float? = -1.0f,
    val id_comprador: String? = null,
    val rutaImagen: String? = "ERROR",
    val idImagen: String? = "ERROR",
    val comentarios: MutableMap<String, Comentario>? = mutableMapOf<String, Comentario>(),
    var fechaCreacion: Long = System.currentTimeMillis(),
    var amonestada: Boolean = false
) : Serializable