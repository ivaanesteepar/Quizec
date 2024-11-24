package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.Rol
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun UserQuizzesScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String?
) {
    val usersViewModel = UsersViewModel()
    var preguntas by remember { mutableStateOf(emptyList<Pregunta>()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<List<String>?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var isAnswerSelected by remember { mutableStateOf(false) }
    var trueButtonColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    var falseButtonColor by remember { mutableStateOf(Color(0xFF2196F3)) }

    // Estado para controlar si el botón "Aceptar" fue presionado
    var isAcceptButtonClicked by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var userName by remember { mutableStateOf<String?>(null) }

    // Observamos el totalTime
    val totalTime by quizViewModel.totalTime.collectAsState()
    println("totalTime fuera del launch: $totalTime")

    var remainingTime by remember { mutableStateOf(30) } // Tiempo por pregunta
    var timerActive by remember { mutableStateOf(true) }

    // Estado global desde el ViewModel
    val _remainingTime = quizViewModel.remainingTime // Obtenido desde el ViewModel
    val remainingTimeState: State<Int> = _remainingTime


    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            println("Usuario ha entrado con id: $userId al cuestionario: $codigoQuiz")
            quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            userName = usersViewModel.obtenerNombreUsuario(userId, codigoQuiz)
            usersViewModel.agregarUsuarioAQuiz(codigoQuiz)
            quizViewModel.resetTimes()  // Restablece los tiempos a su valor inicial
        }
    }

    // Lógica para verificar si el quiz ha sido terminado
    LaunchedEffect(userId, codigoQuiz) {
        if (codigoQuiz != null) {
            val db = FirebaseFirestore.getInstance()
            val usuariosEsperaRef = db.collection("usuariosEspera").document(codigoQuiz)

            // Listener en tiempo real para escuchar cambios en el estado del quiz
            usuariosEsperaRef.addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Log.e("QuizViewModel", "Error al verificar el estado de quizTerminado: ${error.message}")
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val usuariosMap = documentSnapshot.get("usuarios") as? Map<String, Map<String, Any>> ?: return@addSnapshotListener
                    val usuarioData = usuariosMap[userId]
                    val quizTerminado = usuarioData?.get("quizTerminado") as? Boolean

                    // Si el estado cambia a true, navega a la pantalla de resultados
                    if (quizTerminado == true) {
                        navController.navigate("results_screen/$codigoQuiz")
                    }
                }
            }
        }
    }



    // Lógica del temporizador para la pregunta actual y el tiempo total
    LaunchedEffect(totalTime) {
        println("totalTime: $totalTime")
        if (totalTime == 0) {
            navController.navigate("results_screen/$codigoQuiz") // Navega al finalizar
        }
    }

    LaunchedEffect(currentQuestionIndex) {
        remainingTime = 30 // Reinicia el tiempo de la pregunta al cambiar
        timerActive = true

        while (timerActive && remainingTime > 0 && totalTime > 0) {
            delay(1000)
            remainingTime -= 1
            quizViewModel.tick() // Disminuye el tiempo total
        }

        if (remainingTime == 0) {
            if (currentQuestionIndex < preguntas.size - 1) {
                currentQuestionIndex++ // Cambiar a la siguiente pregunta
            } else {
                navController.navigate("results_screen/$codigoQuiz") // Ir a resultados
            }
        }
    }

    preguntas = quizViewModel.preguntas

    Box(
        modifier = Modifier
            .fillMaxSize() // Llena toda la pantalla
            .padding(16.dp) // Agrega un pequeño padding a los lados
    ) {
        if (preguntas.isEmpty()) {
            Text(
                text = "No hay preguntas disponibles",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center) // Centra el texto en el centro de la pantalla
            )
        } else {
            val currentQuestion = preguntas[currentQuestionIndex]
            // Opciones de respuesta para completar las palabras
            val opcionesCorrectas = currentQuestion.opcionesCorrectasCompletarPalabras
            var userInputs = remember { mutableStateListOf<String>().apply { repeat(opcionesCorrectas.size) { add("") } } }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Número de pregunta actual
                Text(
                    text = "Pregunta ${currentQuestionIndex + 1} de ${preguntas.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
                // Aquí agregamos un Spacer de tamaño ajustado para mayor separación
                Spacer(modifier = Modifier.width(50.dp)) // Ajusta el valor según lo necesites

                Text(
                    text = "Tiempo restante: $remainingTime segundos (Total: $totalTime)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
            }

            // Centrar todo el contenido dentro de un Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Centra todo el contenido horizontalmente
                verticalArrangement = Arrangement.Center, // Centra todo el contenido verticalmente
                modifier = Modifier
                    .align(Alignment.Center) // Alinea la columna en el centro de la pantalla
                    .verticalScroll(rememberScrollState())

            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Mostrar el título de la pregunta
                Text(
                    text = currentQuestion.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Opciones de respuesta dependiendo del tipo de pregunta
                if (currentQuestion.tipo == TipoPregunta.VERDADERO_FALSO) {
                    Row(
                        horizontalArrangement = Arrangement.Center, // Centra los botones horizontalmente
                        verticalAlignment = Alignment.CenterVertically, // Centra los botones verticalmente
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isAnswerSelected) {
                                    selectedAnswer = listOf("Verdadero")
                                    trueButtonColor = Color(0xFFFFA500)
                                    falseButtonColor = Color(0xFF2196F3)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = trueButtonColor),
                            enabled = !isAnswerSelected,
                            modifier = Modifier.weight(1f) // Ocupa el mismo espacio que el botón de Falso
                        ) {
                            Text(text = "Verdadero", color = Color.White)
                        }

                        Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

                        Button(
                            onClick = {
                                if (!isAnswerSelected) {
                                    selectedAnswer = listOf("Falso")
                                    falseButtonColor = Color(0xFFFFA500)
                                    trueButtonColor = Color(0xFF2196F3)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = falseButtonColor),
                            enabled = !isAnswerSelected,
                            modifier = Modifier.weight(1f) // Ocupa el mismo espacio que el botón de Verdadero
                        ) {
                            Text(text = "Falso", color = Color.White)
                        }
                    }
                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_UNA) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        currentQuestion.opciones.forEach { opcion ->
                            val isSelected = selectedAnswer?.contains(opcion) == true

                            Button(
                                onClick = {
                                    if (!isSelected) {
                                        selectedAnswer = listOf(opcion) // Solo seleccionamos una opción
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFFFA500) else Color(0xFF2196F3)),
                                enabled = !isAnswerSelected,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = opcion, color = Color.White)
                            }
                        }
                    }
                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_MULTIPLES) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        currentQuestion.opciones.forEach { opcion ->
                            val isSelected = selectedAnswer?.contains(opcion) == true

                            Button(
                                onClick = {
                                    if (isSelected) {
                                        selectedAnswer = selectedAnswer?.filter { it != opcion }
                                    } else {
                                        selectedAnswer = selectedAnswer?.toMutableList()?.apply { add(opcion) } ?: listOf(opcion)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFFFFA500) else Color(0xFF2196F3)),
                                enabled = !isAnswerSelected,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = opcion, color = Color.White)
                            }
                        }
                    }

                } else if (currentQuestion.tipo == TipoPregunta.ORDENAR) {
                    val orderedItems = currentQuestion.itemsOrdenados.toMutableStateList()
                    println("Items ordenados: $orderedItems")

                    Column {
                        orderedItems.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            ) {
                                // Mostrar el texto del ítem
                                Text(
                                    text = item,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(16.dp)
                                )

                                // Botón para mover el ítem hacia arriba
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            // Mover el ítem hacia arriba
                                            orderedItems.removeAt(index)
                                            orderedItems.add(index - 1, item)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Mover arriba")
                                }

                                // Botón para mover el ítem hacia abajo
                                IconButton(
                                    onClick = {
                                        if (index < orderedItems.size - 1) {
                                            // Mover el ítem hacia abajo
                                            orderedItems.removeAt(index)
                                            orderedItems.add(index + 1, item)
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Mover abajo")
                                }
                            }
                        }
                    }
                } else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {
                    val fraseCompletar = currentQuestion.fraseCompletar

                    // Dividir la frase en palabras y reemplazar las correctas por un espacio (___)
                    val palabrasFrase = fraseCompletar.split(" ")
                    val fraseConEspacios = palabrasFrase.joinToString(" ") { palabra ->
                        if (opcionesCorrectas.contains(palabra)) {
                            "___" // Reemplaza las palabras correctas con "___"
                        } else {
                            palabra // Mantiene las otras palabras
                        }
                    }

                    // Mostrar la frase modificada
                    Text(
                        text = "Completa la frase: $fraseConEspacios",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp) // Agregado padding horizontal
                    ) {
                        opcionesCorrectas.forEachIndexed { index, palabraCorrecta ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Número de la palabra (1, 2, 3,...)
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(24.dp) // Espacio fijo para el número
                                )

                                // Cuadro de texto para que el usuario ingrese la respuesta
                                TextField(
                                    value = userInputs[index],
                                    onValueChange = { newValue -> userInputs[index] = newValue },
                                    label = { Text(text = "Ingresa la palabra correcta") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (selectedAnswer != null || currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {
                            val correctAnswers = currentQuestion.respuestasCorrectas
                            val isCorrect = when (currentQuestion.tipo) {
                                TipoPregunta.VERDADERO_FALSO -> selectedAnswer == correctAnswers
                                TipoPregunta.OPCION_MULTIPLE_UNA -> selectedAnswer?.sorted() == correctAnswers.sorted()
                                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> selectedAnswer?.sorted() == correctAnswers.sorted()
                                else -> false
                            }

                            isAnswerCorrect = isCorrect
                            isAnswerSelected = true
                            isAcceptButtonClicked = true

                            if (isCorrect) {
                                // Primero, obtener el valor actualizado de las respuestas correctas desde Firestore
                                usersViewModel.obtenerRespuestasCorrectas(userId, codigoQuiz!!) { respuestasCorrectas ->
                                    // Incrementar el número de respuestas correctas
                                    val respuestasCorrectasActualizadas = respuestasCorrectas + 1
                                    usersViewModel.actualizarRespuestasCorrectas(userId, codigoQuiz, respuestasCorrectasActualizadas)
                                    println("Respuestas correctas actualizadas: $respuestasCorrectasActualizadas")
                                }
                            }
                        }
                    },
                    enabled = !isAcceptButtonClicked && selectedAnswer != null // Habilitar el botón solo si hay una respuesta seleccionada
                ) {
                    Text(text = "Aceptar")
                }


                Spacer(modifier = Modifier.height(10.dp))

                // Mostrar si la respuesta es correcta o incorrecta
                if (isAnswerSelected) {
                    Text(
                        text = if (isAnswerCorrect == true) "¡Respuesta correcta!" else "Respuesta incorrecta",
                        color = if (isAnswerCorrect == true) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                // Botón "Siguiente pregunta"
                Button(
                    onClick = {
                        selectedAnswer = null
                        isAnswerCorrect = null
                        isAnswerSelected = false
                        isAcceptButtonClicked = false
                        trueButtonColor = Color(0xFF2196F3)
                        falseButtonColor = Color(0xFF2196F3)

                        if (currentQuestionIndex < preguntas.size - 1) {
                            currentQuestionIndex++
                        } else {
                            navController.navigate("results_screen/$codigoQuiz")
                        }
                    },
                    enabled = isAnswerSelected,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(text = "Siguiente pregunta")
                }
            }
        }
    }
}

