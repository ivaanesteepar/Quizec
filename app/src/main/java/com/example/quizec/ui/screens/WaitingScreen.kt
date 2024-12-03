
package com.example.quizec.ui.screens

import androidx.compose.foundation.border
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
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel

@Composable
fun WaitingScreen(navController: NavHostController, codigoQuiz: String?, usersViewModel: UsersViewModel) {
    val quizViewModel = remember { QuizViewModel() }
    val userRole by quizViewModel.userRole.collectAsState()

    val isQuizIniciado by quizViewModel.isQuizIniciado.collectAsState()
    if (codigoQuiz != null) {
        quizViewModel.getIsQuizIniciado(codigoQuiz) //obtiene de firebase y actualiza el estado del quiz
    }
    LaunchedEffect(isQuizIniciado) { //si cambia el estado del quiz, se ejecuta el código
        if (isQuizIniciado && userRole == Rol.PARTICIPANTE.toString()) {
            navController.navigate("user_quiz/$codigoQuiz") // Navegar al quiz del participante
        }
    }

    // Cuando cambia el código del quiz, agregamos el usuario y comenzamos a escuchar la lista
    LaunchedEffect(codigoQuiz) {
        if (codigoQuiz != null) {
            usersViewModel.agregarUsuarioAQuiz(codigoQuiz)
            usersViewModel.escucharUsuariosEnEspera(codigoQuiz)
        }
    }

    // Obtener la lista de usuarios en espera desde el ViewModel
    val usuariosEnEspera by usersViewModel.usuariosEnEspera.collectAsState()

    // Todo dentro de una sola columna
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp) // Espacio entre los elementos
    ) {
        // Título de jugadores en espera
        Text(
            text = "Esperando jugadores",
            style = MaterialTheme.typography.headlineMedium
        )
        //Codigo quiz
        Text(
            text = "$codigoQuiz",
            style = MaterialTheme.typography.headlineMedium
        )
        // Indicador de progreso circular animado
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.Gray,
            strokeWidth = 2.dp
        )

        LazyColumn(
            modifier = Modifier
                .width(250.dp)
                .height(250.dp) // Ajustar el tamaño de la lista para que no ocupe demasiado espacio
                .border(width = 2.dp, color = Color(0xFF800080)), // Borde morado
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(usuariosEnEspera) { nombre ->
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = if (usuariosEnEspera.indexOf(nombre) == 0) 8.dp else 0.dp)
                )
            }
        }

        // Botones debajo de la lista de usuarios, más cerca del centro
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp) // Espaciado entre los botones
        ) {
            // Botón "Iniciar Quiz"
            Button(
                onClick = {
                    if (userRole == Rol.CREADOR.toString()) {
                        quizViewModel.actualizarEstadoQuiz(codigoQuiz){ exito ->
                            if (exito){
                                println("El usuario es el creador. Navegando a creator_quiz.")
                                navController.navigate("creator_quiz/$codigoQuiz")
                            }
                            else{
                                println("Error al actualizar el estado del quiz.")
                            }

                        }
                    } else {
                        println("Rol desconocido: $userRole")
                    }
                },
                enabled = userRole != null && (userRole == Rol.CREADOR.toString()) //|| userRole == Rol.PARTICIPANTE.toString())
            ) {
                Text("Iniciar Quiz")
            }

            // Botón "Volver"
            Button(onClick = {
                // Eliminar al usuario de la lista en la base de datos
                if (codigoQuiz != null) {
                    usersViewModel.eliminarUsuarioDeQuiz(codigoQuiz) // Llamada para eliminar el usuario
                }
                // Volver a la pantalla anterior
                navController.popBackStack()
            }) {
                Text(text = "Volver")
            }
        }
    }
}
