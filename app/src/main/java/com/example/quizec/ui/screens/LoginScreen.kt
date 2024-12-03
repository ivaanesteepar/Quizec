package com.example.quizec.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quizec.R
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.data.model.Usuario
import com.example.quizec.data.model.Rol
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val viewModel: QuizViewModel = viewModel()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Login Background",
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f)), // Pantalla oscura
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Nombre de usuario",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF8A2BE2))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Password",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = Color.White
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF8A2BE2))
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (username.isEmpty() || password.isEmpty()) {
                        errorMessage = "Por favor, ingresa todos los campos."
                    } else {
                        if (auth != null) {
                            loginUser(username, password, auth, navController, { message ->
                                errorMessage = message
                                if (message.isEmpty()) {
                                    // Ya no es necesario llamar a viewModel.agregarUsuario aquí
                                    // El usuario se asigna dentro de com.example.quizec.ui.screens.loginUser
                                }
                            }, viewModel) // Pasamos el viewModel a com.example.quizec.ui.screens.loginUser
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de registro
            Button(
                onClick = {
                    // Navegar a la pantalla de registro
                    navController.navigate("register")
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = "Registro", color = Color.White)
            }
        }
    }
}

private fun loginUser(
    username: String,
    password: String,
    auth: FirebaseAuth,
    navController: NavHostController,
    onError: (String) -> Unit,
    viewModel: QuizViewModel // Pasamos el ViewModel
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .whereEqualTo("nombre", username) // Buscamos el nombre de usuario
        .get()
        .addOnSuccessListener { result ->
            if (result.isEmpty) {
                onError("Usuario no encontrado.")
            } else {
                val userDocument = result.documents.firstOrNull()
                val userEmail = userDocument?.getString("correo") // Obtenemos el correo del usuario

                if (userEmail != null) {
                    auth.signInWithEmailAndPassword(userEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Después de un login exitoso, asignar el usuario al ViewModel
                                FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
                                    val usuario = Usuario(
                                        id = firebaseUser.uid,
                                        nombre = firebaseUser.displayName ?: "Sin nombre",
                                        correo = firebaseUser.email ?: "No disponible", // Se usa el correo de Firebase
                                        rol = Rol.PARTICIPANTE // El rol por defecto puede ser PARTICIPANTE, o el que decidas
                                    )
                                    // Asignar el usuario al ViewModel
                                    viewModel.usuario.value = usuario
                                }

                                // Actualizar el rol del usuario en la base de datos (si es necesario)
                                db.collection("users").document(auth.currentUser!!.uid)
                                    .update("rol", Rol.PARTICIPANTE)
                                    .addOnFailureListener { e ->
                                        // Puedes manejar un posible error al actualizar el rol
                                        onError("Error al actualizar el rol: ${e.message}")
                                    }


                                // Navegar a la pantalla principal
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                onError("Error en el login: ${task.exception?.message ?: "Error desconocido"}")
                            }
                        }
                } else {
                    onError("Correo del usuario no encontrado.")
                }
            }
        }
        .addOnFailureListener { e ->
            onError("Error al obtener el correo del usuario: ${e.message}")
        }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = rememberNavController())
}
