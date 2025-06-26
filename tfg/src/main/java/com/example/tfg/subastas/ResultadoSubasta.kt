package com.example.tfg.subastas

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    if (cargando || obra == null || subasta == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val esAutor = obra!!.autor == usuarioId
        val esGanador = subasta!!.idGanador == usuarioId
        val subastaTerminada = subasta!!.fechaLimite < System.currentTimeMillis()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            AsyncImage(
                model = obra!!.rutaImagen,
                contentDescription = "Imagen obra",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(obra!!.titulo ?: "Sin título", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(obra!!.descripcion ?: "", fontSize = 16.sp)
            Text(
                "Cierre de subasta: ${Util.obtenerFecha(subasta!!.fechaLimite)}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pujas realizadas", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            if (pujasOrdenadas.isEmpty()) {
                Text("No hubo pujas.", fontSize = 14.sp)
            } else {
                pujasOrdenadas.forEachIndexed { index, puja ->
                    Text(
                        "${index + 1}. Usuario: ${puja.pujadorId.take(8)}... - ${puja.cantidad}€",
                        fontSize = 14.sp
                    )
                }

                if (subastaTerminada && ganador != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Ganador: ${ganador!!.nombre}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!esAutor) {
                if (esGanador) {
                    LaunchedEffect(Unit) {
                        Log.d("CarritoManager", "Obra ganadora: ${obra!!.id_firebase}")
                        CarritoManager.aniadirSubastaGanada(obra!!)
                    }

                    Text(
                        "¡Felicidades! Has ganado la subasta",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { nav.navigate("carrito") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pagar ahora")
                    }
                } else {
                    Text(
                        "Lo sentimos, no ganaste esta subasta.",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
