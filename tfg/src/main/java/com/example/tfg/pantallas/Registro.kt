package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var db_ref = FirebaseDatabase.getInstance().reference
    var contexto = LocalContext.current

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

            OutlinedButton(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = { nav?.navigate("login") }) {
                Text("Volver")
            }

            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),
                modifier = Modifier
                    .width(200.dp),
                onClick = {

                    if (validarRegistro(db_ref, contexto, usuario, correo, contrasena)) {

                        val id_firebase = db_ref.child("usuarios").push().key

                        val u = Usuario(
                            id_firebase = id_firebase!!,
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
            ) {
                Text("Registrarse")
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

fun validarRegistro(db_ref: DatabaseReference, contexto: Context, usuario: String, correo: String, contrasena: String) : Boolean{

    var registroValido = true

    Util.obtenerUsuarios(db_ref) { usuarios ->
        if (usuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty()){
            Toast.makeText(contexto, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            registroValido = false

        } else {
            for (u in usuarios) {
                if (u.nombre == usuario) {
                    Toast.makeText(contexto, "El usuario ya existe", Toast.LENGTH_SHORT)
                        .show()
                    registroValido = false
                }

                if (u.correo == correo) {
                    Toast.makeText(contexto, "El correo ya existe", Toast.LENGTH_SHORT)
                        .show()
                    registroValido = false
                }
            }

            Log.d("Registro", "Usuario: $usuario, Correo: $correo, Contraseña: $contrasena")

            Log.d("Registro", "Longitud: ${contrasena.length}, Mayúsculas: ${contrasena.any { it.isUpperCase() }}, Números: ${contrasena.any { it.isDigit() }}")

            var conLenght = contrasena.length < 6
            var mayus = !contrasena.any { it.isUpperCase() }
            var num = !contrasena.any { it.isDigit() }

            Log.d("Registro", "Longitud: $conLenght, Mayúsculas: $mayus, Números: $num")

            if (contrasena.length < 6 || !contrasena.any { it.isDigit() } || !contrasena.any { it.isUpperCase() }) {
                Toast.makeText(contexto, "La contraseña debe tener al menos 6 caracteres, una mayúscula y un número", Toast.LENGTH_LONG).show()
                registroValido = false
            }
        }

    }

    return registroValido
}