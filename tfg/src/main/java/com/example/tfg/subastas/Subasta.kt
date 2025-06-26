package com.example.tfg.subastas

import com.example.tfg.clases.Comentario
import com.example.tfg.crudObra.Obra

data class Subasta(
    val idObra_firebase: String? = null,
    val idSubasta_firebase: String? = null,
    val fechaLimite: Long = 0L,
    val idGanador: String? = null
)