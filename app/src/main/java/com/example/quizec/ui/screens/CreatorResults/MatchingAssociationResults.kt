package com.example.quizec.ui.screens.CreatorResults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.quizec.data.model.TipoPregunta

@Composable
fun MatchingAssociationResults(
    respuestasUsuarioPregunta: List<Map<String, Any>>,
    tipoPregunta: TipoPregunta
) {
    // Mapa para contar las asociaciones o emparejamientos concepto-definición
    val conteoResultados = mutableMapOf<String, Int>()

    // Procesar las respuestas de todos los usuarios
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        // Obtener el mapa de respuestas (concepto -> definición)
        val respuestas = respuestaUsuario["respuesta"] as? Map<String, String>

        respuestas?.forEach { (concepto, definicion) ->
            // Crear clave única para la asociación o emparejamiento
            val resultado = "$concepto - $definicion"
            conteoResultados[resultado] = conteoResultados.getOrDefault(resultado, 0) + 1
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
            text = "Results",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        resultadosOrdenados.forEach { (resultado, conteo) ->
            val partes = resultado.split(" - ")
            val concepto = partes[0]
            val definicion = partes[1]

            // Cada fila con fondo diferenciado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Concepto: manejar imagen solo si es de tipo Asociación
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tipoPregunta == TipoPregunta.ASOCIACION && concepto.startsWith("http")) {
                            // Si es de tipo Asociación y el concepto es una URL, mostramos la imagen
                            AsyncImage(
                                model = concepto,
                                contentDescription = "Concepto como imagen",
                                modifier = Modifier.size(64.dp)
                            )
                        } else {
                            // Si no es de tipo Asociación, mostramos solo texto
                            Text(
                                text = concepto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Definición: siempre como texto
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = definicion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
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
                            text = "$conteo ${if (conteo == 1) "choice" else "choices"}",
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