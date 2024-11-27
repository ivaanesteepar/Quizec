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

    // ViewModel para historial
    val quizViewModel = remember { QuizViewModel() }

    // Obtener el nombre del usuario autenticado
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            quizViewModel.obtenerNombreUsuario(userId) { nombre ->
                userName = nombre ?: "Usuario Anónimo"
                println("Nombre del usuario: $userName")

                // Guardar el cuestionario en el historial después de obtener el nombre del usuario
                if (codigoQuiz != null) {
                    // Suponiendo que tienes el historialData en alguna variable (debes crearla o recuperarla)
                    val historialData = mapOf(
                        "codigoQuiz" to codigoQuiz,
                        "resultado" to usuariosConRespuestas // O lo que sea que desees guardar
                    )
                    quizViewModel.guardarCuestionarioEnHistorial(userId, codigoQuiz)
                }
            }
        }
    }

    // Escuchar datos del quiz
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            usersViewModel.escucharNombreYRespuestasCorrectas(codigoQuiz) { usuarios ->
                setUsuariosConRespuestas(usuarios)

            }
        }
    }

    // Obtener el cuestionario completo
    LaunchedEffect(codigoQuiz, userId) {
        if (codigoQuiz != null) {
            // Obtener el cuestionario y hacer println
            quizViewModel.obtenerCuestionarioPorCodigo(codigoQuiz, userId)
            val cuestionario = quizViewModel.cuestionario.value
            if (cuestionario != null) {
                println("Cuestionario: $cuestionario")
            } else {
                println("No se pudo obtener el cuestionario con el código: $codigoQuiz")
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

