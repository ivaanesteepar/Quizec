package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizec.data.model.Pregunta

@Composable
fun MissingWordsQuestionScreen(
    currentQuestion: Pregunta,
    opcionesCorrectas: List<String>,
    userInputs: MutableList<String>,
    isAcceptButtonClicked: Boolean
) {
    val fraseCompletar = currentQuestion.fraseCompletar

    // Dividir la frase en palabras y reemplazar las correctas por un espacio (___)
    val palabrasFrase = fraseCompletar.split(" ")
    val fraseConEspacios = palabrasFrase.joinToString(" ") { palabra ->
        if (opcionesCorrectas.contains(palabra)) {
            "___" // Reemplaza las palabras correctas con "___"
        } else {
            palabra // Mantiene las otras palabras
        }
    }

    // Asegurar que userInputs tiene el mismo tamaño que opcionesCorrectas
    if (userInputs.size < opcionesCorrectas.size) {
        for (i in userInputs.size until opcionesCorrectas.size) {
            userInputs.add("") // Agrega entradas vacías para sincronizar el tamaño
        }
    }

    // Manejar listas vacías
    if (opcionesCorrectas.isEmpty()) {
        Text(
            text = "No hay opciones correctas disponibles.",
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp
        )
        return
    }

    // Mostrar la frase modificada
    Text(
        text = "Completa la frase: $fraseConEspacios",
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Agregado padding horizontal
    ) {
        opcionesCorrectas.forEachIndexed { index, _ ->
            if (index >= userInputs.size) return@forEachIndexed // Evita el acceso fuera de rango

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Número de la palabra (1, 2, 3,...)
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    modifier = Modifier.width(24.dp) // Espacio fijo para el número
                )

                if (isAcceptButtonClicked) {
                    TextField(
                        value = opcionesCorrectas[index], // Muestra la respuesta correcta
                        onValueChange = { newValue -> userInputs[index] = newValue },
                        label = { Text(text = "Respuesta correcta") },
                        textStyle = TextStyle(color = Color.Green),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false
                    )
                } else {
                    TextField(
                        value = userInputs[index],
                        onValueChange = { newValue -> userInputs[index] = newValue },
                        label = { Text(text = "Ingresa la palabra correcta") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}


