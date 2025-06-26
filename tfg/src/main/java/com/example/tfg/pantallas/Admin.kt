package com.example.tfg.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.tfg.Util
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.example.tfg.clases.Amonestacion
import com.example.tfg.subastas.Subasta
import com.google.firebase.database.*

@Composable
fun Admin(nav: NavHostController) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    val secciones = listOf("Usuarios", "Obras", "Subastas", "Amonestaciones")
    var pestaniaSeleccionada by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestaniaSeleccionada) {
            secciones.forEachIndexed { index, titulo ->
                Tab(
                    selected = pestaniaSeleccionada == index,
                    onClick = { pestaniaSeleccionada = index },
                    text = { Text(titulo) }
                )
            }
        }

        when (pestaniaSeleccionada) {
            0 -> ListaUsuarios(nav, dbRef, contexto)
            1 -> ListaObras(nav, dbRef, contexto)
            2 -> ListaSubastas(nav, dbRef, contexto)
            3 -> ListaAmonestaciones(nav, dbRef, contexto)
        }
    }
}

// Fragmento completo con iconos de acción para cada sección

@Composable
fun ListaUsuarios(nav: NavHostController, dbRef: DatabaseReference, contexto: Context) {
    val usuarios = remember { mutableStateListOf<Usuario>() }

    LaunchedEffect(Unit) {
        Util.obtenerUsuarios(dbRef) { lista ->
            usuarios.clear()
            usuarios.addAll(lista)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(usuarios) { usuario ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).clickable {
                        nav.navigate("perfil/${usuario.id_firebase}")
                    }) {
                        Text(text = "Nombre: ${usuario.nombre}")
                        Text(text = "Email: ${usuario.correo}")
                    }

                    IconButton(onClick = {
                        dbRef.child("usuarios").child(usuario.id_firebase).removeValue()
                        Toast.makeText(contexto, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        usuarios.remove(usuario)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun ListaObras(nav: NavHostController, dbRef: DatabaseReference, contexto: Context) {
    val obras = remember { mutableStateListOf<Obra>() }
    var obraSeleccionada by remember { mutableStateOf<Obra?>(null) }

    LaunchedEffect(Unit) {
        Util.obtenerObras(dbRef, contexto) { lista ->
            obras.clear()
            obras.addAll(lista)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(obras) { obra ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).clickable {
                        Util.obtenerObra(dbRef, contexto, obra.id_firebase!!) { o ->
                            obraSeleccionada = o
                        }
                    }) {
                        Text(text = "Título: ${obra.titulo}")
                        Text(text = "Autor: ${obra.autor}")
                    }

                    Row {
                        IconButton(onClick = { nav.navigate("editarObra/${obra.id_firebase}") }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = {
                            dbRef.child("obras").child(obra.id_firebase!!).removeValue()
                            Toast.makeText(contexto, "Obra eliminada", Toast.LENGTH_SHORT).show()
                            obras.remove(obra)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }

    obraSeleccionada?.let {
        Dialog(onDismissRequest = { obraSeleccionada = null }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(10.dp)) {
                PublicacionInfoItem(nav, it.id_firebase!!)
            }
        }
    }
}

@Composable
fun ListaSubastas(nav: NavHostController, dbRef: DatabaseReference, contexto: Context) {
    val subastas = remember { mutableStateListOf<Subasta>() }

    LaunchedEffect(Unit) {
        Util.obtenerSubastas(dbRef) { lista ->
            subastas.clear()
            subastas.addAll(lista)
        }
    }

    var obraSeleccionada by remember { mutableStateOf<Obra?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {


        items(subastas) { subasta ->
            var obra by remember { mutableStateOf<Obra?>(null) }


            LaunchedEffect(subasta.idObra_firebase) {
                Util.obtenerObra(dbRef, contexto, subasta.idObra_firebase!!) { result ->
                    obra = result
                }
            }

            obra?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).clickable {
                            obraSeleccionada = it
                        }) {
                            Text(text = "Título: ${it.titulo}")
                            Text(text = "Precio inicial: ${it.precio}")
                        }

                        Row {
                            IconButton(onClick = {
                                nav.navigate("editarSubasta/${subasta.idObra_firebase}")
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                dbRef.child("subastas").child(subasta.idObra_firebase!!).removeValue()
                                Toast.makeText(contexto, "Subasta eliminada", Toast.LENGTH_SHORT).show()
                                subastas.remove(subasta)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }


    obraSeleccionada?.let {
        Dialog(onDismissRequest = { obraSeleccionada = null }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(10.dp)) {
                PublicacionInfoItem(nav, it.id_firebase!!)
            }
        }
    }
}

@Composable
fun ListaAmonestaciones(nav: NavHostController, dbRef: DatabaseReference, contexto: Context) {
    val amonestaciones = remember { mutableStateListOf<Amonestacion>() }
    var obraSeleccionada by remember { mutableStateOf<Obra?>(null) }

    LaunchedEffect(Unit) {
        Util.obtenerAmonestaciones(dbRef, contexto) { lista ->
            amonestaciones.clear()
            amonestaciones.addAll(lista)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(amonestaciones) { amonestacion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Obra denunciada: ${amonestacion.obraId}")
                    Text(text = "Motivo: ${amonestacion.motivo}")
                    Text(text = "Fecha: ${amonestacion.fecha}")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = {
                            dbRef.child("amonestaciones").child(amonestacion.id!!).removeValue()
                            Toast.makeText(contexto, "Amonestación aceptada", Toast.LENGTH_SHORT).show()
                            amonestaciones.remove(amonestacion)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("Aceptar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            dbRef.child("amonestaciones").child(amonestacion.id!!).removeValue()
                            Toast.makeText(contexto, "Amonestación rechazada", Toast.LENGTH_SHORT).show()
                            amonestaciones.remove(amonestacion)
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))) {
                            Text("Rechazar")
                        }
                    }
                }
            }
        }
    }

    obraSeleccionada?.let {
        Dialog(onDismissRequest = { obraSeleccionada = null }) {
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(10.dp)) {
                PublicacionInfoItem(nav, it.id_firebase!!)
            }
        }
    }
}
