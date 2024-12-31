package com.example.quizec.ui.screens.CreatorResults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizec.R

@Composable
fun FillBlankResults(
    respuestasUsuarioPregunta: List<Map<String, Any>>,
    opcionCorrecta: String // Se pasa la opción correcta como parámetro
) {
    // Mapa para contar las frecuencias de las respuestas
    val conteoResultados = mutableMapOf<String, Int>()

    // Procesar las respuestas de todos los usuarios
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        // Obtener la respuesta del usuario como String
        val respuesta = respuestaUsuario["respuesta"] as? String

        respuesta?.let {
            // Contar la frecuencia de cada respuesta
            conteoResultados[it] = conteoResultados.getOrDefault(it, 0) + 1
        }
    }

    // Ordenar los resultados por cantidad de selecciones
    val resultadosOrdenados = conteoResultados.entries
        .sortedByDescending { it.value }
        .map { Pair(it.key, it.value) }

    // Mostrar los resultados en una UI profesional
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.resultados),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        resultadosOrdenados.forEach { (respuesta, cont) ->
            // Determinar si la respuesta es correcta
            val esCorrecta = respuesta == opcionCorrecta

            // Cada fila con fondo diferenciado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        color = if (esCorrecta) Color.Green else MaterialTheme.colorScheme.surfaceVariant, // Fondo verde si es correcta
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Respuesta como texto
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = respuesta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start
                        )
                    }

                    // Contador de elecciones
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$cont ${if (cont == 1) stringResource(R.string.eleccion) else stringResource(
                                R.string.elecciones
                            )
                            }",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
