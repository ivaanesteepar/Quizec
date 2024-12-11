package com.example.quizec.ui.screens

import android.os.Bundle
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
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AnswerResultScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    selectedAnswer: String?,
    isAnswerCorrect: Boolean?,
    codigoQuiz: String?
) {
    // Variables para manejar el índice de la pregunta y las respuestas
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var resultMessage by remember { mutableStateOf("") }
    var buttonPressed by remember { mutableStateOf(selectedAnswer ?: "") }

    // Cargar preguntas desde el ViewModel cuando el código del cuestionario cambia
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null && userId != null) {
            quizViewModel.iniciarQuiz(codigoQuiz, userId)
            quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
        }
    }

    // Obtener las preguntas cargadas desde el ViewModel
    val preguntasState by rememberUpdatedState(quizViewModel.preguntas)

    // Lógica para determinar si la respuesta es correcta o incorrecta
    resultMessage = if (isAnswerCorrect == true) {
        "¡Respuesta correcta!"
    } else {
        "Respuesta incorrecta"
    }

    // Obtenemos la pregunta actual
    val currentQuestion = preguntasState.getOrNull(currentQuestionIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostrar el número de pregunta
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Pregunta ${currentQuestionIndex + 1} de ${preguntasState.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp
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

                // Mostrar la imagen de la pregunta si tiene una
                currentQuestion.imagen?.let { imageUrl ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Imagen de la pregunta",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Ajusta el tamaño de la imagen
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mostrar el botón pulsado por el usuario
                Text(
                    text = "Botón pulsado: $buttonPressed",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Mostrar si la respuesta es correcta o incorrecta
                Text(
                    text = resultMessage,
                    color = if (isAnswerCorrect == true) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón para continuar a la siguiente pregunta o ir a los resultados
                Button(
                    onClick = {
                        if (currentQuestionIndex < preguntasState.size - 1) {
                            // Si no es la última pregunta, avanzar
                            currentQuestionIndex++
                        } else {
                            // Si es la última pregunta, navegar a los resultados
                            navController.navigate("results_screen/$codigoQuiz")
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = "Siguiente pregunta")
                }
            }
        } else {
            // Mientras se cargan las preguntas
            Text("Cargando preguntas...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
