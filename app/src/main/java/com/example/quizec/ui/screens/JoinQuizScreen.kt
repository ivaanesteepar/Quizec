package com.example.quizec.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun JoinQuizScreen(navController: NavHostController, quizViewModel: QuizViewModel) {
    var codigoQuiz by remember { mutableStateOf("") }  // Estado para almacenar el código del quiz
    var errorMessage by remember { mutableStateOf("") }  // Mensaje de error si el código es vacío
    var loading by remember { mutableStateOf(false) }  // Estado para manejar la carga de la verificación

    // Usamos un scope para las funciones suspendidas en composables
    val coroutineScope = rememberCoroutineScope()

    // Función para unirse al quiz
    fun unirseAlQuiz() {
        if (codigoQuiz.isEmpty()) {
            errorMessage = "Por favor, ingrese el código del quiz."
        } else {
            loading = true

            // Usamos LaunchedEffect para ejecutar código suspendido en un Composable
            coroutineScope.launch {
                try {
                    // Llamada a la función suspendida para obtener immediateAccess
                    val immediateAccess = quizViewModel.obtenerImmediateAccess(codigoQuiz)
                    loading = false
                    println("immediateAccess: $immediateAccess")
                    println("codigoQuiz: $codigoQuiz")

                    if (immediateAccess == null) {
                        errorMessage = "Error al verificar el acceso."
                    } else if (immediateAccess == false) {
                        // Si immediateAccess es false, navegar a la pantalla restringida
                        navController.navigate("waiting_screen/$codigoQuiz")
                    } else {
                        // Si immediateAccess es true, navegar a la pantalla de espera
                        navController.navigate("user_quiz/$codigoQuiz")
                    }
                } catch (e: Exception) {
                    loading = false
                    errorMessage = "Hubo un error al verificar el acceso."
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Unirse a un Quiz", style = MaterialTheme.typography.titleLarge)

            Text("Código del Quiz", style = MaterialTheme.typography.bodyMedium)
            BasicTextField(
                value = codigoQuiz,
                onValueChange = { codigoQuiz = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(12.dp)
            )

            Button(
                onClick = { unirseAlQuiz() },
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("Unirse al Quiz")
            }

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            }

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Text("Volver", color = Color.White)
            }
        }
    }
}





