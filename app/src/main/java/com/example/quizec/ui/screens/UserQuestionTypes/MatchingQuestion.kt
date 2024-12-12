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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun MatchingQuestion(
    currentQuestion: Pregunta,
    userSelections: MutableMap<String, String>
){
    var selectedLeftItem by remember { mutableStateOf<String?>(null) }

    // Elementos de la derecha desordenados
    val shuffledRightItems = remember { currentQuestion.rightItems.shuffled() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Espaciado entre los elementos en la columna
    ) {

        // Instrucciones para el usuario
        Text(
            text = "Selecciona un ítem de la izquierda y uno de la derecha para emparejarlos.\n" +
                    "Para eliminar la seleccion, pulse en el elemento de la derecha",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp), // Espaciado inferior
            textAlign = TextAlign.Center // Alineación del texto al centro
        )

        // Contenedor para las dos columnas (izquierda y derecha)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Espaciado entre las columnas
        ) {
            // Columna izquierda (con los elementos a emparejar)
            Column(
                modifier = Modifier.weight(1f), // Toma 1/2 del espacio disponible
                verticalArrangement = Arrangement.spacedBy(8.dp), // Espaciado entre los elementos de la columna
                horizontalAlignment = Alignment.CenterHorizontally // Alineación centrada horizontalmente
            ) {
                // Iteramos sobre los elementos de la izquierda
                currentQuestion.leftItems.forEach { leftItem ->
                    // Verificamos si el ítem ya ha sido seleccionado
                    val isDisabled = userSelections.containsKey(leftItem)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                // Cambia el fondo si el ítem está seleccionado
                                if (selectedLeftItem == leftItem) Color.LightGray else Color.Transparent,
                                shape = RoundedCornerShape(8.dp) // Esquinas redondeadas
                            )
                            .clickable {
                                // Si no está deshabilitado, podemos seleccionar o deseleccionar el ítem
                                if (!isDisabled) {
                                    selectedLeftItem =
                                            // Si el ítem ya está seleccionado, lo deseleccionamos
                                        if (selectedLeftItem == leftItem) null else leftItem
                                }
                            }
                            .padding(16.dp), // Espaciado interno
                        contentAlignment = Alignment.Center // Alineación centrada del texto
                    ) {
                        // Mostrar el texto del ítem de la izquierda
                        Text(
                            text = leftItem,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDisabled) Color.Gray else Color.Unspecified // Si está deshabilitado, el texto es gris
                        )
                    }
                }
            }

            // Espacio entre las dos columnas
            Spacer(modifier = Modifier.width(16.dp))

            // Columna derecha (con los elementos que se pueden emparejar)
            Column(
                modifier = Modifier.weight(1f), // Toma 1/2 del espacio disponible
                verticalArrangement = Arrangement.spacedBy(8.dp), // Espaciado entre los elementos de la columna
                horizontalAlignment = Alignment.CenterHorizontally // Alineación centrada horizontalmente
            ) {
                // Iteramos sobre los elementos desordenados de la derecha
                shuffledRightItems.forEach { rightItem ->
                    // Verificamos si el ítem ya ha sido emparejado
                    val isUsed = userSelections.containsValue(rightItem)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isUsed) {
                                    // Si el ítem de la derecha ya está emparejado, lo liberamos
                                    val leftItem =
                                        userSelections.entries.find { it.value == rightItem }?.key
                                    leftItem?.let {
                                        userSelections.remove(it) // Removemos el par de la selección
                                    }
                                } else if (selectedLeftItem != null) {
                                    // Si hay un ítem de la izquierda seleccionado, lo emparejamos con el de la derecha
                                    userSelections[selectedLeftItem!!] = rightItem
                                    selectedLeftItem =
                                        null // Reseteamos la selección de la izquierda
                                }
                            }
                            .padding(16.dp), // Espaciado interno
                        contentAlignment = Alignment.CenterStart // Alineación del texto al inicio
                    ) {
                        // Mostramos el texto del ítem de la derecha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween, // Espacio entre el texto y el ícono de liberación
                            verticalAlignment = Alignment.CenterVertically // Alineación vertical centrada
                        ) {
                            // Texto del ítem de la derecha
                            Text(
                                text = rightItem,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isUsed) Color.Gray else Color.Unspecified, // Si ya está emparejado, el texto es gris
                                modifier = Modifier.weight(1f) // Asegura que el texto ocupe el espacio disponible
                            )
                        }
                    }
                }
            }
        }
    }
}