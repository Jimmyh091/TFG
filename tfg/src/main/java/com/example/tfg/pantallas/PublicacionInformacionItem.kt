package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tfg.R
import com.example.tfg.Util
import com.example.tfg.clases.Amonestacion
import com.example.tfg.clases.CarritoManager
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.example.tfg.subastas.Subasta
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicacionInfoItem(
    nav: NavHostController? = null,
    obraKey: String = ""
) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var obra by remember { mutableStateOf<Obra?>(null) }
    var autorObra by remember { mutableStateOf(Usuario()) }
    var usuario by remember { mutableStateOf(Usuario()) }
    var subasta by remember { mutableStateOf<Subasta?>(null) }
    var mostrarDialogReporte by remember { mutableStateOf(false) }
    var esFavorita by remember { mutableStateOf(false) }

    val usuarioId = Util.obtenerDatoShared(contexto, "id") ?: ""

    LaunchedEffect(obraKey) {
        Util.obtenerObra(dbRef, contexto, obraKey) { itObra ->
            if (itObra != null) {
                obra = itObra
                Util.obtenerUsuario(dbRef, itObra.autor!!) { autorObra = it ?: Usuario() }
                Util.obtenerUsuario(dbRef, usuarioId) {
                    usuario = it ?: Usuario()
                    esFavorita = usuario.obrasFavoritas.contains(obraKey)
                }
                Util.obtenerSubastas(dbRef) { todasSubastas ->
                    subasta = todasSubastas.find { it.idObra_firebase == obraKey }
                }
            } else {
                nav?.navigate("principal")
            }
        }
    }

    val esAutorObra = obra?.autor == usuarioId
    val esComprador = usuario.obrasCompradas.contains(obraKey)
    val deshabilitado = esAutorObra || esComprador
    var mostrarComentarios by remember { mutableStateOf(false) }
    val esAdmin = Util.obtenerDatoSharedBoolean(contexto, "admin")
    val esSubasta = subasta != null
    val subastaTerminada by remember(subasta) {
        derivedStateOf { subasta?.fechaLimite?.let { it < System.currentTimeMillis() } ?: false }
    }

    val textoBoton = when {
        esSubasta -> "Ir a subasta"
        esAutorObra -> "Tu obra"
        esComprador -> "Comprado"
        else -> "Comprar"
    }

    obra?.let { obra ->
        Column {
            Image(
                painter = rememberAsyncImagePainter(obra.rutaImagen),
                contentDescription = "Imagen de la obra",
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )

            ConstraintLayout(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val (titulo, autor, descripcion, fecha, boton, fechaCierre, iconos) = createRefs()

                Text(obra.titulo ?: "Sin título", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 13.dp, top = 13.dp).constrainAs(titulo) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    })

                Text(autorObra.nombre, modifier = Modifier.padding(start = 13.dp).constrainAs(autor) {
                    top.linkTo(titulo.bottom)
                    start.linkTo(parent.start)
                }.clickable { nav?.navigate("perfil/${autorObra.id_firebase}") })

                Text(obra.descripcion ?: "", modifier = Modifier.padding(13.dp).constrainAs(descripcion) {
                    top.linkTo(autor.bottom)
                    start.linkTo(parent.start)
                })

                Text(Util.obtenerFecha(obra.fechaCreacion), modifier = Modifier.padding(8.dp).constrainAs(fecha) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                })

                if (esSubasta && subasta != null) {
                    Text(
                        "Finaliza: ${if (subasta!!.fechaLimite > System.currentTimeMillis()) Util.tiempoATranscurrir(subasta!!.fechaLimite) else Util.tiempoTranscurrido(subasta!!.fechaLimite)}",
                        fontSize = 14.sp,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 13.dp).constrainAs(fechaCierre) {
                            top.linkTo(fecha.bottom)
                            end.linkTo(parent.end)
                        }
                    )
                }

                Button(
                    onClick = {
                        if (CarritoManager.hayBloqueo()) {
                            Toast.makeText(contexto, "Debes pagar el carrito", Toast.LENGTH_SHORT).show()
                        } else if (esSubasta && subasta != null) {
                            if (subastaTerminada) {
                                Util.cerrarSubasta(dbRef, subasta!!)
                                nav?.navigate("resultadoSubasta/${subasta!!.idSubasta_firebase}")
                            } else {
                                nav?.navigate("detalleSubasta/${subasta!!.idSubasta_firebase}")
                            }
                        } else if (!deshabilitado) {
                            CarritoManager.aniadir(obra)
                            Toast.makeText(contexto, "Obra añadida al carrito", Toast.LENGTH_SHORT).show()
                            nav?.navigate("carrito")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3479BF), contentColor = Color.White),
                    enabled = !deshabilitado || esSubasta,
                    modifier = Modifier.padding(8.dp).constrainAs(boton) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                ) {
                    Text(textoBoton)
                }

                Row(modifier = Modifier.padding(8.dp).constrainAs(iconos) {
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                }) {
                    Image(
                        painter = painterResource(id = if (esFavorita) R.drawable.favoritosseleccionado else R.drawable.favorito),
                        contentDescription = "Favorito",
                        modifier = Modifier.padding(5.dp).size(20.dp).clickable {
                            Util.obtenerUsuario(dbRef, usuarioId) { usAux ->
                                usAux?.let {
                                    if (it.obrasFavoritas.contains(obraKey)) {
                                        it.obrasFavoritas.remove(obraKey)
                                        esFavorita = false
                                    } else {
                                        it.obrasFavoritas.add(obraKey)
                                        esFavorita = true
                                    }
                                    Util.editarUsuario(dbRef, contexto, it)
                                }
                            }
                        }
                    )
                    Image(painter = painterResource(id = R.drawable.comentarios), contentDescription = "Comentarios",
                        modifier = Modifier.padding(5.dp).size(20.dp).clickable { mostrarComentarios = !mostrarComentarios })
                    if (esAdmin || esAutorObra) {
                        Image(painter = painterResource(id = R.drawable.modificar), contentDescription = "Modificar",
                            modifier = Modifier.padding(5.dp).size(20.dp).clickable { nav?.navigate("modificarObra/${obraKey}") })
                        Image(painter = painterResource(id = R.drawable.borrar), contentDescription = "Borrar",
                            modifier = Modifier.padding(5.dp).size(20.dp).clickable {
                                Util.eliminarObraCompleto(dbRef, obraKey) {
                                    nav?.navigate("principal")
                                }
                            })
                    }
                    Image(painter = painterResource(id = R.drawable.reportar), contentDescription = "Denunciar",
                        modifier = Modifier.padding(5.dp).size(20.dp).clickable { mostrarDialogReporte = true })
                }
            }

            AnimatedVisibility(visible = mostrarComentarios) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                ComentariosPublicacion(obraKey, obra.comentarios ?: mutableMapOf())
            }

            if (mostrarDialogReporte) {
                Dialog(onDismissRequest = { mostrarDialogReporte = false }) {
                    ReportarObraDialog(contexto = contexto, obraKey = obraKey, obra = obra, onDismiss = { mostrarDialogReporte = false })
                }
            }
        }
    }
}

