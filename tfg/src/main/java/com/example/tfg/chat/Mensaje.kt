package layout

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mensaje(
    var id:String? = null,
    var id_emisor:String? = null,
    var id_receptor:String? = null,
    var contenido:String? = null,
    var fechaCreacion: Long = System.currentTimeMillis(),
): Parcelable