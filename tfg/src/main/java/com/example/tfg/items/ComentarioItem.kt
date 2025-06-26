package com.example.tfg.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.tfg.R
import com.example.tfg.clases.Comentario
import com.example.tfg.clases.Usuario
import com.example.tfg.Util
import com.google.firebase.database.FirebaseDatabase

@Preview(showBackground = true)
@Composable
fun ComentarioItem(
    obraKey: String = "ERROR",
    comentario: Comentario = Comentario()
){

    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    val esAdmin = Util.obtenerDatoSharedBoolean(contexto, "admin")
    val esAutor = Util.obtenerDatoShared(contexto, "id") == comentario.autor

    var autor by remember { mutableStateOf<Usuario>(Usuario()) }
    Util.obtenerUsuario(db_ref, comentario.autor) {
        autor = it!!
    }

    ConstraintLayout (modifier = Modifier.fillMaxWidth()) {
        var (imagenR, textoR, usuarioR, fechaR, editar, borrar) = createRefs()

        Image(
            painter = painterResource(id = R.drawable.usuario),
            contentDescription = "Imagen",
            modifier = Modifier
                .padding(16.dp, 16.dp, 10.dp, 13.dp)
                .size(20.dp)
                .constrainAs(imagenR) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )

        Text (autor.nombre,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .constrainAs(usuarioR) {
                    top.linkTo(parent.top)
                    start.linkTo(imagenR.end)
                    bottom.linkTo(imagenR.bottom)
                })

        Text (comentario.texto, modifier = Modifier
            .padding(16.dp, 0.dp, 16.dp, 16.dp)
            .constrainAs(textoR) {
                top.linkTo(imagenR.bottom)
                start.linkTo(parent.start)
            })

        Text( text = Util.tiempoTranscurrido(comentario.fechaCreacion),
            color = Color.Gray,
            modifier = Modifier
            .padding(16.dp)
            .constrainAs(fechaR) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            })

        if (esAdmin || esAutor){

            Image(
                painter = painterResource(id = R.drawable.modificar),
                contentDescription = "Imagen",
                modifier = Modifier
                    .padding(10.dp)
                    .size(15.dp)
                    .constrainAs(editar) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(borrar.start)
                    }
                    .clickable {
                        Util.escribirComentario(db_ref, obraKey, Comentario(
                            id = comentario.id,
                            autor = comentario.autor,
                            texto = comentario.texto,
                            fechaCreacion = comentario.fechaCreacion
                        )) // t
                    }
            )

            Image(
                painter = painterResource(id = R.drawable.borrar),
                contentDescription = "Imagen",
                modifier = Modifier
                    .padding(10.dp)
                    .size(15.dp)
                    .constrainAs(borrar) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                    .clickable {
                        Util.eliminarComentario(db_ref, obraKey, comentario.id)
                    }
            )

        }
    }
}