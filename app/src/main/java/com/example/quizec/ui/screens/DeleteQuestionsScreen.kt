package com.example.quizec.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.QuizViewModel

@Composable
fun DeleteQuestionsScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    codigoQuiz: String
) {
    // Cargar preguntas al inicio
    LaunchedEffect(codigoQuiz) {
        quizViewModel.cargarPreguntasPorCodigo(codigoQuiz)
    }

    // Lista de preguntas obtenida del ViewModel (reactiva por mutableStateListOf)
    val preguntas = quizViewModel.preguntas
    val questionsViewModel = QuestionsViewModel()

    // Estados para los checkboxes
    val checkedStates = remember(preguntas) {
        mutableStateMapOf<String, Boolean>().apply {
            preguntas.forEach { pregunta ->
                put(pregunta.id, false) // Usamos un identificador único para cada pregunta
            }
        }
    }

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título centrado y grande
        Text(
            text = stringResource(R.string.eliminar_preguntas),
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de preguntas con checkboxes
        LazyColumn(modifier = Modifier
            .weight(1f)
            .fillMaxWidth()) {
            items(preguntas) { pregunta ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedStates[pregunta.id] == true,
                        onCheckedChange = { isChecked ->
                            checkedStates[pregunta.id] = isChecked
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = pregunta.titulo,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para eliminar preguntas seleccionadas
        Button(
            onClick = {
                // Obtener las preguntas seleccionadas
                val preguntasSeleccionadas = preguntas.filter { pregunta ->
                    checkedStates[pregunta.id] == true
                }

                // Llamar al ViewModel para eliminarlas
                questionsViewModel.eliminarPreguntasCuestionario(codigoQuiz, preguntasSeleccionadas)
                // Actualizar directamente las preguntas en quizViewModel
                val preguntasRestantes = preguntas.filter { pregunta ->
                    checkedStates[pregunta.id] != true
                }
                quizViewModel._preguntas.clear()
                quizViewModel._preguntas.addAll(preguntasRestantes)

                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.eliminar_preguntas_boton))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.volver))
        }

    }
}