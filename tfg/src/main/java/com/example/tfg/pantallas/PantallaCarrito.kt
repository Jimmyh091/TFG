package com.example.tfg.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.clases.CarritoManager
import com.example.tfg.crudObra.Obra
import com.example.tfg.Util
import com.google.firebase.database.FirebaseDatabase

@Composable
fun CarritoScreen(nav: NavHostController) {
    val contexto = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference
    val listaObras = remember { mutableStateListOf<Pair<Obra, String>>() }

    // Recarga las obras cada vez que el carrito cambia
    LaunchedEffect(CarritoManager.obrasEnCarrito) {
        listaObras.clear()
        CarritoManager.obrasEnCarrito.forEach { id ->
            Util.obtenerObra(dbRef, contexto, id) { obra ->
                obra?.let {
                    Util.obtenerUsuario(dbRef, it.autor ?: "") { usuario ->
                        val nombreAutor = usuario?.nombre ?: "Desconocido"
                        if (!listaObras.any { par -> par.first.id_firebase == obra.id_firebase }) {
                            listaObras.add(obra to nombreAutor)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Carrito",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (listaObras.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("El carrito está vacío", fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(listaObras) { (obra, nombreAutor) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = obra.titulo ?: "Sin título",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text("Autor: $nombreAutor", fontSize = 14.sp)
                            Text("Precio: ${obra.precio}€", fontSize = 14.sp)

                            Button(
                                onClick = {
                                    CarritoManager.borrar(obra.id_firebase!!)
                                    listaObras.removeIf { it.first.id_firebase == obra.id_firebase }
                                },
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { nav.navigate("pagoCarrito") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Comprar todo")
            }
        }
    }
}
