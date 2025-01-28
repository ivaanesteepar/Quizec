package com.example.quizec.ui.screens.CreatorResults

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

@Composable
fun BarChartBar(
    questionIndex: Int,
    correctAnswersCount: Int,
    incorrectAnswersCount: Int,
    totalResponses: Int, // Total de respuestas
    esCorrecta: Boolean, // Indica si la respuesta es correcta
    hayRespuestas: Boolean, // Indica si hay respuestas disponibles para esta pregunta
    respuestasUsuarioPregunta: List<Map<String, Any>> // Las respuestas de los usuarios
) {
    val maxHeight = 300f // Altura máxima de las barras
    val barWidth = 80f // Ancho de las barras
    val spaceBetweenBars = 120f // Espacio entre las barras
    val additionalOffset = 200f // Desplazamiento hacia la derecha

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

        // Verificar si hay respuesta para esta pregunta antes de dibujar las barras
        if (hayRespuestas && totalResponses > 0) {
            // Calcular la altura de las barras
            val correctBarHeight = maxHeight * (correctAnswersCount.toFloat() / totalResponses)
            val incorrectBarHeight = maxHeight * (incorrectAnswersCount.toFloat() / totalResponses)

            // Dibujar la barra para respuestas correctas
            drawRect(
                color = Color.Green,  // Verde para respuestas correctas
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = additionalOffset, // Desplazamiento hacia la derecha
                    y = maxHeight - correctBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = correctBarHeight)
            )

            // Dibujar la barra para respuestas incorrectas
            drawRect(
                color = Color.Red,  // Rojo para respuestas incorrectas
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = additionalOffset + barWidth + spaceBetweenBars, // Más a la derecha
                    y = maxHeight - incorrectBarHeight
                ),
                size = androidx.compose.ui.geometry.Size(width = barWidth, height = incorrectBarHeight)
            )

            // Mostrar el número de respuestas correctas debajo de la barra de correctas
            drawContext.canvas.nativeCanvas.drawText(
                "$correctAnswersCount",
                additionalOffset + barWidth / 2 - 3f,  // Centrar debajo de la barra
                maxHeight + 20f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )

            // Mostrar el número de respuestas incorrectas debajo de la barra de incorrectas
            drawContext.canvas.nativeCanvas.drawText(
                "$incorrectAnswersCount",
                additionalOffset + barWidth + spaceBetweenBars + barWidth / 2 - 3f,  // Centrar debajo de la barra
                maxHeight + 20f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                }
            )

            // Mostrar las palabras "Correcto" y "Incorrecto" debajo de los números
            drawContext.canvas.nativeCanvas.drawText(
                "Correct",
                additionalOffset + barWidth / 2 - 25f,  // Desplazado un poco a la izquierda
                maxHeight + 40f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14f
                }
            )

            drawContext.canvas.nativeCanvas.drawText(
                "Incorrect",
                additionalOffset + barWidth + spaceBetweenBars + barWidth / 2 - 25f,  // Desplazado un poco a la izquierda
                maxHeight + 40f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 14f
                }
            )

        }
    }
}
