package com.example.tfg.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavHostController
import com.example.tfg.R
import com.example.tfg.Util
import com.google.firebase.database.*
import layout.Mensaje

@Composable
fun ChatPrivado(
    usuarioDestinoId: String,
    nav: NavHostController? = null
) {
    val dbRef = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current
    val mensajes = remember { mutableStateListOf<Mensaje>() }
    var mensajeTexto by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val usuarioActual = Util.obtenerDatoShared(contexto, "id")!!

    var chatId = ""
    var nombreUsuario by remember { mutableStateOf("") }

    Util.obtenerUsuario(dbRef, usuarioDestinoId) {
        nombreUsuario = it!!.nombre
    }

    chatId = generarChatId(usuarioActual, usuarioDestinoId)

    // Escuchar mensajes
    LaunchedEffect(Unit) {
        dbRef.child("chatsPrivados").child(chatId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, prevKey: String?) {
                    val mensaje = snapshot.getValue(Mensaje::class.java)
                    mensaje?.let { mensajes.add(it) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.height(70.dp)) {
            Image(
                painter = painterResource(id = R.drawable.atras),
                contentDescription = "Volver",
                modifier = Modifier
                    .padding(16.dp)
                    .height(20.dp)
                    .clickable { nav?.navigate("principal") }
            )
            Text(
                text = nombreUsuario,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(13.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(mensajes) { mensaje ->
                val esMio = mensaje.id_emisor == usuarioActual
                MensajeItem(mensaje = mensaje, esMio = esMio)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = mensajeTexto,
                onValueChange = { mensajeTexto = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Escribe un mensaje") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = {
                if (mensajeTexto.trim().isNotEmpty()) {
                    val id = dbRef.child("chatsPrivados").child(chatId).push().key!!
                    val fechaHora = System.currentTimeMillis()
                    val nuevoMensaje = Mensaje(
                        id = id,
                        id_emisor = usuarioActual,
                        id_receptor = usuarioDestinoId,
                        contenido = mensajeTexto,
                        fechaCreacion = fechaHora
                    )
                    enviarMensaje(dbRef, usuarioActual, usuarioDestinoId, mensajeTexto) { receptorId ->
                        enviarNotificacionFirebase(contexto, receptorId, "Nuevo mensaje de ${Util.obtenerDatoShared(contexto, "nombre")}")
                    }

                    mensajeTexto = ""
                } else {
                    Toast.makeText(contexto, "Escribe algo", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Enviar")
            }
        }
    }

    LaunchedEffect(mensajes.size) {
        listState.animateScrollToItem(mensajes.size)
    }
}

fun generarChatId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) "${userId1}|${userId2}" else "${userId2}|${userId1}"
}

fun enviarNotificacionFirebase(context: Context, receptorId: String, mensaje: String) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel("chat_id", "Chat", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(canal)
    }

    val notification = NotificationCompat.Builder(context, "chat_id")
        .setContentTitle("Nuevo mensaje")
        .setContentText(mensaje)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .build()

    notificationManager.notify(receptorId.hashCode(), notification)
}
