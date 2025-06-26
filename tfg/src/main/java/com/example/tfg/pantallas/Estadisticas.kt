package com.example.tfg.estadisticas

import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.tfg.clases.Usuario
import com.example.tfg.Util
import com.google.firebase.database.FirebaseDatabase

@Composable
fun EstadisticasScreen(navController: NavHostController? = null, usuarioId: String? = null) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val usuarioState = remember { mutableStateOf<Usuario?>(null) }

    LaunchedEffect(usuarioId) {
        Util.obtenerUsuario(dbRef, usuarioId!!) {
            usuarioState.value = it
        }
    }

    usuarioState.value?.let { usuario ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Estadísticas del usuario",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            BarChartView(
                compras = usuario.obrasCompradas.size,
                favoritos = usuario.obrasFavoritas.size,
                ventas = usuario.obrasCreadas.size
            )
        }
    } ?: run {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando estadísticas...")
        }
    }
}

@Composable
fun BarChartView(ventas: Int, compras: Int, favoritos: Int) {
    val isDarkTheme = isSystemInDarkTheme()

    AndroidView(factory = { context ->
        BarChart(context).apply {
            val entries = listOf(
                BarEntry(0f, ventas.toFloat()),
                BarEntry(1f, compras.toFloat()),
                BarEntry(2f, favoritos.toFloat())
            )

            val dataSet = BarDataSet(entries, "Estadísticas").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextColor = if (isDarkTheme) Color.WHITE else Color.BLACK
                valueTextSize = 16f
            }

            val barData = BarData(dataSet)
            barData.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            })

            this.data = barData

            val description = Description()
            description.text = "Comparación"
            this.description = description

            this.setDrawGridBackground(false)
            this.setDrawBorders(false)
            this.setFitBars(true)
            this.setDrawValueAboveBar(true)
            this.animateY(1000)
            this.invalidate()
        }
    }, modifier = Modifier
        .fillMaxWidth()
        .height(300.dp))
}
