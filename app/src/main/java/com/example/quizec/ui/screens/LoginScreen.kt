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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        // Si está en modo Landscape, usamos la función para pantalla Landscape
        LoginScreenLandscape(navController)
    } else {
        // Si está en modo Portrait, usamos la función para pantalla Portrait
        LoginScreenPortrait(navController)
    }
}
@Composable
fun LoginScreenPortrait(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val viewModel: QuizViewModel = viewModel()
    val context = LocalContext.current

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

            // Imagen del título QUIZEC
            Image(
                painter = painterResource(id = R.drawable.quizec_title),
                contentDescription = "QUIZEC Title",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = stringResource(R.string.login),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.nombre_de_usuario),
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
                text = stringResource(R.string.password),
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
                        errorMessage =
                            context.getString(R.string.por_favor_ingresa_todos_los_campos)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink)
                ), // Color de fondo del botón
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = stringResource(R.string.login), color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de registro
            Button(
                onClick = {
                    // Navegar a la pantalla de registro
                    navController.navigate("register")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_darkpurple)
                ), // Color de fondo del botón
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = stringResource(R.string.registro), color = Color.White)
            }
        }
    }
}

@Composable
fun LoginScreenLandscape(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val viewModel: QuizViewModel = viewModel()
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Login Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido para la pantalla en orientación landscape
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp).fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),

        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(id = R.drawable.quizec_title),
                contentDescription = "QUIZEC Title",
                modifier = Modifier
                    .width(200.dp)  // Ajusta el tamaño de la imagen
                    .height(200.dp)
                    .padding(end = 32.dp), // Espaciado entre la imagen y el contenido
                contentScale = ContentScale.Fit
            )

            // Contenido a la derecha (formulario de login y botones)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos
                verticalArrangement = Arrangement.Top, // Alinea los elementos hacia la parte superior
                modifier = Modifier
                    .fillMaxWidth(0.6f) // Ajusta el ancho para que los elementos quepan correctamente
                    .fillMaxHeight() // Asegura que la columna llene toda la altura disponible
                    .padding(top = 16.dp)
            ){
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.login),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Nombre de usuario
                Text(
                    text = stringResource(R.string.nombre_de_usuario),
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

                Spacer(modifier = Modifier.height(8.dp)) // Reducido el espaciado

                // Contraseña
                Text(
                    text = stringResource(R.string.password),
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

                // Mensaje de error
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Reducido el espaciado

                // Botón de login
                Button(
                    onClick = {
                        if (username.isEmpty() || password.isEmpty()) {
                            errorMessage =
                                context.getString(R.string.por_favor_ingresa_todos_los_campos)
                        } else {
                            if (auth != null) {
                                loginUser(username, password, auth, navController, { message ->
                                    errorMessage = message
                                }, viewModel)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.logo_pink)
                    ), // Color de fondo del botón
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(4.dp) // Espaciado adicional
                ) {
                    Text(text = stringResource(R.string.login), color = Color.White)
                }

                // Botón de registro
                Button(
                    onClick = {
                        navController.navigate("register")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.logo_darkpurple)
                    ), // Color de fondo del botón
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 4.dp) // Espaciado reducido entre los botones
                ) {
                    Text(text = stringResource(R.string.registro), color = Color.White)
                }
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
                onError("User not found.")
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
                                onError("Error: ${task.exception?.message ?: "Unknown error"}")
                            }
                        }
                } else {
                    onError("Email not found.")
                }
            }
        }
        .addOnFailureListener { e ->
            onError("Error: ${e.message}")
        }
}
