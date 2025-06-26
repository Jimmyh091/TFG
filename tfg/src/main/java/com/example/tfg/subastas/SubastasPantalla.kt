package com.example.tfg.subastas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.Util
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import com.example.tfg.crudObra.Obra

@Composable
fun SubastasPantalla(nav: NavHostController) {

    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var subastas by remember { mutableStateOf<List<Subasta>>(emptyList()) }
    LaunchedEffect(Unit) {
        Util.obtenerSubastasActivas(dbRef){ subastas = it }
    }
    LazyColumn {
        items(subastas
            .filter { it.fechaLimite > System.currentTimeMillis() }
            .sortedBy { it.fechaLimite }
        ) { subasta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable { nav.navigate("detalleSubasta/${subasta.idSubasta_firebase}") }
            ) {

                var obra by remember { mutableStateOf<Obra?>(null) }
                Util.obtenerObra(dbRef, contexto, subasta.idObra_firebase!!) { obra = it }

                Column(Modifier.padding(16.dp)) {
                    Text(obra!!.titulo!!, style = MaterialTheme.typography.titleMedium)
                    Text("Cierra: ${Util.tiempoATranscurrir(subasta.fechaLimite)}")
                    Text("Desde: ${obra!!.precio}€")
                }
            }
        }
    }

}
