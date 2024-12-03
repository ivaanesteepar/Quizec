package com.example.quizec.ui.screens

import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.listacontactos.utils.location.LocationManagerHandler
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun CreateQuizScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    locationManagerHandler: LocationManagerHandler
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var tituloError by rememberSaveable { mutableStateOf(false) }  // Control de error del título
    var descripcionError by rememberSaveable { mutableStateOf(false) }  // Control de error de la descripción
    val nombreUsuario by quizViewModel.nombreUsuario.collectAsState()
    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    var immediateAccess by remember { mutableStateOf(false) }
    var locationRestricted by remember { mutableStateOf(false) }
    var immediateResults by remember { mutableStateOf(false) }

    // Estado para la ubicación
    var currentLocation by remember { mutableStateOf<Location?>(null) }


    // Si ya se ha creado el cuesitonario, se resetean los valores de creación
    var isCuestionarioCreado by remember { mutableStateOf(false) }
    if (isCuestionarioCreado) {
        quizViewModel.contadorPreguntas.value = 0
        titulo = ""
        descripcion = ""
        imageUri = null
        errorMessage = ""
        tituloError = false
        descripcionError = false
    }

    // Launcher for picking an image
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            quizViewModel.imageUri = uri
            Log.d("CreateQuizScreen", "Imagen seleccionada: $uri")
        } else {
            Log.d("CreateQuizScreen", "No se seleccionó ninguna imagen.")
        }
    }

    // Manejar el cambio de ubicación
    DisposableEffect(locationRestricted) {
        val locationListener = { location: Location ->
            currentLocation = location
        }

        locationManagerHandler.onLocation = locationListener

        if (locationRestricted) {
            locationManagerHandler.startLocationUpdates()
        } else {
            locationManagerHandler.stopLocationUpdates()
        }

        onDispose {
            locationManagerHandler.stopLocationUpdates()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuestionario", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de título
        Text("Título del Cuestionario", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = titulo,
            onValueChange = {
                titulo = it
                if (it.isNotEmpty()) {
                    tituloError = false
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    1.dp,
                    if (tituloError) Color.Red else Color.Gray
                )  // Cambiar color si hay error
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de descripción
        Text("Descripción", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                if (it.isNotEmpty()) {
                    descripcionError = false
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(
                    1.dp,
                    if (descripcionError) Color.Red else Color.Gray
                )  // Cambiar color si hay error
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para elegir imagen
        Text("Subir Imagen (Opcional)", style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = {
                pickImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        ) {
            Text("Seleccionar Imagen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar imagen seleccionada
        imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = "Imagen seleccionada",
                modifier = Modifier
                    .size(150.dp)
                    .border(1.dp, Color.Gray)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botones para otras acciones
        Button(
            onClick = { navController.navigate("make_questions") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Crear Preguntas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("select_cuestionario") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Seleccionar Cuestionario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Numero de preguntas: ${quizViewModel.contadorPreguntas.value}")

        // Mensaje de error si aplica
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para seleccionar preguntas
        Button(
            onClick = { navController.navigate("select_questions/$userUid") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Seleccionar Preguntas")
        }

        Spacer(modifier = Modifier.height(16.dp))


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Añadir algo de padding a la columna general para mejorar la legibilidad
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acceso Inmediato al Cuestionario",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge // Puede ajustar el estilo para mejorar la visibilidad
                )
                Switch(
                    checked = immediateAccess,
                    onCheckedChange = { newValue ->
                        immediateAccess = newValue // Cambia el valor de immediateAccess según la acción del usuario
                        println("El valor de immediateAccess es: $immediateAccess")
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )

            }

            Spacer(modifier = Modifier.height(12.dp)) // Más espacio entre los elementos

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Restringir por Ubicación",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = locationRestricted,  // El estado actual del switch depende de locationRestricted
                    onCheckedChange = { newValue ->  // Cuando el usuario cambia el estado del switch
                        locationRestricted = newValue  // Se actualiza locationRestricted con el nuevo valor de newValue

                        // Imprimir el nuevo valor de locationRestricted
                        println("El valor de locationRestricted es: $locationRestricted")
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acceso Inmediato a Resultados",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = immediateResults,  // El estado actual del switch depende de immediateResults
                    onCheckedChange = { newValue ->  // Cuando el usuario cambia el estado del switch
                        immediateResults = newValue  // Se actualiza immediateResults con el nuevo valor de newValue

                        // Imprimir el nuevo valor de immediateResults
                        println("El valor de immediateResults es: $immediateResults")
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la ubicación actual
        currentLocation?.let { location ->
            Text(
                text = "Ubicación actual: Lat ${location.latitude}, Long ${location.longitude}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                var valid = true
                if (titulo.isEmpty()) {
                    tituloError = true  // Activar el error del título
                    valid = false
                }

                if (descripcion.isEmpty()) {
                    descripcionError = true  // Activar el error de la descripción
                    valid = false
                }

                if (!valid) {
                    errorMessage = "Por favor, complete todos los campos requeridos."  // Mostrar el mensaje de error
                } else {
                    isCuestionarioCreado = true
                    crearCuestionario(
                        navController,
                        titulo,
                        descripcion,
                        nombreUsuario,
                        quizViewModel,
                        { errorMessage = it },
                        immediateAccess,
                        locationRestricted,
                        immediateResults
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Crear Cuestionario", color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Volver", color = Color.White)
        }
    }
}

private fun crearCuestionario(
    navController: NavHostController,
    titulo: String,
    descripcion: String,
    nombreUsuario: String?,
    quizViewModel: QuizViewModel,
    onError: (String) -> Unit,
    //NEW
    immediateAccess: Boolean,
    locationRestricted: Boolean,
    immediateResults: Boolean
) {
    if (titulo.isEmpty() || descripcion.isEmpty()) {
        onError("Por favor, complete todos los campos requeridos.")
    } else if (quizViewModel.contadorPreguntas.value <= 0) {
        onError("Debe agregar al menos una pregunta al cuestionario.")
    } else {
        quizViewModel.viewModelScope.launch {
            val quizCode = quizViewModel.generarClave()
            val imageUriString = quizViewModel.imageUri.toString()
            println("El url de la imagen es: $imageUriString")

            val userUid = FirebaseAuth.getInstance().currentUser?.uid

            // Crear una instancia de Cuestionario con todos los detalles de las preguntas
            val cuestionario = Cuestionario(
                id = quizCode,
                titulo = titulo,
                descripcion = descripcion,
                creadorId = userUid ?: "",
                imagen = imageUriString,
                preguntas = quizViewModel.preguntas, // Incluye la lista completa de preguntas
                //NEW
                immediateAccess = immediateAccess,  // Nuevo parámetro
                locationRestricted = locationRestricted,  // Nuevo parámetro
                immediateResults = immediateResults  // Nuevo parámetro
            )

            Log.d("CreateQuizScreen", "URL firebase : $imageUriString")
            Log.d("CreateQuizScreen", "URL quizView : ${quizViewModel.imageUri.toString()}")

            // Guardar el cuestionario en Firestore
            quizViewModel.guardarCuestionarioEnFirestore(cuestionario.toMap()) { error ->
                if (error != null) {
                    onError("Error al crear el cuestionario: $error")
                } else {
                    if (userUid != null) {
                        actualizarRolUsuario(userUid, Rol.CREADOR)
                    }
                    if (immediateAccess){
                        // ir al quiz
                    } else {
                        navController.navigate("waiting_screen/$quizCode")
                    }
                }
            }
        }
    }
}

// Extensión para convertir Cuestionario a Map<String, Any>
fun Cuestionario.toMap(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "titulo" to titulo,
        "descripcion" to descripcion,
        "creadorId" to creadorId,
        "imagen" to (imagen ?: ""),
        "preguntas" to preguntas.map { pregunta ->
            mapOf(
                "id" to pregunta.id,
                "titulo" to pregunta.titulo,
                "tipo" to pregunta.tipo.name,
                "opciones" to pregunta.opciones,
                "imagen" to pregunta.imagen,
                "respuestasCorrectas" to pregunta.respuestasCorrectas,
                "emparejamientos" to pregunta.emparejamientos,
                "itemsOrdenados" to pregunta.itemsOrdenados,
                "fraseCompletar" to pregunta.fraseCompletar,
                "opcionCorrecta" to pregunta.opcionCorrecta,
                "conceptosYDefiniciones" to pregunta.conceptosYDefiniciones,
                "user_id" to pregunta.user_id,
                "isSelected" to pregunta.isSelected,
                "opcionesCorrectasCompletarPalabras" to pregunta.opcionesCorrectasCompletarPalabras,
                "leftItems" to pregunta.leftItems,
                "rightItems" to pregunta.rightItems
            )
        },
        "immediateAccess" to immediateAccess,
        "locationRestricted" to locationRestricted,
        "immediateResults" to immediateResults
    )
}


// Función para actualizar el rol del usuario en Firestore
private fun actualizarRolUsuario(userUid: String, rol: Rol) {
    val db = FirebaseFirestore.getInstance()
    val usuarioRef = db.collection("users").document(userUid)

    // Actualizar el campo 'rol' en Firestore
    usuarioRef.update("rol", rol)
        .addOnSuccessListener {
            Log.d("CrearCuestionario", "Rol de usuario actualizado a '${rol.name}'.")
        }
        .addOnFailureListener { e ->
            Log.w("CrearCuestionario", "Error al actualizar el rol del usuario", e)
        }
}