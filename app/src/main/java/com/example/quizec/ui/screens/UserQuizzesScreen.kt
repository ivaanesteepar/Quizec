package com.example.quizec.ui.screens

import com.example.quizec.utils.TimeProgressBar
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.screens.UserQuestionTypes.FillBlankQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MatchingQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.OrderingQuesitonScreen
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

    //JIMENA
    var enableAcept by remember { mutableStateOf(false) } //habilita el botoón de aceptar
    var selectedOption by remember { mutableStateOf<String?>(null) } //ESPACIOS
    var userOrderedItems by remember { mutableStateOf<List<String>>(emptyList()) } //ORDENAR
    val userSelections = remember { mutableStateMapOf<String, String>() }

    // Estado para controlar si el botón "Aceptar" fue presionado
    var isAcceptButtonClicked by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var userName by remember { mutableStateOf<String?>(null) }

    // Observamos el totalTime
    val totalTime by quizViewModel.totalTime.collectAsState()

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

                TimeProgressBar(remainingTime)

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






                }else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_ESPACIOS) {
                    FillBlankQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedOption = selectedOption,
                        onOptionSelected = { newOption ->
                            selectedOption = newOption
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked
                    )
                    //habilitar o no el boton de aceptar
                    if (selectedOption != null){
                        enableAcept = true
                    }



                }else if (currentQuestion.tipo == TipoPregunta.ORDENAR) {
                    OrderingQuesitonScreen(
                        currentQuestion = currentQuestion,
                        userOrderedItems = { newOrderedItems ->
                            userOrderedItems = newOrderedItems // Actualiza la lista en el estado
                        }
                    )
                    enableAcept = true


                }else if (currentQuestion.tipo == TipoPregunta.EMPAREJAR) {
                    MatchingQuestionScreen(
                        currentQuestion = currentQuestion,
                        userSelections = userSelections
                    )
                    if (userSelections.size == currentQuestion.emparejamientos.size) {
                        enableAcept = true
                    }


                }else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {
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

                var resultMessage by remember { mutableStateOf("") }

                Button(
                    onClick = {
                        //USO DE ENABLEACEPT PARA VERIFICAR QUE HAY UNA RESPUESTA

                        //if (selectedAnswer != null || currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {
                            val correctAnswers = currentQuestion.respuestasCorrectas
                            when (currentQuestion.tipo) {
                                TipoPregunta.VERDADERO_FALSO -> if (selectedAnswer == correctAnswers) isAnswerCorrect = true
                                TipoPregunta.OPCION_MULTIPLE_UNA -> if (selectedAnswer?.sorted() == correctAnswers.sorted()) isAnswerCorrect = true
                                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> if (selectedAnswer?.sorted() == correctAnswers.sorted()) isAnswerCorrect = true
                                TipoPregunta.COMPLETAR_ESPACIOS -> if (selectedOption == currentQuestion.opcionCorrecta) isAnswerCorrect = true
                                TipoPregunta.ORDENAR -> if( userOrderedItems == currentQuestion.itemsOrdenados) isAnswerCorrect = true
                                TipoPregunta.EMPAREJAR -> {
                                    var isAllCorrect = true
                                    currentQuestion.emparejamientos.forEach { correctPair ->
                                        // Obtener la clave y el valor correctos de emparejamientos
                                        val (key, correctValue) = correctPair.entries.first() // Asumimos un solo par clave-valor por item
                                        // Comparar si el valor seleccionado por el usuario coincide con el valor correcto

                                        if (userSelections[key] != correctValue) {
                                            isAllCorrect = false
                                            return@forEach
                                        }
                                    }
                                    if (isAllCorrect) isAnswerCorrect = true
                                }
                                else -> false
                            }

                            isAcceptButtonClicked = true //para luego mostrar si es correcta o no la respuesta en un Text(

                            if (isAnswerCorrect == true) {
                                // Primero, obtener el valor actualizado de las respuestas correctas desde Firestore
                                usersViewModel.obtenerRespuestasCorrectas(userId, codigoQuiz!!) { respuestasCorrectas ->
                                    // Incrementar el número de respuestas correctas
                                    val respuestasCorrectasActualizadas = respuestasCorrectas + 1
                                    usersViewModel.actualizarRespuestasCorrectas(userId, codigoQuiz, respuestasCorrectasActualizadas)
                                    println("Respuestas correctas actualizadas: $respuestasCorrectasActualizadas")
                                }
                            }

                            resultMessage = if (isAnswerCorrect == true){
                                "¡Respuesta correcta!"
                            } else {
                                "Respuesta incorrecta"
                            }
                        Log.d("RESULT", "resultMessage: ${resultMessage}")

                        //}
                    },
                    enabled = enableAcept == true && !isAcceptButtonClicked// Habilitar el botón si hay una respuesta y desactivar una vez pulsado
                ) {
                    Text(text = "Aceptar")
                }


                Spacer(modifier = Modifier.height(10.dp))
                Log.d("UserQuizzesScreen", "Resultado de la respuesta: ${resultMessage}")
                // Mostrar si la respuesta es correcta o incorrecta
                if (resultMessage.isNotEmpty()) {
                    Text(
                        text = resultMessage,
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
                        resultMessage = ""

                        if (currentQuestionIndex < preguntas.size - 1) {
                            currentQuestionIndex++
                        } else {
                            navController.navigate("results_screen/$codigoQuiz")
                        }
                    },
                    enabled = isAcceptButtonClicked,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(text = "Siguiente pregunta")
                }
            }
        }
    }
}

