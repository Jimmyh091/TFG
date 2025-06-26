package com.example.tfg.pantallas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.Util
import com.example.tfg.clases.Usuario
import com.google.firebase.database.FirebaseDatabase

@Composable
fun EditarPerfilScreen(nav: NavHostController? = null) {
    val contexto = LocalContext.current
    val dbRef = FirebaseDatabase.getInstance().reference

    val usuarioId = Util.obtenerDatoShared(contexto, "id") ?: return
    var usuario by remember { mutableStateOf(Usuario()) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasenia by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        Util.obtenerUsuario(dbRef, usuarioId) {
            if (it != null) {
                usuario = it
                nombre = it.nombre
                correo = it.correo
                contrasenia = it.contrasenia
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Editar perfil", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.perfil),
            contentDescription = "Foto de perfil",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contrasenia,
            onValueChange = { contrasenia = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val actualizado = usuario.copy(
                    nombre = nombre,
                    correo = correo,
                    contrasenia = contrasenia
                )

                dbRef.child("usuarios").child(usuarioId).setValue(actualizado)
                    .addOnSuccessListener {
                        Toast.makeText(contexto, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        nav?.popBackStack()
                    }
                    .addOnFailureListener {
                        Toast.makeText(contexto, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Guardar cambios")
        }
    }
}
