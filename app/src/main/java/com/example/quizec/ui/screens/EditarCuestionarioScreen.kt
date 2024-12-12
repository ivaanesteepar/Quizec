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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
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

    val preguntasSeleccionadas = quizViewModel.preguntas

    //NEW
    var preguntasInicialesQuiz by rememberSaveable { mutableStateOf(emptyList<Pregunta>()) }


    println("preguntasSeleccionadas en editar cuestionario: $preguntasSeleccionadas") // bien

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


        println("tamaño de la lista de preguntas al cambiar de cuestionario: ${listaPreguntas.size}")
    }

    println("listaPreguntas: $listaPreguntas")

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
                            imageUri = result // Now you're setting a String?
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
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título del cuestionario
        Text("Editar Cuestionario", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Título del Cuestionario", style = MaterialTheme.typography.bodyMedium)
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

        Text("Descripción", style = MaterialTheme.typography.bodyMedium)
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

        Text("Subir Imagen (Opcional)", style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = {
                pickPicture.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        ) {
            Text("Seleccionar Imagen")
        }

        if (imageUri != null) {
            AsyncImage(
                model = imageUri, // Now using a String (imageUrl) instead of Uri
                contentDescription = "Imagen cargada del servidor",
                modifier = Modifier.size(200.dp)
            )
        } else {
            Text(text = "No hay imagen para mostrar.")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contador de preguntas: ${listaPreguntas.size}") //tiene las pregs nuevas y las iniciales

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para seleccionar preguntas
        Button(
            onClick = { navController.navigate("select_questions_edit/$creadorId/$cuestionarioId") },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Seleccionar Preguntas")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boton para seleccionar preguntas
        // DESAPARECEN LAS PREGUNTAS SELECCIONADAS CUANDO SE VUELVE DE ESTA PANTALLA
        Button(
            onClick = {
                navController.navigate("delete_questions/$cuestionarioId")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Eliminar Preguntas")
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Switches para configuración adicional
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Acceso Inmediato al Cuestionario", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
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
            Text("Restringir por Ubicación", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = locationRestricted,
                onCheckedChange = { locationRestricted = it },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (locationRestricted) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Radio del área permitida (km)", style = MaterialTheme.typography.bodyLarge)
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
        Spacer(modifier = Modifier.height(16.dp))

        // ARREGLAR CUANDO ACTUALIZO EL CUESTIONARIO SIN HABER HECHO CAMBIOS, SE DUPLICAN LAS PREGUNTAS INICIALES
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

                val radioValue = radio.toDoubleOrNull()
                if (radioValue == null && locationRestricted) {  // Validar si el radio es requerido
                    errorMessage = "Por favor, ingrese un valor válido para el radio."
                    valid = false
                }

                if (!valid) {
                    errorMessage = "Por favor, complete todos los campos requeridos."  // Mostrar el mensaje de error
                } else {
                    // Actualizar el cuestionario con las preguntas seleccionadas
                    quizViewModel.actualizarCuestionario(
                        cuestionario = Cuestionario(
                            id = cuestionarioId,
                            titulo = titulo,
                            descripcion = descripcion,
                            creadorId = creadorId,
                            imagen = imageUri?.toString(),
                            preguntas = listaPreguntas, //CAMBIADO
                            immediateAccess = immediateAccess,
                            locationRestricted = locationRestricted,
                            immediateResults = immediateResults,
                            isQuizIniciado = false,
                            isUsed = false,
                            latitude = 0.0,
                            longitude = 0.0,
                            radio = radioValue ?: 0.0
                        ),
                        codigoQuiz = cuestionarioId,
                        onError = { errorMessage = it ?: "Unknown error" }
                    )

                    // Guardar las preguntas en el ViewModel
                    questionsViewModel.guardarPreguntasCuestionario(cuestionarioId, listaPreguntas - preguntasInicialesQuiz)

                    // Vaciar las preguntas seleccionadas para evitar que se sumen a otro cuestionario
                    quizViewModel.resetearPreguntas()

                    // Navegar de vuelta a la pantalla de cuestionarios
                    navController.navigate("select_cuestionario")
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Actualizar Cuestionario", color = Color.White)
        }


        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                // Restaurar las preguntas a su estado original
                listaPreguntas = listaPreguntasOriginal
                quizViewModel.resetearPreguntas()
                navController.navigate("select_cuestionario")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Volver", color = Color.White)
        }



        // Mostrar mensaje de error si es necesario
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }
    }
}

