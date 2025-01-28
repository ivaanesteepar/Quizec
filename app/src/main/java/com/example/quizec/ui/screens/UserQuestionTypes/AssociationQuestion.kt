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
    isAcceptButtonClicked: Boolean,
    immediateResults: Boolean,
    quizTerminado: Boolean
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
        pairColors.clear() // Limpiar los colores previos
        colorIndex = 0

        // Asignar colores a cada par
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
        Text( // Instrucciones para el usuario
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
                modifier = Modifier.weight(0.8f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                concepts.forEach { concept ->
                    val isDisabled = userSelections.containsKey(concept) // Verificar si el concepto ya fue seleccionado
//                    val conceptColor = userSelections[concept]?.let { definition ->
//                        pairColors[concept to definition] ?: Color.Transparent
//                    } ?: Color.Transparent

                    // Determinar el color que debe tener el concepto
                    val conceptColor = if (isAcceptButtonClicked && immediateResults) {
                        // Color de los pares correctos basados en pairColors
                        currentQuestion.conceptosYDefiniciones[concept]?.let { definition ->
                            pairColors[concept to definition] ?: Color.Transparent
                        } ?: Color.Transparent
                    } else {
                        // Color basado en las selecciones del usuario
                        userSelections[concept]?.let { definition ->
                            pairColors[concept to definition] ?: Color.Transparent
                        } ?: Color.Transparent
                    }


                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp) // Aseguramos una altura consistente
                            .background(
                                if (selectedConcept == concept) Color.LightGray else conceptColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isDisabled) { //si esta habilitado, se puede hacer click en el concepto
                                    selectedConcept =
                                        if (selectedConcept == concept) null else concept
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Si el concepto es una URL, mostramos una imagen en lugar de texto
                        if (concept.startsWith("http://") || concept.startsWith("https://")) {
                            AsyncImage(
                                model = concept,
                                contentDescription = "Imagen del concepto",
                                contentScale = ContentScale.Crop, // Ajusta la imagen para que aproveche todo el tamaño
                                modifier = Modifier
                                    .size(80.dp) // Aseguramos que la imagen tenga un tamaño fijo
                                    .align(Alignment.Center)
                            )
                        } else {
                            Text(
                                text = concept,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Unspecified, // Mantener color original
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 180.dp) // Controla el ancho de los conceptos
                                    .wrapContentWidth() // Ajusta el ancho según el contenido
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
                    val isUsed = userSelections.containsValue(definition) // Verificar si la definición ya fue seleccionada y asociada
//                    val definitionColor = userSelections.entries.firstOrNull { it.value == definition }?.let {
//                        pairColors[it.key to definition] ?: Color.Transparent
//                    } ?: Color.Transparent

                    // Determinar el color que debe tener la definición
                    val definitionColor = if (isAcceptButtonClicked && immediateResults) {
                        // Color de los pares correctos basados en pairColors
                        currentQuestion.conceptosYDefiniciones.entries.firstOrNull { it.value == definition }?.let { (concept, _) ->
                            pairColors[concept to definition] ?: Color.Transparent
                        } ?: Color.Transparent
                    } else {
                        // Color basado en las selecciones del usuario
                        userSelections.entries.firstOrNull { it.value == definition }?.let {
                            pairColors[it.key to definition] ?: Color.Transparent
                        } ?: Color.Transparent
                    }

                    Box( //lo q envuelve la def
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp) // Altura adaptable para evitar corte del texto
                            .background(
                                if (isUsed) definitionColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isAcceptButtonClicked) {
                                    if (isUsed) { //Si la definición ya ha sido utilizada
                                        // Eliminar la asociación existente
                                        val conceptToRemove =
                                            userSelections.entries.firstOrNull { it.value == definition }?.key
                                        conceptToRemove?.let { userSelections.remove(it) }
                                        pairColors.remove(conceptToRemove to definition)
                                    } else if (selectedConcept != null) { //si un concepto ha sido ya seleccionado
                                        // Asociar el concepto seleccionado con la definición
                                        userSelections[selectedConcept!!] = definition
                                        //Se agrega un color único para el par
                                        pairColors[selectedConcept!! to definition] = getNextColor()
                                        selectedConcept = null
                                    }
                                }

                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = definition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Unspecified, // Mantener color original
                            modifier = Modifier
                                .fillMaxWidth() // El texto ocupa tdo el ancho disponible
                                .padding(horizontal = 8.dp) // Un poco de padding horizontal para evitar que el texto quede pegado a los bordes
                                .wrapContentWidth() // Ajusta el ancho del texto al contenido
                        )
                    }
                }
            }

        }
    }
}
