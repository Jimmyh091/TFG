package com.example.tfg.pantallas

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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

    // Obtener usuario
    val userId = Util.obtenerDatoShared(contexto, "id") ?: return
    Util.obtenerUsuario(dbRef, userId) {
        if (it != null) usuario = it
    }

    // Obtener obras del carrito
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pago de obras", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        listaObras.forEach { obra ->
            Text("• ${obra.titulo} - ${obra.precio}€", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Total: $precioTotal €", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Saldo: ${usuario.dinero} €", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        if (!compraRealizada) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3479BF),
                    contentColor = Color.White
                ),
                onClick = {
                    if (usuario.dinero!! >= precioTotal) {
                        usuario.dinero = usuario.dinero!! - precioTotal
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
                Text("Confirmar Pago")
            }
        }

        if (dineroFaltante != null && !compraRealizada) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Te faltan ${"%.2f".format(dineroFaltante)}€")

            OutlinedTextField(
                value = cantidadAgregar,
                onValueChange = { cantidadAgregar = it },
                label = { Text("¿Cuánto quieres añadir?") }
            )

            Button(
                onClick = {
                    val cantidad = cantidadAgregar.toFloatOrNull() ?: 0f
                    if (cantidad > 0) {
                        usuario.dinero = usuario.dinero!! + cantidad
                        Util.editarUsuario(dbRef, contexto, usuario)
                        dineroFaltante = null
                    }
                }
            ) {
                Text("Añadir saldo")
            }
        }

        if (compraRealizada) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("¡Compra realizada con éxito!")
        }
    }
}
