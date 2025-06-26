package com.example.tfg.subastas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.Util
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.google.firebase.database.FirebaseDatabase

@Composable
fun DetalleSubastaScreen(
    subastaId: String,
    nav: NavHostController
) {
    Log.d("DetalleSubastaScreen", "Subasta ID: $subastaId")

    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var subasta by remember { mutableStateOf<Subasta?>(null) }
    var obra by remember { mutableStateOf<Obra?>(null) }
    var pujas by remember { mutableStateOf<List<Puja>>(emptyList()) }
    var nuevaPuja by remember { mutableStateOf("") }
    var subastaCerrada by remember { mutableStateOf(false) }

    val usuarioId = Util.obtenerDatoShared(contexto, "id") ?: return

    // Cargar subasta, luego obra y pujas
    LaunchedEffect(subastaId) {
        Util.obtenerSubasta(dbRef, subastaId) { s ->
            subasta = s
            s?.idObra_firebase?.let { obraId ->
                Util.obtenerObra(dbRef, contexto, obraId) {
                    obra = it
                }
            }
            Util.obtenerPujas(dbRef, subastaId) { pujas = it }
        }
    }

    // Cierre automático de la subasta si se ha superado la fecha límite
    LaunchedEffect(subasta, pujas) {
        val s = subasta
        if (s != null && System.currentTimeMillis() > s.fechaLimite && !subastaCerrada) {
            Log.d("Subasta", "Fecha límite superada. Cerrando automáticamente.")
            Util.cerrarSubasta(dbRef, s)
            subastaCerrada = true
            nav.navigate("resultadoSubasta/${s.idSubasta_firebase}")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)) {
        if (subasta == null || obra == null) {
            Text("Cargando subasta...")
            return@Column
        }

        val s = subasta!!
        val o = obra!!

        Text(o.titulo ?: "Sin título", style = MaterialTheme.typography.titleLarge)
        Text(o.descripcion ?: "")
        Spacer(Modifier.height(8.dp))

        // Campo para ingresar puja
        OutlinedTextField(
            value = nuevaPuja,
            onValueChange = { nuevaPuja = it },
            label = { Text("Tu puja (€)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val cantidad = nuevaPuja.toFloatOrNull()
                if (cantidad == null || cantidad <= 0f) return@Button

                Util.hacerPuja(
                    dbRef,
                    Puja(
                        subastaId = s.idSubasta_firebase ?: "",
                        pujadorId = usuarioId,
                        cantidad = cantidad
                    )
                ) {
                    nuevaPuja = ""
                    Util.obtenerPujas(dbRef, subastaId) { pujas = it } // recargar
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Pujar")
        }

        Spacer(Modifier.height(16.dp))

        Text("Pujas (más altas primero):", style = MaterialTheme.typography.titleMedium)
        pujas.sortedByDescending { it.cantidad }.forEach { p ->
            var us by remember { mutableStateOf<Usuario?>(null) }
            LaunchedEffect(p.pujadorId) {
                Util.obtenerUsuario(dbRef, p.pujadorId) { us = it }
            }
            if (us != null) {
                Text("${us!!.nombre}: ${p.cantidad}€")
            }
        }
    }
}
