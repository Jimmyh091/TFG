package com.example.tfg.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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
    val listaObras = remember { mutableStateListOf<Obra>() }

    // Refrescar las obras cuando cambia el carrito
    LaunchedEffect(CarritoManager.obrasEnCarrito) {
        listaObras.clear()
        CarritoManager.obrasEnCarrito.forEach { id ->
            Util.obtenerObra(dbRef, contexto, id) { obra ->
                obra?.let {
                    if (!listaObras.any { it.id_firebase == obra.id_firebase }) {
                        listaObras.add(it)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carrito", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        LazyColumn {
            items(listaObras.size) { index ->
                val obra = listaObras[index]
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text("${obra.titulo} - ${obra.autor} - ${obra.precio}€")
                    Button(onClick = {
                        CarritoManager.borrar(obra.id_firebase!!)
                        listaObras.remove(obra)
                    }) {
                        Text("Eliminar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            nav.navigate("pagoCarrito")
        }) {
            Text("Comprar todo")
        }
    }
}

