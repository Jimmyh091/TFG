package com.example.tfg.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.HorizontalAlign
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.example.tfg.Util
import com.example.tfg.items.PublicacionItem
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuario(
    nav: NavHostController? = null,
    usuarioId: String = ""
) {
    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var obraSeleccionada by remember { mutableStateOf<Obra?>(null) }
    var usuario by remember { mutableStateOf(Usuario()) }
    var listaObras by remember { mutableStateOf(listOf<Obra>()) }

    // Filtros
    var mostrarMisObras by remember { mutableStateOf(true) }
    var mostrarCompradas by remember { mutableStateOf(false) }
    var mostrarFavoritos by remember { mutableStateOf(false) }

    Util.obtenerUsuario(db_ref, usuarioId) {
        if (it != null) usuario = it
    }

    LaunchedEffect(true) {
        Util.obtenerObras(db_ref, contexto) { obras ->
            listaObras = obras
        }
    }

    val obrasMostradas = listaObras.filter {
        when {
            mostrarFavoritos -> usuario.obrasFavoritas.contains(it.id_firebase)
            mostrarCompradas -> usuario.obrasCompradas.contains(it.id_firebase)
            else -> it.autor == Util.obtenerDatoShared(contexto, "id")
        }
    }

    if (obraSeleccionada != null) {
        Dialog(onDismissRequest = { obraSeleccionada = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                obraSeleccionada!!.id_firebase!!.let {
                    PublicacionInfoItem(nav, it)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Icono de ajustes arriba a la izquierda
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.publicar),
                contentDescription = "Ajustes",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.TopEnd)
                    .clickable {
                        nav?.navigate("ajustes")
                    }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.perfil),
            contentDescription = "Imagen de perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = usuario.nombre,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Cuenta creada en ${Util.obtenerFecha(usuario.fechaCreacion)}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = usuario.correo,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        if (usuarioId != Util.obtenerDatoShared(contexto, "id")) {
            Image(
                painter = painterResource(id = R.drawable.enviar),
                contentDescription = "Mis obras",
                modifier = Modifier
                    .size(55.dp)
                    .padding(8.dp)
                    .align(Alignment.End)
            )
        }else{
            Spacer(modifier = Modifier.height(55.dp))
        }
            Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 450.dp)
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 8.dp)
                ) {
                    // Mis obras
                    Image(
                        painter = painterResource(id = R.drawable.publicaciones),
                        contentDescription = "Mis obras",
                        modifier = Modifier
                            .size(55.dp)
                            .padding(8.dp)
                            .clickable {
                                mostrarMisObras = true
                                mostrarCompradas = false
                                mostrarFavoritos = false
                            }
                    )

                    // Compradas
                    Image(
                        painter = painterResource(id = R.drawable.publicar),
                        contentDescription = "Compradas",
                        modifier = Modifier
                            .size(55.dp)
                            .padding(8.dp)
                            .clickable {
                                mostrarMisObras = false
                                mostrarCompradas = true
                                mostrarFavoritos = false
                            }
                    )

                    // Favoritos
                    Image(
                        painter = painterResource(id = R.drawable.favorito),
                        contentDescription = "Favoritos",
                        modifier = Modifier
                            .size(55.dp)
                            .padding(8.dp)
                            .clickable {
                                mostrarMisObras = false
                                mostrarCompradas = false
                                mostrarFavoritos = true
                            }
                    )
                }

                Text(
                    text = when {
                        mostrarMisObras -> "Tus obras publicadas"
                        mostrarCompradas -> "Obras que compraste"
                        else -> "Obras favoritas"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalItemSpacing = 20.dp,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(obrasMostradas.size) { pos ->
                        PublicacionItem(
                            obra = obrasMostradas[pos],
                            onclick = {
                                obraSeleccionada = obrasMostradas[pos]
                            }
                        )
                    }
                }
            }
        }
    }
}
