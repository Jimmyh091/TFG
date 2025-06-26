package com.example.tfg.chat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.Util
import layout.Mensaje

@Composable
fun MensajeItem(mensaje: Mensaje, esMio: Boolean) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (esMio) {
        if (isDarkTheme) Color(0xFF2E7D32) else Color(0xFFD0F0C0)
    } else {
        if (isDarkTheme) Color(0xFF424242) else Color(0xFFF0F0F0)
    }

    val textColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (esMio) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = mensaje.contenido ?: "",
                    fontSize = 16.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Util.obtenerFecha(mensaje.fechaCreacion),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

