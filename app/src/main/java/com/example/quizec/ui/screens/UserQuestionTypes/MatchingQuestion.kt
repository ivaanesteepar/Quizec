package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun MatchingQuestionScreen(
    currentQuestion: Pregunta,
    userSelections: MutableMap<String, String>,
    isAcceptButtonClicked: Boolean
) {
    var selectedLeftItem by remember { mutableStateOf<String?>(null) }
    val shuffledRightItems = remember { currentQuestion.rightItems.shuffled() }

    // Generador de colores para las parejas
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Yellow, Color.Cyan, Color.Magenta)
    var colorIndex by remember { mutableStateOf(0) }

    // Mapa de colores de las parejas (derecha-izquierda)
    val colorMap = remember { mutableStateMapOf<String, Color>() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Contenedor para las columnas
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
                    val itemColor = colorMap[leftItem] ?: Color.Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedLeftItem == leftItem) Color.LightGray else itemColor,
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                            .clickable {
                                if (!isDisabled) {
                                    selectedLeftItem =
                                        if (selectedLeftItem == leftItem) null else leftItem
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = leftItem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDisabled) Color.Black else Color.Unspecified
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
                    var itemColor = Color.Transparent

                    if (isUsed) {
                        val leftItem = userSelections.entries.find { it.value == rightItem }?.key
                        leftItem?.let {
                            itemColor = colorMap[it] ?: Color.Transparent
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isUsed) itemColor else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                            .clickable {
                                if (isUsed) {
                                    val leftItem =
                                        userSelections.entries.find { it.value == rightItem }?.key
                                    leftItem?.let {
                                        userSelections.remove(it)
                                        colorMap.remove(it)
                                    }
                                } else if (selectedLeftItem != null) {
                                    // Asignar el color solo cuando se seleccionan ambos
                                    userSelections[selectedLeftItem!!] = rightItem
                                    colorMap[selectedLeftItem!!] = colors[colorIndex % colors.size]
                                    colorMap[rightItem] = colors[colorIndex % colors.size]
                                    colorIndex++
                                    selectedLeftItem = null
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rightItem,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isUsed) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isUsed) Color.Black else Color.Unspecified
                        )
                    }
                }
            }
        }
    }

    // Cuando el botón es aceptado, se colorean las parejas correctas
    if (isAcceptButtonClicked) {
        // Recorrer las parejas correctas que están en currentQuestion.emparejamiento
        currentQuestion.emparejamientos.forEach { pareja ->
            val leftItem = pareja.keys.first() // El valor de la clave es el elemento izquierdo
            val rightItem = pareja.values.first() // El valor de la clave es el elemento derecho

            // Asignar un color a cada pareja correcta
            colorMap[leftItem] = colors[colorIndex % colors.size]
            colorMap[rightItem] = colors[colorIndex % colors.size]
            colorIndex++
        }
    }
}


