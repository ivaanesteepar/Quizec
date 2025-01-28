package com.example.quizec.ui.screens.CreatorResults

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MultiChoiceBarChart(
    questionIndex: Int,
    totalResponses: Int, // Total de respuestas
    opcionesPregunta: List<String>, // Opciones de la pregunta
    hayRespuestas: Boolean, // Indica si hay respuestas disponibles para esta pregunta
    respuestasUsuarioPregunta: List<Map<String, Any>>, // Respuestas de los usuarios
    respuestasCorrectas: List<String> // Lista de respuestas correctas (puede ser una o varias)
) {
    val maxHeight = 300f // Altura máxima de las barras
    val barWidth = 80f // Ancho de las barras
    val spaceBetweenBars = 35f // Espacio entre las barras
    val additionalOffset = 20f // Desplazamiento adicional hacia la derecha

    // Inicializamos un mapa para contar cuántas veces cada opción ha sido seleccionada
    val responseCounts = mutableMapOf<String, Int>()
    opcionesPregunta.forEach { opcion ->
        responseCounts[opcion] = 0
    }

    // Contamos cuántas veces cada opción ha sido seleccionada
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        val respuesta = (respuestaUsuario["respuesta"] as? List<*>)?.firstOrNull() as? String

        if (respuesta != null && responseCounts.containsKey(respuesta)) {
            responseCounts[respuesta] = responseCounts[respuesta]!! + 1
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Altura del gráfico
                .padding(16.dp) // Asegurarse de que esté centrado
        ) {
            // Dibujar los ejes
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(10f, maxHeight),
                end = androidx.compose.ui.geometry.Offset(size.width - 10f, maxHeight),
                strokeWidth = 2f
            )
            drawLine(
                color = Color.Black,
                start = androidx.compose.ui.geometry.Offset(10f, 0f),
                end = androidx.compose.ui.geometry.Offset(10f, maxHeight),
                strokeWidth = 2f
            )

            // Verificamos si tenemos respuestas para esta pregunta
            if (totalResponses > 0) {
                // Calculamos la altura de las barras basada en las respuestas
                val maxCount = responseCounts.values.maxOrNull() ?: 0
                opcionesPregunta.forEachIndexed { index, opcion ->
                    val count = responseCounts[opcion] ?: 0
                    val barHeight = maxHeight * (count.toFloat() / totalResponses)

                    // Determinamos si la opción es correcta
                    val isCorrect = respuestasCorrectas.contains(opcion)

                    // Dibujamos la barra para esta opción
                    val barColor = if (isCorrect) {
                        Color.Green // Si la opción es correcta, la barra será verde
                    } else {
                        Color.Blue // Si la opción no es correcta, la barra será azul
                    }

                    drawRect(
                        color = barColor, // Usamos el color determinado para las barras
                        topLeft = androidx.compose.ui.geometry.Offset(
                            x = (index * (barWidth + spaceBetweenBars)) + 10f + additionalOffset, // Ajuste en el eje X
                            y = maxHeight - barHeight // La altura de la barra dependerá del número de respuestas
                        ),
                        size = androidx.compose.ui.geometry.Size(width = barWidth, height = barHeight)
                    )

                    // Dibujamos el número de respuestas debajo de la barra
                    drawContext.canvas.nativeCanvas.drawText(
                        "$count", // El número de respuestas seleccionadas para esta opción
                        (index * (barWidth + spaceBetweenBars)) + 11f + additionalOffset + 33f, // Posición en X
                        maxHeight + 21f, // Posición en Y para mostrar el número debajo de la barra
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = 16f
                        }
                    )
                }
            }
        }

        // Lista con las opciones y sus respectivos contadores justo debajo del gráfico
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp) // Pequeña separación entre el gráfico y la lista
        ) {
            opcionesPregunta.forEachIndexed { index, opcion ->
                val count = responseCounts[opcion] ?: 0
                Text(
                    text = "$opcion - $count",
                    style = TextStyle( fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}