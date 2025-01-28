package com.example.quizec.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ResultsScreen(
    navController: NavHostController,
    usersViewModel: UsersViewModel,
    codigoQuiz: String?
) {

    // Estado para almacenar la lista de usuarios y sus respuestas correctas.  Lista de pares (nombre, respuestas correctas)
    val (usuariosConRespuestas, setUsuariosConRespuestas) = remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }

    // Usuario autenticado
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Nombre del usuario
    var userName by remember { mutableStateOf("Usuario Anónimo") }

    // Estado para el valor de immediateResults
    var immediateResults by remember { mutableStateOf(false) }

    // ViewModel para historial
    val quizViewModel = remember { QuizViewModel() }

    val userRole by quizViewModel.userRole.collectAsState()

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
                setUsuariosConRespuestas(usuarios) // Actualizar la lista de usuarios con respuestas correctas
            }
        }
    }

    // Interfaz de usuario
    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Título
        Text(
            text = stringResource(R.string.leaderboard),
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

                // Filtrar y ordenar usuarios: excluir al creador y ordenar por respuestas correctas
                val usuariosOrdenados = usuariosConRespuestas
                    .filter { it.first != userName || userRole != Rol.CREADOR.toString() }
                    .sortedByDescending { it.second }

                items(usuariosOrdenados.size) { index ->
                    val (nombre, respuestasCorrectas) = usuariosOrdenados[index]

                    val backgroundColor = when (index) { // Cambiar el color de fondo según la posición
                        0 -> colorResource(id = R.color.logo_pink)
                        1 -> colorResource(id = R.color.logo_purple)
                        2 -> colorResource(id = R.color.logo_darkpurple)
                        else -> Color.LightGray
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                            .padding(16.dp)
                    ) {
                        Text( // Mostrar el nombre del usuario y sus respuestas correctas
                            text = "${index + 1}. $nombre - $respuestasCorrectas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (index < 3) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // Condición para mostrar el botón "Ver resultados" si immediateResults es false y solo si es participante
        if (userRole == Rol.PARTICIPANTE.toString() && !immediateResults) {
            Button(
                onClick = {
                    navController.navigate("answers/$codigoQuiz")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = stringResource(R.string.ver_resultados))
            }
        }

        BackHandler {} //no hace nada si da atrás

        // Botón para regresar, elimina los usuarios de la lista del quiz y vuelve al home
        Button(
            onClick = {
                if (userRole == Rol.CREADOR.toString()){
                    if (userId.isNotEmpty() && codigoQuiz != null) {
                        usersViewModel.eliminarUsuariosDeQuiz(codigoQuiz)
                    }
                }
                navController.navigate("home")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor // Aplicamos el color de fondo del botón
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(text = stringResource(R.string.volver_al_home))
        }
    }
}


