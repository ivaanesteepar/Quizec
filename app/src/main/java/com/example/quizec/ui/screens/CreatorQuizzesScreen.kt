package com.example.quizec.ui.screens

import android.util.Log
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
    val coroutineScope = rememberCoroutineScope()

    // Cargar las respuestas cuando el código de cuestionario cambia
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            quizViewModel.obtenerRespuestasDelCuestionario(codigoQuiz)
        }
    }

    // Obtener las respuestas desde el ViewModel
    val respuestasUsuario = quizViewModel.respuestasUsuario.value

    // Obtener las preguntas
    val preguntas = quizViewModel.preguntas

    // Estado para controlar el progreso del cuestionario
    var currentIndex by remember { mutableStateOf(0) }

    // Control para mostrar gráficos y resultados
    var isQuizFinished by remember { mutableStateOf(false) }

    //Codigo quiz
    Text(
        text = "$codigoQuiz",
        style = MaterialTheme.typography.headlineMedium
    )

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

            // Contar las respuestas correctas e incorrectas
            val respuestasUsuarioPregunta = respuestasUsuario.getOrNull(currentIndex) ?: emptyList()
            println("Respuestas del usuario: $respuestasUsuarioPregunta")

            // Verificar el tipo de pregunta
            when (pregunta.tipo) {
                TipoPregunta.VERDADERO_FALSO -> {
                    // Contadores para respuestas correctas e incorrectas
                    var correctAnswersCount = 0
                    var incorrectAnswersCount = 0

                    // Verificar las respuestas de los usuarios
                    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
                        val respuesta = respuestaUsuario["respuesta"] as? List<*> // Lista de respuestas
                        if (respuesta != null) {
                            respuesta.forEach {
                                val respuestaUsuarioTrim = it.toString().trim()

                                // Contar "Verdadero" y "Falso"
                                if (respuestaUsuarioTrim.equals("Verdadero", ignoreCase = true)) {
                                    correctAnswersCount++
                                } else if (respuestaUsuarioTrim.equals("Falso", ignoreCase = true)) {
                                    incorrectAnswersCount++
                                }
                            }
                        }
                    }

                    // Mostrar gráfico con las respuestas correctas e incorrectas
                    BarChartBar(
                        questionIndex = currentIndex,
                        correctAnswersCount = correctAnswersCount,
                        incorrectAnswersCount = incorrectAnswersCount,
                        totalResponses = respuestasUsuarioPregunta.size,
                        esCorrecta = correctAnswersCount > 0, // Si hay respuestas correctas
                        hayRespuestas = respuestasUsuarioPregunta.isNotEmpty(), // Verificar si hay respuestas
                        respuestasUsuarioPregunta = respuestasUsuarioPregunta
                    )
                }

                TipoPregunta.OPCION_MULTIPLE_UNA -> {
                    // Lógica para preguntas de opción múltiple
                    // Mostrar las opciones de respuesta y las barras de resultados
//                    MultiChoiceBarChart(
//                        questionIndex = currentIndex,
//                        totalResponses = respuestasUsuarioPregunta.size,
//                        opcionesPregunta = pregunta.opciones, // Lista de opciones
//                        respuestasUsuarioPregunta = respuestasUsuarioPregunta
//                    )
                }

                TipoPregunta.ASOCIACION -> {
                    // Recorrer las respuestas de los usuarios
                    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
                        // Obtener la lista de respuestas (ya en formato Map<String, String>)
                        val respuestas = respuestaUsuario["respuesta"] as? Map<String, String> // Aquí es un mapa de concepto -> definición

                        Log.d("CreatorQuizzesScreen", "Respuestas obtenidas: $respuestas") // Log para verificar si las respuestas existen

                        if (respuestas != null) {
                            // Mapa para contar las asociaciones de concepto-definición
                            val conteoAsociaciones = mutableMapOf<String, Int>()

                            Log.d("CreatorQuizzesScreen", "Inicializando el conteo de asociaciones...") // Log para verificar si se entra en este bloque

                            // Iterar sobre las respuestas de los usuarios
                            respuestas.forEach { (concepto, definicion) ->
                                Log.d("CreatorQuizzesScreen", "Respuesta: concepto = $concepto, definición = $definicion") // Log para verificar los valores

                                // Crear una clave única para la combinación concepto-definición
                                val asociacion = "$concepto - $definicion"

                                // Incrementar el contador de esa combinación si ya existe, o agregarla si no
                                conteoAsociaciones[asociacion] = conteoAsociaciones.getOrDefault(asociacion, 0) + 1

                                Log.d("CreatorQuizzesScreen", "Asociación contada: $asociacion, Total: ${conteoAsociaciones[asociacion]}") // Log para verificar el conteo
                            }

                            // Ordenar las asociaciones por la cantidad de veces que fueron elegidas, en orden descendente
                            val resultadosOrdenados = conteoAsociaciones.entries
                                .sortedByDescending { it.value } // Ordenar por el número de elecciones
                                .map { "${it.key}: ${it.value} elecciones" } // Crear representación legible

                            Log.d("CreatorQuizzesScreen", "Resultados ordenados: $resultadosOrdenados") // Log para verificar los resultados ordenados

                            // Mostrar los resultados ordenados
                            println("Resultados: \n" + resultadosOrdenados.joinToString("\n"))
                            Log.d("CreatorQuizzesScreen", "Resultados en consola: ${resultadosOrdenados.joinToString("\n")}") // Log para verificar la salida en consola

                            // Mostrar los resultados en la UI
                            Column {
                                Text(text = "Resultados de las asociaciones:")
                                resultadosOrdenados.forEach { resultado ->
                                    Text(text = resultado)
                                }
                            }
                        } else {
                            Log.d("CreatorQuizzesScreen", "No se encontraron respuestas válidas en la lista.") // Log para verificar si las respuestas son nulas o vacías
                        }
                    }
                }


                // Otros tipos de preguntas pueden ser manejados aquí
                else -> {
                    // Manejo de otro tipo de preguntas
                    Text(text = "Otro tipo de pregunta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir al siguiente gráfico o finalizar el cuestionario
            if (currentIndex < preguntas.size - 1) {
                Button(
                    onClick = {
                        currentIndex++ // Pasar al siguiente gráfico
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Siguiente pregunta")
                }
            } else {
                // Mostrar el botón de finalizar cuando se llegue a la última pregunta
                Button(
                    onClick = {
                        // Ejecutar lógica para finalizar el quiz
                        coroutineScope.launch {
                            if (codigoQuiz != null) {
                                quizViewModel.endQuiz(codigoQuiz)
                            }
                            // Después de finalizar el quiz, navegar a la pantalla de resultados
                            navController.navigate("results_screen/$codigoQuiz")
                        }
                        // Marcar que el quiz ha terminado
                        isQuizFinished = true
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Finalizar cuestionario")
                }
            }
        } else {
            // Mensaje cuando se haya llegado al final
            Text("Has visto todas las preguntas")
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
    hayRespuestas: Boolean, // Indica si hay respuestas disponibles para esta pregunta
    respuestasUsuarioPregunta: List<Map<String, Any>> // Las respuestas de los usuarios
) {
    val maxHeight = 300f // Altura máxima de las barras
    val barWidth = 80f // Ancho de las barras
    val spaceBetweenBars = 80f // Espacio entre las barras
    val additionalOffset = 300f // Desplazamiento adicional hacia la derecha

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

            // Dibujar la barra para respuestas correctas
            drawRect(
                color = ComposeColor.Green,  // Verde para respuestas correctas
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = (questionIndex * (barWidth + spaceBetweenBars)) + 10f + additionalOffset, // Mover más a la derecha
                    y = maxHeight - correctBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = correctBarHeight)
            )

            // Dibujar la barra para respuestas incorrectas
            drawRect(
                color = ComposeColor.Red,  // Rojo para respuestas incorrectas
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = (questionIndex * (barWidth + spaceBetweenBars)) + 10f + additionalOffset + barWidth + spaceBetweenBars, // Mover más a la derecha
                    y = maxHeight - incorrectBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = incorrectBarHeight)
            )

            // Definir las posiciones fijas para los números
            val correctCountPositionX = (questionIndex * (barWidth + spaceBetweenBars)) + 10f + additionalOffset + 32f
            val incorrectCountPositionX = (questionIndex * (barWidth + spaceBetweenBars)) + 10f + additionalOffset + 34f + barWidth + spaceBetweenBars

            // Posiciones fijas en Y para los números
            val fixedPositionY = maxHeight + 20f // Esta posición no cambiará

            // Mostrar el número de respuestas correctas debajo de la barra de correctas
            drawContext.canvas.nativeCanvas.drawText(
                "$correctAnswersCount",
                correctCountPositionX,  // Posición fija en X
                fixedPositionY, // Usar la posición fija en Y
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )

            // Mostrar el número de respuestas incorrectas debajo de la barra de incorrectas
            drawContext.canvas.nativeCanvas.drawText(
                "$incorrectAnswersCount",
                incorrectCountPositionX,  // Posición fija en X
                fixedPositionY, // Usar la posición fija en Y
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )

            // Crear un Set para almacenar respuestas únicas
            val respuestasUnicas = mutableSetOf<String>()

            // Procesar las respuestas del usuario
            respuestasUsuarioPregunta.forEachIndexed { index, respuestaUsuario ->
                val respuesta = respuestaUsuario["respuesta"] as? List<*> // Lista de respuestas del usuario
                if (respuesta != null) {
                    respuesta.forEach { respuestaTexto ->
                        val respuestaUsuarioTrim = respuestaTexto.toString().trim()

                        // Agregar "Verdadero" y "Falso" al Set si no son nulos ni vacíos
                        if (respuestaUsuarioTrim.isNotEmpty()) {
                            if (respuestaUsuarioTrim.equals("Verdadero", ignoreCase = true)) {
                                respuestasUnicas.add("Verdadero")
                            } else if (respuestaUsuarioTrim.equals("Falso", ignoreCase = true)) {
                                respuestasUnicas.add("Falso")
                            }
                        }
                    }
                }
            }

            // Mostrar las respuestas únicas debajo de cada barra
            val baseY = fixedPositionY + 30f // Posición base para las respuestas, ajustada con respecto al número

            respuestasUnicas.forEachIndexed { index, respuesta ->
                val offsetX = if (respuesta == "Verdadero") {
                    (questionIndex * (barWidth + spaceBetweenBars)) + 16f + additionalOffset
                } else {
                    (questionIndex * (barWidth + spaceBetweenBars)) + 33f + additionalOffset + barWidth + spaceBetweenBars
                }

                // Ajustar solo la posición de "Falso"
                val adjustedY = baseY

                // Dibujar las respuestas
                drawContext.canvas.nativeCanvas.drawText(
                    respuesta,
                    offsetX,
                    adjustedY, // Usar la posición ajustada para "Falso"
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 14f
                    }
                )
            }
        }
    }
}