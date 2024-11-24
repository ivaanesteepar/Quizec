package com.example.quizec.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizec.ui.screens.HomeScreen
import com.example.quizec.ui.screens.JoinQuizScreen
import com.example.quizec.ui.screens.RegisterScreen
import com.example.quizec.ui.viewmodel.QuizViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quizec.data.model.Pregunta
import com.example.quizec.ui.screens.SelectQuestionsScreen
import com.example.quizec.ui.screens.UserQuizzesScreen
import com.example.quizec.ui.screens.CreatorQuizzesScreen
import com.example.quizec.ui.screens.EditarPreguntaScreen
import com.example.quizec.ui.screens.ResultsScreen
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
                HomeScreen(navController)  // Pantalla de inicio
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
                // Pasa el ViewModel a CreateQuizScreen
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
            composable("results_screen/{codigoQuiz}") {
                val codigoQuiz = it.arguments?.getString("codigoQuiz")
                ResultsScreen(navController, usersViewModel, codigoQuiz)
            }
            composable("select_cuestionario") {
                SelectCuestionarioScreen(
                    navController,
                    quizViewModel,
                    questionsViewModel = viewModel(),
                    context = navController.context
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
                    preguntaState.value = questionsViewModel.obtenerPregunta(preguntaId)  // Llamamos la función suspendida
                }

                // Mostramos la pantalla solo si la pregunta está cargada
                val pregunta = preguntaState.value

                if (pregunta != null) {
                    // Si la pregunta está cargada, mostramos la pantalla de edición
                    EditarPreguntaScreen(preguntaMod = pregunta, userId = userId ?: "", navController = navController)
                } else {
                    // Mientras la pregunta no esté cargada, podemos mostrar un indicador de carga o algo similar
                    CircularProgressIndicator()
                }
            }


        }
    }
}
