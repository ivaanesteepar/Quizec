package com.example.quizec.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.example.quizec.utils.LocationUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


@Composable
fun SelectCuestionarioScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    questionsViewModel: QuestionsViewModel
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val cuestionariosState = questionsViewModel.cuestionariosState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var cuestionarioToDelete by remember { mutableStateOf<Cuestionario?>(null) }
    val estadosIsUsed = remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var latitudActual by remember { mutableStateOf<Double?>(null) }
    var longitudActual by remember { mutableStateOf<Double?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
    }

    // Verificar si se tiene permiso para acceder a las imágenes
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    // Si no se tiene permiso, lanzamos la solicitud
    if (!hasPermission) {
        launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    // Mostrar mensaje si el permiso no se ha concedido
    var permissionMessage by remember { mutableStateOf("") }
    if (!hasPermission) {
        permissionMessage = "No se ha concedido el permiso para acceder a las imágenes."
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted = true

            // Aquí lo que se va a hacer es obtener la ubicación actual del usuario cada 1 segundo
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(1000)  // 1 segundo
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val location = locationResult.lastLocation
                    if (location != null) {
                        latitudActual = location.latitude
                    }
                    if (location != null) {
                        longitudActual = location.longitude
                    }
                    Log.d("Location", "Latitud: $latitudActual, Longitud: $longitudActual")
                }
            }
            // Start receiving location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    LaunchedEffect(userId) {
        userId?.let {
            estadosIsUsed.value = quizViewModel.obtenerEstadosIsUsed(it) // Esto debería actualizar el estado reactivo
            estadosIsUsed.value.forEach { (codigoQuiz, isUsed) ->
                Log.d("Estado isUsed", "Cuestionario ID: $codigoQuiz, isUsed: $isUsed")
            }
        }
    }

    LaunchedEffect(userId) {
        userId?.let {
            quizViewModel.cargarImagenesCuestionariosUsuario(it) // Cargar las imágenes de los cuestionarios del usuario desde la base de datos
            questionsViewModel.cargarCuestionariosUsuario(it) // Cargar los cuestionarios
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cuestionariosState.value.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "SELECCIONE UN CUESTIONARIO",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier.padding(top = 26.dp).height(500.dp)
                ) {
                    items(cuestionariosState.value) { cuestionario ->
                        // Obtener el valor de isUsed para cada cuestionario
                        val isUsed = estadosIsUsed.value[cuestionario.id] ?: false
                        val isSelected = questionsViewModel.selectedCuestionario == cuestionario.id

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    questionsViewModel.toggleCuestionarioSelection(cuestionario.id)
                                },
                                modifier = Modifier.size(20.dp)
                            )

                            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                Text(
                                    text = cuestionario.titulo,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "ID: ${cuestionario.id}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Muestra la imagen usando la URI almacenada
                                cuestionario.imagen?.let { imageUri ->
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(Uri.parse(imageUri)) // Convierte la cadena en una URI válida
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Imagen del cuestionario",
                                        modifier = Modifier
                                            .size(150.dp)
                                            .border(1.dp, Color.Gray)
                                            .padding(8.dp),
                                        contentScale = ContentScale.Crop // Ajusta el contenido para que no se deforme
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    navController.navigate("editCuestionario/${cuestionario.id}")
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(text = "Editar")
                            }

                            if (!isUsed) {
                                Button(
                                    onClick = {
                                        showDeleteConfirmationDialog = true
                                        cuestionarioToDelete = cuestionario
                                    },
                                    modifier = Modifier.padding(start = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text(text = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    val selectedCuestionarioId = questionsViewModel.selectedCuestionario
                    println("Cuestionario seleccionado: $selectedCuestionarioId")

                    // Verificar si el cuestionario seleccionado es válido
                    if (selectedCuestionarioId != null) {
                        val cuestionarioSeleccionado = questionsViewModel.obtenerCuestionarioPorId(selectedCuestionarioId)
                        val codigoQuiz = cuestionarioSeleccionado?.id // Obtener el código del cuestionario

                        if (codigoQuiz != null) {
                            userId?.let {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        val locationRestricted = quizViewModel.obtenerLocationRestricted(codigoQuiz)

                                        quizViewModel.actualizarRolUsuario(nuevoRol = Rol.CREADOR.toString()) { errorMessage ->
                                            if (errorMessage == null) {
                                                if (locationRestricted == true) {
                                                    if (locationPermissionGranted) {
                                                        LocationUtils.fetchLocation(context, fusedLocationClient) { location ->
                                                            quizViewModel.actualizarCoordenadasCuestionario(
                                                                selectedCuestionarioId,
                                                                latitudActual,
                                                                longitudActual
                                                            ) { updateError ->
                                                                if (updateError == null) {
                                                                    navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                                } else {
                                                                    Log.e("SelectCuestionario", "Error al actualizar las coordenadas: $updateError")
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val latitud = 0.0
                                                    val longitud = 0.0
                                                    quizViewModel.actualizarCoordenadasCuestionario(
                                                        selectedCuestionarioId,
                                                        latitud,
                                                        longitud
                                                    ) { updateError ->
                                                        if (updateError == null) {
                                                            navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                        } else {
                                                            Log.e("SelectCuestionario", "Error al actualizar las coordenadas: $updateError")
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("SelectCuestionario", "Error al actualizar el rol: $errorMessage")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SelectCuestionario", "Error al obtener locationRestricted: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            Log.e("SelectCuestionario", "No se pudo obtener el código del cuestionario")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "Continuar")
            }

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Volver")
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteConfirmationDialog && cuestionarioToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este cuestionario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuestionarioToDelete?.let {
                            questionsViewModel.eliminarCuestionario(it.id)
                        }
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}

