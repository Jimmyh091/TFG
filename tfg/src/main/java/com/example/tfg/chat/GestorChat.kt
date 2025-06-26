package com.example.tfg.chat

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.clases.Usuario
import com.example.tfg.Util
import com.google.firebase.database.*

@Composable
fun GestorChat(
    nav: NavHostController
) {
    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current
    var listaConversaciones by remember { mutableStateOf<List<Pair<String, Long>>>(emptyList()) }
    var usuarios by remember { mutableStateOf<Map<String, Usuario>>(emptyMap()) }

    val usuarioId = Util.obtenerDatoShared(contexto, "id")!!

    LaunchedEffect(Unit) {
        Util.obtenerConversaciones(db_ref, contexto) { conversaciones ->
            listaConversaciones = conversaciones
            usuarios = emptyMap() // Reinicia para evitar duplicados

            conversaciones.forEach { (id, _) ->
                Util.obtenerUsuario(db_ref, id) { user ->
                    user?.let {
                        usuarios = usuarios + (id to it)
                    }
                }
            }
        }
    }

    Log.v("idLogAlgo", "Lista: " + listaConversaciones)

    Column {

        Text(
            "Chats",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 30.sp
        )

        Log.v("idLogAlgo", "Lista: " + listaConversaciones)

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaConversaciones) { (otroId, timestamp) ->
                val usuario = usuarios[otroId]

                if (usuario != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nav.navigate("chat/${usuario.id_firebase}")
                            }
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.usuario),
                                contentDescription = "Perfil",
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = usuario.nombre,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = Util.obtenerFecha(timestamp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun enviarMensaje(
    dbRef: DatabaseReference,
    emisorId: String,
    receptorId: String,
    texto: String,
    onEnviarNotificacion: (String) -> Unit = {}
) {
    val chatId = generarChatId(emisorId, receptorId)
    val mensajeId = dbRef.child("chatsPrivados").child(chatId).push().key!!
    val timestamp = System.currentTimeMillis()

    val mensaje = mapOf(
        "id_emisor" to emisorId,
        "id_receptor" to receptorId,
        "contenido" to texto,
        "timestamp" to timestamp
    )

    val chatRef = dbRef.child("chatsPrivados").child(chatId)

    chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var haEscritoElOtro = false
            var haEscritoElEmisor = false

            for (child in snapshot.children) {
                val emisorExistente = child.child("id_emisor").getValue(String::class.java)
                if (emisorExistente == emisorId) haEscritoElEmisor = true
                if (emisorExistente == receptorId) haEscritoElOtro = true
            }

            // Enviar notificación si es la primera vez que este emisor habla en este chat
            if (!haEscritoElEmisor) {
                onEnviarNotificacion(receptorId)
            } else if (!haEscritoElOtro) {
                onEnviarNotificacion(emisorId) // responder por primera vez
            }

            // Guardar el mensaje en Firebase
            chatRef.child(mensajeId).setValue(mensaje)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("EnviarMensaje", "Error al enviar mensaje: ${error.message}")
        }
    })
}


fun enviarMensajeConNotificacionSiEsPrimero(
    dbRef: DatabaseReference,
    contexto: Context,
    emisorId: String,
    receptorId: String,
    texto: String,
    onPrimeraVez: () -> Unit = {}
) {
    val resumenRef = dbRef.child("ultimosMensajes").child(receptorId).child(emisorId)
    resumenRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val esPrimeraVez = !snapshot.exists()

            enviarMensaje(dbRef, emisorId, receptorId, texto) { receptorId ->
                enviarNotificacionFirebase(contexto, receptorId, "Nuevo mensaje de $emisorId")
            }

            if (esPrimeraVez) {
                onPrimeraVez()
                // Aquí podrías lanzar una notificación local, o usar Firebase Cloud Messaging (FCM)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}
