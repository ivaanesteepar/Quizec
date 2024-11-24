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
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun JoinQuizScreen(navController: NavHostController, quizViewModel: QuizViewModel) {
    var codigoQuiz by remember { mutableStateOf("") }  // Estado para almacenar el código del quiz
    var errorMessage by remember { mutableStateOf("") }  // Mensaje de error si el código es vacío

    // Obtener el nombre del jugador desde Firebase Authentication
    val auth = FirebaseAuth.getInstance()
    val nombreJugador = auth.currentUser?.displayName ?: "Jugador Anónimo"  // Si no hay nombre, mostrar "Jugador Anónimo"

    // Observamos el estado del mensaje de error desde el ViewModel
    val errorState by quizViewModel.errorMessage.collectAsState()

    fun unirseAlQuiz() {
        if (codigoQuiz.isEmpty()) {
            errorMessage = "Por favor, ingrese el código del quiz."
        } else {
            quizViewModel.verificarCodigoQuiz(codigoQuiz) { esValido ->
                if (esValido) {
                    // Actualizar el rol del usuario a PARTICIPANTE en Firestore
                    quizViewModel.actualizarRolUsuario("PARTICIPANTE") {
                        if (it == null) {
                            // Si se actualiza el rol correctamente, navegar a la pantalla de espera
                            navController.navigate("waiting_screen/$codigoQuiz")
                        } else {
                            // Mostrar un mensaje de error si falla la actualización
                            errorMessage = "Error al unirse al quiz: ${it}"
                        }
                    }
                } else {
                    errorMessage = "Código de quiz inválido o no encontrado."
                }
            }
        }
    }


    // Caja para centrar el contenido
    Box(
        modifier = Modifier
            .fillMaxSize()  // Ocupa toda la pantalla
            .padding(16.dp)
            .wrapContentSize(Alignment.Center)  // Centra el contenido dentro de la pantalla
    ) {
        // Contenedor de Column con un diseño centrado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),  // Asegura que la columna no se estire demasiado verticalmente
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)  // Reduce el espaciado entre los elementos (4dp)
        ) {
            Text("Unirse a un Quiz", style = MaterialTheme.typography.titleLarge)

            // Campo para ingresar el código del quiz
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

            // Botón para unirse al quiz
            Button(
                onClick = { unirseAlQuiz() },
                modifier = Modifier.padding(bottom = 4.dp)  // Reducir el padding alrededor del botón
            ) {
                Text("Unirse al Quiz")
            }

            // Mensaje de error si el código está vacío o el quiz no es válido
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            }

            // Botón para volver a la pantalla de inicio
            Button(
                onClick = {
                    navController.navigate("home")  // Navegar a la pantalla de inicio
                },
                modifier = Modifier
                    .padding(top = 1.dp)  // Reducir el padding alrededor del botón
            ) {
                Text("Volver", color = Color.White)
            }
        }
    }
}




