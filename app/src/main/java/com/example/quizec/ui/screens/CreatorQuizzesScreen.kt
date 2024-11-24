package com.example.quizec.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.launch

@Composable
fun CreatorQuizzesScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String?
) {
    // Cargar las preguntas al principio
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Obtener las preguntas y respuestas del ViewModel
    val respuestasUsuario by quizViewModel.respuestas.collectAsState() // Utilizar collectAsState para obtener el estado actualizado
    val preguntas = quizViewModel.preguntas

    // Estado para controlar el progreso del cuestionario
    var currentIndex by remember { mutableStateOf(0) }
    var totalTime by remember { mutableStateOf(60) } // Tiempo total del quiz (60 segundos)
    var remainingTime by remember { mutableStateOf(totalTime) }

    // Control para mostrar gráficos y resultados
    var isQuizFinished by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (preguntas.isNotEmpty()) {
            // Mostrar el número de la pregunta actual
            Text(
                text = "Pregunta ${currentIndex + 1}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mostrar el título de la pregunta actual
            val pregunta = preguntas[currentIndex]
            Text(
                text = pregunta.titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val respuestasCorrectas = pregunta.respuestasCorrectas
            val respuesta = respuestasUsuario[pregunta.id]

            if (respuestasCorrectas.isNullOrEmpty()) {
                return@Column
            }

            // Lógica para determinar si la respuesta es correcta
            val esCorrecta = when (pregunta.tipo) {
                TipoPregunta.VERDADERO_FALSO -> {
                    val respuestaUsuario =
                        (respuesta as? List<*>)?.firstOrNull()?.toString()?.trim()
                    val respuestaCorrecta = respuestasCorrectas.firstOrNull()?.toString()?.trim()
                    respuestaUsuario == respuestaCorrecta
                }

                TipoPregunta.OPCION_MULTIPLE_UNA -> {
                    val respuestaUsuario =
                        (respuesta as? List<*>)?.firstOrNull()?.toString()?.trim()
                    val respuestaCorrecta = respuestasCorrectas.firstOrNull()?.toString()?.trim()
                    respuestaUsuario == respuestaCorrecta
                }

                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                    val respuestaUsuario = (respuesta as? List<*>)?.map { it.toString().trim() }
                    val respuestasCorrectasTrim = respuestasCorrectas.map { it.toString().trim() }
                    respuestaUsuario?.size == respuestasCorrectasTrim.size &&
                            respuestaUsuario?.containsAll(respuestasCorrectasTrim) == true
                }

                TipoPregunta.COMPLETAR_PALABRAS -> {
                    val respuestaUsuario = (respuesta as? List<*>)?.map { it.toString().trim() }
                    val respuestasCorrectasTrim = respuestasCorrectas.map { it.toString().trim() }
                    respuestaUsuario?.containsAll(respuestasCorrectasTrim) == true
                }

                else -> false
            }

            val correctAnswersCount = if (esCorrecta) 1 else 0
            val incorrectAnswersCount = if (!esCorrecta) 1 else 0
            val totalResponses = correctAnswersCount + incorrectAnswersCount

            // Mostrar gráfico solo si hay respuesta para esta pregunta
            BarChartBar(
                questionIndex = currentIndex,
                correctAnswersCount = correctAnswersCount,
                incorrectAnswersCount = incorrectAnswersCount,
                totalResponses = totalResponses,
                esCorrecta = esCorrecta,
                hayRespuestas = respuesta != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir al siguiente gráfico o finalizar el cuestionario
            Button(
                onClick = {
                    if (currentIndex < preguntas.size - 1) {
                        currentIndex++ // Pasar al siguiente gráfico
                    } else {
                        // Finalizar el cuestionario
                        isQuizFinished = true
                    }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(if (currentIndex < preguntas.size - 1) "Siguiente pregunta" else "Finalizar cuestionario")
            }
        } else {
            // Mensaje cuando se haya llegado al final
            Text("Has visto todas las preguntas")
        }

        // Botón para terminar el quiz
        if (isQuizFinished) {
            Button(
                onClick = {
                    // Launch a coroutine to call the suspend function endQuiz
                    coroutineScope.launch {
                        // Restablecer el estado de las respuestas y progreso
                        if (codigoQuiz != null) {
                            quizViewModel.endQuiz(codigoQuiz)
                        }
                        navController.navigate("results_screen/$codigoQuiz")
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Terminar Quiz")
            }
        }
    }
}

@Composable
fun BarChartBar(
    questionIndex: Int,
    correctAnswersCount: Int,
    incorrectAnswersCount: Int,
    totalResponses: Int, // Total de respuestas
    esCorrecta: Boolean, // Indica si la respuesta es correcta
    hayRespuestas: Boolean // Indica si hay respuestas disponibles para esta pregunta
) {
    val maxHeight = 300f // Altura máxima de las barras
    val barWidth = 30f // Ancho de las barras
    val spaceBetweenBars = 24f // Espacio entre las barras

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp) // Altura del gráfico
            .padding(16.dp) // Asegurarse de que esté centrado
    ) {
        // Dibujar los ejes
        drawLine(
            color = ComposeColor.Black,
            start = androidx.compose.ui.geometry.Offset(10f, maxHeight),
            end = androidx.compose.ui.geometry.Offset(size.width - 10f, maxHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = ComposeColor.Black,
            start = androidx.compose.ui.geometry.Offset(10f, 0f),
            end = androidx.compose.ui.geometry.Offset(10f, maxHeight),
            strokeWidth = 2f
        )

        // Verificar si hay respuesta para esta pregunta antes de dibujar las barras
        if (hayRespuestas && totalResponses > 0) {
            // Calcular la altura de las barras
            val correctBarHeight = maxHeight * (correctAnswersCount.toFloat() / totalResponses)
            val incorrectBarHeight = maxHeight * (incorrectAnswersCount.toFloat() / totalResponses)

            // Dibuja la barra para respuestas correctas
            drawRect(
                color = if (esCorrecta) ComposeColor.Green else ComposeColor.Gray,  // Verde si es correcta
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = (questionIndex * (barWidth + spaceBetweenBars)) + 10f,
                    y = maxHeight - correctBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = correctBarHeight)
            )

            // Dibuja la barra para respuestas incorrectas
            drawRect(
                color = if (!esCorrecta) ComposeColor.Red else ComposeColor.Gray,  // Rojo si es incorrecta
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = (questionIndex * (barWidth + spaceBetweenBars)) + 10f,
                    y = maxHeight - incorrectBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = incorrectBarHeight)
            )

            // Mostrar el número de respuestas correctas debajo de la barra de correctas
            drawContext.canvas.nativeCanvas.drawText(
                "$correctAnswersCount respuestas correctas",
                (questionIndex * (barWidth + spaceBetweenBars)) + 10f,
                maxHeight - correctBarHeight - 10f, // Posicionar debajo de la barra
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )

            // Mostrar el número de respuestas incorrectas debajo de la barra de incorrectas
            drawContext.canvas.nativeCanvas.drawText(
                "$incorrectAnswersCount respuestas incorrectas",
                (questionIndex * (barWidth + spaceBetweenBars)) + 10f,
                maxHeight - incorrectBarHeight - 10f, // Posicionar debajo de la barra
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )
        }
    }
}
