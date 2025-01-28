package com.example.quizec.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.example.quizec.utils.LocationUtils
import com.example.quizec.utils.LocationUtils.fetchLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun JoinQuizScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val usersViewModel = UsersViewModel()

    var codigoQuiz by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

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

    // FORMULA DE HAVERSINE
    // Función para calcular la distancia entre dos puntos geográficos (latitud, longitud)
    fun calculateDistance(location: String, quizLat: Double, quizLng: Double): Float {
        // Parsear las coordenadas de la ubicación actual
        val userLocation = location.split(",")
        val userLat = userLocation[0].substringAfter("Lat: ").toDouble()
        val userLng = userLocation[1].substringAfter("Lng: ").toDouble()

        val earthRadius = 6371 // Radio de la tierra en km
        val dLat = Math.toRadians(quizLat - userLat) // Convertir a radianes
        val dLng = Math.toRadians(quizLng - userLng)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(userLat)) * cos(Math.toRadians(quizLat)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c // Distancia en km

        //val distanceInMeters = (distance * 1000).toFloat() // Convertir a metros

        // Imprimir la distancia en consola para depuración
        println("Distancia entre el usuario y el quiz: $distance km")
        //return distanceInMeters

        return distance.toFloat() // Devolver la distancia en km
    }

    // Función para unirse al quiz, incluye la lógica de validación de código y distancia
    fun unirseAlQuiz() {
        if (codigoQuiz.isEmpty()) {
            errorMessage = context.getString(R.string.por_favor_ingrese_el_c_digo_del_quiz)
            return
        }

        loading = true // Mostrar indicador de carga

        // Ejecutar la lógica en una corrutina
        coroutineScope.launch {
            try {
                // devuelve (lat, lng, radius)
                val datosQuiz = quizViewModel.obtenerDatosDelQuiz(codigoQuiz)
                Log.d("JoinQuizScreen", "Datos del quiz: $datosQuiz")

                if (datosQuiz == null) {
                    errorMessage = context.getString(R.string.error_al_obtener_los_datos_del_quiz)
                    loading = false
                    return@launch
                }

                val immediateAccess = quizViewModel.obtenerImmediateAccess(codigoQuiz)
                Log.d("JoinQuizScreen", "Modo Inmediato: $immediateAccess")

                // Obtener datos del quiz (lat, lng, radius)
                val (quizLat, quizLng, quizRadius) = datosQuiz

                // Si la latitud y longitud son 0, no comprobar ubicación
                if (quizLat == 0.0 && quizLng == 0.0) {
                    //se grega al user directamente y se le convierte en participante
                    usersViewModel.agregarUsuarioAQuiz(codigoQuiz)
                    quizViewModel.actualizarRolUsuario(Rol.PARTICIPANTE.toString()) { errorMessage ->
                        if (errorMessage == null) {
                            Log.d("JoinQuizScreen", "Rol actualizado a 'Participante'.")
                        } else {
                            Log.e("JoinQuizScreen", "Error al actualizar el rol: $errorMessage")
                        }
                    }
                    // Si el acceso inmediato es verdadero, navegar al quiz
                    if (immediateAccess == true) {
                        navController.navigate("user_quiz/$codigoQuiz")
                    } else {
                        navController.navigate("waiting_screen/$codigoQuiz")
                    }
                    loading = false
                    return@launch

                }else {
                // Obtener la ubicación del usuario y calcular la distancia
                fetchLocation(context, fusedLocationClient) { location ->
                    Log.d("Geolocalización", "Ubicación actual: $location")

                    // Calcular la distancia entre el usuario y el quiz
                    val distance = calculateDistance(location, quizLat, quizLng)
                    Log.d("JoinQuizScreen", "Distancia al quiz: $distance")

                    // Verificar si el usuario está dentro del radio del quiz
                    if (distance <= quizRadius) {
                        // Agregar usuario al quiz solo si la ubicación es válida
                        usersViewModel.agregarUsuarioAQuiz(codigoQuiz)

                        quizViewModel.actualizarRolUsuario(Rol.PARTICIPANTE.toString()) { errorMessage ->
                            if (errorMessage == null) {
                                Log.d("JoinQuizScreen", "Rol actualizado a 'Participante'.")
                            } else {
                                Log.e("JoinQuizScreen", "Error al actualizar el rol: $errorMessage")
                            }
                        }

                        // Si el acceso inmediato es verdadero, navegar direct al quiz
                        if (immediateAccess == true) {
                            navController.navigate("user_quiz/$codigoQuiz")
                        } else {
                            navController.navigate("waiting_screen/$codigoQuiz")
                        }
                    } else { // Si el usuario está fuera del radio, mostrar mensaje de error
                        errorMessage =
                            context.getString(R.string.no_est_s_dentro_del_radio_del_quiz)
                    }
                    loading = false
                }
            }
            } catch (e: Exception) {
                loading = false
                errorMessage = context.getString(R.string.hubo_un_error_al_verificar_el_acceso)
            }
        }
    }



    // Interfaz de usuario
    Box(
        modifier = Modifier
            .fillMaxSize()  // Asegura que el Box ocupe toda la pantalla
            .background(colorResource(id = R.color.background_color))  // Establecer el color de fondo
            .wrapContentSize(Alignment.Center)  // Centrar el contenido
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(stringResource(R.string.unirse_a_un_quiz), style = MaterialTheme.typography.titleLarge)

            Text(stringResource(R.string.c_digo_del_quiz), style = MaterialTheme.typography.bodyMedium)
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
                    // Si se tiene permiso de ubicación, obtener la ubicación y unirse al quiz
                    if (locationPermissionGranted) {
                        fetchLocation(context, fusedLocationClient) { location ->
                            Log.d("Geolocalización", "Ubicación actual: $location")
                            unirseAlQuiz() // Asegúrate de llamar a unirseAlQuiz en una corrutina
                        }
                    } else {
                        Log.e("Permisos", "Permisos de ubicación no concedidos.")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                )
            ) {
                Text(stringResource(R.string.unirse_al_quiz))
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            }
            // Mostrar indicador de carga si se está esperando
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.volver))
            }
        }
    }
}



