package com.example.quizec.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quizec.R
import com.example.quizec.data.model.Rol
import com.example.quizec.data.model.Usuario
import com.example.quizec.ui.theme.buttonColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavHostController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Verificamos la orientación y ejecutamos la función adecuada
    if (isLandscape) {
        RegisterScreenLandscape(navController)  // Función para Landscape
    } else {
        RegisterScreenPortrait(navController)  // Función para Portrait
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreenPortrait(navController: NavHostController) {
    // Aquí va la implementación para la orientación portrait
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val db = if (LocalInspectionMode.current) null else FirebaseFirestore.getInstance()

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(Rol.PARTICIPANTE) }
    var isEmailEmpty by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        content = {
            Box(
                modifier = Modifier.fillMaxSize()
            ){
                Image(
                    painter = painterResource(id = R.drawable.fondo_login),
                    contentDescription = "Login Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop

                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f)), // Pantalla oscura
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image( // Imagen del título QUIZEC
                    painter = painterResource(id = R.drawable.quizec_title),
                    contentDescription = "QUIZEC Title",
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(100.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.registro),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(55.dp))

                Text(
                    text = stringResource(R.string.nombre),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(8.dp)) // Bordes redondeados
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White)
                        .padding(8.dp) // Espaciado interno
                ) {
                    BasicTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (nombre.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.nombre), // Texto de etiqueta
                                    style = LocalTextStyle.current.copy(color = Color.Gray)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.email),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(8.dp)) // Bordes redondeados
                        .border(
                            width = 1.dp,
                            color = if (isEmailEmpty) MaterialTheme.colorScheme.error else Color.Gray, // Color del borde según error
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White)
                        .padding(8.dp) // Espaciado interno
                ) {
                    BasicTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailEmpty = email.isEmpty()
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (email.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.email), // Texto de etiqueta
                                    style = LocalTextStyle.current.copy(color = Color.Gray)
                                )
                            }
                            innerTextField() // Renderizamos el campo de texto
                        }
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
                        .clip(RoundedCornerShape(8.dp)) // Bordes redondeados
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White)
                        .padding(8.dp) // Espaciado interno
                ) {
                    BasicTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isPasswordEmpty = password.isEmpty()
                        },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        visualTransformation = PasswordVisualTransformation(), // Ocultar la contraseña
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (password.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.password), // Texto de etiqueta
                                    style = LocalTextStyle.current.copy(color = Color.Gray)
                                )
                            }
                            innerTextField() // Renderizamos el campo de texto
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.confirmar_password),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(8.dp)) // Bordes redondeados
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(Color.White)
                        .padding(8.dp) // Espaciado interno
                ) {
                    BasicTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        visualTransformation = PasswordVisualTransformation(), // Ocultar la contraseña
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (confirmPassword.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.confirmar_password), // Texto de etiqueta
                                    style = LocalTextStyle.current.copy(color = Color.Gray)
                                )
                            }
                            innerTextField() // Renderizamos el campo de texto
                        }
                    )
                }

                Spacer(modifier = Modifier.height(46.dp))

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
                                            scaffoldState.showSnackbar(context.getString(R.string.registro_exitoso))
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
                                scaffoldState.showSnackbar(context.getString(R.string.el_campo_de_correo_es_obligatorio))
                            }
                        } else if (isPasswordEmpty) {
                            scope.launch {
                                scaffoldState.showSnackbar(context.getString(R.string.el_campo_de_contrase_a_es_obligatorio))
                            }
                        } else {
                            scope.launch {
                                scaffoldState.showSnackbar(context.getString(R.string.las_contrase_as_no_coinciden))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(R.string.registro))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        navController.navigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor // Aplicamos el color de fondo del botón
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ){
                    Text(stringResource(R.string.volver))
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreenLandscape(navController: NavHostController) {
    // Aquí va la implementación para la orientación landscape
    val auth = if (LocalInspectionMode.current) null else FirebaseAuth.getInstance()
    val db = if (LocalInspectionMode.current) null else FirebaseFirestore.getInstance()

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf(Rol.PARTICIPANTE) }
    var isEmailEmpty by remember { mutableStateOf(false) }
    var isPasswordEmpty by remember { mutableStateOf(false) }
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        content = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center

            ) {
                Image(
                    painter = painterResource(id = R.drawable.fondo_login),
                    contentDescription = "Login Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .verticalScroll(rememberScrollState())
            ) {
                // Imagen a la izquierda
                Image(
                    painter = painterResource(id = R.drawable.quizec_title),
                    contentDescription = "QUIZEC Title",
                    modifier = Modifier
                        .width(200.dp)  // Ajusta el tamaño de la imagen
                        .height(200.dp)
                        .padding(start = 16.dp, end = 16.dp), // Espaciado entre la imagen y el contenido
                    contentScale = ContentScale.Fit
                )

                // Columna derecha con los campos de texto y botones
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.registro),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre
                    Text(
                        text = stringResource(R.string.nombre),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (nombre.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.nombre),
                                        style = LocalTextStyle.current.copy(color = Color.Gray)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    Text(
                        text = stringResource(R.string.email),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = if (isEmailEmpty) MaterialTheme.colorScheme.error else Color.Gray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                isEmailEmpty = email.isEmpty()
                            },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (email.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.email),
                                        style = LocalTextStyle.current.copy(color = Color.Gray)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Text(
                        text = stringResource(R.string.password),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isPasswordEmpty = password.isEmpty()
                            },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (password.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.password),
                                        style = LocalTextStyle.current.copy(color = Color.Gray)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password
                    Text(
                        text = stringResource(R.string.confirmar_password),
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (confirmPassword.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.confirmar_password),
                                        style = LocalTextStyle.current.copy(color = Color.Gray)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(46.dp))

                    // Register Button
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
                                                scaffoldState.showSnackbar(context.getString(R.string.registro_exitoso))
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
                                    scaffoldState.showSnackbar(context.getString(R.string.el_campo_de_correo_es_obligatorio))
                                }
                            } else if (isPasswordEmpty) {
                                scope.launch {
                                    scaffoldState.showSnackbar(context.getString(R.string.el_campo_de_contrase_a_es_obligatorio))
                                }
                            } else {
                                scope.launch {
                                    scaffoldState.showSnackbar(context.getString(R.string.las_contrase_as_no_coinciden))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.logo_pink)
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(stringResource(R.string.registro))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Back Button
                    Button(
                        onClick = {
                            navController.navigate("login")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(stringResource(R.string.volver))
                    }
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
