package com.example.quizec.ui

import android.content.Context
import android.location.LocationManager
import android.util.Log
import com.example.quizec.ui.screens.SelectCuestionarioScreen
import com.example.quizec.ui.screens.CreateQuestionsScreen
import com.example.quizec.ui.screens.LoginScreen
import com.example.quizec.ui.screens.CreateQuizScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizec.ui.screens.HomeScreen
import com.example.quizec.ui.screens.JoinQuizScreen
import com.example.quizec.ui.screens.RegisterScreen
import com.example.quizec.ui.viewmodel.QuizViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
import com.example.quizec.ui.screens.AnswerResultScreen
import com.example.quizec.ui.screens.SelectQuestionsScreen
import com.example.quizec.ui.screens.UserQuizzesScreen
import com.example.quizec.ui.screens.CreatorQuizzesScreen
import com.example.quizec.ui.screens.DeleteQuestionsScreen
import com.example.quizec.ui.screens.DetalleCuestionarioScreen
import com.example.quizec.ui.screens.EditarCuestionarioScreen
import com.example.quizec.ui.screens.EditarPreguntaScreen
import com.example.quizec.ui.screens.HistorialScreen
import com.example.quizec.ui.screens.ResultsScreen
import com.example.quizec.ui.screens.SelectQuestionsEditScreen
import com.example.quizec.ui.screens.WaitingScreen
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QuizecApp() {
    val navController = rememberNavController()  // Crea el NavController para manejar la navegación

    // Crea la instancia de QuizViewModel
    val quizViewModel: QuizViewModel = viewModel()
    val usersViewModel: UsersViewModel = viewModel()
    val questionsViewModel: QuestionsViewModel = viewModel()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "login", // Pantalla de inicio es el login
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(navController)  // Pantalla de login
            }
            composable("home") {
                HomeScreen(navController, quizViewModel)   // Pantalla de inicio
            }
            composable("creator_quiz/{quizCode}") { backStackEntry -> // Ruta para el quiz del creador
                val quizCode: String? = backStackEntry.arguments?.getString("quizCode")
                CreatorQuizzesScreen(navController, quizViewModel, quizCode)
            }
            composable("user_quiz/{codigoQuiz}") { backStackEntry -> // Ruta para el quiz del usuario
                val codigoQuiz = backStackEntry.arguments?.getString("codigoQuiz")
                if (codigoQuiz != null) {
                    UserQuizzesScreen(
                        navController = navController,
                        quizViewModel = quizViewModel,
                        codigoQuiz = codigoQuiz
                    )
                } else {
                    // Manejo en caso de que `codigoQuiz` sea null
                    Text("Error: No se recibió el código del cuestionario")
                }
            }

            composable("createQuiz") {
                // Pasa el ViewModel y el LocationManagerHandler a CreateQuizScreen
                CreateQuizScreen(navController, quizViewModel)
            }

            composable("joinQuiz") {
                JoinQuizScreen(navController, quizViewModel)  // Pantalla para unirse a un quiz
            }
            composable("make_questions") {
                // Pasa el ViewModel a com.example.quizec.ui.screens.com.example.quizec.ui.screens.CreateQuestionsScreen
                CreateQuestionsScreen(navController, quizViewModel)
            }
            composable("register") {
                RegisterScreen(navController)  // Pantalla de registro
            }
            composable("waiting_screen/{quizCode}") { backStackEntry -> // Ruta para la pantalla de espera
                val quizCode = backStackEntry.arguments?.getString("quizCode")
                WaitingScreen(
                    navController = navController,
                    codigoQuiz = quizCode ?: "N/A",
                    usersViewModel = viewModel()
                )  // Pasa el código del quiz a la pantalla de espera
            }
            composable("select_questions/{userId}") { // Ruta para la pantalla de preguntas
                // Recupera el userId de los argumentos
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    // Si el usuario está autenticado, pasa el userId a la pantalla de preguntas
                    SelectQuestionsScreen(
                        navController = navController,
                        userId = userId,
                        quizViewModel = quizViewModel
                    )
                }
            }
            composable("select_questions_edit/{userId}/{codigoQuiz}") { // Ruta para la pantalla de preguntas
                // Recupera el userId de los argumentos
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val codigoQuiz = it.arguments?.getString("codigoQuiz")
                if (userId != null) {
                    // Si el usuario está autenticado, pasa el userId a la pantalla de preguntas
                    if (codigoQuiz != null) {
                        SelectQuestionsEditScreen(
                            navController = navController,
                            userId = userId,
                            quizViewModel = quizViewModel,
                            codigoQuiz = codigoQuiz
                        )
                    }
                }
            }
            composable("results_screen/{codigoQuiz}") {
                val codigoQuiz = it.arguments?.getString("codigoQuiz")
                ResultsScreen(navController, usersViewModel, codigoQuiz)
            }
            composable("select_cuestionario") {
                SelectCuestionarioScreen(
                    navController,
                    quizViewModel,
                    questionsViewModel = viewModel()
                )
            }
            composable("editPregunta/{preguntaId}") { backStackEntry ->
                val preguntaId = backStackEntry.arguments?.getString("preguntaId") ?: ""
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val questionsViewModel = viewModel<QuestionsViewModel>()

                // Creamos un estado para almacenar la pregunta cargada
                val preguntaState = remember { mutableStateOf<Pregunta?>(null) }

                // Usamos LaunchedEffect para cargar la pregunta de forma asíncrona cuando el preguntaId cambie
                LaunchedEffect(preguntaId) {
                    preguntaState.value =
                        questionsViewModel.obtenerPregunta(preguntaId)  // Llamamos la función suspendida
                }

                // Mostramos la pantalla solo si la pregunta está cargada
                val pregunta = preguntaState.value

                if (pregunta != null) {
                    // Si la pregunta está cargada, mostramos la pantalla de edición
                    EditarPreguntaScreen(preguntaMod = pregunta, navController = navController)
                } else {
                    // Mientras la pregunta no esté cargada, podemos mostrar un indicador de carga o algo similar
                    CircularProgressIndicator()
                }
            }
            composable("historial/{userId}") {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    HistorialScreen(navController, quizViewModel, userId)
                } else {
                    Text("Error: No se pudo obtener el ID del usuario")
                }
            }
            composable("detalleCuestionarios/{cuestionarioId}") { backStackEntry ->
                val cuestionarioId =
                    backStackEntry.arguments?.getString("cuestionarioId") // Obtener el ID del cuestionario desde la ruta
                if (cuestionarioId != null) {
                    // Obtener el userId del usuario autenticado
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {
                        // Pasar los parámetros necesarios a la pantalla de detalles
                        DetalleCuestionarioScreen(
                            navController = navController,
                            codigoQuiz = cuestionarioId, // ID del cuestionario
                            userId = userId, // User ID del usuario autenticado
                            questionsViewModel = viewModel() // El ViewModel que contiene la lógica para las preguntas
                        )
                    } else {
                        // Manejar caso en el que el usuario no esté autenticado
                        Log.e("Navigation", "User not authenticated")
                    }
                } else {
                    // Manejar caso en el que no se recibe el cuestionarioId
                    Log.e("Navigation", "No cuestionarioId provided")
                }
            }
            composable("editCuestionario/{cuestionarioId}") { backStackEntry ->
                val cuestionarioId =
                    backStackEntry.arguments?.getString("cuestionarioId") // Obtener el ID del cuestionario desde la ruta
                var cuestionarioMod by remember { mutableStateOf<Cuestionario?>(null) } // Estado para almacenar el cuestionario cargado

                // Usamos un LaunchedEffect para cargar el cuestionario cuando el Id esté disponible
                LaunchedEffect(cuestionarioId) {
                    cuestionarioId?.let {
                        quizViewModel.obtenerCuestionario(cuestionarioId) { cuestionario ->
                            cuestionarioMod = cuestionario // Guardamos el cuestionario en el estado
                        }
                    }
                }

                // Esperamos a que el cuestionario se haya cargado
                if (cuestionarioMod != null) {
                    if (cuestionarioId != null) {
                        EditarCuestionarioScreen(
                            navController = navController,
                            cuestionarioId = cuestionarioId,
                            quizViewModel = quizViewModel,
                            cuestionarioMod = cuestionarioMod!!
                        )
                    }
                } else {
                    Text("Cargando...")
                }
            }
            composable("delete_questions/{cuestionarioId}") { backStackEntry ->
                val cuestionarioId = backStackEntry.arguments?.getString("cuestionarioId")
                if (cuestionarioId != null) {
                    //quizViewModel.eliminarPreguntasCuestionario(cuestionarioId)
                    DeleteQuestionsScreen(navController, cuestionarioId, quizViewModel)
                }
            }
            composable("answers/{codigoQuiz}") {
                val codigoQuiz = it.arguments?.getString("codigoQuiz")
                if (codigoQuiz != null) {
                    AnswerResultScreen(navController, quizViewModel, codigoQuiz)
                } else {
                    Text("Error: No se recibió el código del cuestionario")
                }
            }
        }
    }
}

