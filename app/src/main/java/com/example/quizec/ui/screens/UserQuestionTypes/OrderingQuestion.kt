package com.example.quizec.ui.screens.UserQuestionTypes


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun OrderingQuestionScreen(
    currentQuestion: Pregunta,
    userOrderedItems: (List<String>) -> Unit,
    isAcceptButtonClicked: Boolean // Agregar el estado de si el botón de aceptar ha sido presionado
) {
    // Estado para los ítems desordenados
    var disorderedItems by remember { mutableStateOf(currentQuestion.itemsOrdenados.shuffled().toMutableList()) }

    // Mostrar los ítems ordenados si se presionó el botón de aceptar
    val itemsToDisplay = if (isAcceptButtonClicked) {
        currentQuestion.itemsOrdenados // Muestra el orden correcto
    } else {
        disorderedItems // Muestra los ítems desordenados
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        itemsToDisplay.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            ) {
                // Mostrar el texto del ítem con color verde si se ha aceptado el orden
                Text(
                    text = item,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    style = if (isAcceptButtonClicked) {
                        TextStyle(color = Color.Green) // Cambiar color a verde cuando se muestra el orden correcto
                    } else {
                        TextStyle(color = Color.Black) // Color negro para cuando no se ha aceptado el orden
                    }
                )

                // Si no se ha aceptado, se pueden mover los ítems
                if (!isAcceptButtonClicked) {
                    // Botón para mover el ítem hacia arriba
                    IconButton(
                        onClick = {
                            if (index > 0) { // Mover el ítem hacia arriba
                                val newList = disorderedItems.toMutableList()
                                val itemToMove = newList.removeAt(index)
                                newList.add(index - 1, itemToMove)
                                disorderedItems = newList // Actualiza el estado
                            }
                            println("NUEVO ORDEN: $disorderedItems")
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Mover arriba")
                    }

                    // Botón para mover el ítem hacia abajo
                    IconButton(
                        onClick = {
                            if (index < disorderedItems.size - 1) {
                                // Mover el ítem hacia abajo
                                val newList = disorderedItems.toMutableList()
                                val itemToMove = newList.removeAt(index)
                                newList.add(index + 1, itemToMove)
                                disorderedItems = newList // Actualiza el estado
                            }
                            println("NUEVO ORDEN: $disorderedItems")
                        }
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Mover abajo")
                    }
                }
            }
        }
    }

    // Pasar los ítems ordenados al estado del padre
    userOrderedItems(disorderedItems.toMutableList())
}

