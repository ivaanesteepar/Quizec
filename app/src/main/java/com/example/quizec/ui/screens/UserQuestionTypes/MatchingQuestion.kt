package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun MatchingQuestionScreen(
    currentQuestion: Pregunta,
    userSelections: MutableMap<String, String>,
    isAcceptButtonClicked: Boolean,
    immediateResults: Boolean,
    quizTerminado: Boolean
) {
    var selectedLeftItem by remember { mutableStateOf<String?>(null) }
    val shuffledRightItems = remember(currentQuestion) { currentQuestion.rightItems.shuffled() }


    // Lista de colores fijos predefinidos
    val fixedColors = listOf(
        Color(0xFFEF9A9A), // Rojo claro
        Color(0xFF81C784), // Verde claro
        Color(0xFF64B5F6), // Azul claro
        Color(0xFFFFD54F), // Amarillo
        Color(0xFFBA68C8), // Morado
        Color(0xFFFF8A65)  // Naranja
    )

    val pairColors = remember { mutableMapOf<Pair<String, String>, Color>() }
    var colorIndex by remember { mutableStateOf(0) }

    fun getNextColor(): Color {
        val color = fixedColors[colorIndex]
        colorIndex = (colorIndex + 1) % fixedColors.size
        return color
    }

    // Resaltar pares correctos si el botón de aceptar es pulsado
    if (isAcceptButtonClicked && immediateResults || (!immediateResults && quizTerminado)) {
        pairColors.clear()
        colorIndex = 0

        currentQuestion.emparejamientos.forEach { (leftItem, rightItem) ->
            pairColors[leftItem to rightItem] = getNextColor()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna izquierda
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                currentQuestion.leftItems.forEach { leftItem ->
                    val isDisabled = userSelections.containsKey(leftItem)
                    val itemColor = userSelections[leftItem]?.let { rightItem ->
                        pairColors[leftItem to rightItem] ?: Color.Transparent
                    } ?: Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedLeftItem == leftItem) Color.LightGray else itemColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !isDisabled) {
                                selectedLeftItem = if (selectedLeftItem == leftItem) null else leftItem
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = leftItem,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Columna derecha
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                shuffledRightItems.forEach { rightItem ->
                    val isUsed = userSelections.containsValue(rightItem)
                    val associatedLeftItem = userSelections.entries.firstOrNull { it.value == rightItem }?.key
                    val itemColor = associatedLeftItem?.let {
                        pairColors[it to rightItem] ?: Color.Transparent
                    } ?: Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isUsed) itemColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                if (associatedLeftItem != null) {
                                    // Deshacer el par
                                    userSelections.remove(associatedLeftItem)
                                    pairColors.remove(associatedLeftItem to rightItem)
                                } else if (selectedLeftItem != null) {
                                    // Crear un nuevo par
                                    userSelections[selectedLeftItem!!] = rightItem
                                    pairColors[selectedLeftItem!! to rightItem] = getNextColor()
                                    selectedLeftItem = null
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rightItem,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
