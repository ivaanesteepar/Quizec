package com.example.quizec.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.quizec.R
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.utils.AMovServer

@Composable
fun EditarCuestionarioScreen(
    navController: NavHostController,
    cuestionarioId: String,
    quizViewModel: QuizViewModel,
    cuestionarioMod: Cuestionario
) {
    val questionsViewModel = QuestionsViewModel()
    var titulo by rememberSaveable { mutableStateOf(cuestionarioMod.titulo) }
    var descripcion by rememberSaveable { mutableStateOf(cuestionarioMod.descripcion) }
    var imageUri by rememberSaveable { mutableStateOf(cuestionarioMod.imagen) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var tituloError by rememberSaveable { mutableStateOf(false) }
    var descripcionError by rememberSaveable { mutableStateOf(false) }
    val creadorId = cuestionarioMod.creadorId
    val listaPreguntasOriginal by rememberSaveable { mutableStateOf(cuestionarioMod.preguntas) }
    var listaPreguntas by remember { mutableStateOf(cuestionarioMod.preguntas) } // Lista mutable de preguntas

    // Variables para el estado de los switches
    var immediateAccess by remember { mutableStateOf(cuestionarioMod.immediateAccess) }
    var locationRestricted by remember { mutableStateOf(cuestionarioMod.locationRestricted) }
    var immediateResults by remember { mutableStateOf(cuestionarioMod.immediateResults) }
    var radio by rememberSaveable { mutableStateOf(cuestionarioMod.radio.toString()) }
    //tiempo
    var questionsTime by remember { mutableStateOf(cuestionarioMod.questionsTime) }

    val preguntasSeleccionadas = quizViewModel.preguntas

    //NEW
    var preguntasInicialesQuiz by rememberSaveable { mutableStateOf(emptyList<Pregunta>()) }

    LaunchedEffect(cuestionarioId) {
        // Llamamos a la función suspendida para cargar las preguntas del nuevo cuestionario
        listaPreguntas = questionsViewModel.cargarPreguntasCuestionario(cuestionarioId)
        //quizViewModel.cargarPreguntasPorCodigo(cuestionarioId)

        // Actualizar las preguntas con las seleccionadas si es necesario
        listaPreguntas = listaPreguntas + quizViewModel.preguntas.filter { nuevaPregunta ->
            // Verificamos si la pregunta ya existe en la lista, basándonos en el id
            !listaPreguntas.any { preguntaExistente -> preguntaExistente.id == nuevaPregunta.id }
        }

        //NEW
        preguntasInicialesQuiz = questionsViewModel.cargarPreguntasCuestionario(cuestionarioId)

    }

    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    val pickPicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("EditarCuestionarioScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("EditarCuestionarioScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            imageUri = result
                            error = null
                        } else {
                            Log.d("EditarCuestionarioScreen", "Error al subir la imagen")
                            imageUri = null
                        }
                    }
                )
            }
        }
    )

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título del cuestionario
        Text(stringResource(R.string.editar_cuestionario), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.titulo_del_cuestionario), style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = titulo,
            onValueChange = {
                titulo = it
                if (it.isNotEmpty()) {
                    tituloError = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, if (tituloError) Color.Red else Color.Gray)
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(R.string.descripcion), style = MaterialTheme.typography.bodyMedium)
        BasicTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                if (it.isNotEmpty()) {
                    descripcionError = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, if (descripcionError) Color.Red else Color.Gray)
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Componente para seleccionar o eliminar imagen
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor // Aplicamos el color de fondo del botón
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa tdo el ancho disponible
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                    ) {
                        Text(stringResource(R.string.seleccionar_imagen))
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

                    // Botón para eliminar la imagen
                    Button(
                        onClick = {
                            // Eliminar la imagen
                            imageUri = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red // Aplicamos el color de fondo del botón
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.eliminar_imagen),
                            color = Color.White // Texto blanco para mayor visibilidad
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar la imagen seleccionada si existe
            if (!imageUri.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUri, // Usando la URI de la imagen cargada
                    contentDescription = stringResource(R.string.imagen_cargada_del_servidor),
                    contentScale = ContentScale.Crop, // Ajusta la imagen para que aproveche todo el tamaño
                    modifier = Modifier.size(100.dp)
                )
            } else {
                // Mostrar una imagen predeterminada si no se ha seleccionado ninguna imagen
                Image(
                    painter = painterResource(id = R.drawable.no_image_icon), // Reemplaza con tu recurso
                    contentDescription = stringResource(R.string.imagen_predeterminada),
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }


        Text(stringResource(R.string.contador_de_preguntas, listaPreguntas.size)) // tiene las pregs nuevas y las iniciales

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para seleccionar preguntas
        Button(
            onClick = { navController.navigate("select_questions_edit/$creadorId/$cuestionarioId") },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.seleccionar_preguntas))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para deseleccionar preguntas
        Button(
            onClick = {
                navController.navigate("delete_questions/$cuestionarioId")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.eliminar_preguntas_boton))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Mostrar mensaje de error si es necesario
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                value = questionsTime.toString(),
                onValueChange = { newquestionsTime ->
                    questionsTime = newquestionsTime.toIntOrNull() ?: 0 // Actualiza el tiempo, validando la entrada
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp)) // Más espacio entre los elementos

        // Switches para configuración adicional
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.acceso_inmediato_al_cuestionario), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = immediateAccess,
                onCheckedChange = { immediateAccess = it },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.restringir_por_ubicaci_n), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = locationRestricted,
                onCheckedChange = { locationRestricted = it },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (locationRestricted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.radio_del_rea_permitida_km), style = MaterialTheme.typography.bodyLarge)
            BasicTextField(
                value = radio,
                onValueChange = { radio = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

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
        Spacer(modifier = Modifier.height(16.dp))

        // ARREGLAR CUANDO ACTUALIZO EL CUESTIONARIO SIN HABER HECHO CAMBIOS, SE DUPLICAN LAS PREGUNTAS INICIALES
        Button(
            onClick = {
                errorMessage = "" // Reinicia el mensaje de error
                tituloError = false
                descripcionError = false

                when {
                    titulo.isEmpty() -> {
                        tituloError = true
                        errorMessage =
                            context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_el_t_tulo)
                    }
                    descripcion.isEmpty() -> {
                        descripcionError = true
                        errorMessage =
                            context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_la_descripci_n)
                    }

                    listaPreguntas.isEmpty() -> {
                        errorMessage =
                            context.getString(R.string.debe_agregar_al_menos_una_pregunta_al_cuestionario)
                    }

                    locationRestricted && radio.toDoubleOrNull() == null -> {
                        errorMessage =
                            context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_el_radio)
                    }
                    questionsTime <= 0 -> {
                        errorMessage =
                            context.getString(R.string.por_favor_ingrese_un_valor_v_lido_para_el_tiempo)
                    }
                    else -> {
                        // Actualizar el cuestionario con las preguntas seleccionadas
                        quizViewModel.actualizarCuestionario(
                            cuestionario = Cuestionario(
                                id = cuestionarioId,
                                titulo = titulo,
                                descripcion = descripcion,
                                creadorId = creadorId,
                                imagen = imageUri,
                                preguntas = listaPreguntas, // Actualización de preguntas
                                immediateAccess = immediateAccess,
                                locationRestricted = locationRestricted,
                                immediateResults = immediateResults,
                                quizIniciado = false,
                                quizUsed = false,
                                latitude = 0.0,
                                longitude = 0.0,
                                radio = radio.toDoubleOrNull() ?: 0.0,
                                questionsTime = questionsTime
                            ),
                            codigoQuiz = cuestionarioId,
                            onError = { errorMessage = it ?: context.getString(R.string.error_desconocido) }
                        )

                        // Guardar las preguntas en el ViewModel
                        questionsViewModel.guardarPreguntasCuestionario(cuestionarioId, listaPreguntas - preguntasInicialesQuiz)

                        // Reiniciar las preguntas seleccionadas
                        quizViewModel.resetearPreguntas()

                        // Navegar de vuelta a la pantalla de selección de cuestionarios
                        navController.navigate("select_cuestionario")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.actualizar_cuestionario), color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Restaurar las preguntas a su estado original
                listaPreguntas = listaPreguntasOriginal
                // Actualizar el cuestionario para recuperar las preguntas borradas
                quizViewModel.actualizarCuestionario(
                    cuestionario = Cuestionario(
                        id = cuestionarioId,
                        titulo = cuestionarioMod.titulo,
                        descripcion = cuestionarioMod.descripcion,
                        creadorId = cuestionarioMod.creadorId,
                        imagen = cuestionarioMod.imagen,
                        preguntas = listaPreguntas, // Recupero las preguntas borradas
                        immediateAccess = cuestionarioMod.immediateAccess,
                        locationRestricted = cuestionarioMod.locationRestricted,
                        immediateResults = cuestionarioMod.immediateResults,
                        quizIniciado = cuestionarioMod.quizIniciado,
                        quizUsed = cuestionarioMod.quizUsed,
                        latitude = cuestionarioMod.latitude,
                        longitude = cuestionarioMod.longitude,
                        radio = cuestionarioMod.radio,
                        questionsTime = cuestionarioMod.questionsTime
                    ),
                    codigoQuiz = cuestionarioId,
                    onError = { errorMessage = it ?: context.getString(R.string.error_desconocido) }
                )
                quizViewModel.resetearPreguntas()
                navController.navigate("select_cuestionario")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(stringResource(R.string.volver), color = Color.White)
        }

    }
}

