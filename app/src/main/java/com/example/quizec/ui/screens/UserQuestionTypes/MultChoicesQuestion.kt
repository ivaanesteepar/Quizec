package com.example.quizec.ui.screens.UserQuestionTypes

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quizec.data.model.Pregunta

@Composable
fun MultChoicesQuestionScreen(
    currentQuestion: Pregunta,
    selectedAnswer: List<String>?,
    onSelectedAnswerChange: (List<String>?) -> Unit,
    isAcceptButtonClicked: Boolean,
    correctAnswers: List<String> // Parámetro para almacenar las respuestas correctas
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        currentQuestion.opciones.forEach { opcion ->
            val isSelected = selectedAnswer?.contains(opcion) == true
            val isCorrect = correctAnswers.contains(opcion) // Verifica si la opción es correcta
            val isDisabled = isAcceptButtonClicked && !isCorrect // Deshabilitar respuestas incorrectas

            // El color del botón cambia solo si ya se hizo clic en el botón de aceptación
            val buttonColor = when {
                isAcceptButtonClicked && isCorrect -> Color.Green // Verde para respuestas correctas cuando se ha aceptado
                isSelected -> Color(0xFFFF9800) // Naranja para respuestas seleccionadas pero incorrectas
                else -> Color.Unspecified // Gris para respuestas no seleccionadas
            }

            Button(
                onClick = {
                    val updatedAnswer = if (isSelected) {
                        // Si la opción ya está seleccionada, la eliminamos
                        selectedAnswer?.filter { it != opcion }
                    } else {
                        // Si no está seleccionada, la agregamos
                        (selectedAnswer ?: emptyList()) + opcion
                    }
                    onSelectedAnswerChange(updatedAnswer)
                    Log.d("MultChoicesScreen", "selectedAnswer: $updatedAnswer")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor // Aplicamos el color del botón según la respuesta
                ),
                enabled = !isDisabled, // Deshabilitar el botón si es incorrecto y ya se aceptó la respuesta
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = opcion, color = Color.White)
            }
        }
    }
}


