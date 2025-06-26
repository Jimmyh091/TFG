package com.example.tfg.crudObra

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tfg.Util
import com.example.tfg.clases.Comentario
import com.google.firebase.database.FirebaseDatabase

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificarObraScreen(
    nav: NavHostController? = null,
    obraKey: String = "",
    onCancelClick: () -> Unit = {},
    onConfirmClick: (Obra) -> Unit = {}// Función para manejar la confirmación con los datos de la obra
) {

    var id_firebase by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var precio by remember { mutableFloatStateOf(0.0f) }
    var id_comprador by remember { mutableStateOf("") }
    var rutaImagen by remember { mutableStateOf("") }
    var fechaCreacion by remember { mutableLongStateOf(0) }
    var selectedGenre by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var comentarios by remember { mutableStateOf(mutableMapOf<String, Comentario>()) }

    var db_ref = FirebaseDatabase.getInstance().reference
    var contexto = LocalContext.current

    var obra by remember { mutableStateOf(Obra()) }
    Log.v("obraKey", "a " + obraKey)
    Util.Companion.obtenerObra(db_ref, contexto, obraKey) {
        obra = it!!

        id_firebase = obra.id_firebase!!
        autor = obra.autor!!
        titulo = obra.titulo!!
        descripcion = obra.descripcion!!
        genero = obra.genero!!
        precio = obra.precio!!
        id_comprador = obra.id_comprador!!
        rutaImagen = obra.rutaImagen!!
        fechaCreacion = obra.fechaCreacion
        comentarios = obra.comentarios!!
    }

    val context = LocalContext.current
    val genres = listOf("Fantasía", "Ciencia Ficción", "Drama", "Comedia", "Terror", "Romance", "Aventura") // Lista de géneros

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Modificar Obra") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Para que se pueda desplazar si hay mucho contenido
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre los elementos
        ) {
            // Campo de texto para el Título
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo de texto para la Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            // Spinner (Desplegable) para los Géneros
            Box {
                OutlinedTextField(
                    value = selectedGenre,
                    onValueChange = {}, // No permitimos editar el texto directamente
                    label = { Text("Género") },
                    readOnly = true, // Hacemos que sea solo de lectura
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Desplegar géneros",
                            Modifier.clickable { expanded = !expanded } // Al hacer clic, cambiamos el estado de expansión
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true } // También al hacer clic en el campo
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genres.forEach { genre ->
                        DropdownMenuItem(
                            text = { Text(genre) },
                            onClick = {
                                selectedGenre = genre
                                expanded = false // Cerramos el desplegable al seleccionar
                            }
                        )
                    }
                }
            }

            // Espacio para la Imagen de la Obra
            Box(
                modifier = Modifier
                    .size(200.dp) // Tamaño del recuadro para la imagen
                    .border(1.dp, Color.Gray) // Borde para visualizar el área
                    .clickable { imagePickerLauncher.launch("image/*") }, // Al hacer clic, abrimos el selector de imágenes
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context).data(selectedImageUri).build()
                        ),
                        contentDescription = "Imagen de la obra",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // Para que la imagen se ajuste al recuadro
                    )
                } else {
                    Text("Toca para seleccionar una imagen")
                }
            }

            // Campo de texto para el Precio
            OutlinedTextField(
                value = precio.toString(),
                onValueChange = { precio = it.toFloatOrNull() ?: 0.0f },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth()
            )

            /*
            // Campo de texto para el Tipo de Venta
            OutlinedTextField(
                value = tipoVenta,
                onValueChange = { tipoVenta = it },
                label = { Text("Tipo de Venta (Ej: Venta, Alquiler)") },
                modifier = Modifier.fillMaxWidth()
            )*/

            // Botones de Cancelar y Confirmar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly // Espacio equitativo entre los botones
            ) {
                Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = onCancelClick) {
                    Text("Cancelar")
                }
                Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = {
                    // Aquí puedes validar los datos antes de confirmar

                    /*
                    val id_firebase: String? = "ERROR",
                    val autor: String? = "ERROR",
                    val titulo: String? = "ERROR",
                    val descripcion: String? = "ERROR",
                    val genero: String? = "ERROR",
                    val precio: Double? = -1.0,
                    val id_comprador: Boolean = false,
                    val tipoVenta: String? = "ERROR",
                    val rutaImagen: String? = "ERROR",
                    val comentarios: MutableMap<String, Comentario>? = mutableMapOf<String, Comentario>(),
                    val fecha: String? = "ERROR"*/

                    val obra = Obra(
                        id_firebase = id_firebase,
                        autor = autor,
                        titulo = titulo,
                        descripcion = descripcion,
                        genero = selectedGenre,
                        precio = precio,
                        rutaImagen = selectedImageUri.toString(), // Pasamos la URI de la imagen
                        id_comprador = id_comprador,
                        fechaCreacion = fechaCreacion,
                        comentarios = comentarios
                    )

                    Util.Companion.editarObra(db_ref, obra)
                    nav!!.navigate("principal")
                    //onConfirmClick(obra)
                }) {
                    Text("Confirmar")
                }
            }
        }
    }
}