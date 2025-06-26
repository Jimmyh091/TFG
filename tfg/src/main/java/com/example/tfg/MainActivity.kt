package com.example.tfg

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tfg.chat.ChatPrivado
import com.example.tfg.chat.GestorChat
import com.example.tfg.crudObra.CrearObra
import com.example.tfg.crudObra.ModificarObraScreen
import com.example.tfg.estadisticas.EstadisticasScreen
import com.example.tfg.items.BottomNavItem
import com.example.tfg.pantallas.*
import com.example.tfg.subastas.DetalleSubastaScreen
import com.example.tfg.subastas.EditarSubastaScreen
import com.example.tfg.subastas.ResultadoSubastaScreen
import com.example.tfg.subastas.SubastasPantalla
import com.example.tfg.ui.theme.TfgTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TfgTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun AppNavegacion(navController: NavHostController = rememberNavController(), innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("principal") { PantallaPrincipal(navController) }
        composable("perfil/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            PerfilUsuario(navController, usuarioId)
        }
        composable("buscador") { Buscador(navController) }
        composable("pagoCarrito") { PantallaPagoCarrito(navController) }
        composable("registro") { Registro(navController) }
        composable("login") { Login(navController) }
        composable("crearObra") { CrearObra(navController) }
        composable("admin") { Admin(navController) }
        composable("chat/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            ChatPrivado(usuarioId, navController)
        }
        composable("gestorChat") { GestorChat(navController) }
        composable("modificarObra/{obraId}") { backStackEntry ->
            val obraId = backStackEntry.arguments?.getString("obraId") ?: ""
            ModificarObraScreen(navController, obraId)
        }
        composable("estadisticas/{usuarioId}") { backStackEntry ->
            val usuarioId = backStackEntry.arguments?.getString("usuarioId") ?: ""
            EstadisticasScreen(navController, usuarioId)
        }
        composable("carrito") { CarritoScreen(navController) }
        composable("subastas") { SubastasPantalla(navController) }
        composable("detalleSubasta/{id}") { back->
            DetalleSubastaScreen(back.arguments!!.getString("id")!!, navController)
        }
        composable("editarSubasta/{id?}") { back->
            EditarSubastaScreen(back.arguments?.getString("id"), navController)
        }
        composable("resultadoSubasta/{id}") { back->
            ResultadoSubastaScreen(back.arguments!!.getString("id")!!, navController)
        }
        composable("ajustes") { AjustesPantalla(navController) }
        composable("editarPerfil") { EditarPerfilScreen(navController) }

    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val contexto = LocalContext.current

    // Estado para obtener siempre el usuario actualizado
    val usuarioId by rememberUpdatedState(newValue = Util.obtenerDatoShared(contexto, "id"))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {

            var rutas = listOf(
                "principal",
                "buscador",
                "subastas"
            )

            if (currentRoute in rutas) {
                TopBarConCarrito(navController)
            }
        },
        bottomBar = {

            var rutas = listOf(
                "login",
                "registro"
            )

            if (currentRoute !in rutas) {
                BottomNavigationBar(navController, usuarioId)
            }
        }
    ) { innerPadding ->
        AppNavegacion(navController, innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarConCarrito(nav: NavController) {
    val contexto = LocalContext.current
    val esAdmin = Util.obtenerDatoSharedBoolean(contexto, "admin")

    Box(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { },
            navigationIcon = {
                if (esAdmin) {
                    Button(
                        onClick = { nav.navigate("admin") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3479BF),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Admin")
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = { nav.navigate("carrito") },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.publicar),
                        contentDescription = "Carrito",
                        modifier = Modifier.size(34.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Texto centrado horizontalmente y ligeramente más abajo (p.ej. centrado a altura de iconos)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 35.dp), // AJUSTA ESTE VALOR PARA BAJARLO MÁS SI QUIERES
            contentAlignment = Alignment.Center
        ) {
            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color(0xFF52BF34))) { append("art") }
                    withStyle(style = SpanStyle(color = Color(0xFF3479BF))) { append("arte") }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = chango,
                modifier = Modifier.clickable { nav.navigate("principal") }
            )
        }
    }
}





@Composable
fun BottomNavigationBar(navController: NavController, usuarioId: String?) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val items = listOf(
        BottomNavItem("principal", R.drawable.principal, "principal"),
        BottomNavItem("buscador", R.drawable.buscar, "buscador"),
        BottomNavItem("publicar", R.drawable.publicar, "crearObra"),
        BottomNavItem("chat", R.drawable.chat, "gestorChat"),
        BottomNavItem("perfil", R.drawable.usuario, "perfil/${usuarioId ?: ""}")
    )

    NavigationBar (modifier = Modifier.height(70.dp)) {
        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.height(30.dp),
                icon = {
                    Icon(
                        painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.fillMaxSize()
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
