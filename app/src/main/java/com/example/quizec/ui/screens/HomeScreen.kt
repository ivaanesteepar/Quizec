package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.data.model.Rol
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavHostController, quizViewModel: QuizViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        // Si está en modo Landscape, usamos la función para pantalla Landscape
        HomeScreenLandscape(navController, quizViewModel)
    } else {
        // Si está en modo Portrait, usamos la función para pantalla Portrait
        HomeScreenPortrait(navController, quizViewModel)
    }
}
@Composable
fun HomeScreenPortrait(navController: NavHostController, quizViewModel: QuizViewModel) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("Usuario") }  // Nombre por defecto
    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading by remember { mutableStateOf(true) }  // Estado para indicar si la carga está en proceso

    // Obtener el nombre del usuario desde Firestore o FirebaseAuth
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        user?.let {
            // Si el usuario está autenticado, intentar obtener el nombre desde Firestore
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Suponiendo que el nombre está guardado en el campo "nombre"
                        userName = document.getString("nombre") ?: "Usuario"
                    }
                    isLoading = false  // Indicamos que la carga ha finalizado
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeScreen", "Error obteniendo el nombre del usuario", exception)
                    isLoading = false  // Indicamos que la carga ha fallado
                }
        } ?: run {
            isLoading = false  // Si no hay usuario autenticado
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Home Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido de la pantalla de inicio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp), // Espaciado entre botones
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)  // Añade el padding alrededor
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Asegura que el botón se mueva al inicio de la pantalla

            // Imagen del título QUIZEC
            Image(
                painter = painterResource(id = R.drawable.quizec_title),
                contentDescription = "QUIZEC Title",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(100.dp) // Puedes ajustar la altura según sea necesario
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            if (isLoading) {
                // Si aún estamos cargando, mostrar un mensaje de carga
                Text(
                    text = "Cargando...",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            } else {
                // Saludo de bienvenida con el nombre del usuario
                Text(
                    text = "¡Bienvenido, $userName!",
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Botón para unirse a un quiz
                Button(
                    onClick = {
                        navController.navigate("joinQuiz")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Unirse a un Quiz")
                }

                // Botón para crear un quiz
                Button(
                    onClick = {
                        navController.navigate("createQuiz")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Crear un Quiz")
                }


                Button(
                    onClick = {
                        if (userUid != null) {
                            quizViewModel.actualizarRolUsuario2(userUid, Rol.CREADOR)
                        }
                        navController.navigate("select_cuestionario")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Seleccionar Cuestionario")
                }


                // Botón para ir al historial, pasando el userId
                Button(
                    onClick = {
                        val user = auth.currentUser
                        val userId = user?.uid ?: ""
                        navController.navigate("historial/$userId")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(bottom = 16.dp)
                ) {
                    Text(text = "Historial")
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Asegura que el botón se mueva al final de la pantalla

            // Botón para cerrar sesión
            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Opcional, para añadir algo de espacio alrededor
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Color de fondo rojo para resaltar
            ) {
                // Icono y texto dentro del botón
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.padding(end = 8.dp), // Espacio entre icono y texto
                    tint = Color.White // Color blanco para el icono
                )
                Text(
                    text = "Cerrar Sesión",
                    color = Color.White // Texto blanco para contrastar con el fondo rojo
                )
            }
        }
    }
}


@Composable
fun HomeScreenLandscape(navController: NavHostController, quizViewModel: QuizViewModel) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("Usuario") }  // Nombre por defecto
    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading by remember { mutableStateOf(true) }  // Estado para indicar si la carga está en proceso

    // Obtener el nombre del usuario desde Firestore o FirebaseAuth
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        user?.let {
            // Si el usuario está autenticado, intentar obtener el nombre desde Firestore
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Suponiendo que el nombre está guardado en el campo "nombre"
                        userName = document.getString("nombre") ?: "Usuario"
                    }
                    isLoading = false  // Indicamos que la carga ha finalizado
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeScreen", "Error obteniendo el nombre del usuario", exception)
                    isLoading = false  // Indicamos que la carga ha fallado
                }
        } ?: run {
            isLoading = false  // Si no hay usuario autenticado
        }
    }

    // Contenido para orientación landscape
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo de pantalla
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Home Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido para la pantalla de inicio en orientación landscape
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen a la izquierda
            Image(
                painter = painterResource(id = R.drawable.quizec_title),
                contentDescription = "QUIZEC Title",
                modifier = Modifier
                    .width(200.dp)  // Ajusta el tamaño de la imagen
                    .height(200.dp)
                    .padding(end = 32.dp),
                contentScale = ContentScale.Fit
            )

            // Contenido a la derecha
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,  // Centra los botones
                verticalArrangement = Arrangement.spacedBy(4.dp), // Reduce el espaciado entre los botones
                modifier = Modifier.fillMaxWidth(0.6f) // Ajusta el ancho para que los botones caben en la pantalla
            ) {
                if (isLoading) {
                    // Si aún estamos cargando, mostrar un mensaje de carga
                    Text(
                        text = "Cargando...",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                } else {
                    // Saludo de bienvenida con el nombre del usuario
                    Text(
                        text = "¡Bienvenido, $userName!",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Botón para unirse a un quiz
                    Button(
                        onClick = {
                            navController.navigate("joinQuiz")
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)  // Botón más ancho
                            .padding(bottom = 4.dp) // Espaciado reducido entre los botones
                    ) {
                        Text(text = "Unirse a un Quiz")
                    }

                    // Botón para crear un quiz
                    Button(
                        onClick = {
                            navController.navigate("createQuiz")
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)  // Botón más ancho
                            .padding(bottom = 4.dp) // Espaciado reducido entre los botones
                    ) {
                        Text(text = "Crear un Quiz")
                    }

                    // Botón para seleccionar cuestionario
                    Button(
                        onClick = {
                            if (userUid != null) {
                                quizViewModel.actualizarRolUsuario2(userUid, Rol.CREADOR)
                            }
                            navController.navigate("select_cuestionario")
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)  // Botón más ancho
                            .padding(bottom = 4.dp) // Espaciado reducido entre los botones
                    ) {
                        Text("Seleccionar Cuestionario")
                    }

                    // Botón para ir al historial
                    Button(
                        onClick = {
                            val user = auth.currentUser
                            val userId = user?.uid ?: ""
                            navController.navigate("historial/$userId")
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)  // Botón más ancho
                            .padding(bottom = 4.dp) // Espaciado reducido entre los botones
                    ) {
                        Text(text = "Historial")
                    }

                    // Nuevo botón debajo de la imagen
                    Button(
                        onClick = {
                            navController.navigate("login") // Cambia "newFeature" por el destino adecuado
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)  // Botón más ancho
                            .padding(top = 4.dp), // Espaciado superior para separar del contenido anterior
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        // Icono y texto dentro del botón
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            modifier = Modifier.padding(end = 8.dp), // Espacio entre icono y texto
                            tint = Color.White // Color blanco para el icono
                        )
                        Text(
                            text = "Cerrar Sesión",
                            color = Color.White // Texto blanco para contrastar con el fondo rojo
                        )
                    }
                }
            }
        }
    }
}



