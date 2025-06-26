package com.example.tfg.pantallas

import android.content.Context
import android.util.Log
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Text
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

@Composable
fun ComentariosPublicacion(
    obraKey: String = "error",
    comentarios: MutableMap<String, Comentario> = mutableMapOf()
) {
    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    val comentariosObservables = remember {
        mutableStateListOf<Comentario>().apply {
            addAll(comentarios.values.sortedByDescending { it.fechaCreacion })
        }
    }
    var texto by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val (textFieldRef, sendImageRef) = createRefs()

            TextField(
                value = texto,
                onValueChange = { texto = it },
                placeholder = { Text("Escribe un comentario...") },
                modifier = Modifier
                    .constrainAs(textFieldRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(sendImageRef.start, margin = 8.dp)
                        width = Dimension.fillToConstraints
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.enviar),
                contentDescription = "Enviar",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(40.dp)
                    .constrainAs(sendImageRef) {
                        top.linkTo(textFieldRef.top)
                        bottom.linkTo(textFieldRef.bottom)
                        end.linkTo(parent.end)
                    }
                    .clickable {
                        enviarMensaje(db_ref, contexto, comentariosObservables, obraKey, texto)
                        texto = ""
                    }
            )
        }

        Spacer(modifier = Modifier.padding(top = 12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = comentariosObservables,
                key = { it.id }
            ) { comentario ->
                ComentarioItem(
                    obraKey = obraKey,
                    comentario = comentario
                )
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