package com.example.tfg.subastas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg.Util
import com.example.tfg.clases.CarritoManager
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.google.firebase.database.FirebaseDatabase

@Composable
fun ResultadoSubastaScreen(
    subastaId: String,
    nav: NavHostController
) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var subasta by remember { mutableStateOf<Subasta?>(null) }
    var obra by remember { mutableStateOf<Obra?>(null) }
    var pujasOrdenadas by remember { mutableStateOf<List<Puja>>(emptyList()) }
    var ganador by remember { mutableStateOf<Usuario?>(null) }
    var cargando by remember { mutableStateOf(true) }

    val usuarioId = Util.obtenerDatoShared(contexto, "id")

    LaunchedEffect(subastaId) {
        cargando = true

        Util.obtenerSubasta(dbRef, subastaId) { s ->
            subasta = s

            if (s != null) {
                Util.obtenerObra(dbRef, contexto, s.idObra_firebase ?: "") { obraObtenida ->
                    obra = obraObtenida
                    cargando = false
                }

                Util.obtenerPujas(dbRef, subastaId) { pujas ->
                    val ordenadas = pujas.sortedByDescending { it.cantidad }
                    pujasOrdenadas = ordenadas

                    if (s.fechaLimite < System.currentTimeMillis() && ordenadas.isNotEmpty()) {
                        val idGanador = ordenadas.first().pujadorId
                        Util.obtenerUsuario(dbRef, idGanador) {
                            ganador = it
                        }
                    }
                }
            } else {
                cargando = false
            }
        }
    }

    when {
        cargando || obra == null || subasta == null -> {
            Column(Modifier.padding(16.dp)) {
                Text("Cargando resultado de la subasta...")
            }
        }

        else -> {
            val esAutor = obra!!.autor == usuarioId
            val esGanador = subasta!!.idGanador == usuarioId
            val subastaTerminada = subasta!!.fechaLimite < System.currentTimeMillis()

            Column(Modifier.padding(16.dp)) {
                AsyncImage(
                    model = obra!!.rutaImagen,
                    contentDescription = "Imagen obra",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Título: ${obra!!.titulo}", fontWeight = FontWeight.Bold)
                Text("Descripción: ${obra!!.descripcion}")
                Text("Fecha de cierre: ${Util.obtenerFecha(subasta!!.fechaLimite)}")

                Spacer(modifier = Modifier.height(16.dp))

                Text("Pujas:", fontWeight = FontWeight.Bold)
                if (pujasOrdenadas.isEmpty()) {
                    Text("No hubo pujas.")
                } else {
                    pujasOrdenadas.forEachIndexed { index, puja ->
                        Text("${index + 1}. Usuario: ${puja.pujadorId} - Monto: ${puja.cantidad}€")
                    }

                    if (subastaTerminada && ganador != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ganador: ${ganador!!.nombre}", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!esAutor) {
                    if (esGanador) {
                        LaunchedEffect(Unit) {
                            Log.d("CarritoManager", "Obra ganadora: ${obra!!.id_firebase}")
                            CarritoManager.aniadirSubastaGanada(obra!!)
                        }
                        Text("¡Felicidades! Ganaste la subasta")
                        Button(onClick = {
                            nav.navigate("carrito")
                        }) {
                            Text("Pagar ahora")
                        }
                    } else {
                        Text("Lo sentimos, no ganaste esta subasta.")
                    }
                }
            }
        }
    }
}
