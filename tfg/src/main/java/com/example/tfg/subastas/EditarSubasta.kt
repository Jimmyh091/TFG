package com.example.tfg.subastas


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.Util
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.Instant
import java.util.Date

@Composable
fun EditarSubastaScreen(
    subastaId: String?,
    nav: NavHostController
){

    val dbRef = FirebaseDatabase.getInstance().reference

    var titulo by remember{ mutableStateOf("") }
    var desc by remember{ mutableStateOf("") }
    var precio by remember{ mutableStateOf("") }
    var fechaLimite by remember{ mutableStateOf("") } // como “2025-07-01T12:00”
/*
    LaunchedEffect(subastaId){
        subastaId?.let{ id ->
            dbRef.child("arte").child("subastas").child(id)
                .get().addOnSuccessListener{ snap->
                    snap.getValue(Subasta::class.java)?.let{ s->
                        titulo=s.titulo!!; desc=s.descripcion!!
                        precio=s.precio.toString()
                        // formatea fechaLimite de timestamp a ISO
                        fechaLimite = Date(s.fechaLimite).toInstant().toString()
                    }
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)){
        OutlinedTextField(titulo,{titulo=it},label={Text("Título")})
        OutlinedTextField(desc,{desc=it},label={Text("Descripción")})
        OutlinedTextField(precio,{precio=it},label={Text("Precio inicial")})
        OutlinedTextField(fechaLimite,{fechaLimite=it},label={Text("Fecha límite ISO")})

        Button(onClick={
            val ts = Date.from(Instant.parse(fechaLimite)).time
            val sub = Subasta(
                idSubasta_firebase = subastaId,
                titulo=titulo, descripcion=desc,
                id_firebase = "", fechaLimite=ts,
                precio=precio.toFloat()
            )
            Util.guardarSubasta(dbRef, sub){
                nav.popBackStack()
            }
        }){
            Text(if(subastaId==null)"Crear" else "Guardar")
        }
    }
    *^/
 */
}
