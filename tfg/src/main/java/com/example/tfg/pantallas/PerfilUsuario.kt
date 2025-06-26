package com.example.tfg.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.padding(10.dp)) {
                obraSeleccionada!!.id_firebase?.let {
                    PublicacionInfoItem(nav, it)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // ===== HEADER =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(top = 24.dp, bottom = 16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ajustes),
                        contentDescription = "Ajustes",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { nav?.navigate("ajustes") },
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.usuario),
                    contentDescription = "Perfil",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(usuario.nombre, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Desde ${Util.obtenerFecha(usuario.fechaCreacion)}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(usuario.correo, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (usuarioId != Util.obtenerDatoShared(contexto, "id")) {
                    IconButton(
                        onClick = { nav?.navigate("chat/$usuarioId") },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.enviar),
                            contentDescription = "Mensaje",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }

        // ===== FILTROS =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FiltroChip("Mis obras", mostrarMisObras) {
                mostrarMisObras = true; mostrarCompradas = false; mostrarFavoritos = false
            }
            FiltroChip("Compradas", mostrarCompradas) {
                mostrarMisObras = false; mostrarCompradas = true; mostrarFavoritos = false
            }
            FiltroChip("Favoritos", mostrarFavoritos) {
                mostrarMisObras = false; mostrarCompradas = false; mostrarFavoritos = true
            }
        }

        // ===== TÍTULO =====
        Text(
            text = when {
                mostrarMisObras -> "Tus obras publicadas"
                mostrarCompradas -> "Obras que compraste"
                else -> "Obras favoritas"
            },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        // ===== GRID OBRAS =====
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
                    onclick = { obraSeleccionada = obrasMostradas[pos] }
                )
            }
        }
    }
}

@Composable
fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(texto) },
        shape = RoundedCornerShape(24.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            labelColor = if (seleccionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    )
}
