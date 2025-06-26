package com.example.tfg.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.APIMonedas.CurrencyConverterScreen
import com.example.tfg.clases.Usuario
import com.example.tfg.crudObra.Obra
import com.example.tfg.Util
import com.google.firebase.database.FirebaseDatabase

/*
@Preview(showBackground = true)
@Composable
fun PantalalaPago(
    nav: NavHostController? = null,
    obraKey: String? = null
) {
    var numeroTarjeta by remember { mutableStateOf("") }
    var nombreTitular by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val contexto = LocalContext.current
    var marcado by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pago con Tarjeta",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 26.dp))

        Spacer(modifier = Modifier.weight(3f))

        TextField(
            value = nombreTitular,
            onValueChange = { nombreTitular = it },
            label = { Text("Nombre del Titular") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = nombreTitular,
            onValueChange = { nombreTitular = it },
            label = { Text("Número de Tarjeta") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextField(
                value = fechaVencimiento,
                onValueChange = { fechaVencimiento = it },
                label = { Text("Vencimiento") },
                modifier = Modifier.weight(1f)
            )

            TextField(
                value = cvv,
                onValueChange = { cvv = it },
                label = { Text("CVV") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
        }

        Row {
            Checkbox(
                checked = marcado,
                onCheckedChange = { marcado = it },
                modifier = Modifier
                    .padding(end = 5.dp)
            )

            Text(
                text = "Recordar tarjeta",
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable {
                        marcado = !marcado
                    }
            )
        }


        Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally),
            onClick = {

                gestionarPago()

                Toast.makeText(contexto, "Pago realizado", Toast.LENGTH_SHORT).show()

                nav?.navigate("lasdkjfald") {
                    popUpTo(nav.graph.startDestinationId) { // O popUpTo(0)
                        inclusive = true
                    }
                    launchSingleTop = true // Buena práctica
                }
            },
        ) {
            Text("Pagar")
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}*/

@Composable
fun PantallaPago(
    nav: NavHostController,
    obraId: String,
) {

    val db_ref = FirebaseDatabase.getInstance().reference
    val contexto = LocalContext.current

    var obra by remember { mutableStateOf(Obra()) }
    Util.obtenerObra(db_ref, contexto, obraId) { ob ->
        if (ob != null) {
            obra = ob
        }
    }

    var usuario by remember { mutableStateOf(Usuario()) }
    Util.obtenerUsuario(db_ref, Util.obtenerDatoShared(contexto, "id")!!) { us ->
        if (us != null) {
            usuario = us
        }
    }

    var dineroFaltante by remember { mutableStateOf<Float?>(null) }
    var cantidadAgregar by remember { mutableStateOf("") }
    var compraRealizada by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Comprar: ${obra.titulo}", fontWeight = FontWeight.Bold)
        Text("Precio: ${obra.precio}€")
        Text("Tu saldo: ${usuario.dinero}€")

        Spacer(modifier = Modifier.height(16.dp))

        if (!compraRealizada) {
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = {
                intentarComprar(
                    usuario = usuario,
                    obra = obra,
                    onCompraExitosa = {
                        Util.editarUsuario(db_ref, contexto, usuario)
                        Util.escribirObra(db_ref, obra) // Por si quieres actualizar la obra también
                        compraRealizada = true
                    },
                    onFaltaDinero = { faltante ->
                        dineroFaltante = faltante
                    }
                )
            }) {
                Text("Comprar obra")
            }
        }

        if (dineroFaltante != null && !compraRealizada) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Te faltan ${"%.2f".format(dineroFaltante)}€")

            Text("¿Cuánto quieres añadir?")
            OutlinedTextField(
                value = cantidadAgregar,
                onValueChange = { cantidadAgregar = it },
                label = { Text("Cantidad (€)") }
            )

            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = {
                val cantidad = cantidadAgregar.toFloatOrNull() ?: 0f
                if (cantidad > 0) {
                    usuario.dinero = usuario.dinero!! + cantidad
                    Util.editarUsuario(db_ref, contexto, usuario)
                    dineroFaltante = null
                }
            }) {
                Text("Añadir saldo")
            }
        }

        CurrencyConverterScreen()

        if (compraRealizada) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("¡Compra realizada con éxito!")
            Button(colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3479BF),
                contentColor = Color.White          
            ),onClick = { nav.navigate("principal") }) {
                Text("Volver a inicio")
            }
        }
    }
}

fun intentarComprar(
    usuario: Usuario,
    obra: Obra,
    onCompraExitosa: () -> Unit,
    onFaltaDinero: (dineroFaltante: Float) -> Unit
) {
    if (usuario.dinero >= (obra.precio ?: 0f)) {
        usuario.dinero = usuario.dinero - obra.precio!!
        usuario.obrasCompradas.add(obra.id_firebase!!)
        onCompraExitosa()
    } else {
        val faltante = obra.precio!! - usuario.dinero
        onFaltaDinero(faltante)
    }
}



/*
private fun fetchCryptoPrices() {
    val coinIds = "bitcoin,ethereum,litecoin"
    val vsCurrencies = "usd,eur,ars,mxn,jpy"

    val call = RetrofitClient.coinGeckoApi.getSimplePrice(coinIds, vsCurrencies)

    call.enqueue(object : retrofit2.Callback<Map<String, Map<String, Double>>> {
        override fun onResponse(
            call: retrofit2.Call<Map<String, Map<String, Double>>>,
            response: retrofit2.Response<Map<String, Map<String, Double>>>
        ) {
            if (response.isSuccessful) {
                val responseBody = response.body()
                responseBody?.let { map ->
                    pricesList.clear()

                    // map tiene estructura: Map<crypto, Map<monedaFiat, precio>>
                    for ((crypto, prices) in map) {
                        for ((currency, price) in prices) {
                            pricesList.add(CryptoPrice(crypto, currency, price))
                        }
                    }

                    // Ahora tienes una lista plana con todos los precios para manejar
                    // Puedes usarla para mostrar en RecyclerView, log, etc.
                    for (item in pricesList) {
                        println("${item.cryptoName} en ${item.currency}: ${item.price}")
                    }
                }
            } else {
                println("Error en la respuesta: ${response.code()}")
            }
        }

        override fun onFailure(call: retrofit2.Call<Map<String, Map<String, Double>>>, t: Throwable) {
            println("Error en la llamada: ${t.message}")
        }
    })
}*/