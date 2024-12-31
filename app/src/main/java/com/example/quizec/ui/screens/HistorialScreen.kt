package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.QuizViewModel

@Composable
fun HistorialScreen(
    navController: NavHostController,
    quizViewModel: QuizViewModel,
    userId: String
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) {
        // Si está en modo Landscape, usamos la función para pantalla Landscape
        HistorialScreenLandscape(navController, quizViewModel, userId)
    } else {
        // Si está en modo Portrait, usamos la función para pantalla Portrait
        HistorialScreenPortrait(navController, quizViewModel, userId)
    }
}

@Composable
fun HistorialScreenPortrait(navController: NavHostController, quizViewModel: QuizViewModel, userId: String) {
    val cuestionariosState = remember { mutableStateOf<List<Cuestionario>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Cargar los cuestionarios del usuario
    LaunchedEffect(userId) {
        quizViewModel.cargarCuestionariosDeUsuario(
            userId = userId,
            onSuccess = { cuestionarios ->
                println("Historial: Cuestionarios de usuario cargados: ${cuestionarios.size}")
                cuestionariosState.value = cuestionarios
                loading.value = false
                errorMessage.value = null
            },
            onFailure = { error ->
                Log.e("HistorialScreen", "No se encontraron cuestionarios para el usuario.", error)
                loading.value = false
                errorMessage.value = context.getString(R.string.no_hay_cuestionarios_disponibles)
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Mantiene el título en la parte superior centrado
    ) {
        if (loading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Centra tdo horizontalmente
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Título en la parte superior
                Text(
                    text = stringResource(R.string.historial_de_cuestionarios),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = 16.dp) // Espaciado debajo del título
                )

                // Contenido (cuestionarios) inmediatamente después del título
                when {
                    errorMessage.value != null -> {
                        Text(
                            text = errorMessage.value ?: "",
                            color = androidx.compose.ui.graphics.Color.Red,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    cuestionariosState.value.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.no_tienes_cuestionarios_guardados),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(650.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Espaciado entre cuestionarios
                            horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los cuestionarios
                        ) {
                            items(cuestionariosState.value) { cuestionario ->
                                // Envolviendo en una tarjeta con color morado
                                androidx.compose.material3.Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp), // Margen lateral
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = androidx.compose.ui.graphics.Color(0xFFBB86FC) // Color morado claro
                                    ),
                                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Text(
                                        text = cuestionario.titulo,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp) // Padding dentro de la tarjeta
                                            .clickable {
                                                navController.navigate("detalleCuestionarios/${cuestionario.id}")
                                            },
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center // Centra el texto dentro de cada tarjeta
                                    )
                                }
                            }
                        }
                    }
                }

                // Espaciado para que el botón se ubique al final
                Spacer(modifier = Modifier.weight(1f))

                // Botón en la parte inferior
                androidx.compose.material3.Button(
                    onClick = {
                        // Acción del botón, por ejemplo, navegar a otra pantalla
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.volver))
                }
            }
        }
    }
}

@Composable
fun HistorialScreenLandscape(navController: NavHostController, quizViewModel: QuizViewModel, userId: String) {
    val cuestionariosState = remember { mutableStateOf<List<Cuestionario>>(emptyList()) }
    val loading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Cargar los cuestionarios del usuario
    LaunchedEffect(userId) {
        quizViewModel.cargarCuestionariosDeUsuario(
            userId = userId,
            onSuccess = { cuestionarios ->
                println("Historial: Cuestionarios de usuario cargados: ${cuestionarios.size}")
                cuestionariosState.value = cuestionarios
                loading.value = false
                errorMessage.value = null
            },
            onFailure = { error ->
                Log.e("HistorialScreen", "No se encontraron cuestionarios para el usuario.", error)
                loading.value = false
                errorMessage.value = context.getString(R.string.no_tienes_cuestionarios_guardados)
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Mantiene el título en la parte superior centrado
    ) {
        if (loading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, // Centra todo horizontalmente
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Título en la parte superior
                Text(
                    text = stringResource(R.string.historial_de_cuestionarios),
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp, top = 16.dp) // Espaciado debajo del título
                )

                // Contenido (cuestionarios) inmediatamente después del título
                when {
                    errorMessage.value != null -> {
                        Text(
                            text = errorMessage.value ?: "",
                            color = androidx.compose.ui.graphics.Color.Red,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    cuestionariosState.value.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.no_tienes_cuestionarios_guardados),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .height(180.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Espaciado entre cuestionarios
                            horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente los cuestionarios
                        ) {
                            items(cuestionariosState.value) { cuestionario ->
                                // Envolviendo en una tarjeta con color morado
                                androidx.compose.material3.Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp), // Margen lateral
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = androidx.compose.ui.graphics.Color(0xFFBB86FC) // Color morado claro
                                    ),
                                    elevation = androidx.compose.material3.CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Text(
                                        text = cuestionario.titulo,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp) // Padding dentro de la tarjeta
                                            .clickable {
                                                navController.navigate("detalleCuestionarios/${cuestionario.id}")
                                            },
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center // Centra el texto dentro de cada tarjeta
                                    )
                                }
                            }
                        }
                    }
                }

                // Espaciado para que el botón se ubique al final
                Spacer(modifier = Modifier.weight(1f))

                // Botón en la parte inferior
                androidx.compose.material3.Button(
                    onClick = {
                        // Acción del botón, por ejemplo, navegar a otra pantalla
                        navController.navigate("home")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.volver))
                }
            }
        }
    }
}


