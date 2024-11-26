package com.example.quizec.ui.screens.UserQuestionTypes

import android.util.Log
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
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun OrderingQuesitonScreen(
    currentQuestion: Pregunta,
    userOrderedItems: (List<String>) -> Unit
) {
    var disorderedItems by remember { mutableStateOf(currentQuestion.itemsOrdenados.shuffled().toMutableList()) }
    //var disorderedItems = orderedItems.shuffled()
    Column(modifier = Modifier.fillMaxWidth()) {
        disorderedItems.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
            ) {
                // Mostrar el texto del ítem
                Text(
                    text = item,
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )

                // Botón para mover el ítem hacia arriba
                IconButton(
                    onClick = {
                        if (index > 0) {
                            // Mover el ítem hacia arriba
                            val newList = disorderedItems.toMutableList()
                            val itemToMove = newList.removeAt(index)
                            newList.add(index - 1, itemToMove)
                            disorderedItems = newList // Actualiza el estado para que Compose lo detecte
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
                            disorderedItems = newList // Actualiza el estado para que Compose lo detecte
                        }
                        println("NUEVO ORDEN: $disorderedItems")
                    }
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Mover abajo")
                }
            }
        }
    }
    userOrderedItems(disorderedItems.toMutableList())

}