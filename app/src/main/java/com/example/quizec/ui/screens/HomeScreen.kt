package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    var userName by remember { mutableStateOf("Usuario") }
    val userUid = FirebaseAuth.getInstance().currentUser?.uid
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Obtener el nombre del usuario desde Firestore o FirebaseAuth
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        user?.let {
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("nombre") ?: context.getString(R.string.usuario)
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("HomeScreen",
                        context.getString(R.string.error_obteniendo_el_nombre_del_usuario), exception)
                    isLoading = false
                }
        } ?: run {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_login),
            contentDescription = "Home Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
        ) {
            Spacer(modifier = Modifier.height(150.dp)) // ajusta la columna de elementos

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

            if (isLoading) {
                Text(
                    text = stringResource(R.string.cargando),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            } else {
                // Saludo de bienvenida con el nombre del usuario
                Text(
                    text = stringResource(R.string.bienvenido, userName),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Cuadrícula de botones (2x2) con botones más grandes y bordes redondeados
                Grid(
                    rows = 2, columns = 2, modifier = Modifier.fillMaxWidth(0.8f)
                ) { row, col ->
                    when (row to col) {
                        0 to 0 -> {
                            Button(
                                onClick = { navController.navigate("joinQuiz") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.logo_pink)
                                ), // Color de fondo del botón
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f) // Hace que el botón sea cuadrado
                                    .clip(RoundedCornerShape(16.dp)) // Bordes redondeados más grandes
                                    .padding(4.dp) // Espaciado adicional
                            ) {
                                Text(text = stringResource(R.string.unirse))
                            }
                        }
                        0 to 1 -> {
                            Button(
                                onClick = {
                                    quizViewModel.resetearPreguntas() //JIMENA
//                                    quizViewModel._preguntas.clear() // Limpia la lista de preguntas seleccionadas
//                                    quizViewModel.contadorPreguntas.value = 0 // Limpia el contador de preguntas seleccionadas
                                    navController.navigate("createQuiz") // Navega a la pantalla de creación de cuestionarios
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.logo_darkpurple)
                                ), // Color de fondo del botón
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f) // Hace que el botón sea cuadrado
                                    .clip(RoundedCornerShape(16.dp)) // Bordes redondeados más grandes
                                    .padding(4.dp) // Espaciado adicional
                            ) {
                                Text(text = stringResource(R.string.crear))
                            }

                        }
                        1 to 0 -> {
                            Button(
                                onClick = {
                                    if (userUid != null) {
                                        quizViewModel.actualizarRolUsuario2(userUid, Rol.CREADOR)
                                    }
                                    navController.navigate("select_cuestionario")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.logo_darkpurple)
                                ), // Color de fondo del botón
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f) // Hace que el botón sea cuadrado
                                    .clip(RoundedCornerShape(16.dp)) // Bordes redondeados más grandes
                                    .padding(4.dp) // Espaciado adicional
                            ) {
                                Text(
                                    text = stringResource(R.string.seleccionar_cuestionario),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        1 to 1 -> {
                            Button(
                                onClick = {
                                    val user = auth.currentUser
                                    val userId = user?.uid ?: ""
                                    navController.navigate("historial/$userId")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.logo_pink)
                                ), // Color de fondo del botón
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f) // Hace que el botón sea cuadrado
                                    .clip(RoundedCornerShape(16.dp)) // Bordes redondeados más grandes
                                    .padding(4.dp) // Espaciado adicional
                            ) {
                                Text(text = stringResource(R.string.historial))
                            }
                        }
                    }
                }
            }

            // Espaciador para empujar el botón de cierre de sesión hacia abajo
            Spacer(modifier = Modifier.weight(1f))

            // Botón para cerrar sesión
            Button(
                onClick = {
                    navController.navigate("login")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = stringResource(R.string.cerrar_sesion),
                    modifier = Modifier.padding(end = 8.dp),
                    tint = Color.White
                )
                Text(
                    text = stringResource(R.string.cerrar_sesion),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun Grid(rows: Int, columns: Int, modifier: Modifier = Modifier, content: @Composable (Int, Int) -> Unit) {
    Column(
        modifier = modifier
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until columns) {
                    Box(
                        modifier = Modifier
                            .weight(1f) // Se asegura de que los botones ocupen tdo el espacio disponible
                    ) {
                        content(row, col)
                    }
                }
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
    val context = LocalContext.current

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
                        userName = document.getString("nombre") ?: context.getString(R.string.usuario)
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
                        text = stringResource(R.string.cargando),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                } else {
                    // Saludo de bienvenida con el nombre del usuario
                    Text(
                        text = stringResource(R.string.bienvenido, userName),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Botón para unirse a un quiz
                    Button(
                        onClick = { navController.navigate("joinQuiz") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.logo_pink)
                        ), // Color de fondo del botón
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(4.dp) // Espaciado adicional
                    ) {
                        Text(text = stringResource(R.string.unirse))
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
                        Text(text = stringResource(R.string.crear))
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
                        Text(stringResource(R.string.seleccionar_cuestionario))
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
                        Text(text = stringResource(R.string.historial))
                    }

                    // Nuevo botón debajo de la imagen
                    Button(
                        onClick = {
                            navController.navigate("login") // Cambia "newFeature" por el destino adecuado
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)  // Botón más ancho
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
                            text = stringResource(R.string.cerrar_sesion),
                            color = Color.White // Texto blanco para contrastar con el fondo rojo
                        )
                    }
                }
            }
        }
    }
}



