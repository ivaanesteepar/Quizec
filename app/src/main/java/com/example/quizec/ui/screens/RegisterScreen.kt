package com.example.quizec.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quizec.data.model.Rol
import com.example.quizec.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(navController: NavHostController) {
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val db = if (LocalInspectionMode.current) null else FirebaseFirestore.getInstance()

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(Rol.PARTICIPANTE) } // Valor inicial por defecto
    var isEmailEmpty by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Register", style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailEmpty = email.isEmpty()
                    },
                    label = { Text("Email") },
                    isError = isEmailEmpty,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isEmailEmpty) {
                    Text(
                        text = "El campo de correo es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordEmpty = password.isEmpty()
                    },
                    label = { Text("Password") },
                    isError = isPasswordEmpty,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isPasswordEmpty) {
                    Text(
                        text = "El campo de contraseña es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isEmailEmpty = email.isEmpty()
                        isPasswordEmpty = password.isEmpty()
                        if (!isEmailEmpty && !isPasswordEmpty && password == confirmPassword) {
                            if (auth != null && db != null) {
                                registerUser(
                                    auth = auth,
                                    db = db,
                                    nombre = nombre,
                                    email = email,
                                    password = password,
                                    rol = rol,
                                    onSuccess = {
                                        scope.launch {
                                            scaffoldState.showSnackbar("Registro exitoso")
                                        }
                                    },
                                    onError = { errorMessage ->
                                        scope.launch {
                                            scaffoldState.showSnackbar(errorMessage)
                                        }
                                    },
                                    navController = navController
                                )
                            }
                        } else if (isEmailEmpty) {
                            scope.launch {
                                scaffoldState.showSnackbar("El campo de correo es obligatorio")
                            }
                        } else if (isPasswordEmpty) {
                            scope.launch {
                                scaffoldState.showSnackbar("El campo de contraseña es obligatorio")
                            }
                        } else {
                            scope.launch {
                                scaffoldState.showSnackbar("Las contraseñas no coinciden")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar")
                }
            }
        }
    )
}

private fun registerUser(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    nombre: String,
    email: String,
    password: String,
    rol: Rol,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    navController: NavHostController
) {
    db.collection("users")
        .whereEqualTo("nombre", nombre)
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val usuario = Usuario(
                                id = user?.uid ?: "",
                                nombre = nombre,
                                correo = email,
                                rol = Rol.PARTICIPANTE
                            )

                            user?.uid?.let { uid ->
                                db.collection("users").document(uid).set(usuario)
                                    .addOnSuccessListener {
                                        onSuccess()
                                        navController.navigate("login")
                                    }
                                    .addOnFailureListener { e ->
                                        onError("Failed to save user data: ${e.message}")
                                    }
                            } ?: onError("User ID not found")
                        } else {
                            onError("Registration failed: ${task.exception?.message ?: "Unknown error"}")
                        }
                    }
            } else {
                onError("Nombre de usuario ya registrado")
            }
        }
        .addOnFailureListener { e ->
            onError("Failed to check username availability: ${e.message}")
        }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(navController = rememberNavController())
}
