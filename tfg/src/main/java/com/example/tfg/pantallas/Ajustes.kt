package com.example.tfg.pantallas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.Util

@Composable
fun AjustesPantalla(nav: NavHostController? = null) {
    val contexto = LocalContext.current
    val nombre = Util.obtenerDatoShared(contexto, "nombre") ?: "Usuario"
    val correo = Util.obtenerDatoShared(contexto, "correo") ?: "correo@ejemplo.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.usuario),
                contentDescription = "Imagen perfil",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(correo, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
            }
        }

        Divider(thickness = 1.dp)

        Spacer(modifier = Modifier.height(16.dp))

        AjusteItem("Editar perfil", Icons.Default.Edit) {
            nav?.navigate("editarPerfil")
        }

        AjusteItem("Estadísticas", Icons.Default.Language) {
            nav?.navigate("estadisticas/${Util.obtenerDatoShared(contexto, "id")}")
        }

        AjusteItem("Notificaciones", Icons.Default.Notifications) {
            Toast.makeText(contexto, "Funcionalidad próximamente", Toast.LENGTH_SHORT).show()
        }

        AjusteItem("Política de privacidad", Icons.Default.Info) {
            Toast.makeText(contexto, "Política de privacidad no implementada aún", Toast.LENGTH_SHORT).show()
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                Util.cerrarSesion(contexto)
                nav?.navigate("login")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun AjusteItem(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(texto, fontSize = 16.sp)
    }
}
