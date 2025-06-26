package com.example.tfg

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.tfg.clases.Amonestacion
import com.example.tfg.clases.Comentario
import com.example.tfg.subastas.Puja
import com.example.tfg.subastas.Subasta
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Util {
    companion object {

        // ---------------- OBRAS ----------------

        fun obtenerObras(db: DatabaseReference, ctx: Context, cb: (List<Obra>) -> Unit) {
            db.child("arte/obras")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snap: DataSnapshot) {
                        val obras = snap.children.mapNotNull { it.getValue(Obra::class.java) }
                        cb(obras)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(ctx, "Error al obtener obras", Toast.LENGTH_SHORT).show()
                        cb(emptyList())
                    }
                })
        }

        fun obtenerObra(db: DatabaseReference, ctx: Context, id: String, cb: (Obra?) -> Unit) {
            db.child("arte/obras").child(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.getValue(Obra::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(null)
                    }
                })
        }

        fun escribirObra(db: DatabaseReference, obra: Obra) {
            db.child("arte/obras").child(obra.id_firebase!!).setValue(obra)
        }

        fun eliminarObra(db: DatabaseReference, id: String, cb: () -> Unit = {}) {
            db.child("arte/obras").child(id).removeValue().addOnSuccessListener { cb() }
        }

        fun editarObra(db: DatabaseReference, obra: Obra) {
            escribirObra(db, obra)
        }

        fun eliminarObraCompleto(dbRef: DatabaseReference, obraId: String, onComplete: () -> Unit = {}) {
            // Eliminar la obra
            dbRef.child("obras").child(obraId).removeValue()

            // Eliminar subasta si existe
            dbRef.child("subastas").orderByChild("idObra_firebase").equalTo(obraId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { it.ref.removeValue() }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            // Eliminar amonestaciones relacionadas
            dbRef.child("amonestaciones").orderByChild("obraId").equalTo(obraId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { it.ref.removeValue() }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            // Quitarla de favoritos, comprados y creadas en usuarios
            dbRef.child("usuarios").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (userSnap in snapshot.children) {
                        val userRef = userSnap.ref
                        val favRef = userRef.child("obrasFavoritas").child(obraId)
                        val compRef = userRef.child("obrasCompradas").child(obraId)
                        val creadasRef = userRef.child("obrasCreadas").child(obraId)

                        favRef.removeValue()
                        compRef.removeValue()
                        creadasRef.removeValue()
                    }
                    onComplete()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }


        // ---------------- USUARIOS ----------------

        fun obtenerUsuarios(db: DatabaseReference, cb: (List<Usuario>) -> Unit) {
            db.child("usuarios")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lista = snapshot.children.mapNotNull { it.getValue(Usuario::class.java) }
                        cb(lista)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(emptyList())
                    }
                })
        }

        fun obtenerUsuario(db: DatabaseReference, id: String, cb: (Usuario?) -> Unit) {
            db.child("usuarios").child(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snap: DataSnapshot) {
                        cb(snap.getValue(Usuario::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(null)
                    }
                })
        }

        fun obtenerUsuarioLogin(db: DatabaseReference, usuario: String, contrasenia: String, cb: (Usuario?) -> Unit) {
            obtenerUsuarios(db) { lista ->
                cb(lista.firstOrNull { it.nombre == usuario && it.contrasenia == contrasenia })
            }
        }

        fun aniadirUsuario(db_ref: DatabaseReference, usuario: Usuario) {
            db_ref.child("usuarios").child(usuario.id_firebase).setValue(usuario)
        }

        fun editarUsuario(db: DatabaseReference, ctx: Context, usuario: Usuario, ok: () -> Unit = {}, err: (String) -> Unit = {}) {
            db.child("usuarios").child(usuario.id_firebase)
                .setValue(usuario)
                .addOnSuccessListener {
                    actualizarShared(ctx, usuario)
                    ok()
                }.addOnFailureListener {
                    err(it.message ?: "Error desconocido al editar usuario")
                }
        }

        fun eliminarUsuarioCompleto(dbRef: DatabaseReference, usuarioId: String, onComplete: () -> Unit = {}) {
            // Eliminar el propio usuario
            dbRef.child("usuarios").child(usuarioId).removeValue()

            // Eliminar obras creadas por el usuario
            dbRef.child("obras").orderByChild("autor").equalTo(usuarioId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val obras = snapshot.children.mapNotNull { it.key }
                        obras.forEach { obraId ->
                            eliminarObraCompleto(dbRef, obraId)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            // Eliminar subastas creadas por el usuario
            dbRef.child("subastas").orderByChild("autorId").equalTo(usuarioId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val subastas = snapshot.children.mapNotNull { it.key }
                        subastas.forEach { subastaId ->
                            eliminarSubastaCompleto(dbRef, subastaId)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            // Eliminar amonestaciones realizadas por el usuario
            dbRef.child("amonestaciones").orderByChild("denuncianteId").equalTo(usuarioId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { it.ref.removeValue() }
                        onComplete()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            // Eliminar comentarios hechos por el usuario en cualquier obra
            dbRef.child("obras").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(obrasSnapshot: DataSnapshot) {
                    for (obraSnap in obrasSnapshot.children) {
                        val comentariosSnap = obraSnap.child("comentarios")
                        for (comentarioSnap in comentariosSnap.children) {
                            val autorId = comentarioSnap.child("autorId").getValue(String::class.java)
                            if (autorId == usuarioId) {
                                comentarioSnap.ref.removeValue()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        // ---------------- COMENTARIOS ----------------

        fun obtenerComentarios(db: DatabaseReference, ctx: Context, obraId: String, cb: (List<Comentario>) -> Unit) {
            db.child("arte/obras").child(obraId).child("comentarios")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.children.mapNotNull { it.getValue(Comentario::class.java) })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(ctx, "Error al obtener comentarios", Toast.LENGTH_SHORT).show()
                        cb(emptyList())
                    }
                })
        }

        fun escribirComentario(db: DatabaseReference, obraId: String, comentario: Comentario) {
            db.child("arte/obras").child(obraId).child("comentarios").child(comentario.id).setValue(comentario)
        }

        fun eliminarComentario(db: DatabaseReference, obraId: String, comentarioId: String) {
            db.child("arte/obras").child(obraId).child("comentarios").child(comentarioId).removeValue()
        }

        // ---------------- SUBASTAS ----------------

        fun obtenerSubastas(db: DatabaseReference, cb: (List<Subasta>) -> Unit) {
            db.child("arte/subastas")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.children.mapNotNull { it.getValue(Subasta::class.java) })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(emptyList())
                    }
                })
        }

        fun obtenerSubasta(db: DatabaseReference, id: String, cb: (Subasta?) -> Unit) {
            db.child("arte/subastas").child(id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.getValue(Subasta::class.java))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(null)
                    }
                })
        }

        fun obtenerSubastasActivas(db: DatabaseReference, cb: (List<Subasta>) -> Unit) {
            db.child("arte/subastas")
                .orderByChild("fechaLimite")
                .startAt(System.currentTimeMillis().toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.children.mapNotNull { it.getValue(Subasta::class.java) })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(emptyList())
                    }
                })
        }

        fun guardarSubasta(db: DatabaseReference, sub: Subasta, cb: () -> Unit = {}) {
            db.child("arte/subastas").child(sub.idSubasta_firebase!!).setValue(sub).addOnSuccessListener { cb() }
        }

        fun eliminarSubastaCompleto(dbRef: DatabaseReference, subastaId: String, onComplete: () -> Unit = {}) {
            dbRef.child("subastas").child(subastaId).get().addOnSuccessListener { snapshot ->
                val idObra = snapshot.child("idObra_firebase").value as? String
                dbRef.child("subastas").child(subastaId).removeValue()
                dbRef.child("pujas").orderByChild("idSubasta").equalTo(subastaId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(pujaSnap: DataSnapshot) {
                            pujaSnap.children.forEach { it.ref.removeValue() }
                            if (idObra != null) {
                                eliminarObraCompleto(dbRef, idObra, onComplete)
                            } else {
                                onComplete()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
            }
        }


        fun cerrarSubasta(
            dbRef: DatabaseReference,
            subasta: Subasta
        ) {
            val subastaId = subasta.idSubasta_firebase!!

            // Obtener todas las pujas de la subasta
            dbRef.child("arte").child("pujas").child(subastaId)
                .get().addOnSuccessListener { snapshot ->
                    val pujas = snapshot.children.mapNotNull { it.getValue(Puja::class.java) }
                    val maxPuja = pujas.maxByOrNull { it.cantidad }

                    if (maxPuja != null) {
                        val ganadorId = maxPuja.pujadorId

                        // 1. Guardar el ganador en la subasta
                        dbRef.child("arte").child("subastas").child(subastaId)
                            .child("idGanador").setValue(ganadorId)

                    }

                }
        }

        fun obtenerPujasDeUsuario(
            db: DatabaseReference,
            userId: String,
            callback: (List<Puja>) -> Unit
        ) {
            db.child("arte").child("pujas")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lista = mutableListOf<Puja>()
                        for (subastaSnap in snapshot.children) {
                            for (pujaSnap in subastaSnap.children) {
                                val puja = pujaSnap.getValue(Puja::class.java)
                                if (puja != null && puja.pujadorId == userId) {
                                    lista.add(puja)
                                }
                            }
                        }
                        callback(lista)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(emptyList())
                    }
                })
        }


        fun hacerPuja(db: DatabaseReference, puja: Puja, cb: () -> Unit = {}) {
            val id = puja.id ?: db.child("arte/pujas/${puja.subastaId}").push().key!!
            db.child("arte/pujas/${puja.subastaId}/$id").setValue(puja.copy(id = id)).addOnSuccessListener { cb() }
        }

        fun obtenerPujas(db: DatabaseReference, subastaId: String, cb: (List<Puja>) -> Unit) {
            db.child("arte/pujas/$subastaId")
                .orderByChild("cantidad")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot.children.mapNotNull { it.getValue(Puja::class.java) })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(emptyList())
                    }
                })
        }

        // ---------------- CHAT ----------------

        fun obtenerConversaciones(db: DatabaseReference, ctx: Context, cb: (List<Pair<String, Long>>) -> Unit) {
            val userId = obtenerDatoShared(ctx, "id") ?: return cb(emptyList())
            db.child("chatsPrivados")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snap: DataSnapshot) {
                        val convos = mutableListOf<Pair<String, Long>>()
                        snap.children.forEach { chat ->
                            val ids = chat.key?.split("|") ?: return@forEach
                            if (ids.contains(userId)) {
                                val otroId = ids.first { it != userId }
                                val ultimo = chat.children.maxOfOrNull {
                                    it.child("timestamp").getValue(Long::class.java) ?: 0L
                                } ?: 0L
                                convos.add(otroId to ultimo)
                            }
                        }
                        cb(convos.sortedByDescending { it.second })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cb(emptyList())
                    }
                })
        }

        // ---------------- AMONESTACIONES ----------------

        fun obtenerAmonestaciones(
            dbRef: DatabaseReference,
            contexto: Context,
            onResult: (List<Amonestacion>) -> Unit
        ) {
            dbRef.child("amonestaciones")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lista = mutableListOf<Amonestacion>()
                        snapshot.children.forEach {
                            val am = it.getValue(Amonestacion::class.java)
                            if (am != null) {
                                lista.add(am)
                            }
                        }
                        onResult(lista)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(contexto, "Error al obtener amonestaciones", Toast.LENGTH_SHORT).show()
                    }
                })
        }


        // ---------------- FECHA / TIEMPO ----------------

        fun obtenerFecha(ts: Long): String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(ts))

        fun tiempoTranscurrido(ts: Long): String {
            val diff = System.currentTimeMillis() - ts
            val s = diff / 1000
            val m = s / 60
            val h = m / 60
            val d = h / 24
            val mo = d / 30
            val y = d / 365

            return when {
                s < 60 -> "hace ${s}s"
                m < 60 -> "hace ${m}min"
                h < 24 -> "hace ${h}h"
                d < 30 -> "hace ${d}d"
                mo < 12 -> "hace ${mo}m"
                else -> "hace ${y}a"
            }
        }

        fun tiempoATranscurrir(ts: Long): String {
            val diff = ts - System.currentTimeMillis()
            if (diff <= 0) return "Finalizada"
            val s = diff / 1000
            val m = s / 60
            val h = m / 60
            val d = h / 24

            return when {
                d > 0 -> "En $d días"
                h > 0 -> "En $h h"
                m > 0 -> "En $m min"
                else -> "En segundos"
            }
        }

        fun cerrarSesion(context: Context) {
            val prefs = context.getSharedPreferences("usuario", MODE_PRIVATE)
            prefs.edit().clear().apply()

            Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }


        // ---------------- SHARED PREFERENCES ----------------

        fun actualizarShared(ctx: Context, u: Usuario) {
            val prefs = ctx.getSharedPreferences("usuario", MODE_PRIVATE).edit()
            prefs.clear()
            prefs.putString("id", u.id_firebase)
            prefs.putBoolean("admin", u.admin)
            prefs.putString("nombre", u.nombre)
            prefs.putString("correo", u.correo)
            prefs.putString("contrasenia", u.contrasenia)
            prefs.putFloat("dinero", u.dinero)
            prefs.putString("foto", u.imagen)
            prefs.putStringSet("favoritos", u.obrasFavoritas.toSet())
            prefs.putStringSet("obras_compradas", u.obrasCompradas.toSet())
            prefs.putStringSet("obras_creadas", u.obrasCreadas.toSet())
            prefs.apply()
        }

        fun obtenerDatoShared(ctx: Context, clave: String): String? =
            ctx.getSharedPreferences("usuario", MODE_PRIVATE).getString(clave, null)

        fun obtenerDatoSharedFloat(ctx: Context, clave: String): Float =
            ctx.getSharedPreferences("usuario", MODE_PRIVATE).getFloat(clave, -1f)

        fun obtenerDatoSharedBoolean(ctx: Context, clave: String): Boolean =
            ctx.getSharedPreferences("usuario", MODE_PRIVATE).getBoolean(clave, false)

        fun guardarSubastaOcultada(contexto: Context, idSubasta: String) {
            val prefs = contexto.getSharedPreferences("subastas", Context.MODE_PRIVATE)
            val ocultadas = prefs.getStringSet("ocultadas", mutableSetOf()) ?: mutableSetOf()
            val nuevas = ocultadas.toMutableSet().apply { add(idSubasta) }
            prefs.edit().putStringSet("ocultadas", nuevas).apply()
        }

        fun obtenerSubastasOcultadas(contexto: Context): Set<String> {
            val prefs = contexto.getSharedPreferences("subastas", Context.MODE_PRIVATE)
            return prefs.getStringSet("ocultadas", mutableSetOf()) ?: emptySet()
        }

    }
}
