package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.items // Esta es la que probablemente buscas para items(List<T>, key, itemContent)
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tfg.items.ComentarioItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.tfg.clases.Comentario
import com.example.tfg.R
import com.example.tfg.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


@Preview(showBackground = true)
@Composable
fun ComentariosPublicacion (
    obraKey: String = "error",
    comentarios: MutableMap<String, Comentario> = mutableMapOf()
){

    Log.d("comentarios", comentarios.values.toString())

    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    val comentariosObservables = remember { mutableStateListOf<Comentario>().apply { addAll(comentarios.values) } }
    var texto by remember { mutableStateOf("") }

    Column {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth() // Ocupa t0do el ancho disponible
                .padding(16.dp) // Espacio alrededor del ConstraintLayout
        ) {
            val (textFieldRef, sendImageRef) = createRefs() // Referencias para los elementos

            TextField(
                value = texto, // Usar la variable de estado
                onValueChange = { texto = it }, // Actualizar la variable de estado
                label = { }, // Puedes poner un Label si quieres
                modifier = Modifier
                    // .background(Color.LightGray) // Opcional: fondo para visualizar el área
                    .constrainAs(textFieldRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(sendImageRef.start, margin = 8.dp) // Espacio entre TextField e Image
                        width = Dimension.fillToConstraints // El ancho se ajusta a las restricciones
                        height = Dimension.wrapContent // La altura se ajusta al contenido del TextField
                    },
            )

            Image(
                painter = painterResource(id = R.drawable.enviar), // Reemplaza R.drawable.home con tu recurso
                contentDescription = "Enviar", // Descripción para accesibilidad
                contentScale = ContentScale.Crop, // Para que la imagen se ajuste al tamaño de la imagen
                modifier = Modifier
                    .width(40.dp) // Ancho fijo
                    .constrainAs(sendImageRef) {
                        top.linkTo(textFieldRef.top) // Alinear la parte superior con el TextField
                        bottom.linkTo(textFieldRef.bottom) // Alinear la parte inferior con el TextField
                        end.linkTo(parent.end)
                    }
                    .clickable {
                        enviarMensaje(db_ref, contexto, comentariosObservables, obraKey, texto)
                        Toast.makeText(contexto, "Mensaje enviado", Toast.LENGTH_SHORT).show()
                        texto = ""
                    }
            )
        }

        LazyColumn(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(
                items = comentariosObservables,
                key = { comentario -> comentario.id }
            ) { comentario ->
                ComentarioItem(
                    obraKey = obraKey,
                    comentario = comentario
                )
            }

            items(comentariosObservables.size){

                Spacer(modifier = Modifier.padding(5.dp))

            }
        }
    }
}

fun enviarMensaje(db_ref: DatabaseReference, contexto: Context, comentarios: MutableList<Comentario>, obraKey: String, texto: String){

    if (texto.isEmpty()){

        Toast.makeText(contexto, "Rellene el comentario", Toast.LENGTH_SHORT).show()

    }else{

        val sp = Util.obtenerDatoShared(contexto, "id")

        val comentarioKey = db_ref.child("arte").child("obras").child(obraKey).child("comentarios").push().key
        val comentario =
            Comentario(
                comentarioKey!!,
                sp!!,
                texto,
                System.currentTimeMillis()
            )

        comentarios.add(comentario)
        Util.escribirComentario(db_ref, obraKey, comentario)
    }
}