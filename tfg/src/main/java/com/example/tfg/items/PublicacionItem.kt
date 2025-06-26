package com.example.tfg.items

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.tfg.R
import com.example.tfg.Util
import com.example.tfg.crudObra.Obra
import com.example.tfg.subastas.Subasta


@Preview(showBackground = true)
@Composable
fun PublicacionItem(
    obra: Obra = Obra(),
    onclick: () -> Unit = {}
) {
    val esSubasta = obra is Subasta
    val subasta = obra as? Subasta

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray)
            .clickable { onclick.invoke() }
    ) {
        Image(
            painter = rememberAsyncImagePainter(obra.rutaImagen),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp), // 👈 esto arregla el problema visual,
            contentScale = ContentScale.Crop
        )


        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = obra.titulo ?: "Sin título",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Precio: ${obra.precio}€",
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )

            if (esSubasta && subasta != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Finaliza: ${
                        if (subasta.fechaLimite < System.currentTimeMillis()) {
                            Util.tiempoATranscurrir(subasta.fechaLimite)
                        } else {
                            Util.tiempoTranscurrido(subasta.fechaLimite)
                        }
                    }",
                    fontSize = 14.sp,
                    color = Color.Red,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onclick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBF3434),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text("Ir a subasta")
                }
            }
        }
    }

}
