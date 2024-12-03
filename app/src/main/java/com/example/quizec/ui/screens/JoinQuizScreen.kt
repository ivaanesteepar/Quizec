package com.example.quizec.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun JoinQuizScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var codigoQuiz by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    //val coroutineScope = rememberCoroutineScope()

    // Solicitar permisos de ubicación
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    // Solicitar permisos al inicio
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        locationPermissionGranted = hasPermission
        if (!hasPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Función para calcular la distancia entre dos puntos geográficos (latitud, longitud)
    fun calculateDistance(location: String, quizLat: Double, quizLng: Double): Float {
        // Parsear las coordenadas de la ubicación actual
        val userLocation = location.split(",")
        val userLat = userLocation[0].substringAfter("Lat: ").toDouble()
        val userLng = userLocation[1].substringAfter("Lng: ").toDouble()

        val earthRadius = 6371 // Radio de la tierra en km
        val dLat = Math.toRadians(quizLat - userLat)
        val dLng = Math.toRadians(quizLng - userLng)
        val a = Math.sin(dLat / 2).pow(2) + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(quizLat)) * Math.sin(dLng / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = earthRadius * c // Distancia en km

        val distanceInMeters = (distance * 1000).toFloat() // Convertir a metros

        // Imprimir la distancia en consola para depuración
        println("Distancia entre el usuario y el quiz: $distanceInMeters metros")

        return distanceInMeters
    }


    // Función para unirse al quiz
    fun unirseAlQuiz() {
        if (codigoQuiz.isEmpty()) {
            errorMessage = "Por favor, ingrese el código del quiz."
        } else {
            loading = true

//            // Ejecutar la lógica en una corrutina
//            coroutineScope.launch {
//                try {
//                    val datosQuiz = quizViewModel.obtenerDatosDelQuiz(codigoQuiz)
//                    if (datosQuiz == null) {
//                        errorMessage = "Error al obtener los datos del quiz."
//                        loading = false
//                        return@launch
//                    }
//                    val immediateAccess = quizViewModel.obtenerImmediateAccess(codigoQuiz)
//
//                    // Obtener la ubicación del usuario y calcular la distancia
//                    fetchLocation(fusedLocationClient) { location ->
//                        Log.d("Geolocalización", "Ubicación actual: $location")
//
//                        val (quizLat, quizLng, quizRadius) = datosQuiz
//                        val distance = calculateDistance(location, quizLat, quizLng)
//                        if (distance <= quizRadius) {
//                            if (immediateAccess == null) {
//                                errorMessage = "Error al verificar el acceso."
//                            } else {
//                                if (immediateAccess) {
//                                    navController.navigate("user_quiz/$codigoQuiz")
//                                } else {
//                                    navController.navigate("waiting_screen/$codigoQuiz")
//                                }
//                            }
//                        } else {
//                            errorMessage = "No estás dentro del radio del quiz."
//                        }
//                        loading = false
//                    }
//                } catch (e: Exception) {
//                    loading = false
//                    errorMessage = "Hubo un error al verificar el acceso."
//                }
//            }
            //JIMENA con QuizViewModel nueva funcion de obtenerAccess
            // Llamada a la función del ViewModel que usa un callback
            quizViewModel.obtenerImmediateAccess(codigoQuiz) { immediateAccess ->
                loading = false
                println("immediateAccess: $immediateAccess")
                println("codigoQuiz: $codigoQuiz")

                // Verificación del resultado recibido a través del callback
                if (immediateAccess == null) {
                    errorMessage = "Error al verificar el acceso."
                } else if (immediateAccess == false) {
                    navController.navigate("waiting_screen/$codigoQuiz")
                } else {
                    navController.navigate("user_quiz/$codigoQuiz")
                }
            }
        }
    }

    // Interfaz de usuario
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Unirse a un Quiz", style = MaterialTheme.typography.titleLarge)

            Text("Código del Quiz", style = MaterialTheme.typography.bodyMedium)
            BasicTextField(
                value = codigoQuiz,
                onValueChange = { codigoQuiz = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(12.dp)
            )

            Button(
                onClick = {
                    if (locationPermissionGranted) {
                        fetchLocation(fusedLocationClient) { location ->
                            Log.d("Geolocalización", "Ubicación actual: $location")
                            unirseAlQuiz() // Asegúrate de llamar a unirseAlQuiz en una corrutina
                        }
                    } else {
                        Log.e("Permisos", "Permisos de ubicación no concedidos.")
                    }
                }
            ) {
                Text("Unirse al Quiz")
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Volver")
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (String) -> Unit
) {
    val cancellationTokenSource = CancellationTokenSource()
    fusedLocationClient.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, // Usar el nuevo valor constante
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location != null) {
            val locationString = "Lat: ${location.latitude}, Lng: ${location.longitude}"
            onLocationFetched(locationString)
        } else {
            Log.e("Geolocalización", "Ubicación no disponible.")
        }
    }.addOnFailureListener { exception ->
        Log.e("Geolocalización", "Error obteniendo la ubicación: ${exception.message}")
    }
}

