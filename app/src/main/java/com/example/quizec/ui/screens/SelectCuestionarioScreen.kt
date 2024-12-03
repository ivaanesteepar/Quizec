package com.example.quizec.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SelectCuestionarioScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    questionsViewModel: QuestionsViewModel = viewModel(),
    context: Context // Pasamos el contexto para usar ContentResolver
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    // Estado de los cuestionarios
    val cuestionariosState = questionsViewModel.cuestionariosState.collectAsState()
    // Estado del diálogo de confirmación de eliminación
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var cuestionarioToDelete by remember { mutableStateOf<Cuestionario?>(null) }

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)
    var latitudActual by remember { mutableStateOf<Double?>(null) }
    var longitudActual by remember { mutableStateOf<Double?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    var locationCancellationTokenSource by remember { mutableStateOf<CancellationTokenSource?>(null) }

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

        if (!hasPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            locationPermissionGranted = true
        }
    }

    // Cargar los cuestionarios y sus imágenes desde la base de datos cuando la pantalla se muestra
    LaunchedEffect(userId) {
        userId?.let {
            quizViewModel.cargarImagenesCuestionariosUsuario(it) // Cargar las imágenes de los cuestionarios del usuario desde la base de datos
            questionsViewModel.cargarCuestionariosUsuario(it) // Cargar los cuestionarios
        }
    }

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitudActual = location.latitude
                longitudActual = location.longitude
                Log.d("Location", "Latitud: $latitudActual, Longitud: $longitudActual")
            } else {
                Log.e("Location", "No se pudo obtener la ubicación actual.")
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (cuestionariosState.value.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp)) // Indicador de carga
        }
        else {
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

                // LazyColumn para mostrar los cuestionarios
                LazyColumn(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .height(380.dp) // Esto hará que ocupe el espacio disponible
                ) {
                    items(cuestionariosState.value) { cuestionario ->
                        val isSelected = questionsViewModel.selectedCuestionario == cuestionario.id

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Mostrar checkbox para seleccionar el cuestionario
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    questionsViewModel.toggleCuestionarioSelection(cuestionario.id)
                                },
                                modifier = Modifier.size(20.dp)
                            )

                            // Mostrar título, ID y URL del cuestionario
                            Column(
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

                                // Mostrar la URL de la imagen
                                cuestionario.imagen?.let { imageUrl ->
                                    Text(
                                        text = "URL de la imagen: $imageUrl",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    // Cargar y mostrar la imagen desde URI
                                    val imageBitmap by remember(imageUrl) {
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

                            // Botón de Edición
                            Button(
                                onClick = {
                                    navController.navigate("editCuestionario/${cuestionario.id}")
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(text = "Editar")
                            }

                            // Botón de Eliminación
                            Button(
                                onClick = {
                                    // Mostrar el diálogo de confirmación de eliminación
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

        // Los botones de "Continuar" y "Volver" se colocan en la parte inferior
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            // Botón de "Continuar"
            Button(
                onClick = {
                    val selectedCuestionarioId = questionsViewModel.selectedCuestionario
                    if (selectedCuestionarioId != null) {
                        if (userId != null) {
                            // Paso 1: Actualizar el rol del usuario
                            quizViewModel.actualizarRolUsuario(
                                nuevoRol = Rol.CREADOR.toString()
                            ) { errorMessage ->
                                if (errorMessage == null) {
                                    // Paso 2: Si la actualización del rol es exitosa, obtener la ubicación actual
                                    if (locationPermissionGranted) {
                                        // Obtener la ubicación más reciente
                                        fetchLocation(fusedLocationClient) { location ->
                                            // Imprimir el log solo si la ubicación se obtiene correctamente
                                            Log.d("Geolocalización", "Ubicación actual: $location")

                                            // Paso 3: Actualizar las coordenadas del cuestionario en Firebase
                                            quizViewModel.actualizarCoordenadasCuestionario(
                                                selectedCuestionarioId,
                                                latitudActual,
                                                longitudActual
                                            ) { updateError ->
                                                if (updateError == null) {
                                                    // Si no hubo error en la actualización de coordenadas
                                                    Log.d("SelectCuestionario", "Coordenadas actualizadas con éxito.")
                                                    // Paso 4: Navegar a la waiting room
                                                    navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                } else {
                                                    // Manejo de error en la actualización de coordenadas
                                                    Log.e("SelectCuestionario", "Error al actualizar las coordenadas: $updateError")
                                                }
                                            }
                                        }
                                    } else {
                                        Log.e("Permisos", "Permisos de ubicación no concedidos.")
                                    }
                                } else {
                                    // Manejo de error al actualizar el rol
                                    Log.e("SelectCuestionario", "Error al actualizar el rol: $errorMessage")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "Continuar")
            }



            // Botón de "Volver"
            Button(
                onClick = {
                    navController.popBackStack() // Navegar hacia atrás
                },
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
                            println("Id del cuestionario a eliminar: ${it.id}")
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
                    onClick = {
                        showDeleteConfirmationDialog = false
                    }
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

