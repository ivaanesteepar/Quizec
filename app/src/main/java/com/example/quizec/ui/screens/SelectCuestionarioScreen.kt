package com.example.quizec.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.example.quizec.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SelectCuestionarioScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    questionsViewModel: QuestionsViewModel = viewModel()
) {
    val context = LocalContext.current

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val cuestionariosState = questionsViewModel.cuestionariosState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var cuestionarioToDelete by remember { mutableStateOf<Cuestionario?>(null) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var latitudActual by remember { mutableStateOf<Double?>(null) }
    var longitudActual by remember { mutableStateOf<Double?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationPermissionGranted = isGranted
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

            // Aqui lo que se va a hacer es obtener la ubicacion actual del usuario cada 1 segundo
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(1000)  // 1 second
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
            quizViewModel.cargarImagenesCuestionariosUsuario(it) // Cargar las imágenes de los cuestionarios del usuario desde la base de datos
            questionsViewModel.cargarCuestionariosUsuario(it) // Cargar los cuestionarios
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (cuestionariosState.value.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "SELECCIONE UN CUESTIONARIO",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .height(380.dp)
                ) {
                    items(cuestionariosState.value) { cuestionario ->
                        val isSelected = questionsViewModel.selectedCuestionario == cuestionario.id

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox( //// Mostrar checkbox para seleccionar el cuestionario
                                checked = isSelected,
                                onCheckedChange = {
                                    questionsViewModel.toggleCuestionarioSelection(cuestionario.id)
                                },
                                modifier = Modifier.size(20.dp)
                            )

                            Column( // Mostrar título, ID y URL del cuestionario
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = cuestionario.titulo,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "ID: ${cuestionario.id}",
                                    style = MaterialTheme.typography.labelSmall
                                )

                                cuestionario.imagen?.let { imageUrl ->
                                    Text(
                                        text = "URL de la imagen: $imageUrl",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    val imageBitmap by remember(imageUrl) { // Cargar y mostrar la imagen desde URI
                                        mutableStateOf(loadImageFromUri(context, imageUrl))
                                    }

                                    imageBitmap?.let { bitmap ->
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Imagen del cuestionario",
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .size(100.dp)
                                        )
                                    }
                                }
                            }

                            Button( // Botón de Edit
                                onClick = {
                                    navController.navigate("editCuestionario/${cuestionario.id}")
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(text = "Editar")
                            }

                            Button( // Botón de Eliminación
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

        Column( // Botones inferiores de "Continuar" y "Volver"
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

                        // Si el código del cuestionario existe
                        if (codigoQuiz != null) {
                            // Llamar a la función suspendida dentro de una corrutina
                            userId?.let {
                                // Lanzar una corrutina para obtener el valor de locationRestricted
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        val locationRestricted = quizViewModel.obtenerLocationRestricted(codigoQuiz)

                                        // Continuar con el flujo de trabajo
                                        quizViewModel.actualizarRolUsuario(
                                            nuevoRol = Rol.CREADOR.toString()
                                        ) { errorMessage ->
                                            if (errorMessage == null) {
                                                // Verificar si la ubicación está restringida
                                                if (locationRestricted == true) {
                                                    // Si locationRestricted es true, obtenemos la ubicación
                                                    if (locationPermissionGranted) {
                                                        LocationUtils.fetchLocation(context, fusedLocationClient) { location ->
                                                            Log.d("Geolocalización", "Ubicación actual: $location")

                                                            quizViewModel.actualizarCoordenadasCuestionario(
                                                                selectedCuestionarioId,
                                                                latitudActual,
                                                                longitudActual
                                                            ) { updateError ->
                                                                if (updateError == null) {
                                                                    Log.d("SelectCuestionario", "Coordenadas actualizadas con éxito.")
                                                                    navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                                } else {
                                                                    Log.e("SelectCuestionario", "Error al actualizar las coordenadas: $updateError")
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    // Si locationRestricted es false, se asignan las coordenadas a 0
                                                    val latitud = 0.0
                                                    val longitud = 0.0

                                                    quizViewModel.actualizarCoordenadasCuestionario(
                                                        selectedCuestionarioId,
                                                        latitud,
                                                        longitud
                                                    ) { updateError ->
                                                        if (updateError == null) {
                                                            Log.d("SelectCuestionario", "Coordenadas actualizadas con éxito.")
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
                onClick = { navController.navigate("home") }, // Navegar hacia atrás
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

// Función para cargar la imagen desde un URI usando ContentResolver
fun loadImageFromUri(context: Context, imageUri: String): Bitmap? {
    return try {
        val uri = Uri.parse(imageUri)
        val contentResolver: ContentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
