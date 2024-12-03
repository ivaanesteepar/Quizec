package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("Usuario") }  // Nombre por defecto
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
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
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                ) {
                    Text(text = "Unirse a un Quiz")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para crear un quiz
                Button(
                    onClick = {
                        navController.navigate("createQuiz")
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Crear un Quiz")
                }

                Spacer(modifier = Modifier.height(16.dp))

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
        }
    }
}
