package com.example.quizec.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.screens.UserQuestionTypes.AssociationQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.FillBlankQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MatchingQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MissingWordsQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MultChoicesQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.OneMultChoicesQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.OrderingQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.TrueFalseQuestionScreen
import com.example.quizec.ui.theme.defaultButtonColor
import com.example.quizec.ui.theme.selectedButtonColor
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.io.File

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
    var trueButtonColor by remember { mutableStateOf(Color.Unspecified) }
    var falseButtonColor by remember { mutableStateOf(Color.Unspecified) }
    //JIMENA
    var enableAcept by remember { mutableStateOf(false) } //habilita el botoón de aceptar
    var selectedOption by remember { mutableStateOf<String?>(null) } //ESPACIOS
    var userOrderedItems by remember { mutableStateOf<List<String>>(emptyList()) } //ORDENAR
    val userSelections = remember { mutableStateMapOf<String, String>() } //ASOCIAR Y MATCH
    var answer by remember { mutableStateOf<Any?>(null) }
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

    var immediateResults by remember { mutableStateOf(false) }


    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            println("Usuario ha entrado con id: $userId al cuestionario: $codigoQuiz")
            quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
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
        isAcceptButtonClicked = false // Reinicia el estado del botón "Aceptar"

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
            var userInputs = remember {
                mutableStateListOf<String>().apply {
                    repeat(opcionesCorrectas.size) {
                        add("")
                    }
                }
            }

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

                currentQuestion.imagen?.let { imageUrl ->
                    if (imageUrl.startsWith("file://")) {
                        // Asegúrate de que el archivo existe antes de intentar cargarlo
                        val file = File(Uri.parse(imageUrl).path ?: "")
                        if (file.exists()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrl),
                                contentDescription = "Imagen de la pregunta",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp) // Ajusta el tamaño de la imagen
                                    .padding(8.dp)
                            )
                        } else {
                            // Si el archivo no existe, puedes mostrar un mensaje o una imagen predeterminada
                            Text(
                                text = "Archivo no encontrado"
                            )
                        }
                    }
                }

                // Opciones de respuesta dependiendo del tipo de pregunta
                if (currentQuestion.tipo == TipoPregunta.VERDADERO_FALSO) {
                    val correctAnswer = currentQuestion.respuestasCorrectas.firstOrNull() ?: ""

                    TrueFalseQuestionScreen(
                        onSelectedAnswerChange = { newAnswer ->
                            selectedAnswer = newAnswer
                            if (selectedAnswer?.contains("Verdadero") == true) {
                                trueButtonColor = selectedButtonColor
                                falseButtonColor = Color.Unspecified // alterna color naranja
                            } else {
                                falseButtonColor = selectedButtonColor
                                trueButtonColor = Color.Unspecified // alterna color naranja
                            }
                        },
                        falseButtonColor = falseButtonColor,
                        trueButtonColor = trueButtonColor,
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        correctAnswer = correctAnswer
                    )

                    if (selectedAnswer != null) enableAcept = true

                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_UNA) {
                    val correctAnswer = currentQuestion.respuestasCorrectas.firstOrNull() ?: ""

                    OneMultChoicesQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedAnswer = selectedAnswer,
                        onSelectedAnswerChange = { newAnswer ->
                            selectedAnswer = newAnswer
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        correctAnswer = correctAnswer

                    )
                    enableAcept = if (!selectedAnswer.isNullOrEmpty()) true else false


                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_MULTIPLES) {
                    MultChoicesQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedAnswer = selectedAnswer,
                        onSelectedAnswerChange = { newAnswers ->
                            selectedAnswer = newAnswers
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        correctAnswers = currentQuestion.respuestasCorrectas
                    )
                    println("selectedAnswer: $selectedAnswer")
                    enableAcept = if (!selectedAnswer.isNullOrEmpty()) true else false


                } else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_ESPACIOS) {
                    FillBlankQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedOption = selectedOption,
                        onOptionSelected = { newOption ->
                            selectedOption = newOption
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,

                        )
                    //habilitar o no el boton de aceptar
                    if (selectedOption != null){
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }



                } else if (currentQuestion.tipo == TipoPregunta.ORDENAR) {
                    OrderingQuestionScreen(
                        currentQuestion = currentQuestion,
                        userOrderedItems = { newOrderedItems ->
                            userOrderedItems = newOrderedItems // Actualiza la lista en el estado
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked
                    )
                    enableAcept = true


                } else if (currentQuestion.tipo == TipoPregunta.EMPAREJAR) {
                    MatchingQuestionScreen(
                        currentQuestion = currentQuestion,
                        userSelections = userSelections,
                        isAcceptButtonClicked = isAcceptButtonClicked
                    )
                    println("userSelections: $userSelections")
                    if (userSelections.size == currentQuestion.emparejamientos.size) {
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }


                } else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {

                    MissingWordsQuestionScreen(
                        currentQuestion = currentQuestion,
                        opcionesCorrectas = opcionesCorrectas,
                        userInputs = userInputs,
                        isAcceptButtonClicked = isAcceptButtonClicked
                    )
                    if (userInputs.all { it.isNotBlank() } && userInputs.size == currentQuestion.opcionesCorrectasCompletarPalabras.size) {
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }

                } else if (currentQuestion.tipo == TipoPregunta.ASOCIACION) {
                    AssociationQuestionScreen(
                        currentQuestion = currentQuestion,
                        userSelections = userSelections
                    )

                    if (userSelections.size == currentQuestion.conceptosYDefiniciones.size) {
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                var resultMessage by remember { mutableStateOf("") }

                Button(
                    onClick = {
                        val correctAnswers = currentQuestion.respuestasCorrectas
                        var localIsAnswerCorrect = false // Variable local para evitar smart cast issues

                        when (currentQuestion.tipo) {
                            TipoPregunta.VERDADERO_FALSO -> {
                                if (selectedAnswer == correctAnswers) localIsAnswerCorrect =
                                    true
                                // Registrar la respuesta seleccionada
                                selectedAnswer?.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.OPCION_MULTIPLE_UNA -> {
                                if (selectedAnswer?.sorted() == correctAnswers.sorted()) localIsAnswerCorrect = true

                                // Registrar la respuesta seleccionada
                                selectedAnswer?.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                                if (selectedAnswer?.sorted() == correctAnswers.sorted()) localIsAnswerCorrect = true
                                // Registrar la respuesta seleccionada
                                selectedAnswer?.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.COMPLETAR_ESPACIOS -> {
                                if (selectedOption == currentQuestion.opcionCorrecta) localIsAnswerCorrect = true
                                selectedOption?.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.ORDENAR -> {
                                if (userOrderedItems == currentQuestion.itemsOrdenados) localIsAnswerCorrect =
                                    true
                                userOrderedItems.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.EMPAREJAR -> {
                                var isAllCorrect = true
                                currentQuestion.emparejamientos.forEach { correctPair ->
                                    val (key, correctValue) = correctPair.entries.first()
                                    if (userSelections[key] != correctValue) {
                                        isAllCorrect = false
                                        return@forEach
                                    }
                                }
                                if (isAllCorrect) localIsAnswerCorrect = true

                                userSelections.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }

                            }

                            TipoPregunta.COMPLETAR_PALABRAS -> {
                                if (userInputs.sorted() == currentQuestion.opcionesCorrectasCompletarPalabras.sorted()) localIsAnswerCorrect =
                                    true
                                userInputs.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }

                            TipoPregunta.ASOCIACION -> {
                                var isAllCorrect = true
                                currentQuestion.conceptosYDefiniciones.forEach { correctPair ->
                                    val (key, correctValue) = correctPair.entries.first()
                                    if (userSelections[key] != correctValue) {
                                        isAllCorrect = false
                                        return@forEach
                                    }
                                }
                                Log.d("isAllCorrect asociar", "valor de is allcorrrct: $isAllCorrect")

                                if (isAllCorrect) localIsAnswerCorrect = true

                                userSelections.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        it
                                    )
                                }
                            }
                        }
                        // Asigna el mensaje de resultado dependiendo de si la respuesta es correcta o no
                        resultMessage = if (localIsAnswerCorrect) {
                            "Respuesta Correcta!"
                        } else {
                            "Respuesta Incorrecta!"
                        }
                        isAnswerCorrect = localIsAnswerCorrect
                        isAcceptButtonClicked = true

                        if (localIsAnswerCorrect) {
                            // Actualizar respuestas correctas en Firestore
                            usersViewModel.obtenerRespuestasCorrectas(
                                userId,
                                codigoQuiz!!
                            ) { respuestasCorrectas ->
                                val respuestasCorrectasActualizadas = respuestasCorrectas + 1
                                usersViewModel.actualizarRespuestasCorrectas(
                                    userId,
                                    codigoQuiz,
                                    respuestasCorrectasActualizadas
                                )
                                println("Respuestas correctas actualizadas: $respuestasCorrectasActualizadas")
                            }
                        }
                        if (!immediateResults) {
                            // Si immediateResults es false, avanzar automáticamente
                            if (currentQuestionIndex < preguntas.size - 1) {
                                currentQuestionIndex++
                                isAcceptButtonClicked = false // Reset para la próxima pregunta
                            } else {
                                navController.navigate("results_screen/$codigoQuiz")
                            }
                        }
                        // Reinicia los estados para la próxima pregunta
                        selectedAnswer = null
                        isAnswerCorrect = null
                        isAnswerSelected = false
                        isAcceptButtonClicked = false
                        enableAcept = false
                        trueButtonColor = Color.Unspecified
                        falseButtonColor = Color.Unspecified
                        resultMessage = ""
                    },
                    enabled = enableAcept
                ) {
                    Text(text = "Aceptar")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mostrar si la respuesta es correcta o incorrecta si immediateResults es true
                if (immediateResults && isAcceptButtonClicked) {
                    println("isAnswerCorrect: $isAnswerCorrect resultMessage: $resultMessage")
                    Text(
                        text = resultMessage,
                        color = if (isAnswerCorrect == true) Color.Green else Color.Red,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                println("isAcceptButtonClicked: $isAcceptButtonClicked")

                // Botón "Siguiente pregunta" (solo visible si immediateResults es true)
                if (isAcceptButtonClicked && immediateResults) {
                    Button(
                        onClick = {
                            // Reinicia los estados para la próxima pregunta
                            selectedAnswer = null
                            isAnswerCorrect = null
                            isAnswerSelected = false
                            isAcceptButtonClicked = false
                            enableAcept = false
                            trueButtonColor = Color.Unspecified
                            falseButtonColor = Color.Unspecified
                            resultMessage = ""

                            if (currentQuestionIndex < preguntas.size - 1) {
                                // Avanzar a la siguiente pregunta
                                currentQuestionIndex++
                            } else {
                                // Navegar a la pantalla de resultados si es la última pregunta
                                navController.navigate("results_screen/$codigoQuiz")
                            }
                        },
                        enabled = isAcceptButtonClicked, // Habilitar solo si se presionó "Aceptar"
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = "Siguiente pregunta")
                    }
                }
            }
        }
    }
}