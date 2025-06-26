package com.example.tfg.subastas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var subasta by remember { mutableStateOf<Subasta?>(null) }
    var obra by remember { mutableStateOf<Obra?>(null) }
    var pujas by remember { mutableStateOf<List<Puja>>(emptyList()) }
    var nuevaPuja by remember { mutableStateOf("") }
    var subastaCerrada by remember { mutableStateOf(false) }

    val usuarioId = Util.obtenerDatoShared(contexto, "id") ?: return

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

    LaunchedEffect(subasta, pujas) {
        val s = subasta
        if (s != null && System.currentTimeMillis() > s.fechaLimite && !subastaCerrada) {
            Util.cerrarSubasta(dbRef, s)
            subastaCerrada = true
            nav.navigate("resultadoSubasta/${s.idSubasta_firebase}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (subasta == null || obra == null) {
            Text("Cargando subasta...")
            return@Column
        }

        val s = subasta!!
        val o = obra!!
        val esAutor = o.autor == usuarioId

        // Obra destacada
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(o.titulo ?: "Sin título", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(o.descripcion ?: "", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!esAutor) {
            Text("Introduce tu puja", style = MaterialTheme.typography.titleMedium)

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
                        Util.obtenerPujas(dbRef, subastaId) { pujas = it }
                    }
                },
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            ) {
                Text("Pujar")
            }

            Spacer(Modifier.height(24.dp))
        } else {
            Text(
                "Eres el autor de esta subasta. No puedes pujar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Divider()
        Text("Historial de pujas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            pujas.sortedByDescending { it.cantidad }.forEach { p ->
                var us by remember { mutableStateOf<Usuario?>(null) }

                LaunchedEffect(p.pujadorId) {
                    Util.obtenerUsuario(dbRef, p.pujadorId) { us = it }
                }

                if (us != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = us!!.nombre, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${p.cantidad}€", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
