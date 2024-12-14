package com.example.quizec.ui.screens.UserQuestionTypes

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.quizec.data.model.Pregunta

@Composable
fun AssociationQuestionScreen(
    currentQuestion: Pregunta,
    userSelections: MutableMap<String, String>
) {
    var selectedLeftItem by remember { mutableStateOf<String?>(null) }

    // Shuffle items for randomized display
    val shuffledConceptsAndDefinitions = remember { currentQuestion.conceptosYDefiniciones.shuffled() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Selecciona un concepto y una definición para emparejarlos. \n" +
                    "Pulsa nuevamente para deshacer la selección.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Column: Concepts
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                shuffledConceptsAndDefinitions.forEach { pair ->
                    val concept = pair.keys.firstOrNull() ?: ""
                    val isDisabled = userSelections.containsKey(concept)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedLeftItem == concept) Color.LightGray else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (!isDisabled) {
                                    selectedLeftItem = if (selectedLeftItem == concept) null else concept
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (concept.startsWith("http://")) {
                            AsyncImage(
                                model = concept,
                                contentDescription = "Imagen del concepto",
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.Gray, RoundedCornerShape(8.dp))
                            )
                        } else {
                            Text(
                                text = concept,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDisabled) Color.Gray else Color.Unspecified
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Column: Definitions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                shuffledConceptsAndDefinitions.forEach { pair ->
                    val definition = pair.values.firstOrNull() ?: ""
                    val isUsed = userSelections.containsValue(definition)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isUsed) {
                                    // Si el ítem de la derecha ya está emparejado, lo liberamos
                                    val conceptToRemove = userSelections.entries.firstOrNull { it.value == definition }?.key
                                    conceptToRemove?.let {
                                        userSelections.remove(it)
                                    }

                                } else if (selectedLeftItem != null){
                                    userSelections[selectedLeftItem!!] = definition
                                    selectedLeftItem = null
                                }

                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = definition,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isUsed) Color.Gray else Color.Unspecified
                        )
                    }
                }
            }
        }
    }
}