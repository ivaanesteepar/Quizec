package com.example.quizec.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.navigation.NavHostController
import com.example.quizec.data.model.Pregunta
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import java.util.*

@Composable
fun DetalleCuestionarioScreen(
    navController: NavHostController,
    codigoQuiz: String,
    questionsViewModel: QuestionsViewModel,
    userId: String
) {
    val preguntasState = remember { mutableStateOf<List<Pregunta>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val showDialog = remember { mutableStateOf(false) }
    val selectedPregunta = remember { mutableStateOf<Pregunta?>(null) }

    // Cargar las preguntas cuando se entra en la pantalla
    LaunchedEffect(codigoQuiz) {
        try {
            val preguntas = questionsViewModel.cargarPreguntasCuestionario(codigoQuiz)
            preguntasState.value = preguntas
            loading.value = false
        } catch (e: Exception) {
            Log.e("DetalleCuestionarioScreen", "Error al cargar preguntas", e)
            errorMessage.value = "Error al cargar las preguntas"
            loading.value = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (loading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Botón de "Volver" envuelto en un círculo
                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(16.dp)
                        .size(48.dp) // Tamaño del círculo
                        .clip(CircleShape) // Hace que el botón sea circular
                        .background(Color.Gray) // Color de fondo del círculo
                        .clickable {
                            navController.popBackStack() // Acción de volver
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White // Color del ícono (blanco)
                    )
                }

                Text(
                    text = "Cuestionario: ${codigoQuiz}",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when {
                    errorMessage.value != null -> {
                        Text(
                            text = errorMessage.value ?: "",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    preguntasState.value.isEmpty() -> {
                        Text(
                            text = "No hay preguntas disponibles.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(preguntasState.value) { pregunta ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    onClick = {
                                        selectedPregunta.value = pregunta
                                        showDialog.value = true
                                    }
                                ) {
                                    Text(
                                        text = pregunta.titulo,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color.White,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mostrar el diálogo de confirmación
        if (showDialog.value && selectedPregunta.value != null) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = {
                    Text(text = "¿Duplicar pregunta?")
                },
                text = {
                    Text(text = "¿Estás seguro de que quieres duplicar esta pregunta?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val preguntaDuplicada = selectedPregunta.value?.copy(id = UUID.randomUUID().toString()) // Duplica la pregunta
                            preguntaDuplicada?.let {
                                println("Pregunta duplicada es $preguntaDuplicada")
                                // Aquí puedes agregar la lógica para guardar la pregunta duplicada en la base de datos
                                questionsViewModel.duplicarPreguntaConUsuarioActual(it, userId)

                                // Mostrar el Toast de notificación
                                Toast.makeText(
                                    navController.context, // Contexto desde el NavController
                                    "Pregunta duplicada con éxito.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            showDialog.value = false
                        }
                    ) {
                        Text("Sí")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog.value = false }) {
                        Text("No")
                    }
                }
            )
        }

        // Botón de "Volver" en la parte inferior de la pantalla
        Button(
            onClick = {
                navController.navigate("home") // Volver a la pantalla anterior o Home
            },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alineación al fondo
                .padding(16.dp) // Margen alrededor
                .fillMaxWidth(), // El botón ocupa todo el ancho
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9C27B0) // Color morado para el botón
            )
        ) {
            Text(
                text = "Volver",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.White) // Texto blanco en el botón
            )
        }
    }
}
