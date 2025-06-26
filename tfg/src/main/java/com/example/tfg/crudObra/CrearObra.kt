package com.example.tfg.crudObra

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.tfg.Util
import com.example.tfg.subastas.Subasta
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.app.DatePickerDialog
import java.time.Instant
import java.util.Calendar
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.onFocusedBoundsChanged
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.*
import kotlin.io.copyTo

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearObra(
    nav: NavHostController? = null,
    onCancelClick: () -> Unit = {},
    onConfirmClick: (Obra) -> Unit = {}
) {
    val contexto = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference
    val genres = listOf("Fantasía", "Ciencia Ficción", "Drama", "Comedia", "Terror", "Romance", "Aventura")

    var crearSubasta by remember { mutableStateOf(false) }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableFloatStateOf(0.0f) }
    var selectedGenre by remember { mutableStateOf("Selecciona un género") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val id_projecto = "685a6ede000a0960943a"
    val id_bucket = "685a6ff4003677af3a0b"
    val client = remember { Client(contexto).setEndpoint("https://fra.cloud.appwrite.io/v1").setProject(id_projecto) }
    val storage = remember { Storage(client) }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // Campos para subasta
    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
    var horaSeleccionada by remember { mutableStateOf<LocalTime?>(null) }
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            contexto,
            { _, year, month, dayOfMonth -> fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }
    val timePickerDialog = remember {
        TimePickerDialog(
            contexto,
            { _, hour, minute -> horaSeleccionada = LocalTime.of(hour, minute) },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Obra") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("¿Subasta?", fontSize = 14.sp)
                        Switch(checked = crearSubasta, onCheckedChange = { crearSubasta = it })
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

            Box(modifier = Modifier.clickable { expanded = true }) {
                OutlinedTextField(
                    value = selectedGenre,
                    onValueChange = {},
                    label = { Text("Género") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Desplegar géneros",
                            Modifier.clickable { expanded = !expanded }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genres.forEach { genero ->
                        DropdownMenuItem(
                            text = { Text(genero) },
                            onClick = {
                                selectedGenre = genero
                                expanded = false
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(1.dp, Color.Gray)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(ImageRequest.Builder(contexto).data(selectedImageUri).build()),
                        contentDescription = "Imagen de la obra",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Toca para seleccionar una imagen")
                }
            }

            OutlinedTextField(
                value = if (precio == 0f) "" else precio.toString(),
                onValueChange = { precio = it.toFloatOrNull() ?: 0f },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Campos adicionales si es subasta
            if (crearSubasta) {

                OutlinedTextField(
                    value = fechaSeleccionada?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha límite") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) datePickerDialog.show()
                        }
                )
                OutlinedTextField(
                    value = horaSeleccionada?.toString() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Hora límite") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) timePickerDialog.show()
                        }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCancelClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3479BF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = {
                        if (titulo.isBlank() || descripcion.isBlank() || selectedImageUri == null || selectedGenre == "Selecciona un género") {
                            Toast.makeText(contexto, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (crearSubasta && (fechaSeleccionada == null || horaSeleccionada == null)) {
                            Toast.makeText(contexto, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val obraId = dbRef.child("arte").child("obras").push().key!!
                        val autorId = Util.obtenerDatoShared(contexto, "id")!!
                        val identificadorFile = ID.unique()

                        scope.launch(Dispatchers.IO) {
                            try {
                                val url = "https://cloud.appwrite.io/v1/storage/buckets/$id_bucket/files/$identificadorFile/view?project=$id_projecto"

                                if (crearSubasta) {

                                    Util.obtenerUsuario(dbRef, autorId) { usuario ->
                                        usuario?.let {
                                            it.obrasCreadas.add(obraId)
                                            Util.editarUsuario(dbRef, contexto, it)
                                        }
                                    }

                                    val inputStream = contexto.contentResolver.openInputStream(selectedImageUri!!)
                                    val tempFile = kotlin.io.path.createTempFile(identificadorFile).toFile()
                                    inputStream?.copyTo(tempFile.outputStream())
                                    val file = InputFile.fromFile(tempFile)
                                    storage.createFile(bucketId = id_bucket, fileId = identificadorFile, file = file)

                                    val zona = ZoneId.systemDefault()
                                    val fechaHora = ZonedDateTime.of(fechaSeleccionada, horaSeleccionada, zona)
                                    val timestamp = fechaHora.toInstant().toEpochMilli()
                                    val subasta = Subasta(
                                        idObra_firebase = obraId,
                                        idSubasta_firebase = dbRef.child("arte").child("subastas").push().key,
                                        fechaLimite = timestamp,
                                    )
                                    Util.guardarSubasta(dbRef, subasta) {}
                                }else {

                                    val inputStream =
                                        contexto.contentResolver.openInputStream(selectedImageUri!!)
                                    val tempFile =
                                        kotlin.io.path.createTempFile(identificadorFile).toFile()
                                    inputStream?.copyTo(tempFile.outputStream())
                                    val file = InputFile.fromFile(tempFile)
                                    storage.createFile(
                                        bucketId = id_bucket,
                                        fileId = identificadorFile,
                                        file = file
                                    )

                                }

                                val obra = Obra(
                                    id_firebase = obraId,
                                    autor = autorId,
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    genero = selectedGenre,
                                    precio = precio,
                                    id_comprador = null,
                                    rutaImagen = url,
                                    idImagen = identificadorFile,
                                    fechaCreacion = System.currentTimeMillis()
                                )

                                Util.escribirObra(dbRef, obra)

                                Util.obtenerUsuario(dbRef, autorId) { usuario ->
                                    usuario?.let {
                                        it.obrasCreadas.add(obraId)
                                        Util.editarUsuario(dbRef, contexto, it)
                                    }
                                }


                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contexto, "Obra creada", Toast.LENGTH_SHORT).show()
                                    nav?.navigate("principal")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(contexto, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3479BF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}

