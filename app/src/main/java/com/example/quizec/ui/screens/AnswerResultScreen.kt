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
import com.example.quizec.ui.screens.UserQuestionTypes.*
import com.example.quizec.ui.theme.defaultButtonColor
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun AnswerResultScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String?
) {
    val usersViewModel = UsersViewModel()
    var preguntas by remember { mutableStateOf(emptyList<Pregunta>()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var immediateResults by remember { mutableStateOf(false) }

    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
        }
    }

    preguntas = quizViewModel.preguntas

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (preguntas.isEmpty()) {
            Text(
                text = "No hay preguntas disponibles",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val currentQuestion = preguntas[currentQuestionIndex]

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentQuestion.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                currentQuestion.imagen?.let { imageUrl ->
                    val file = File(Uri.parse(imageUrl).path ?: "")
                    if (file.exists()) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Imagen de la pregunta",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        )
                    } else {
                        Text(text = "Archivo no encontrado")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                println("Correct answer: ${currentQuestion.respuestasCorrectas}")

                when (currentQuestion.tipo) {
                    TipoPregunta.VERDADERO_FALSO -> {
                        val respuestaCorrecta = currentQuestion.respuestasCorrectas.firstOrNull() ?: "" // Obtener la primera respuesta correcta

                        TrueFalseQuestionScreen(
                            onSelectedAnswerChange = {},
                            falseButtonColor = if (respuestaCorrecta == "Falso") Color.Green else Color.Unspecified, // Si la respuesta correcta es "Falso", el botón "Falso" se pone verde
                            trueButtonColor = if (respuestaCorrecta == "Verdadero") Color.Green else Color.Unspecified, // Si la respuesta correcta es "Verdadero", el botón "Verdadero" se pone verde
                            isAcceptButtonClicked = false // Asegúrate de que los botones no se deshabiliten, solo cambien de color
                        )
                    }
                    TipoPregunta.OPCION_MULTIPLE_UNA -> {
                        val respuestaCorrecta = currentQuestion.respuestasCorrectas.firstOrNull() ?: "" // Obtener la primera respuesta correcta

                        OneMultChoicesScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = false,
                            // Aplicando color a los botones
                            buttonColors = currentQuestion.respuestasCorrectas.map { respuesta ->
                                if (respuesta == respuestaCorrecta) Color.Green else Color.Unspecified
                            }
                        )
                    }

                    TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                        val respuestasCorrectas = currentQuestion.respuestasCorrectas.toSet() // Conjunto de respuestas correctas

                        MultChoicesScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = false,
                            // Aplicando color a los botones
                            buttonColors = currentQuestion.respuestasCorrectas.map { respuesta ->
                                if (respuestasCorrectas.contains(respuesta)) Color.Green else Color.Unspecified
                            }
                        )
                    }

                    TipoPregunta.COMPLETAR_ESPACIOS -> {
                        FillBlankQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedOption = currentQuestion.opcionCorrecta,
                            onOptionSelected = {},
                            isAcceptButtonClicked = true
                        )
                    }
                    TipoPregunta.ORDENAR -> {
                        OrderingQuesitonScreen(
                            currentQuestion = currentQuestion,
                            userOrderedItems = {}
                        )
                    }
                    TipoPregunta.EMPAREJAR -> {
                        MatchingQuestionScreen(
                            currentQuestion = currentQuestion,
                            userSelections = currentQuestion.emparejamientos.associate { it.entries.first().toPair() }
                                .toMutableMap()
                        )
                    }
                    TipoPregunta.COMPLETAR_PALABRAS -> {
                        MissingWordsQuestion(
                            currentQuestion = currentQuestion,
                            opcionesCorrectas = currentQuestion.opcionesCorrectasCompletarPalabras,
                            userInputs = currentQuestion.opcionesCorrectasCompletarPalabras.toMutableStateList()
                        )
                    }
                    TipoPregunta.ASOCIACION -> {
                        // Añade aquí la lógica específica para manejar preguntas de tipo ASOCIACION
                        Text(text = "Lógica para preguntas de asociación no implementada.")
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (currentQuestionIndex < preguntas.size - 1) {
                            currentQuestionIndex++
                        } else {
                            navController.navigate("results_screen/$codigoQuiz")
                        }
                    }
                ) {
                    Text(text = "Siguiente pregunta")
                }
            }
        }
    }
}
