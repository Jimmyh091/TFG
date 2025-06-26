package com.example.tfg.pantallas

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.tfg.crudObra.Obra
import com.example.tfg.subastas.Subasta
import com.example.tfg.Util
import com.example.tfg.items.PublicacionItem
import com.google.firebase.database.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Buscador(nav: NavHostController) {
    val contexto = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference

    var listaObras by remember { mutableStateOf<List<Obra>>(emptyList()) }
    var listaSubastas by remember { mutableStateOf<List<Subasta>>(emptyList()) }

    var filtroTexto by remember { mutableStateOf("") }
    var filtroGenero by remember { mutableStateOf("") }
    var filtroUsuario by remember { mutableStateOf("") }
    var mostrarVendidos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dbRef.child("arte").child("obras")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaObras = snapshot.children.mapNotNull { it.getValue(Obra::class.java) }
                }

                override fun onCancelled(err: DatabaseError) {
                    Toast.makeText(contexto, "Error cargando obras", Toast.LENGTH_SHORT).show()
                }
            })
    }

    LaunchedEffect(Unit) {
        dbRef.child("arte").child("subastas")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listaSubastas = snapshot.children.mapNotNull { it.getValue(Subasta::class.java) }
                }

                override fun onCancelled(err: DatabaseError) {
                    Toast.makeText(contexto, "Error cargando subastas", Toast.LENGTH_SHORT).show()
                }
            })
    }

    val mapaUsuarios = remember { mutableStateMapOf<String, String>() }
    LaunchedEffect(Unit) {
        Util.obtenerUsuarios(dbRef) { usuarios ->
            usuarios.forEach { mapaUsuarios[it.id_firebase] = it.nombre }
        }
    }

    data class ItemUnion(val obra: Obra, val subasta: Subasta?, val autorNombre: String)

    val listaCombinada = remember(listaObras, listaSubastas, mapaUsuarios) {
        listaObras.map { obra ->
            val subasta = listaSubastas.find { it.idObra_firebase == obra.id_firebase }
            ItemUnion(obra, subasta, mapaUsuarios[obra.autor] ?: "")
        }
    }

    fun String.isActive() = this.isNotBlank()

    val filtrado = listaCombinada.filter { item ->
        val titulo = item.obra.titulo ?: ""
        val genero = item.obra.genero ?: ""
        val usuario = item.autorNombre
        val vendido = item.obra.id_comprador != null

        (!filtroTexto.isActive() || titulo.contains(filtroTexto, true)) &&
                (!filtroGenero.isActive() || genero.contains(filtroGenero, true)) &&
                (!filtroUsuario.isActive() || usuario.contains(filtroUsuario, true)) &&
                (mostrarVendidos || !vendido)
    }

    var seleccionado by remember { mutableStateOf<ItemUnion?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = filtroTexto,
                onValueChange = { filtroTexto = it },
                label = { Text("Buscar título") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = filtroGenero,
                onValueChange = { filtroGenero = it },
                label = { Text("Filtrar género") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = filtroUsuario,
                onValueChange = { filtroUsuario = it },
                label = { Text("Filtrar usuario") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        if (seleccionado != null) {
            Dialog(onDismissRequest = { seleccionado = null }) {
                Card(Modifier.padding(16.dp), shape = RoundedCornerShape(10.dp)) {
                    if (seleccionado!!.subasta != null) {
                        nav.navigate("detalleSubasta/${seleccionado!!.subasta!!.idSubasta_firebase}")
                    } else {
                        PublicacionItem(obra = seleccionado!!.obra) {
                            seleccionado = null
                        }
                    }
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtrado.size) { i ->
                val item = filtrado[i]
                PublicacionItem(
                    obra = item.obra,
                    onclick = {
                        seleccionado = item
                    }
                )
            }
        }
    }
}