@Composable
fun ReportarObraDialog(contexto: Context, obraKey: String, obra: Obra, onDismiss: () -> Unit) {
    val motivos = listOf("Robo", "Interés artificial", "Contenido ofensivo", "Otro")
    var motivoSeleccionado by remember { mutableStateOf(motivos[0]) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Reportar obra", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                OutlinedTextField(value = motivoSeleccionado, onValueChange = {}, readOnly = true,
                    label = { Text("Motivo") },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) expanded = true })
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    motivos.forEach { motivo ->
                        DropdownMenuItem(text = { Text(motivo) }, onClick = {
                            motivoSeleccionado = motivo
                            expanded = false
                        })
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                val amonId = FirebaseDatabase.getInstance().reference.child("amonestaciones").push().key!!
                val amon = Amonestacion(
                    id = amonId,
                    obraId = obraKey,
                    autorId = obra.autor!!,
                    denuncianteId = Util.obtenerDatoShared(contexto, "id")!!,
                    motivo = motivoSeleccionado
                )
                FirebaseDatabase.getInstance().reference.child("amonestaciones").child(amonId).setValue(amon)
                Toast.makeText(contexto, "Denuncia enviada", Toast.LENGTH_SHORT).show()
                onDismiss()
            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3479BF), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth()) {
                Text("Enviar denuncia")
            }
        }
    }
}
