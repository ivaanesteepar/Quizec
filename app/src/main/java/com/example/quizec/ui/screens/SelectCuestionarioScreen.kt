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
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.quizec.R
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.utils.AMovServer
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        // Si está en modo Landscape, usamos la función para pantalla Landscape
        SelectCuestionarioScreenLandscape(navController, quizViewModel, questionsViewModel)
    } else {
        // Si está en modo Portrait, usamos la función para pantalla Portrait
        SelectCuestionarioScreenPortrait(navController, quizViewModel, questionsViewModel)
    }
}

@Composable
fun SelectCuestionarioScreenPortrait(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    questionsViewModel: QuestionsViewModel
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val cuestionariosState = questionsViewModel.cuestionariosState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showDuplicateConfirmationDialog by remember { mutableStateOf(false) }
    var cuestionarioToDelete by remember { mutableStateOf<Cuestionario?>(null) }
    var cuestionarioToDuplicate by remember { mutableStateOf<Cuestionario?>(null) }
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
        permissionMessage =
            stringResource(R.string.no_se_ha_concedido_el_permiso_para_acceder_a_las_im_genes)
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

    Box(modifier = Modifier
        .background(colorResource(id = R.color.background_color))
        .fillMaxSize()
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
                    text = stringResource(R.string.seleccione_un_cuestionario),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .height(640.dp)
                ) {
                    items(cuestionariosState.value) { cuestionario ->
                        // Obtener el valor de isUsed para cada cuestionario
                        val isUsed = estadosIsUsed.value[cuestionario.id] ?: false
                        val isSelected = questionsViewModel.selectedCuestionario == cuestionario.id

                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    enabled = !isUsed, // Deshabilitar si isUsed es true
                                    onCheckedChange = {
                                        questionsViewModel.toggleCuestionarioSelection(cuestionario.id)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )

                                Column(modifier = Modifier
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

                                    Spacer(modifier = Modifier.height(5.dp))
                                }

                                // Si está usado, mostramos Duplicar al lado
                                if (isUsed) {
                                    Button(
                                        onClick = {
                                            showDuplicateConfirmationDialog = true
                                            cuestionarioToDuplicate = cuestionario
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                                        ),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(text = stringResource(R.string.duplicar))
                                    }
                                }
                            }

                            // Si el cuestionario no está usado, botones Editar y Eliminar y Duplicar se ponen debajo
                            if (!isUsed) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp), // Espacio entre los botones
                                    modifier = Modifier.padding(start = 8.dp, top = 5.dp) // Un poco de espacio desde la información
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate("editCuestionario/${cuestionario.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                                        ),
                                        modifier = Modifier.weight(1f) // Hace que los botones ocupen el mismo espacio
                                    ) {
                                        Text(text = stringResource(R.string.editar))
                                    }

                                    Button(
                                        onClick = {
                                            showDeleteConfirmationDialog = true
                                            cuestionarioToDelete = cuestionario
                                        },
                                        modifier = Modifier
                                            .weight(1f) // Hace que los botones ocupen el mismo espacio
                                            .padding(start = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red // Aplicamos el color de fondo del botón
                                        ),
                                    ) {
                                        Text(text = stringResource(R.string.eliminar))
                                    }

                                    Button(
                                        onClick = {
                                            showDuplicateConfirmationDialog = true
                                            cuestionarioToDuplicate = cuestionario
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                                        ),
                                        modifier = Modifier
                                            .weight(1.1f) // Hace que los botones ocupen el mismo espacio
                                            .padding(start = 8.dp)
                                    ) {
                                        Text(text = stringResource(R.string.duplicar))
                                    }
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

                    // Verificar si el cuestionario seleccionado es válido
                    if (selectedCuestionarioId != null) {
                        val cuestionarioSeleccionado = questionsViewModel.obtenerCuestionarioPorId(selectedCuestionarioId)
                        val codigoQuiz = cuestionarioSeleccionado?.id // Obtener el código del cuestionario

                        if (codigoQuiz != null) {
                            userId?.let {
                                CoroutineScope(Dispatchers.Main).launch {
                                    try {
                                        val locationRestricted = quizViewModel.obtenerLocationRestricted(codigoQuiz)
                                        val immediateAccess = quizViewModel.obtenerImmediateAccess(codigoQuiz)

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
                                                            if (immediateAccess == true){
                                                                navController.navigate("creator_quiz/$selectedCuestionarioId")
                                                            }else{
                                                                navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                            }

                                                        } else {
                                                            Log.e("SelectCuestionario", "Error al actualizar las coordenadas: $updateError")
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.e("SelectCuestionario", "Error al actualizar el rol: $errorMessage")
                                            }
                                        }

                                        if (immediateAccess == true){
                                            quizViewModel.actualizarIsQuizIniciado(codigoQuiz) { exito ->
                                                if (exito) {
                                                    codigoQuiz?.let { quizId ->
                                                        quizViewModel.actualizarIsUsed(quizId, true) { success ->
                                                            if (success) {
                                                                println("Campo isUsed actualizado a true.")
                                                                navController.navigate("creator_quiz/$codigoQuiz")
                                                            } else {
                                                                println("Error al actualizar el campo isUsed.")
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    println("Error al actualizar el estado del quiz.")
                                                }
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(text = stringResource(R.string.continuar))
            }

            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.volver))
            }
        }
    }

    // Diálogo de confirmación de duplicado
    if (showDuplicateConfirmationDialog && cuestionarioToDuplicate != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateConfirmationDialog = false },
            title = { Text(stringResource(R.string.confirmar_duplicado)) },
            text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_duplicar_este_cuestionario)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuestionarioToDuplicate?.let { cuestionario ->
                            // Lógica para duplicar el cuestionario
                            if (userId != null) {
                                questionsViewModel.duplicarCuestionario(cuestionario.id)
                            }
                        }
                        showDuplicateConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.si))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDuplicateConfirmationDialog = false }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteConfirmationDialog && cuestionarioToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(stringResource(R.string.confirmar_eliminaci_n)) },
            text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_eliminar_este_cuestionario)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuestionarioToDelete?.let { cuestionario ->
                            // Verifica si el cuestionario tiene una imagen asociada
                            if (!cuestionario.imagen.isNullOrEmpty()) {
                                // Extrae el nombre del archivo de la URL
                                val imageUrl = cuestionario.imagen.toString()
                                val trimmedUrl = imageUrl.trimEnd('/')
                                val segments = trimmedUrl.split("/")
                                val fileName = segments.lastOrNull() ?: ""

                                // Llama a la función para eliminar el archivo en el servidor
                                AMovServer.asyncDeleteFileFromServer(
                                    fileName = fileName,
                                    serverUrl = trimmedUrl,
                                    onResult = { result ->
                                        if (result) {
                                            // Elimina también el cuestionario después de borrar la imagen
                                            questionsViewModel.eliminarCuestionario(cuestionario.id)
                                        } else {
                                            // Manejo del error (por ejemplo, mostrando un mensaje al usuario)
                                            Log.e("DeleteError", "Error al eliminar la imagen: $imageUrl")
                                        }
                                    }
                                )
                            } else {
                                // Si no hay imagen, simplemente elimina el cuestionario
                                questionsViewModel.eliminarCuestionario(cuestionario.id)
                            }
                        }
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.si))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}

