package com.example.quizec.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.data.model.Pregunta
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DeleteQuestionsScreen(
    navController: NavHostController,
    cuestionarioId: String,
    quizViewModel: QuizViewModel,
) {
    // Usamos `preguntas` desde el ViewModel que está observando el estado de la lista
    val preguntas by remember { derivedStateOf { quizViewModel.preguntas } }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var preguntaSeleccionada by remember { mutableStateOf<Pregunta?>(null) }

    // Cargar las preguntas al iniciar
    LaunchedEffect(cuestionarioId) {
        quizViewModel.cargarPreguntasPorCodigo(cuestionarioId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // LazyColumn con las preguntas
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .weight(1f), // Esto hace que LazyColumn ocupe el espacio disponible
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(preguntas) { pregunta ->
                PreguntaCard(
                    pregunta = pregunta,
                    onPreguntaClick = { preguntaSeleccionada = pregunta }
                )
            }
        }

        // Espaciado entre el LazyColumn y el botón
        Spacer(modifier = Modifier.height(16.dp))

        // Botón de volver
        Button(
            onClick = {
                navController.navigate("editCuestionario/$cuestionarioId")
            },
            modifier = Modifier.padding(16.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
        ) {
            Text(text = "Volver")
        }
    }

    // Mostrar diálogo si hay una pregunta seleccionada
    if (preguntaSeleccionada != null) {
        ConfirmDeleteDialog(
            pregunta = preguntaSeleccionada!!,
            onConfirm = {
                if (userId != null) {
                    // Eliminar la pregunta seleccionada
                    quizViewModel.eliminarPreguntaCuestionario(
                        cuestionarioId = cuestionarioId,
                        preguntaId = preguntaSeleccionada!!.id,
                        onSuccess = {
                            // La UI se actualizará automáticamente porque preguntas es un State observado
                            println("Pregunta eliminada correctamente")
                        }
                    )
                }
                preguntaSeleccionada = null
            },
            onDismiss = { preguntaSeleccionada = null }
        )
    }
}




@Composable
fun PreguntaCard(pregunta: Pregunta, onPreguntaClick: (Pregunta) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onPreguntaClick(pregunta) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6200EE) // Morado
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = pregunta.titulo,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    pregunta: Pregunta,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Eliminar Pregunta") },
        text = { Text(text = "¿Estás seguro de que deseas eliminar la pregunta: \"${pregunta.titulo}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Eliminar", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

