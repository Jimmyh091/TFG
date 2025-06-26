package com.example.tfg.pantallas

import androidx.compose.material.icons.filled.Visibility
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.example.tfg.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Preview(showBackground = true)
@Composable
fun Login(
    nav: NavHostController? = null
) {
    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var contrasenaVisible by remember { mutableStateOf(false) }
    var usuario by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val (titulo, content) = createRefs()

        Text(
            "Login",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 75.dp)
                .constrainAs(titulo) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        Column(
            modifier = Modifier
                .constrainAs(content) {
                    top.linkTo(titulo.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    Log.v("Usuario", it)
                },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = contrasena,
                onValueChange = {
                    contrasena = it
                    Log.v("Contraseña", it)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (contrasenaVisible) "Ocultar contraseña" else "Mostrar contraseña"

                    IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3479BF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        nav?.navigate("registro")
                    }
                ) {
                    Text("Registrarse")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3479BF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            usuario.isBlank() || contrasena.isBlank() -> {
                                Toast.makeText(contexto, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
                            }
                            usuario.length < 3 -> {
                                Toast.makeText(contexto, "El nombre de usuario debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show()
                            }
                            contrasena.length < 6 -> {
                                Toast.makeText(contexto, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                validarLogin(db_ref, contexto, usuario, contrasena) { esValido ->
                                    if (esValido) {
                                        Util.obtenerUsuarioLogin(db_ref, usuario, contrasena) {
                                            if (it == null) {
                                                Toast.makeText(contexto, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Util.actualizarShared(contexto, it)
                                                Toast.makeText(contexto, "Inicio de sesión correcto", Toast.LENGTH_SHORT).show()
                                                nav?.navigate("principal")
                                            }
                                        }
                                    } else {
                                        Toast.makeText(contexto, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Iniciar Sesión")
                }
            }
        }
    }
}

fun validarLogin(
    db_ref: DatabaseReference,
    contexto: Context,
    usuario: String,
    contrasena: String,
    callback: (Boolean) -> Unit
) {
    Util.obtenerUsuarios(db_ref) { usuarios ->
        var userValido = false

        for (u in usuarios) {
            if (u.nombre == usuario && u.contrasenia == contrasena) {
                userValido = true
                break
            }
        }

        callback(userValido)
    }
}
