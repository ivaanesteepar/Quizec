package com.example.quizec.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ResultsScreen(
    navController: NavHostController,
    usersViewModel: UsersViewModel,
    codigoQuiz: String?
) {
    // Estado para almacenar la lista de usuarios y sus respuestas correctas
    val (usuariosConRespuestas, setUsuariosConRespuestas) = remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    // Usuario autenticado
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Nombre del usuario
    var userName by remember { mutableStateOf("Usuario Anónimo") }

    // Estado para el valor de immediateResults
    var immediateResults by remember { mutableStateOf(false) }

    // ViewModel para historial
    val quizViewModel = remember { QuizViewModel() }

    // Obtener el valor de immediateResults
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            immediateResults = quizViewModel.obtenerImmediateResults(codigoQuiz)
        }
    }

    // Una vez entrado en esta pantalla, debe guardarse el cuestionario en el historial del usuario
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            quizViewModel.obtenerNombreUsuario(userId) { nombre ->
                userName = nombre ?: "Usuario Anónimo"
                println("Nombre del usuario: $userName")

                // Guardar el cuestionario en el historial después de obtener el nombre del usuario
                if (codigoQuiz != null) {
                    quizViewModel.guardarCuestionarioEnHistorial(userId, codigoQuiz)
                }
            }
        }
    }

    // Escuchar datos del quiz para los cambios de las respuestas correctas o si entra algun usuario nuevo
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            usersViewModel.escucharNombreYRespuestasCorrectas(codigoQuiz) { usuarios ->
                setUsuariosConRespuestas(usuarios)
            }
        }
    }

    // Interfaz de usuario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Título
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Lista de resultados
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
            ) {
                items(usuariosConRespuestas.size) { index ->
                    val (nombre, respuestasCorrectas) = usuariosConRespuestas[index]

                    val backgroundColor = when (index) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        2 -> MaterialTheme.colorScheme.tertiary
                        else -> Color.LightGray
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "${index + 1}. $nombre - $respuestasCorrectas respuestas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (index < 3) Color.White else Color.Black
                        )
                    }
                }
            }
        }
        println("immediateResults en results: $immediateResults")

        // Condición para mostrar el botón "Ver resultados" si immediateResults es false
        if (!immediateResults) {
            Button(
                onClick = {
                    navController.navigate("answers/$codigoQuiz")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Ver resultados")
            }
        }

        // Botón para regresar
        Button(
            onClick = {
                if (userId.isNotEmpty() && codigoQuiz != null) {
                    usersViewModel.eliminarUsuarioDeQuiz(codigoQuiz)
                }
                navController.navigate("home")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = "Volver al Home")
        }
    }
}


