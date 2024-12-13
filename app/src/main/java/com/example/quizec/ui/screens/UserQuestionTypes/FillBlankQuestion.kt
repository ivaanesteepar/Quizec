package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun FillBlankQuestionScreen(
    currentQuestion: Pregunta,
    selectedOption: String?, // Esto será de solo lectura, se actualizará con la función
    onOptionSelected: (String) -> Unit, // Función para actualizar el estado
    isAcceptButtonClicked: Boolean
) {
    val fraseCompletar = currentQuestion.fraseCompletar // La frase completa
    val palabraCorrecta = currentQuestion.opcionCorrecta // La palabra correcta

    // Crear la lista de opciones asegurándose de que la palabra correcta solo aparezca una vez
    val opciones = remember(currentQuestion.opciones) {
        currentQuestion.opciones.toMutableList().apply {
            if (!contains(palabraCorrecta)) {
                add(palabraCorrecta) // Solo agrega la opción correcta si no está ya en la lista
            }
        }
    }

    // Dividir la frase en partes antes y después de la palabra correcta
    val partesFrase = fraseCompletar.split(palabraCorrecta)
    val fraseAntes = partesFrase.getOrNull(0) ?: ""
    val fraseDespues = partesFrase.getOrNull(1) ?: ""

    var expanded by remember { mutableStateOf(false) } // Controla el estado del menú desplegable

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start, // Distribuye de izquierda a derecha sin estiramiento innecesario
            modifier = Modifier.fillMaxWidth()
        ) {
            // Parte antes del espacio en blanco
            Text(
                text = fraseAntes,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.wrapContentWidth() // Tamaño ajustado al contenido
            )

            Spacer(modifier = Modifier.width(8.dp)) // Separación entre el texto y el desplegable

            // Caja para el desplegable
            Box {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    enabled = !isAcceptButtonClicked
                ) {
                    Text(
                        text = if (isAcceptButtonClicked) currentQuestion.opcionCorrecta else selectedOption ?: "Opción",
                        color = if (isAcceptButtonClicked) Color.Green else MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    opciones.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(text = opcion) },
                            onClick = {
                                onOptionSelected(opcion)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp)) // Separación entre el desplegable y el texto

            // Parte después del espacio en blanco
            Text(
                text = fraseDespues,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.wrapContentWidth() // Tamaño ajustado al contenido
            )
        }
    }
}
