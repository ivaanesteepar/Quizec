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
import com.example.quizec.ui.screens.CreatorResults.MatchingAssociationResults

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //Codigo quiz
        Text(
            text = "$codigoQuiz",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))


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
                    MultiChoiceBarChart(
                        questionIndex = currentIndex,
                        totalResponses = respuestasUsuarioPregunta.size,
                        opcionesPregunta = pregunta.opciones, // Lista de opciones de respuesta
                        hayRespuestas = respuestasUsuarioPregunta.isNotEmpty(), // Verificar si hay respuestas
                        respuestasUsuarioPregunta = respuestasUsuarioPregunta,
                        respuestasCorrectas = pregunta.respuestasCorrectas // Opción correcta para esta pregunta
                    )
                }

                TipoPregunta.EMPAREJAR -> {
                    MatchingAssociationResults(
                        respuestasUsuarioPregunta,
                        pregunta.tipo
                    )
                }

                TipoPregunta.ASOCIACION -> {
                    MatchingAssociationResults(
                        respuestasUsuarioPregunta,
                        pregunta.tipo
                    )
                }

//                TipoPregunta.ASOCIACION -> {
//                    AssociationResults(respuestasUsuarioPregunta)
//                    // Mapa para contar las asociaciones concepto-definición
//                    val conteoAsociaciones = mutableMapOf<String, Int>()
//
//                    // Procesar las respuestas de todos los usuarios
//                    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
//                        // Obtener el mapa de respuestas (concepto -> definición)
//                        val respuestas = respuestaUsuario["respuesta"] as? Map<String, String>
//
//                        respuestas?.forEach { (concepto, definicion) ->
//                            // Crear clave única para la asociación
//                            val asociacion = "$concepto - $definicion"
//                            conteoAsociaciones[asociacion] = conteoAsociaciones.getOrDefault(asociacion, 0) + 1
//                        }
//                    }
//
//                    // Ordenar las asociaciones por cantidad de selecciones
//                    val resultadosOrdenados = conteoAsociaciones.entries
//                        .sortedByDescending { it.value }
//                        .map { Pair(it.key, it.value) }
//
//                    // Mostrar los resultados en una UI profesional
//                    Column(
//                        modifier = Modifier
//                            .padding(16.dp)
//                            .fillMaxWidth()
//                    ) {
//                        Text(
//                            text = "Resultados",
//                            style = MaterialTheme.typography.headlineMedium,
//                            color = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.padding(bottom = 16.dp)
//                        )
//
//                        resultadosOrdenados.forEach { (asociacion, conteo) ->
//                            val partes = asociacion.split(" - ")
//                            val concepto = partes[0]
//                            val definicion = partes[1]
//
//                            // Cada fila con fondo diferenciado
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 4.dp)
//                                    .background(
//                                        color = MaterialTheme.colorScheme.surfaceVariant,
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(16.dp)
//                            ) {
//                                Row(
//                                    verticalAlignment = Alignment.CenterVertically,
//                                    modifier = Modifier.fillMaxWidth()
//                                ) {
//                                    // Concepto: como imagen o texto
//                                    Box(
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .padding(end = 8.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        if (concepto.startsWith("http")) {
//                                            AsyncImage(
//                                                model = concepto,
//                                                contentDescription = "Concepto como imagen",
//                                                modifier = Modifier.size(64.dp)
//                                            )
//                                        } else {
//                                            Text(
//                                                text = concepto,
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.onSurface,
//                                                textAlign = TextAlign.Center
//                                            )
//                                        }
//                                    }
//
//                                    // Definición: siempre como texto
//                                    Box(
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .padding(horizontal = 8.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = definicion,
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.onSurface,
//                                            textAlign = TextAlign.Center
//                                        )
//                                    }
//
//                                    // Contador de elecciones
//                                    Box(
//                                        modifier = Modifier
//                                            .weight(1f)
//                                            .padding(start = 8.dp),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = "$conteo ${if (conteo == 1) "elección" else "elecciones"}",
//                                            style = MaterialTheme.typography.bodyMedium,
//                                            color = MaterialTheme.colorScheme.secondary,
//                                            textAlign = TextAlign.Center
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
               // }



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
                                quizViewModel.endQuiz(codigoQuiz) // termina el juego para todos los usuarios
                                quizViewModel.limpiarCampos(codigoQuiz) // limpia las respuestas de los usuarios
                                quizViewModel.actualizarIsQuizIniciadoFalse(codigoQuiz) { success -> // actualiza el estado del quiz
                                    if (success) {
                                        Log.d("QuizScreen", "El quiz ha sido marcado como no iniciado.")
                                    } else {
                                        Log.e("QuizScreen", "Hubo un error al actualizar el estado del quiz.")
                                    }
                                }

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
    val additionalOffset = 170f // Desplazamiento adicional hacia la derecha

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

@Composable
fun MultiChoiceBarChart(
    questionIndex: Int,
    totalResponses: Int, // Total de respuestas
    opcionesPregunta: List<String>, // Opciones de la pregunta
    hayRespuestas: Boolean, // Indica si hay respuestas disponibles para esta pregunta
    respuestasUsuarioPregunta: List<Map<String, Any>>, // Respuestas de los usuarios
    respuestasCorrectas: List<String> // Lista de respuestas correctas (puede ser una o varias)
) {
    val maxHeight = 300f // Altura máxima de las barras
    val barWidth = 80f // Ancho de las barras
    val spaceBetweenBars = 35f // Espacio entre las barras
    val additionalOffset = 20f // Desplazamiento adicional hacia la derecha

    // Inicializamos un mapa para contar cuántas veces cada opción ha sido seleccionada
    val responseCounts = mutableMapOf<String, Int>()
    opcionesPregunta.forEach { opcion ->
        responseCounts[opcion] = 0
    }

    // Contamos cuántas veces cada opción ha sido seleccionada
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        println("RespuestaUsuario: $respuestaUsuario")

        // Acceder al primer valor de la lista dentro de la clave "respuesta"
        val respuesta = (respuestaUsuario["respuesta"] as? List<*>)?.firstOrNull() as? String

        if (respuesta != null && responseCounts.containsKey(respuesta)) {
            responseCounts[respuesta] = responseCounts[respuesta]!! + 1
            println("Respuesta seleccionada: $respuesta opciones de la pregunta: $opcionesPregunta con $responseCounts respuestas")
        } else {
            println("Respuesta no valida: $respuesta")
        }
    }

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

        // Verificamos si tenemos respuestas para esta pregunta
        if (totalResponses > 0) {
            // Calculamos la altura de las barras basada en las respuestas
            val maxCount = responseCounts.values.maxOrNull() ?: 0
            opcionesPregunta.forEachIndexed { index, opcion ->
                val count = responseCounts[opcion] ?: 0
                val barHeight = maxHeight * (count.toFloat() / totalResponses)

                // Determinamos si la opción es correcta
                val isCorrect = respuestasCorrectas.contains(opcion)

                // Dibujamos la barra para esta opción
                val barColor = if (isCorrect) {
                    ComposeColor.Green // Si la opción es correcta, la barra será verde
                } else {
                    ComposeColor.Blue // Si la opción no es correcta, la barra será azul
                }

                drawRect(
                    color = barColor, // Usamos el color determinado para las barras
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = (index * (barWidth + spaceBetweenBars)) + 10f + additionalOffset, // Ajuste en el eje X
                        y = maxHeight - barHeight // La altura de la barra dependerá del número de respuestas
                    ),
                    size = androidx.compose.ui.geometry.Size(width = barWidth, height = barHeight)
                )

                // Dibujamos el número de respuestas debajo de la barra
                drawContext.canvas.nativeCanvas.drawText(
                    "$count", // El número de respuestas seleccionadas para esta opción
                    (index * (barWidth + spaceBetweenBars)) + 11f + additionalOffset + 33f, // Posición en X
                    maxHeight + 21f, // Posición en Y para mostrar el número debajo de la barra
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 16f
                    }
                )

                // Dibujamos el nombre de la opción debajo de la barra
                drawContext.canvas.nativeCanvas.drawText(
                    opcion, // El texto de la opción (por ejemplo, "A", "B", "C", ... )
                    (index * (barWidth + spaceBetweenBars)) + 25f + additionalOffset + 11f, // Posición en X
                    maxHeight + 42f, // Posición en Y para mostrar la opción debajo del número
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 14f
                    }
                )
            }
        }
    }
}