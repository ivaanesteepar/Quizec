package com.example.quizec.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.clip

@Composable
fun TimeProgressBar(remainingTime: Int) {
    var progress by remember { mutableStateOf(1f) } // Progreso inicial en 1 (completo)
    var currentTime = remainingTime // Inicializa el tiempo restante

    // Efecto lanzado para disminuir el tiempo y actualizar el progreso
    LaunchedEffect(remainingTime) {
        val totalTime = remainingTime.toFloat()

        while (currentTime > 0) {
            // Actualiza el progreso en cada ciclo
            progress = (currentTime.toFloat() / totalTime)
            println("Tiempo restante: $currentTime")
            println("Progreso: $progress")
            delay(1000) // Espera 1 segundo
            currentTime-- // Decrementa el tiempo restante
        }

        // Al finalizar, asegura que el progreso llegue a 0
        progress = 0f
    }

    // Barra de progreso visual
    LinearProgressIndicator(
        progress = { progress },
    )
}

