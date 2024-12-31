package com.example.quizec.ui.screens


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.quizec.R
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.utils.AMovServer
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CreateQuizScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel
) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var tituloError by rememberSaveable { mutableStateOf(false) }  // Control de error del título
    var descripcionError by rememberSaveable { mutableStateOf(false) }  // Control de error de la descripción
    val nombreUsuario by quizViewModel.nombreUsuario.collectAsState()
    val userUid = FirebaseAuth.getInstance().currentUser?.uid

    var immediateAccess by remember { mutableStateOf(false) }
    var locationRestricted by remember { mutableStateOf(false) }
    var immediateResults by remember { mutableStateOf(false) }
    var isQuizIniciado by remember { mutableStateOf(false) }
    //GPS
    var radio by rememberSaveable { mutableStateOf("") }
    val latitudActual by remember { mutableStateOf<Double?>(null) }
    val longitudActual by remember { mutableStateOf<Double?>(null) }
    //tiempo
    var questionsTime by remember { mutableStateOf(0) }
    var rawInput by remember { mutableStateOf("") } // Almacena la entrada de texto del usuario


    // se resetean los valores de creación
    var reset by remember { mutableStateOf(false) }
    if (reset) {
        quizViewModel.resetearPreguntas()
        titulo = ""
        descripcion = ""
        imageUri = null
        errorMessage = ""
        tituloError = false
        descripcionError = false
        questionsTime = 0
    }

    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    val pickPicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("CreateQuizScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("CreateQuizScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            imageUri = result // Now you're setting a String?
                            error = null
                        } else {
                            Log.d("CreateQuizScreen", "Error al subir la imagen")
                            imageUri = null
                        }
                    }
                )
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.crear_cuestionario), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de título
        Text(stringResource(R.string.t_tulo_del_cuestionario), style = MaterialTheme.typography.bodyMedium)
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
        Text(stringResource(R.string.descripci_n), style = MaterialTheme.typography.bodyMedium)
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
// Texto explicativo
        Text(stringResource(R.string.subir_imagen_opcional), style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

// Verificamos si hay imagen seleccionada o no
        if (imageUri == null) {
            // Si no hay imagen, centramos el botón de seleccionar imagen
            Button(
                onClick = {
                    pickPicture.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho disponible
                    .padding(horizontal = 50.dp) // Márgenes laterales para que no esté pegado a los bordes
            ) {
                Text(stringResource(R.string.seleccionar_imagen))
            }
        } else {
            // Si hay una imagen, alineamos los botones horizontalmente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Centrar los botones
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para seleccionar imagen
                Button(
                    onClick = {
                        pickPicture.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                ) {
                    Text(stringResource(R.string.seleccionar_imagen))
                }

                Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

                // Botón para eliminar la imagen
                Button(
                    onClick = {
                        // Eliminar la imagen
                        imageUri = null
                        quizViewModel.imageUri = null // Actualizar también en el ViewModel
                    }
                ) {
                    Text(
                        text = stringResource(R.string.eliminar_imagen),
                        color = Color.White // Texto blanco para mayor visibilidad
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar la imagen seleccionada
        if (imageUri != null) {
            AsyncImage(
                model = imageUri, // Usando el String (imageUri)
                contentDescription = "Imagen cargada del servidor",
                contentScale = ContentScale.Crop, // Ajusta la imagen para que aproveche tdo el tamaño
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }


        println("Url de la imagen: $imageUri")

        // Asignamos el enlace a la variable en el quizViewModel
        quizViewModel.imageUri = imageUri

        Spacer(modifier = Modifier.height(16.dp))


        // Botones para otras acciones
        Button(
            onClick = { navController.navigate("make_questions") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.crear_preguntas))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.numero_de_preguntas, quizViewModel.contadorPreguntas.value))

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para seleccionar preguntas
        Button(
            onClick = { navController.navigate("select_questions/$userUid") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.seleccionar_preguntas))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de error si aplica
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Añadir algo de padding a la columna general para mejorar la legibilidad
        ) {

            // Fila para seleccionar el tiempo límite por pregunta
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.tiempo_de_las_preguntas_s),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = rawInput, // Mostrar la entrada del usuario
                    onValueChange = { userInput ->
                        // Actualizar la entrada solo si el valor es numérico
                        if (userInput.isEmpty() || userInput.toIntOrNull() != null) {
                            rawInput = userInput // Actualizar la entrada
                            val parsedTime = userInput.toIntOrNull()
                            if (parsedTime != null && parsedTime > 0) {
                                questionsTime = parsedTime // Si es válido, actualizar questionsTime
                                errorMessage = "" // Limpiar el mensaje de error
                            } else {
                                errorMessage =
                                    context.getString(R.string.por_favor_ingrese_un_tiempo_v_lido) // Si no es válido, mostrar el error
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .width(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // Más espacio entre los elementos

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.acceso_inmediato_al_cuestionario),
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
                    text = stringResource(R.string.restringir_por_ubicaci_n),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = locationRestricted,  // El estado actual del switch depende de locationRestricted
                    onCheckedChange = { newValue ->  // Cuando el usuario cambia el estado del switch
                        locationRestricted = newValue  // Se actualiza locationRestricted con el nuevo valor de newValue
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Este campo de texto solo aparecerá si locationRestricted es true
            if (locationRestricted) {
                Text(stringResource(R.string.radio_del_rea_permitida_km), style = MaterialTheme.typography.bodyMedium)
                BasicTextField(
                    value = radio,
                    onValueChange = { radio = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .border(1.dp, Color.Gray)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.acceso_inmediato_a_resultados),
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

        Button(
            onClick = {
                errorMessage = "" // Reinicia el mensaje de error
                tituloError = false
                descripcionError = false

                when {
                    titulo.isEmpty() -> {
                        tituloError = true
                        errorMessage = context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_el_t_tulo)
                    }
                    descripcion.isEmpty() -> {
                        descripcionError = true
                        errorMessage = context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_la_descripci_n)
                    }
                    quizViewModel.contadorPreguntas.value <= 0 -> {
                        errorMessage = context.getString(R.string.debe_agregar_al_menos_una_pregunta_al_cuestionario)
                    }
                    locationRestricted && radio.toDoubleOrNull() == null -> {
                        errorMessage = context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_el_radio)
                    }
                    questionsTime <= 0 -> {
                        errorMessage = context.getString(R.string.por_favor_ingrese_un_tiempo_v_lido)
                    }
                    else -> {
                        reset = true
                        crearCuestionario(
                            navController,
                            titulo,
                            descripcion,
                            nombreUsuario,
                            quizViewModel,
                            { errorMessage = it },
                            immediateAccess,
                            locationRestricted,
                            immediateResults,
                            isQuizIniciado,
                            latitudActual ?: 0.0, // Si latitudActual es nulo, asigna 0.0
                            longitudActual ?: 0.0, // Si longitudActual es nulo, asigna 0.0
                            radio.toDoubleOrNull() ?: 0.0, // Usamos el valor de radio, si no es válido, usamos 0.0
                            questionsTime
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.crear_cuestionario), color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                reset = true
                navController.navigate("home")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.volver), color = Color.White)
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
    immediateAccess: Boolean,
    locationRestricted: Boolean,
    immediateResults: Boolean,
    isQuizIniciado: Boolean,
    latitude: Double,
    longitude: Double,
    radio: Double,
    questionsTime: Int
) {
    quizViewModel.viewModelScope.launch {
        val quizCode = quizViewModel.generarClave()
        val imageUri = quizViewModel.imageUri
        Log.d("CreateQuizScreen", "El URL de la imagen es: $imageUri")

        val userUid = FirebaseAuth.getInstance().currentUser?.uid

        val cuestionario = Cuestionario(
            id = quizCode,
            titulo = titulo,
            descripcion = descripcion,
            creadorId = userUid ?: "",
            imagen = imageUri, // Asignar la URL de la imagen correctamente
            preguntas = quizViewModel.preguntas,
            immediateAccess = immediateAccess,
            locationRestricted = locationRestricted,
            immediateResults = immediateResults,
            quizIniciado = false,
            quizUsed = false,
            latitude = latitude,
            longitude = longitude,
            radio = radio,
            questionsTime = questionsTime
        )

        // Guardar el cuestionario en Firestore
        Log.d("CreateQuizScreen", "Guardando en Firestore: $cuestionario")
        quizViewModel.guardarCuestionarioEnFirestore(cuestionario.toMap()) { error ->
            if (error != null) {
                onError("Error creating the cuestionary: $error")
                Toast.makeText(navController.context, "Error creating the quiz", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (userUid != null) {
                    quizViewModel.actualizarRolUsuario2(userUid, Rol.CREADOR)
                }
                Toast.makeText(
                    navController.context,
                    "Quiz created successfully",
                    Toast.LENGTH_SHORT
                ).show()
                navController.navigate("home")
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
                "rightItems" to pregunta.rightItems,
                "userAnswers" to pregunta.userAnswers
            )
        },
        "immediateAccess" to immediateAccess,
        "locationRestricted" to locationRestricted,
        "immediateResults" to immediateResults,
        "quizIniciado" to quizIniciado,
        "quizUsed" to quizUsed,
        "latitude" to latitude,
        "longitude" to longitude,
        "radio" to radio, // Añadido el valor de radio
        "questionsTime" to questionsTime
    )
}