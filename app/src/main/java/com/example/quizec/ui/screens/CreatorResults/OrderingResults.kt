package com.example.quizec.ui.screens.CreatorResults

import android.util.Log
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
fun OrderingResults(
    respuestasUsuarioPregunta: List<Map<String, Any>>,
    itemsOrdenados: List<String>
) {
    // Mapa para contar la frecuencia de cada orden seleccionado
    val conteoResultados = mutableMapOf<String, Int>()

    // Procesar las respuestas de todos los usuarios
    respuestasUsuarioPregunta.forEach { respuestaUsuario ->
        // Obtener la lista de respuestas del usuario (orden seleccionado)
        val respuestas = respuestaUsuario["respuesta"] as? List<String>
        Log.d("OrderingResults", "Respuestas del usuario: $respuestas")

        respuestas?.let {
            // Crear una representación única del orden seleccionado
            val resultado = it.joinToString(" -> ")
            conteoResultados[resultado] = conteoResultados.getOrDefault(resultado, 0) + 1
        }
    }

    // Ordenar los resultados por cantidad de selecciones
    val resultadosOrdenados = conteoResultados.entries
        .sortedByDescending { it.value }
        .map { Pair(it.key, it.value) }

    // Representación del orden correcto como texto
    val ordenCorrectoTexto = itemsOrdenados.joinToString(" -> ")

    // Mostrar los resultados en la UI
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

        resultadosOrdenados.forEach { (orden, cont) ->
            // Determinar el color de fondo: verde si es correcto, variante estándar si no lo es
            val backgroundColor = if (orden == ordenCorrectoTexto) {
                Color.Green // Fondo verde para correcto
            } else {
                MaterialTheme.colorScheme.surfaceVariant // Fondo estándar para incorrecto
            }

            // Cada fila con fondo diferenciado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Orden como texto
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orden,
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
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
