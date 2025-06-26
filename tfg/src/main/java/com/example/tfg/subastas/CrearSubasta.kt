package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.example.tfg.Util
import com.example.tfg.clases.Amonestacion
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import com.example.tfg.clases.CarritoManager

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PublicacionInfosItem(
    nav: NavHostController? = null,
    obraKey: String = ""
) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var obra by remember { mutableStateOf(Obra()) }
    var autorObra by remember { mutableStateOf(Usuario()) }
    var usuario by remember { mutableStateOf(Usuario()) }

    var mostrarDialogReporte by remember { mutableStateOf<Boolean>(false) }

    Util.obtenerObra(dbRef, contexto, obraKey) { itObra ->
        if (itObra != null) {
            obra = itObra
            Util.obtenerUsuario(dbRef, obra.autor!!) {
                autorObra = it!!
            }
            Util.obtenerUsuario(dbRef, Util.obtenerDatoShared(contexto, "id")!!) {
                usuario = it!!
            }
        } else {
            Log.w("PublicacionInfoItem", "Obra ya no existe")
            nav?.navigate("principal")
        }
    }

    val esAdmin = Util.obtenerDatoSharedBoolean(contexto, "admin")
    val esAutor = Util.obtenerDatoShared(contexto, "id") == obra.autor

    var imagenSeleccionada by remember { mutableStateOf<String?>(null) }
    var mostrarComentarios by remember { mutableStateOf(false) }

    if (imagenSeleccionada != null) {
        ImagenAmpliadaDialog(imagenUrl = imagenSeleccionada!!) {
            imagenSeleccionada = null
        }
    }

    if (mostrarDialogReporte) {
        val motivos = listOf("Robo", "Contenido generado por IA", "Contenido ofensivo", "Otro")
        var motivoSeleccionado by remember { mutableStateOf(motivos.first()) }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { mostrarDialogReporte = false },
            confirmButton = {
                Button(onClick = {
                    val amonId = dbRef.child("amonestaciones").push().key!!
                    val amon = Amonestacion(
                        id = amonId,
                        obraId = obraKey,
                        autorId = obra.autor!!,
                        denuncianteId = Util.obtenerDatoShared(contexto, "id")!!,
                        motivo = motivoSeleccionado,
                        fecha = System.currentTimeMillis()
                    )
                    dbRef.child("amonestaciones").child(amonId).setValue(amon)
                    mostrarDialogReporte = false
                    Toast.makeText(contexto, "Denuncia enviada", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Enviar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogReporte = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Reportar obra") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Selecciona el motivo:", modifier = Modifier.padding(bottom = 8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = motivoSeleccionado,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            motivos.forEach { motivo ->
                                DropdownMenuItem(
                                    text = { Text(motivo) },
                                    onClick = {
                                        motivoSeleccionado = motivo
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }


    Column {

        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (titulo, autor, descripcion, genero, precio, fecha, favorito, comentarios, editar, borrar, comprar, reportar) = createRefs()

            Text(
                obra.titulo!!,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(13.dp, 13.dp, 13.dp, 5.dp)
                    .constrainAs(titulo) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
            )

            Text(
                autorObra.nombre,
                modifier = Modifier
                    .padding(start = 13.dp, bottom = 10.dp)
                    .constrainAs(autor) {
                        top.linkTo(titulo.bottom)
                        start.linkTo(parent.start)
                    }
                    .clickable {
                        nav?.navigate("perfil/${autorObra.id_firebase}")
                    }
            )

            Text(
                obra.descripcion!!,
                modifier = Modifier
                    .padding(13.dp)
                    .constrainAs(descripcion) {
                        top.linkTo(autor.bottom)
                        start.linkTo(parent.start)
                    }
            )

            Text(
                Util.obtenerFecha(obra.fechaCreacion),
                modifier = Modifier
                    .padding(8.dp)
                    .constrainAs(fecha) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.favorito),
                contentDescription = "Favorito",
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
                    .constrainAs(favorito) {
                        start.linkTo(parent.start)
                        top.linkTo(descripcion.bottom)
                    }
                    .clickable {
                        Util.obtenerUsuario(dbRef, Util.obtenerDatoShared(contexto, "id")!!) { usAux ->
                            if (usAux != null) {
                                if (usAux.obrasFavoritas.contains(obraKey)) {
                                    usAux.obrasFavoritas.remove(obraKey)
                                } else {
                                    usAux.obrasFavoritas.add(obraKey)
                                }
                                Util.editarUsuario(dbRef, contexto, usAux)
                            }
                        }
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.comentarios),
                contentDescription = "Comentarios",
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
                    .constrainAs(comentarios) {
                        start.linkTo(favorito.end)
                        bottom.linkTo(favorito.bottom)
                        top.linkTo(favorito.top)
                    }
                    .clickable {
                        mostrarComentarios = !mostrarComentarios
                    }
            )

            if (esAdmin || esAutor) {
                Image(
                    painter = painterResource(id = R.drawable.modificar),
                    contentDescription = "modificar",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .constrainAs(editar) {
                            start.linkTo(comentarios.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .clickable {
                            nav?.navigate("modificarObra/${obraKey}")
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.borrar),
                    contentDescription = "borrar",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .constrainAs(borrar) {
                            start.linkTo(editar.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .clickable {
                            Util.eliminarObra(dbRef, obraKey) {
                                nav?.navigate("principal")
                            }
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.publicar),
                    contentDescription = "comprar",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                        .constrainAs(reportar) {
                            start.linkTo(borrar.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .clickable {
                            mostrarDialogReporte = true
                        }
                )
            }

            val esComprador = usuario.obrasCompradas.contains(obraKey)
            val esAutorObra = Util.obtenerDatoShared(contexto, "id") == obra.autor
            val textoBoton = when {
                esAutorObra -> "Tu obra"
                esComprador -> "Comprado"
                else -> "Comprar"
            }
            val deshabilitado = esAutorObra || esComprador

            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White
            ),
                onClick = {
                    if (!deshabilitado) {
                        nav?.navigate("pago/${obraKey}")

                        CarritoManager.aniadir(obra)

                    }
                },
                enabled = !deshabilitado,
                modifier = Modifier
                    .padding(8.dp)
                    .constrainAs(comprar) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(textoBoton)
            }
        }

        AnimatedVisibility(visible = mostrarComentarios) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )
            ComentariosPublicacion(obraKey, obra.comentarios ?: mutableMapOf())
        }

        if (mostrarDialogReporte) {
            Dialog(
                onDismissRequest = { mostrarDialogReporte = false }
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    // Ensure obraSeleccionada.id_firebase is not null before passing
                    obra.id_firebase?.let {
                        PublicacionInfoItem(nav, it)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportarObradDdialog(contexto: Context, obraKey: String, obra: Obra) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        Dialog(onDismissRequest = { mostrarDialogo = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    val motivos = listOf("Robo", "Interés artificial", "Contenido ofensivo")
                    var motivoSeleccionado by remember { mutableStateOf(motivos[0]) }

                    motivos.forEach {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = motivoSeleccionado == it,
                                onClick = { motivoSeleccionado = it }
                            )
                            Text(it)
                        }
                    }

                    Button(onClick = {
                        val amonId = FirebaseDatabase.getInstance().reference.child("amonestaciones").push().key!!
                        val amon = Amonestacion(
                            id = amonId,
                            obraId = obraKey,
                            autorId = obra.autor!!,
                            denuncianteId = Util.obtenerDatoShared(contexto, "id")!!,
                            motivo = motivoSeleccionado
                        )
                        FirebaseDatabase.getInstance().reference
                            .child("amonestaciones").child(amonId).setValue(amon)
                        mostrarDialogo = false
                    }) {
                        Text("Enviar denuncia")
                    }
                }
            }
        }
    }

}