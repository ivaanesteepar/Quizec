package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quizec.R
import com.example.quizec.data.model.Pregunta

@Composable
fun FillBlankQuestionScreen(
    currentQuestion: Pregunta,
    selectedOption: String?, // Esto será de solo lectura, se actualizará con la función
    onOptionSelected: (String) -> Unit, // Función para actualizar el estado
    isAcceptButtonClicked: Boolean,
    immediateResults: Boolean,
    quizTerminado: Boolean
) {
    val fraseCompletar = currentQuestion.fraseCompletar // La frase completa
    val palabraCorrecta = currentQuestion.opcionCorrecta // La palabra correcta

    // Crear la lista de opciones asegurándose de que la palabra correcta solo aparezca una vez
    val opciones = remember(currentQuestion.opciones) { //usa remember para recordar esta lista entre recomposicione
        currentQuestion.opciones.toMutableList().apply { //copia mutable de la lista original
            if (!contains(palabraCorrecta)) {
                add(palabraCorrecta) // Solo agrega la opción correcta si no está ya en la lista
            }
        }
    }

    // Sustituir la palabra correcta por un espacio en blanco (___)
    val fraseConEspacio = fraseCompletar.replace(palabraCorrecta, "___")

    var expanded by remember { mutableStateOf(false) } // Controla el estado del menú desplegable

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mostrar la frase con el espacio en blanco
        Text(
            text = fraseConEspacio,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp)) // Separación entre la frase y las opciones

        // Caja para el desplegable
        Box {
            OutlinedButton( // Botón q abre el menú desplegable
                onClick = { expanded = !expanded },
                enabled = !isAcceptButtonClicked // Deshabilitar el botón si el usuario ya aceptó la respuesta
            ) {
                Text(
                    // Mostrar la opción seleccionada o la opción correcta si ya se aceptó
                    text = if (isAcceptButtonClicked && immediateResults || (!immediateResults && quizTerminado))
                        currentQuestion.opcionCorrecta else selectedOption ?: stringResource(
                        R.string.opcion
                    ),
                    color = if (isAcceptButtonClicked && immediateResults
                        || (!immediateResults && quizTerminado)) Color.Green else MaterialTheme.colorScheme.onSurface
                )
            }

            // Menú desplegable con las opciones
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false } // Cerrar el menú cuando se hace clic fuera de él
            ) {
                // Mostrar las opciones disponibles
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(text = opcion) },
                        onClick = {
                            onOptionSelected(opcion) // Llamar la función para actualizar la opción seleccionada
                            expanded = false // Cerrar el menú al seleccionar una opción
                        }
                    )
                }
            }
        }
    }
}


