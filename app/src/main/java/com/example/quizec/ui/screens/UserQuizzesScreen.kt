package com.example.quizec.ui.screens


import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.quizec.R
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
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.theme.selectedButtonColor
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
    var trueButtonColor by remember { mutableStateOf(Color.Unspecified) }
    var falseButtonColor by remember { mutableStateOf(Color.Unspecified) }
    //JIMENA
    var enableAcept by remember { mutableStateOf(false) } //habilita el botón de aceptar
    var selectedOption by remember { mutableStateOf<String?>(null) } //ESPACIOS
    var userOrderedItems by remember { mutableStateOf<List<String>>(emptyList()) } //ORDENAR
    val userSelections = remember { mutableStateMapOf<String, String>() } //ASOCIAR Y MATCH
    // Estado para controlar si el botón "Aceptar" fue presionado
    var isAcceptButtonClicked by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var userName by remember { mutableStateOf<String?>(null) }
    // Observamos el totalTime
    val totalTime by quizViewModel.totalTime.collectAsState()
    println("totalTime fuera del launch: $totalTime")
    var remainingTime by remember { mutableStateOf(30) } // Tiempo por pregunta
    var timerActive by remember { mutableStateOf(true) }
    var immediateResults by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var userInputs = remember { mutableStateListOf<String>() }

    var isTimeLoaded by remember { mutableStateOf(false) }



    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            // Establece el tiempo desde Firestore
            quizViewModel.setQuestionsTime(codigoQuiz)
            quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
            userName = usersViewModel.obtenerNombreUsuario(userId, codigoQuiz)
            usersViewModel.agregarUsuarioAQuiz(codigoQuiz)

            // Establece el tiempo desde Firestore
            quizViewModel.setQuestionsTime(codigoQuiz)
            // Cambiar el estado cuando el tiempo esté cargado, ya q si no, sale 60
            isTimeLoaded = true

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

    LaunchedEffect(isTimeLoaded, currentQuestionIndex) {
        if (isTimeLoaded) {
            userOrderedItems = emptyList()
            selectedOption = null

            //Reinicia el tiempo de la pregunta al cambiar
            remainingTime = quizViewModel.remainingTime.value

            timerActive = true
            isAcceptButtonClicked = false // Reinicia el estado del botón "Aceptar"

            while (timerActive && remainingTime > 0 && totalTime > 0) {
                delay(1000)
                remainingTime -= 1
                quizViewModel.tick() // Disminuye el tiempo total
            }

            if (remainingTime == 0) {
                if (codigoQuiz != null) {
                    immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
                }

                if (immediateResults) {
                    // Si immediateResults es true, esperar 10 segundos antes de cambiar a la siguiente pregunta
                    isAcceptButtonClicked = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (currentQuestionIndex < preguntas.size - 1) {
                            currentQuestionIndex++ // Cambiar a la siguiente pregunta
                        } else {
                            // Aquí va el código que quieres ejecutar si ya no hay más preguntas
                        }
                    }, 10000) // 10000 milisegundos = 10 segundos
                } else {
                    // Si immediateResults es false, cambiar a la siguiente pregunta inmediatamente
                    if (currentQuestionIndex < preguntas.size - 1) {
                        currentQuestionIndex++ // Cambiar a la siguiente pregunta
                    } else {
                        // Aquí va el código que quieres ejecutar si ya no hay más preguntas
                    }
                }
            }

        }
    }

    BackHandler {
        Toast.makeText(context,
            context.getString(R.string.no_puedes_retroceder_durante_el_quiz), Toast.LENGTH_SHORT).show()
    }

    preguntas = quizViewModel.preguntas
    Box(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize() // Llena toda la pantalla
            .padding(16.dp) // Agrega un pequeño padding a los lados
    ) {
        if (preguntas.isEmpty()) {
            Text(
                text = stringResource(R.string.no_hay_preguntas_disponibles),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center) // Centra el texto en el centro de la pantalla
            )
        } else {
            val currentQuestion = preguntas[currentQuestionIndex]
            // Opciones de respuesta para completar las palabras
            val opcionesCorrectas = currentQuestion.opcionesCorrectasCompletarPalabras
            userInputs = remember {
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
                    text = LocalContext.current.getString(R.string.pregunta_texto, currentQuestionIndex + 1, preguntas.size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
                // Aquí agregamos un Spacer de tamaño ajustado para mayor separación
                Spacer(modifier = Modifier.width(50.dp)) // Ajusta el valor según lo necesites

                Text(
                    text = LocalContext.current.getString(R.string.tiempo_restante, remainingTime),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
            }

            // Centrar tdo el contenido dentro de un Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center) // Alinea la columna en el centro de la pantalla
                    .verticalScroll(rememberScrollState())

            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Show selected image if available
                if (currentQuestion.imagen != null) {
                    AsyncImage(
                        model = currentQuestion.imagen,
                        contentDescription = "Imagen cargada del servidor",
                        contentScale = ContentScale.Crop, // Ajusta la imagen para q aproveche tdo el tam
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mostrar el título de la pregunta
                Text(
                    text = currentQuestion.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                val quizTerminado = false

                // Opciones de respuesta dependiendo del tipo de pregunta
                if (currentQuestion.tipo == TipoPregunta.VERDADERO_FALSO) {
                    val correctAnswer = currentQuestion.respuestasCorrectas.firstOrNull() ?: ""
                    var context = LocalContext.current

                    TrueFalseQuestionScreen(
                        onSelectedAnswerChange = { newAnswer ->
                            selectedAnswer = newAnswer
                            if (selectedAnswer?.contains(context.getString(R.string.verdadero)) == true) {
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
                        correctAnswer = correctAnswer,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )

                    if (selectedAnswer != null && remainingTime > 0) enableAcept = true else enableAcept = false

                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_UNA) {
                    val correctAnswer = currentQuestion.respuestasCorrectas.firstOrNull() ?: ""

                    //println("UserQuizzes quizTerminado: $quizTerminado")
                    OneMultChoicesQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedAnswer = selectedAnswer,
                        onSelectedAnswerChange = { newAnswer ->
                            selectedAnswer = newAnswer
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        correctAnswer = correctAnswer,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado

                    )
                    enableAcept = if (!selectedAnswer.isNullOrEmpty() && remainingTime > 0) true else false


                } else if (currentQuestion.tipo == TipoPregunta.OPCION_MULTIPLE_MULTIPLES) {
                    MultChoicesQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedAnswer = selectedAnswer,
                        onSelectedAnswerChange = { newAnswers ->
                            selectedAnswer = newAnswers
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        correctAnswers = currentQuestion.respuestasCorrectas,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )
                    println("selectedAnswer: $selectedAnswer")
                    enableAcept = if (!selectedAnswer.isNullOrEmpty() && remainingTime > 0) true else false


                } else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_ESPACIOS) {
                    FillBlankQuestionScreen(
                        currentQuestion = currentQuestion,
                        selectedOption = selectedOption,
                        onOptionSelected = { newOption ->
                            selectedOption = newOption
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado,

                        )
                    //habilitar o no el boton de aceptar
                    if (selectedOption != null && remainingTime > 0){
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }

                } else if (currentQuestion.tipo == TipoPregunta.ORDENAR) {
                    Log.d("OrderingQuestionScreen", "currentQuestion: $currentQuestion")
                    OrderingQuestionScreen(
                        currentQuestion = currentQuestion,
                        userOrderedItems = { newOrderedItems ->
                            userOrderedItems = newOrderedItems // Actualiza la lista en el estado
                        },
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )
                    enableAcept = if (remainingTime > 0) true else false


                } else if (currentQuestion.tipo == TipoPregunta.EMPAREJAR) {
                    Log.d("MatchingQuestionScreen", "currentQuestion: $currentQuestion")
                    MatchingQuestionScreen(
                        currentQuestion = currentQuestion,
                        userSelections = userSelections,
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )
                    println("userSelections: $userSelections")
                    enableAcept = if (userSelections.size == currentQuestion.emparejamientos.size && remainingTime > 0) true else false


                } else if (currentQuestion.tipo == TipoPregunta.COMPLETAR_PALABRAS) {

                    MissingWordsQuestionScreen(
                        currentQuestion = currentQuestion,
                        opcionesCorrectas = opcionesCorrectas,
                        userInputs = userInputs,
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )
                    if (userInputs.all { it.isNotBlank() } &&
                         userInputs.size == currentQuestion.opcionesCorrectasCompletarPalabras.size
                        && remainingTime > 0) {
                        enableAcept = true
                    }else{
                        enableAcept = false
                    }

                } else if (currentQuestion.tipo == TipoPregunta.ASOCIACION) {
                    Log.d("AssociationQuesiton", "currentQuestion: $currentQuestion")

                    AssociationQuestionScreen(
                        currentQuestion = currentQuestion,
                        userSelections = userSelections,
                        isAcceptButtonClicked = isAcceptButtonClicked,
                        immediateResults = immediateResults,
                        quizTerminado = quizTerminado
                    )
                    println("userSelections: $userSelections")
                    if (userSelections.size == currentQuestion.conceptosYDefiniciones.size && remainingTime > 0) {
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
                                println("selectedAnswer: $selectedAnswer")
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
                                println("selectedOption: $selectedOption")
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
                                for ((key, correctValue) in currentQuestion.emparejamientos) {
                                    if (userSelections[key] != correctValue) {
                                        isAllCorrect = false
                                        break // Salir del bucle en cuanto se encuentre un error
                                    }
                                }

                                if (isAllCorrect) localIsAnswerCorrect = true

                                Log.d("userSelections", "valor de userSelections: ${userSelections}")
                                // Hacer una copia de los datos antes de guardarlos en la base de datos
                                val respuestasParaGuardar = userSelections.toMap() // Crea una copia inmutable

                                userSelections.let {
                                    quizViewModel.actualizarUserAnswers(
                                        codigoQuiz.toString(),
                                        currentQuestionIndex,
                                        userId,
                                        respuestasParaGuardar
                                    )
                                }

                            }

                            TipoPregunta.COMPLETAR_PALABRAS -> {
                                if (userInputs.sorted() == currentQuestion.opcionesCorrectasCompletarPalabras.sorted()) localIsAnswerCorrect = true

                                // Hacer una copia de los datos antes de guardarlos en la base de datos
                                val respuestasParaGuardar = userInputs.toList() // Crea una copia inmutable

                                // Guardar las respuestas de manera persistente en la base de datos
                                quizViewModel.actualizarUserAnswers(
                                    codigoQuiz.toString(),
                                    currentQuestionIndex,
                                    userId,
                                    respuestasParaGuardar // Usa la copia de los datos
                                )
                            }

                            TipoPregunta.ASOCIACION -> {
                                val isAllCorrect = currentQuestion.conceptosYDefiniciones.all { (key, correctValue) ->
                                    userSelections[key] == correctValue
                                }

                                Log.d("isAllCorrect asociar", "valor de isAllCorrect: $isAllCorrect")

                                if (isAllCorrect) localIsAnswerCorrect = true

                                // Hacer una copia de los datos antes de guardarlos en la base de datos
                                val respuestasParaGuardar = userSelections.toMap() // Crea una copia inmutable

                                // Guardar las respuestas de manera persistente en la base de datos
                                quizViewModel.actualizarUserAnswers(
                                    codigoQuiz.toString(),
                                    currentQuestionIndex,
                                    userId,
                                    respuestasParaGuardar // Usa la copia de los datos
                                )
                            }
                        }
                        // Asigna el mensaje de resultado dependiendo de si la respuesta es correcta o no
                        resultMessage = if (localIsAnswerCorrect) {
                            context.getString(R.string.respuesta_correcta)
                        } else {
                            context.getString(R.string.respuesta_incorrecta)
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
                        if (!immediateResults && isAcceptButtonClicked) {
                            // Si immediateResults es false, avanzar automáticamente
                            if (currentQuestionIndex < preguntas.size - 1) {
                                currentQuestionIndex++
                                // Reinicia los estados para la próxima pregunta
                                selectedAnswer = null
                                isAnswerCorrect = null
                                isAnswerSelected = false
                                isAcceptButtonClicked = false
                                enableAcept = false
                                trueButtonColor = Color.Unspecified
                                falseButtonColor = Color.Unspecified
                                resultMessage = ""

                                // Limpiar los datos temporales después de guardar las respuestas en la base de datos
                                userInputs.clear()  // Esto no afectará a las respuestas almacenadas en la base de datos
                                userSelections.clear()
                            }

                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                    ),
                    enabled = !isAcceptButtonClicked && enableAcept
                ) {
                    Text(text = stringResource(R.string.aceptar))
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

                            // Limpiar los datos temporales después de guardar las respuestas en la base de datos
                            userInputs.clear()  // Esto no afectará a las respuestas almacenadas en la base de datos
                            userSelections.clear()

                            if (currentQuestionIndex < preguntas.size - 1) {
                                // Avanzar a la siguiente pregunta
                                currentQuestionIndex++
                            } else {


                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        enabled = isAcceptButtonClicked && (currentQuestionIndex < preguntas.size - 1), // Habilitar solo si se presionó "Aceptar"
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(text = stringResource(R.string.siguiente_pregunta))
                    }
                }
            }
        }
    }
}