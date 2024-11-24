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

    // Usar un LaunchedEffect para obtener los datos una vez que el composable se monta
    LaunchedEffect(key1 = codigoQuiz) {
        // Escuchar todos los usuarios y sus respuestas correctas
        usersViewModel.escucharNombreYRespuestasCorrectas(codigoQuiz ?: "") { usuarios ->
            setUsuariosConRespuestas(usuarios)
        }
    }

    // Ordenar la lista de usuarios por respuestas correctas (de mayor a menor)
    val sortedUsers = usuariosConRespuestas.sortedByDescending { it.second }

    // Obtener el userId para eliminar al usuario cuando se haga click en el botón
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Mostrar los resultados en la interfaz de usuario
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Título en la parte superior
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineLarge.copy(color = Color.Black),
            modifier = Modifier.padding(bottom = 32.dp) // Espacio entre el título y el contenido
        )

        // Mostrar la lista de resultados de los usuarios con LazyColumn
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f) // Limita la altura de la LazyColumn a un 60% de la pantalla
            ) {
                items(sortedUsers.size) { index ->
                    val (nombre, respuestasCorrectas) = sortedUsers[index]

                    // Destacar al usuario en primer lugar
                    val backgroundColor = when (index) {
                        0 -> MaterialTheme.colorScheme.primary // Mejor jugador, color sólido
                        1 -> MaterialTheme.colorScheme.secondary // Segundo lugar
                        2 -> MaterialTheme.colorScheme.tertiary // Tercer lugar
                        else -> Color.LightGray // Para los demás usuarios
                    }

                    // Contenedor de cada fila de usuario
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    ) {
                        // Texto con nombre y respuestas
                        Text(
                            text = "${index + 1}. $nombre - $respuestasCorrectas respuestas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (index < 3) Color.White else Color.Black // Colores especiales para los primeros 3
                        )
                    }
                }
            }
        }

        // Botón para regresar al home y eliminar al usuario
        Button(
            onClick = {
                // Eliminar al usuario antes de navegar
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
