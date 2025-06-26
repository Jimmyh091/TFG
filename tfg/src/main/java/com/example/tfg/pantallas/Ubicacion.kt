package com.example.tfg.pantallas
/*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController

@OptIn(ExperimentalPermissionsApi::class, MapsComposeExperimentalApi::class)
@Composable
fun PantallaUbicacion(
    onUbicacionSeleccionada: (LatLng) -> Unit,
    nav: NavHostController? = null
) {
    val contexto = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedLocationProviderClient = remember { LocationServices.getFusedLocationProviderClient(contexto) }

    val locationPermissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    var miUbicacion by remember { mutableStateOf<LatLng?>(null) }
    var ubicacionSeleccionada by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.hasPermission) {
        if (locationPermissionState.hasPermission) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    miUbicacion = LatLng(location.latitude, location.longitude)
                    ubicacionSeleccionada = miUbicacion
                } else {
                    Toast.makeText(contexto, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Seleccionar ubicación") })
        },
        floatingActionButton = {
            Button(onClick = {
                ubicacionSeleccionada?.let {
                    onUbicacionSeleccionada(it)
                    nav?.popBackStack()
                }
            }) {
                Text("Confirmar ubicación")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (miUbicacion != null) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(miUbicacion!!, 15f)
                    },
                    onMapClick = {
                        ubicacionSeleccionada = it
                    },
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionState.hasPermission
                    )
                ) {
                    ubicacionSeleccionada?.let {
                        Marker(state = MarkerState(position = it), title = "Ubicación seleccionada")
                    }
                }
            } else {
                Text("Cargando mapa...", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
*/