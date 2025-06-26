package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.clases.Usuario
import com.example.tfg.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Preview(showBackground = true)
@Composable
fun Registro(
    nav: NavHostController? = null,
    onRegistrar: (String, String, String) -> Unit = { _, _, _ -> },
    onVolver: () -> Unit = {}
) {
    var usuario by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Registro", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.weight(3f))

        TextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3479BF),
                    contentColor = Color.White
                ),
                onClick = { nav?.navigate("login") }
            ) {
                Text("Volver")
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3479BF),
                    contentColor = Color.White
                ),
                modifier = Modifier.width(200.dp),
                onClick = {
                    validarRegistro(db_ref, contexto, usuario, correo, contrasena) { esValido ->
                        if (esValido) {
                            val id_firebase = db_ref.child("usuarios").push().key!!

                            val u = Usuario(
                                id_firebase = id_firebase,
                                admin = false,
                                nombre = usuario,
                                correo = correo,
                                contrasenia = contrasena,
                            )

                            Util.aniadirUsuario(db_ref, u)
                            Util.actualizarShared(contexto, u)
                            Toast.makeText(contexto, "Registro correcto", Toast.LENGTH_SHORT).show()
                            nav?.navigate("principal")
                        }

                        onRegistrar(usuario, correo, contrasena)
                    }
                }
            ) {
                Text("Registrarse")
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

fun validarRegistro(
    db_ref: DatabaseReference,
    contexto: Context,
    usuario: String,
    correo: String,
    contrasena: String,
    callback: (Boolean) -> Unit
) {
    Util.obtenerUsuarios(db_ref) { usuarios ->

        if (usuario.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            Toast.makeText(contexto, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            callback(false)
            return@obtenerUsuarios
        }

        if (usuarios.any { it.nombre == usuario }) {
            Toast.makeText(contexto, "El nombre de usuario ya está en uso", Toast.LENGTH_SHORT).show()
            callback(false)
            return@obtenerUsuarios
        }

        if (usuarios.any { it.correo == correo }) {
            Toast.makeText(contexto, "El correo ya está en uso", Toast.LENGTH_SHORT).show()
            callback(false)
            return@obtenerUsuarios
        }else if (!correo.contains("@")) {
            Toast.makeText(contexto, "El correo no es válido", Toast.LENGTH_SHORT).show()
            callback(false)
            return@obtenerUsuarios
        }

        val tieneMayus = contrasena.any { it.isUpperCase() }
        val tieneNumero = contrasena.any { it.isDigit() }
        val esLarga = contrasena.length >= 6

        if (!tieneMayus || !tieneNumero || !esLarga) {
            Toast.makeText(
                contexto,
                "La contraseña debe tener al menos 6 caracteres, una mayúscula y un número",
                Toast.LENGTH_LONG
            ).show()
            callback(false)
            return@obtenerUsuarios
        }

        // ✅ Todo correcto
        callback(true)
    }
}
