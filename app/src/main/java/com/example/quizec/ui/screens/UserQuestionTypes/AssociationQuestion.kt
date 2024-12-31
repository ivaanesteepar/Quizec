package com.example.quizec.ui.screens.UserQuestionTypes

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.quizec.R
import com.example.quizec.data.model.Pregunta

@Composable
fun AssociationQuestionScreen(
    currentQuestion: Pregunta,
    userSelections: MutableMap<String, String>,
    isAcceptButtonClicked: Boolean
) {
    var selectedConcept by remember { mutableStateOf<String?>(null) }

    // Barajar conceptos y definiciones para visualización aleatoria
    val concepts = remember(currentQuestion) { currentQuestion.conceptosYDefiniciones.keys }
    // Barajar definiciones para visualización aleatoria
    val shuffledDefinitions = remember(currentQuestion) {
        currentQuestion.conceptosYDefiniciones.values.shuffled()
    }

    Log.d("AssociationQuestionScreen", "conceptos: ${currentQuestion.conceptosYDefiniciones.keys}")
    Log.d("AssociationQuestionScreen", "definiciones: ${currentQuestion.conceptosYDefiniciones.values}")

    // Lista de colores fijos predefinidos (hasta 6)
    val fixedColors = listOf(
        Color(0xFFEF9A9A), // Rojo claro
        Color(0xFF81C784), // Verde claro
        Color(0xFF64B5F6), // Azul claro
        Color(0xFFFFD54F), // Amarillo
        Color(0xFFBA68C8), // Morado
        Color(0xFFFF8A65)  // Naranja
    )

    // Asignar colores fijos a los pares formados
    val pairColors = remember { mutableMapOf<Pair<String, String>, Color>() }
    var colorIndex by remember { mutableStateOf(0) }

    // Función para obtener el siguiente color fijo
    fun getNextColor(): Color {
        val color = fixedColors[colorIndex]
        colorIndex = (colorIndex + 1) % fixedColors.size // Ciclo de colores
        return color
    }

    // Resaltar pares correctos si el botón de aceptar es pulsado
    if (isAcceptButtonClicked) {
        pairColors.clear()
        colorIndex = 0

        currentQuestion.conceptosYDefiniciones.forEach { (concept, definition) ->
            pairColors[concept to definition] = getNextColor()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.associationQuestion_instrucc),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna izquierda: Conceptos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                concepts.forEach { concept ->
                    val isDisabled = userSelections.containsKey(concept)
                    val conceptColor = userSelections[concept]?.let { definition ->
                        pairColors[concept to definition] ?: Color.Transparent
                    } ?: Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // Aseguramos una altura consistente
                            .background(
                                if (selectedConcept == concept) Color.LightGray else conceptColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isDisabled) {
                                    selectedConcept =
                                        if (selectedConcept == concept) null else concept
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (concept.startsWith("http://") || concept.startsWith("https://")) {
                            AsyncImage(
                                model = concept,
                                contentDescription = "Imagen del concepto",
                                contentScale = ContentScale.Crop, // Ajusta la imagen para q aproveche tdo el tam
                                modifier = Modifier
                                    .size(80.dp) // Aseguramos que la imagen tenga un tamaño fijo
                                    .align(Alignment.Center)
                            )
                        } else {
                            Text(
                                text = concept,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Unspecified // Mantener color original
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Columna derecha: Definiciones
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                shuffledDefinitions.forEach { definition ->
                    val isUsed = userSelections.containsValue(definition)
                    val definitionColor = userSelections.entries.firstOrNull { it.value == definition }?.let {
                        pairColors[it.key to definition] ?: Color.Transparent
                    } ?: Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // Aseguramos una altura consistente para las definiciones
                            .background(
                                if (isUsed) definitionColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (isUsed) {
                                    // Si la definición ya está emparejada, eliminar la asociación
                                    val conceptToRemove =
                                        userSelections.entries.firstOrNull { it.value == definition }?.key
                                    conceptToRemove?.let { userSelections.remove(it) }
                                    pairColors.remove(conceptToRemove to definition)
                                } else if (selectedConcept != null) {
                                    // Asociar el concepto seleccionado con la definición
                                    userSelections[selectedConcept!!] = definition
                                    // Asignar un color fijo al par formado
                                    pairColors[selectedConcept!! to definition] = getNextColor()
                                    selectedConcept = null
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = definition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Unspecified // Mantener color original
                        )
                    }
                }
            }
        }
    }
}
