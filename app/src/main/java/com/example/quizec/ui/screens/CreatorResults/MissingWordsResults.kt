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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizec.R

@Composable
fun MissingWordsResults(
    respuestasUsuarioPregunta: List<Map<String, Any>>,
    opcionesCorrectasCompletarPalabras: List<String>,
) {
    // Mapa para contar las frecuencias de las respuestas de cada hueco
    val conteoResultados = mutableMapOf<Int, MutableMap<String, Int>>()

    // Procesar las respuestas de todos los usuarios
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        // Obtener la lista de palabras elegidas por el usuario
        val respuestas = respuestaUsuario["respuesta"] as? List<String>

        respuestas?.forEachIndexed { index, palabra ->
            // Obtener o inicializar el mapa para el hueco actual
            val conteoPorHueco = conteoResultados.getOrPut(index) { mutableMapOf() }

            // Contar la frecuencia de la palabra para el hueco actual
            conteoPorHueco[palabra] = conteoPorHueco.getOrDefault(palabra, 0) + 1
        }
    }

    // Mostrar los resultados
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

        conteoResultados.forEach { (hueco, conteoPorHueco) ->
            // Ordenar las respuestas para el hueco actual por cantidad de selecciones
            val resultadosOrdenados = conteoPorHueco.entries
                .sortedByDescending { it.value }
                .map { Pair(it.key, it.value) }

            Text(
                text = stringResource(R.string.hueco, hueco + 1),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            resultadosOrdenados.forEach { (palabra, cont) ->
                // Determinar si la palabra es correcta
                val isCorrect = opcionesCorrectasCompletarPalabras.getOrNull(hueco) == palabra

                // Cada fila con fondo diferenciado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = if (isCorrect) Color.Green else colorResource(id = R.color.pastel),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Palabra elegida como texto
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = palabra,
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
                                text = "$cont ${if (cont == 1) stringResource(R.string.eleccion) else stringResource(R.string.elecciones)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
