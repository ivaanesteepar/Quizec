package com.example.quizec.ui.screens.CreatorResults

import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.quizec.R
import com.example.quizec.data.model.TipoPregunta

@Composable
fun MatchingAssociationResults(
    respuestasUsuarioPregunta: List<Map<String, Any>>,
    tipoPregunta: TipoPregunta,
    conceptosYDefiniciones: Map<String, String>,
    emparejamientos: Map<String, String>
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

    Log.d("MatchingAssociationResults", "Resultados ordenados $tipoPregunta: $resultadosOrdenados")

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

        resultadosOrdenados.forEach { (resultado, cont) ->
            val partes = resultado.split(" - ")
            val concepto = partes[0]
            val definicion = partes[1]

            // Verificar si el emparejamiento es correcto
            val esCorrecto = (conceptosYDefiniciones[concepto] == definicion || emparejamientos[concepto] == definicion )

            // Cambiar el color de fondo a verde si es correcto
            val backgroundColor = if (esCorrecto) {
                Color.Green // Fondo verde si la respuesta es correcta
            } else {
                colorResource(id = R.color.pastel) // Fondo estándar si es incorrecta
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
                                contentScale = ContentScale.Crop, // Ajusta la imagen para q aproveche tdo el tam
                                modifier = Modifier
                                    .size(80.dp) // Aseguramos que la imagen tenga un tamaño fijo
                                    .align(Alignment.Center)
                            )
                        } else {
                            // Si no tiene img, mostramos solo texto
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
                            .weight(1.1f)
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
                            .weight(0.6f)
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