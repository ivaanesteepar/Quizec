package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.screens.CreatorResults.BarChartBar
import com.example.quizec.ui.screens.CreatorResults.FillBlankResults
import com.example.quizec.ui.screens.CreatorResults.MatchingAssociationResults
import com.example.quizec.ui.screens.CreatorResults.MissingWordsResults
import com.example.quizec.ui.screens.CreatorResults.MultiAnswerBarChart
import com.example.quizec.ui.screens.CreatorResults.MultiChoiceBarChart
import com.example.quizec.ui.screens.CreatorResults.OrderingResults
import com.example.quizec.ui.theme.buttonColor

import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import kotlinx.coroutines.launch

@Composable
fun CreatorQuizzesScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String?
) {
    val coroutineScope = rememberCoroutineScope()
    val usersViewModel = UsersViewModel()
    // Estado para almacenar el número de usuarios
    var numeroUsuarios by remember { mutableStateOf(0) }

    // Cargar las respuestas cuando el código de cuestionario cambia
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            usersViewModel.contarUsuariosEnEsperaTiempoReal(codigoQuiz) { count, exception ->
                if (exception != null) {
                    Log.e("CreatorQuizzesScreen", "Error al contar usuarios: ${exception.message}")
                } else {
                    numeroUsuarios = count ?: 0
                    Log.d("CreatorQuizzesScreen", "Número de usuarios en espera: $numeroUsuarios")
                }
            }

            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
            quizViewModel.obtenerRespuestasDelCuestionario(codigoQuiz)
        }
        else{
            print("Codigo quiz es null")
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
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize() // Esto hace que la columna ocupe tdo el tamaño de la pantalla
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

        // Mostrar el número de usuarios
        Text(
            text = stringResource(R.string.numero_usuarios, numeroUsuarios),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))


        if (preguntas.isNotEmpty()) {
            // Mostrar el número de la pregunta actual
            Text(
                text = stringResource(R.string.numero_pregunta, currentIndex + 1),
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

            Text(
                text = stringResource(
                    R.string.numero_respuestas,
                    respuestasUsuarioPregunta.size,
                    numeroUsuarios
                ),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                    MultiChoiceBarChart(
                        questionIndex = currentIndex,
                        totalResponses = respuestasUsuarioPregunta.size,
                        opcionesPregunta = pregunta.opciones, // Lista de opciones de respuesta
                        hayRespuestas = respuestasUsuarioPregunta.isNotEmpty(), // Verificar si hay respuestas
                        respuestasUsuarioPregunta = respuestasUsuarioPregunta,
                        respuestasCorrectas = pregunta.respuestasCorrectas // Opción correcta para esta pregunta
                    )
                }

                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                    MultiAnswerBarChart(
                        questionIndex = currentIndex,
                        totalResponses = respuestasUsuarioPregunta.size,
                        opcionesPregunta = pregunta.opciones,
                        hayRespuestas = respuestasUsuarioPregunta.isNotEmpty(),
                        respuestasUsuarioPregunta = respuestasUsuarioPregunta,
                        respuestasCorrectas = pregunta.respuestasCorrectas
                    )
                }

                TipoPregunta.EMPAREJAR -> {
                    MatchingAssociationResults(
                        respuestasUsuarioPregunta,
                        pregunta.tipo,
                        pregunta.conceptosYDefiniciones,
                        pregunta.emparejamientos
                    )
                }

                TipoPregunta.ASOCIACION -> {
                    MatchingAssociationResults(
                        respuestasUsuarioPregunta,
                        pregunta.tipo,
                        pregunta.conceptosYDefiniciones,
                        pregunta.emparejamientos
                    )
                }

                TipoPregunta.ORDENAR -> {
                    OrderingResults(respuestasUsuarioPregunta, pregunta.itemsOrdenados)
                }

                TipoPregunta.COMPLETAR_ESPACIOS -> {
                    FillBlankResults(respuestasUsuarioPregunta, pregunta.opcionCorrecta)
                }

                TipoPregunta.COMPLETAR_PALABRAS -> {
                    MissingWordsResults(respuestasUsuarioPregunta, pregunta.opcionesCorrectasCompletarPalabras)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir a la pregunta anterior
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { currentIndex-- },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor // Aplicamos el color de fondo del botón
                    ),
                    enabled = currentIndex > 0, // Deshabilitar si es la primera pregunta
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(stringResource(R.string.pregunta_anterior))
                }

                // Botón para ir al siguiente gráfico o finalizar el cuestionario
                if (currentIndex < preguntas.size - 1) {
                    Button(
                        onClick = {
                            currentIndex++ // Pasar al siguiente gráfico
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(stringResource(R.string.siguiente_pregunta))
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
                                            Log.d(
                                                "QuizScreen",
                                                "El quiz ha sido marcado como no iniciado."
                                            )
                                        } else {
                                            Log.e(
                                                "QuizScreen",
                                                "Hubo un error al actualizar el estado del quiz."
                                            )
                                        }
                                    }

                                }
                                // Después de finalizar el quiz, navegar a la pantalla de resultados
                                navController.navigate("results_screen/$codigoQuiz")
                            }
                            // Marcar que el quiz ha terminado
                            isQuizFinished = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                        ),

                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(stringResource(R.string.finalizar_cuestionario))
                    }
                }
            }
        } else {
            // Mensaje cuando se haya llegado al final
            Text(stringResource(R.string.has_visto_todas_las_preguntas))
        }
    }
}

