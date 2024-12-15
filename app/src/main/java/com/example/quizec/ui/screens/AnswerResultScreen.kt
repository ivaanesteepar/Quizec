package com.example.quizec.ui.screens

import android.net.Uri
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
import com.example.quizec.ui.screens.UserQuestionTypes.FillBlankQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MatchingQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MissingWordsQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.MultChoicesQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.OneMultChoicesQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.OrderingQuestionScreen
import com.example.quizec.ui.screens.UserQuestionTypes.TrueFalseQuestionScreen
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

@Composable
fun AnswerResultScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String?
) {
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
                            isAcceptButtonClicked = true, // Asegúrate de que los botones no se deshabiliten, solo cambien de color
                            correctAnswer = respuestaCorrecta // Pasamos la respuesta correcta como parámetro
                        )
                    }
                    TipoPregunta.OPCION_MULTIPLE_UNA -> {
                        val respuestaCorrecta = currentQuestion.respuestasCorrectas.firstOrNull() ?: "" // Obtener la primera respuesta correcta

                        OneMultChoicesQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = true,
                            correctAnswer = respuestaCorrecta // Pasamos la respuesta correcta como parámetro
                        )
                    }

                    TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                        val respuestasCorrectas = currentQuestion.respuestasCorrectas.toSet() // Conjunto de respuestas correctas

                        MultChoicesQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = true,
                            correctAnswers = respuestasCorrectas.toList() // Convertir el conjunto a una lista
                        )
                    }

                    TipoPregunta.COMPLETAR_ESPACIOS -> {
                        FillBlankQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedOption = currentQuestion.opcionCorrecta,
                            onOptionSelected = {},
                            isAcceptButtonClicked = true,
                        )
                    }
                    TipoPregunta.ORDENAR -> {
                        OrderingQuestionScreen(
                            currentQuestion = currentQuestion,
                            userOrderedItems = {},
                            isAcceptButtonClicked = true
                        )
                    }
                    TipoPregunta.EMPAREJAR -> { //REVISALO PQ AL CAMBIAR emparejamientos NS SI SIGUE TU LOGICA, IVAN
                        MatchingQuestionScreen(
                            currentQuestion = currentQuestion,
                            userSelections = currentQuestion.emparejamientos.toMutableMap(),
                            isAcceptButtonClicked = true
                        )
                    }

                    TipoPregunta.COMPLETAR_PALABRAS -> {
                        MissingWordsQuestionScreen(
                            currentQuestion = currentQuestion,
                            opcionesCorrectas = currentQuestion.opcionesCorrectasCompletarPalabras,
                            userInputs = currentQuestion.opcionesCorrectasCompletarPalabras.toMutableStateList(),
                            isAcceptButtonClicked = true
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