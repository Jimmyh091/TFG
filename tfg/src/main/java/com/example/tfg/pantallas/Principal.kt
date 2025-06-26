package com.example.tfg.pantallas

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tfg.R
import com.example.tfg.crudObra.Obra
import com.example.tfg.Util
import com.example.tfg.items.PublicacionItem
import com.example.tfg.subastas.Subasta
import com.google.firebase.database.FirebaseDatabase

val chango = FontFamily(Font(R.font.changoregular, FontWeight.Normal, FontStyle.Normal))

@Composable
fun PantallaPrincipal(nav: NavHostController? = null) {
    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current
    val usuarioId = Util.obtenerDatoShared(contexto, "id") ?: return

    var obraSeleccionada by remember { mutableStateOf<Obra?>(null) }
    var listaPublicaciones by remember { mutableStateOf<List<Obra>>(emptyList()) }
    var subastasFinalizadas by remember { mutableStateOf<List<Pair<Subasta, Obra>>>(emptyList()) }

    LaunchedEffect(true) {
        Util.obtenerObras(db_ref, contexto) { obras ->
            val obrasVisibles = obras.filter { it.id_comprador == null }
                .sortedByDescending { it.fechaCreacion }
            listaPublicaciones = obrasVisibles

            Util.obtenerPujasDeUsuario(db_ref, usuarioId) { pujasUsuario ->
                val idsSubastasParticipadas = pujasUsuario.map { it.subastaId }.distinct()

                Util.obtenerSubastas(db_ref) { todasSubastas ->
                    val ahora = System.currentTimeMillis()
                    val subastasFinalizadasFiltradas = todasSubastas
                        .filter { it.fechaLimite < ahora && idsSubastasParticipadas.contains(it.idSubasta_firebase) }
                        .filterNot { it.idSubasta_firebase in Util.obtenerSubastasOcultadas(contexto) }

                    val resultado = mutableListOf<Pair<Subasta, Obra>>()
                    var pendientes = subastasFinalizadasFiltradas.size

                    if (pendientes == 0) {
                        subastasFinalizadas = emptyList()
                    }

                    subastasFinalizadasFiltradas.forEach { sub ->
                        Util.obtenerObra(db_ref, contexto, sub.idObra_firebase!!) { obra ->
                            obra?.let { resultado.add(sub to it) }
                            pendientes--
                            if (pendientes == 0) {
                                subastasFinalizadas = resultado.sortedByDescending { it.first.fechaLimite }
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (subastasFinalizadas.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("Subastas Finalizadas", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(subastasFinalizadas) { (subasta, obra) ->
                    Box {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(180.dp)
                                .clickable {
                                    nav?.navigate("resultadoSubasta/${subasta.idSubasta_firebase}")
                                }
                        ) {
                            Column {
                                Image(
                                    painter = rememberAsyncImagePainter(obra.rutaImagen),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Column(Modifier.padding(8.dp)) {
                                    Text(obra.titulo ?: "", fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(
                                        "Finalizó: ${Util.obtenerFecha(subasta.fechaLimite)}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Text(
                            "❌",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clickable {
                                    Util.guardarSubastaOcultada(contexto, subasta.idSubasta_firebase!!)
                                    subastasFinalizadas = subastasFinalizadas.filterNot {
                                        it.first.idSubasta_firebase == subasta.idSubasta_firebase
                                    }
                                },
                            fontSize = 18.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp),
            verticalItemSpacing = 20.dp,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listaPublicaciones.size) { pos ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    PublicacionItem(
                        obra = listaPublicaciones[pos],
                        onclick = { obraSeleccionada = listaPublicaciones[pos] }
                    )
                }
            }
        }
    }

    if (obraSeleccionada != null) {
        Dialog(onDismissRequest = { obraSeleccionada = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(10.dp)
            ) {
                obraSeleccionada?.id_firebase?.let {
                    PublicacionInfoItem(nav, it)
                }
            }
        }
    }
}
