package com.example.tfg.pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.clases.CarritoManager
import com.example.tfg.Util
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.google.firebase.database.FirebaseDatabase

@Composable
fun PantallaPagoCarrito(nav: NavHostController) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current
    val listaObras = remember { mutableStateListOf<Obra>() }
    var usuario by remember { mutableStateOf(Usuario()) }
    var dineroFaltante by remember { mutableStateOf<Float?>(null) }
    var cantidadAgregar by remember { mutableStateOf("") }
    var compraRealizada by remember { mutableStateOf(false) }

    val userId = Util.obtenerDatoShared(contexto, "id") ?: return
    Util.obtenerUsuario(dbRef, userId) {
        if (it != null) usuario = it
    }

    LaunchedEffect(CarritoManager.obrasEnCarrito) {
        listaObras.clear()
        CarritoManager.obrasEnCarrito.forEach { id ->
            Util.obtenerObra(dbRef, contexto, id) { obra ->
                obra?.let {
                    if (!listaObras.any { it.id_firebase == obra.id_firebase }) {
                        listaObras.add(obra)
                    }
                }
            }
        }
    }

    val precioTotal = listaObras.sumOf { (it.precio ?: 0f).toDouble() }.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Resumen de compra", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(listaObras.size) { index ->
                val obra = listaObras[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(obra.titulo ?: "Sin título", fontWeight = FontWeight.Medium)
                        Text("Precio: ${obra.precio} €", fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Text("Total a pagar: $precioTotal €", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Tu saldo: ${usuario.dinero ?: 0f} €", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (!compraRealizada) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3479BF),
                    contentColor = Color.White
                ),
                onClick = {
                    if (usuario.dinero >= precioTotal) {
                        usuario.dinero = usuario.dinero - precioTotal
                        val nuevasCompras = usuario.obrasCompradas.toMutableList()
                        nuevasCompras.addAll(CarritoManager.obrasEnCarrito)
                        usuario.obrasCompradas = nuevasCompras.distinct().toMutableList()

                        Util.editarUsuario(dbRef, contexto, usuario)
                        compraRealizada = true
                        CarritoManager.vaciar()
                        Toast.makeText(contexto, "Pago realizado con éxito", Toast.LENGTH_SHORT).show()
                        nav.navigate("principal")
                    } else {
                        dineroFaltante = precioTotal - usuario.dinero
                    }
                }
            ) {
                Text("Confirmar pago")
            }
        }

        if (dineroFaltante != null && !compraRealizada) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Saldo insuficiente. Te faltan ${"%.2f".format(dineroFaltante)}€",
                color = Color.Red,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cantidadAgregar,
                onValueChange = { cantidadAgregar = it },
                label = { Text("¿Cuánto quieres añadir?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val cantidad = cantidadAgregar.toFloatOrNull() ?: 0f
                    if (cantidad > 0) {
                        usuario.dinero = usuario.dinero + cantidad
                        Util.editarUsuario(dbRef, contexto, usuario)
                        dineroFaltante = null
                        cantidadAgregar = ""
                    }
                }
            ) {
                Text("Añadir saldo")
            }
        }

        if (compraRealizada) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¡Compra realizada con éxito!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
    }
}
