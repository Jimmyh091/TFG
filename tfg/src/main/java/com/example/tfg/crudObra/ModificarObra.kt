package com.example.tfg.crudObra

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tfg.Util
import com.example.tfg.subastas.Subasta
import com.google.firebase.database.FirebaseDatabase
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarObraScreen(
    nav: NavHostController? = null,
    obraId: String,
    onCancelClick: () -> Unit = {}
) {
    val contexto = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference
    val genres = listOf("Fantasía", "Ciencia Ficción", "Drama", "Comedia", "Terror", "Romance", "Aventura")

    var obra by remember { mutableStateOf<Obra?>(null) }
    var subasta by remember { mutableStateOf<Subasta?>(null) }

    var crearSubasta by remember { mutableStateOf(false) }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("") }

    val selectedImageUri = remember { mutableStateOf<Uri?>(null) }
    val expanded = remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        dbRef.child("obras").child(obraId).get().addOnSuccessListener { snap ->
            val obraCargada = snap.getValue(Obra::class.java)
            if (obraCargada != null) {
                obra = obraCargada
                titulo = obraCargada.titulo!!
                descripcion = obraCargada.descripcion!!
                selectedGenre = obraCargada.genero!!

                // Buscar subasta asociada si existe
                dbRef.child("subastas").orderByChild("obraId").equalTo(obraId)
                    .get().addOnSuccessListener { subSnap ->
                        subSnap.children.firstOrNull()?.getValue(Subasta::class.java)?.let {
                            subasta = it
                            crearSubasta = true
                        }
                    }
            }
        }
    }

    if (obra == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val fecha = subasta?.fechaLimite?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val hora = subasta?.fechaLimite?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Obra") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("¿Subasta?", fontSize = 14.sp)
                        Switch(
                            checked = crearSubasta,
                            onCheckedChange = { crearSubasta = it },
                            enabled = subasta == null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título de la Obra") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.clickable { expanded.value = true }) {
                OutlinedTextField(
                    value = selectedGenre,
                    onValueChange = {},
                    label = { Text("Género") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Desplegar géneros",
                            Modifier.clickable { expanded.value = !expanded.value }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    genres.forEach { genero ->
                        DropdownMenuItem(
                            text = { Text(genero) },
                            onClick = {
                                selectedGenre = genero
                                expanded.value = false
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(1.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(contexto)
                            .data(obra!!.rutaImagen)
                            .build()
                    ),
                    contentDescription = "Imagen de la obra",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            OutlinedTextField(
                value = obra!!.precio.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )

            if (crearSubasta) {
                OutlinedTextField(
                    value = fecha?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha límite") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = hora?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora límite") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onCancelClick) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val obraActualizada = obra!!.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            genero = selectedGenre
                        )
                        Util.editarObra(dbRef, obraActualizada)
                        Toast.makeText(contexto, "Obra actualizada", Toast.LENGTH_SHORT).show()
                        nav?.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3479BF))
                ) {
                    Text("Guardar cambios", color = Color.White)
                }
            }
        }
    }
}
