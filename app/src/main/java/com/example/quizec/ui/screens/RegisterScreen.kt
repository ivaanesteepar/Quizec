package com.example.quizec.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.quizec.R
import com.example.quizec.data.model.Rol
import com.example.quizec.data.model.Usuario
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.registro), style = MaterialTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text(stringResource(R.string.nombre)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailEmpty = email.isEmpty()
                    },
                    label = { Text(stringResource(R.string.email)) },
                    isError = isEmailEmpty,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isEmailEmpty) {
                    Text(
                        text = stringResource(R.string.el_campo_de_correo_es_obligatorio),
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
                    label = { Text(stringResource(R.string.password)) },
                    isError = isPasswordEmpty,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

//                if (isPasswordEmpty) {
//                    Text(
//                        text = stringResource(R.string.el_campo_de_contrase_a_es_obligatorio),
//                        color = MaterialTheme.colorScheme.error,
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirmar_password)) },
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.registro))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        navController.navigate("login")
                    },
                    modifier = Modifier.fillMaxWidth()
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
    // Implementación para la orientación landscape
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
            // En Landscape, se usa un Row para mostrar los elementos horizontalmente
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column con los campos y botones
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Register", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text(stringResource(R.string.nombre)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailEmpty = email.isEmpty()
                        },
                        label = { Text(stringResource(R.string.email)) },
                        isError = isEmailEmpty,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isEmailEmpty) {
                        Text(
                            text = stringResource(R.string.el_campo_de_correo_es_obligatorio),
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
                        label = { Text(stringResource(R.string.password)) },
                        isError = isPasswordEmpty,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

//                    if (isPasswordEmpty) {
//                        Text(
//                            text = stringResource(R.string.el_campo_de_contrase_a_es_obligatorio),
//                            color = MaterialTheme.colorScheme.error,
//                            style = MaterialTheme.typography.bodySmall
//                        )
//                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(stringResource(R.string.confirmar_password)) },
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.registro))
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            navController.navigate("login")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ){
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