@Composable
fun SelectCuestionarioScreenLandscape(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    questionsViewModel: QuestionsViewModel
) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val cuestionariosState = questionsViewModel.cuestionariosState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showDuplicateConfirmationDialog by remember { mutableStateOf(false) }
    var cuestionarioToDelete by remember { mutableStateOf<Cuestionario?>(null) }
    var cuestionarioToDuplicate by remember { mutableStateOf<Cuestionario?>(null) }
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
        permissionMessage = stringResource(R.string.no_se_ha_concedido_el_permiso_para_acceder_a_las_im_genes)
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
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Text(
                    text = stringResource(R.string.seleccione_un_cuestionario),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .height(150.dp)
                ) {
                    items(cuestionariosState.value) { cuestionario ->
                        // Obtener el valor de isUsed para cada cuestionario
                        val isUsed = estadosIsUsed.value[cuestionario.id] ?: false
                        val isSelected = questionsViewModel.selectedCuestionario == cuestionario.id

                        // La estructura Row solo para la parte de la Checkbox y los textos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                enabled = !isUsed, // Deshabilitar si isUsed es true
                                onCheckedChange = {
                                    questionsViewModel.toggleCuestionarioSelection(cuestionario.id)
                                },
                                modifier = Modifier.size(20.dp)
                            )

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
                            }
                        }

                        // La estructura Row para los botones debajo del primer Row (con título y ID)
                        Row(
                            modifier = Modifier
                                .padding(start = 8.dp, top = 5.dp) // Espacio adicional entre el ID y los botones
                        ) {
                            // Botones solo visibles si no está usado
                            if (!isUsed) {
                                Button(
                                    onClick = {
                                        navController.navigate("editCuestionario/${cuestionario.id}")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonColor // Aplicamos el color de fondo del botón
                                    ),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(text = stringResource(R.string.editar))
                                }

                                Button(
                                    onClick = {
                                        showDeleteConfirmationDialog = true
                                        cuestionarioToDelete = cuestionario
                                    },
                                    modifier = Modifier.padding(end = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Red // Aplicamos el color de fondo del botón
                                    ),
                                ) {
                                    Text(text = stringResource(R.string.eliminar))
                                }
                            }

                            // Botón de duplicar visible siempre
                            Button(
                                onClick = {
                                    showDuplicateConfirmationDialog = true
                                    cuestionarioToDuplicate = cuestionario
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor // Aplicamos el color de fondo del botón
                                ),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(text = stringResource(R.string.duplicar))
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
                                        val immediateAccess = quizViewModel.obtenerImmediateAccess(codigoQuiz)

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
                                                            if (immediateAccess == true){
                                                                navController.navigate("creator_quiz/$selectedCuestionarioId")
                                                            }else{
                                                                navController.navigate("waiting_screen/$selectedCuestionarioId")
                                                            }

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(text = stringResource(R.string.continuar))
            }

            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.volver))
            }
        }
    }

    // Diálogo de confirmación de duplicado
    if (showDuplicateConfirmationDialog && cuestionarioToDuplicate != null) {
        AlertDialog(
            onDismissRequest = { showDuplicateConfirmationDialog = false },
            title = { Text(stringResource(R.string.confirmar_duplicado)) },
            text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_duplicar_este_cuestionario)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuestionarioToDuplicate?.let { cuestionario ->
                            // Lógica para duplicar el cuestionario
                            if (userId != null) {
                                questionsViewModel.duplicarCuestionario(cuestionario.id)
                            }
                        }
                        showDuplicateConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.si))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDuplicateConfirmationDialog = false }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteConfirmationDialog && cuestionarioToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text(stringResource(R.string.confirmar_eliminaci_n)) },
            text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_eliminar_este_cuestionario)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        cuestionarioToDelete?.let { cuestionario ->
                            // Verifica si el cuestionario tiene una imagen asociada
                            if (!cuestionario.imagen.isNullOrEmpty()) {
                                // Extrae el nombre del archivo de la URL
                                val imageUrl = cuestionario.imagen.toString()
                                val trimmedUrl = imageUrl.trimEnd('/')
                                val segments = trimmedUrl.split("/")
                                val fileName = segments.lastOrNull() ?: ""

                                // Llama a la función para eliminar el archivo en el servidor
                                AMovServer.asyncDeleteFileFromServer(
                                    fileName = fileName,
                                    serverUrl = trimmedUrl,
                                    onResult = { result ->
                                        if (result) {
                                            // Elimina también el cuestionario después de borrar la imagen
                                            questionsViewModel.eliminarCuestionario(cuestionario.id)
                                        } else {
                                            // Manejo del error (por ejemplo, mostrando un mensaje al usuario)
                                            Log.e("DeleteError", "Error al eliminar la imagen: $imageUrl")
                                        }
                                    }
                                )
                            } else {
                                // Si no hay imagen, simplemente elimina el cuestionario
                                questionsViewModel.eliminarCuestionario(cuestionario.id)
                            }
                        }
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.si))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}


