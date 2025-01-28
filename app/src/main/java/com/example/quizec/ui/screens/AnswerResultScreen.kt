package com.example.quizec.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
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

    // Recolectamos el valor de quizTerminado desde el StateFlow
    //val quizTerminado by quizViewModel.quizTerminado.collectAsState()

    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            //quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
        }
    }

    preguntas = quizViewModel.preguntas

    Box(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (preguntas.isEmpty()) {
            Text(
                text = stringResource(R.string.no_hay_preguntas_disponibles),
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

                // Número de pregunta actual
                Text(
                    text = LocalContext.current.getString(R.string.pregunta_texto, currentQuestionIndex + 1, preguntas.size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
                // Show selected image if available
                if (currentQuestion.imagen != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = currentQuestion.imagen,
                        contentDescription = "Imagen cargada del servidor",
                        contentScale = ContentScale.Crop, // Ajusta la imagen para q aproveche tdo el tam
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = currentQuestion.titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )


                Spacer(modifier = Modifier.height(20.dp))

                println("Correct answer: ${currentQuestion.respuestasCorrectas}")

                val quizTerminado = true //para que en la ult pregunta no muestre las opc correctas

                when (currentQuestion.tipo) {
                    TipoPregunta.VERDADERO_FALSO -> {
                        val respuestaCorrecta = currentQuestion.respuestasCorrectas.firstOrNull() ?: "" // Obtener la primera respuesta correcta

                        TrueFalseQuestionScreen(
                            onSelectedAnswerChange = {},
                            falseButtonColor = if (respuestaCorrecta == "Falso") Color.Green else Color.Unspecified, // Si la respuesta correcta es "Falso", el botón "Falso" se pone verde
                            trueButtonColor = if (respuestaCorrecta == "Verdadero") Color.Green else Color.Unspecified, // Si la respuesta correcta es "Verdadero", el botón "Verdadero" se pone verde
                            isAcceptButtonClicked = true, // Asegúrate de que los botones no se deshabiliten, solo cambien de color
                            correctAnswer = respuestaCorrecta,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }
                    TipoPregunta.OPCION_MULTIPLE_UNA -> {
                        val respuestaCorrecta = currentQuestion.respuestasCorrectas.firstOrNull() ?: "" // Obtener la primera respuesta correcta

                        OneMultChoicesQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = true,
                            correctAnswer = respuestaCorrecta, // Pasamos la respuesta correcta como parámetro
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }

                    TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                        val respuestasCorrectas = currentQuestion.respuestasCorrectas.toSet() // Conjunto de respuestas correctas

                        MultChoicesQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedAnswer = currentQuestion.respuestasCorrectas,
                            onSelectedAnswerChange = {},
                            isAcceptButtonClicked = true,
                            correctAnswers = respuestasCorrectas.toList(), // Convertir el conjunto a una lista
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }

                    TipoPregunta.COMPLETAR_ESPACIOS -> {
                        FillBlankQuestionScreen(
                            currentQuestion = currentQuestion,
                            selectedOption = currentQuestion.opcionCorrecta,
                            onOptionSelected = {},
                            isAcceptButtonClicked = true,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }
                    TipoPregunta.ORDENAR -> {
                        OrderingQuestionScreen(
                            currentQuestion = currentQuestion,
                            userOrderedItems = {},
                            isAcceptButtonClicked = true,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }
                    TipoPregunta.EMPAREJAR -> {
                        MatchingQuestionScreen(
                            currentQuestion = currentQuestion,
                            userSelections = currentQuestion.emparejamientos.toMutableMap(),
                            isAcceptButtonClicked = true,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }

                    TipoPregunta.COMPLETAR_PALABRAS -> {
                        MissingWordsQuestionScreen(
                            currentQuestion = currentQuestion,
                            opcionesCorrectas = currentQuestion.opcionesCorrectasCompletarPalabras,
                            userInputs = currentQuestion.opcionesCorrectasCompletarPalabras.toMutableStateList(),
                            isAcceptButtonClicked = true,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }
                    TipoPregunta.ASOCIACION -> {
                        AssociationQuestionScreen(
                            currentQuestion = currentQuestion,
                            userSelections = currentQuestion.conceptosYDefiniciones.toMutableMap(),
                            isAcceptButtonClicked = true,
                            immediateResults = immediateResults,
                            quizTerminado = quizTerminado
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Botón para ir a la pregunta anterior
                    Button(
                        onClick = { currentQuestionIndex-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        enabled = currentQuestionIndex > 0, // Deshabilitar si es la primera pregunta
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(stringResource(R.string.pregunta_anterior))
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < preguntas.size - 1) {
                                currentQuestionIndex++
                            } else {
                                navController.navigate("results_screen/$codigoQuiz")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.siguiente_pregunta))
                    }
                }

            }
        }
    }
}