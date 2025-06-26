package com.example.tfg.clases

import androidx.compose.runtime.mutableStateListOf
import com.example.tfg.crudObra.Obra

object CarritoManager {
    val obrasEnCarrito = mutableStateListOf<String>()
    var subastaBloqueanteId: String? = null

    fun aniadir(obra: Obra) {
        if (subastaBloqueanteId == null) {
            if (!obrasEnCarrito.contains(obra.id_firebase)) {
                obrasEnCarrito.add(obra.id_firebase!!)
            }
        }
    }

    fun aniadirSubastaGanada(obra: Obra) {
        subastaBloqueanteId = obra.id_firebase
        if (!obrasEnCarrito.contains(obra.id_firebase)) {
            obrasEnCarrito.add(obra.id_firebase!!)
        }
    }

    fun borrar(id: String) {
        obrasEnCarrito.remove(id)
        if (subastaBloqueanteId == id) {
            subastaBloqueanteId = null
        }
    }

    fun vaciar() {
        obrasEnCarrito.clear()
        subastaBloqueanteId = null
    }

    fun hayBloqueo(): Boolean = subastaBloqueanteId != null
}
