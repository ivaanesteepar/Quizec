package com.example.quizec.ui.screens.UserQuestionTypes

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
fun OneMultChoicesQuestion(
    currentQuestion: Pregunta,
    selectedAnswer: List<String>?,
    onSelectedAnswerChange: (List<String>) -> Unit,
    isAcceptButtonClicked: Boolean,
    correctAnswer: String // Este es el parámetro para la respuesta correcta
) {
    val selectedButtonColor = Color(0xFFFF9800) // Naranja
    val defaultButtonColor = Color.Unspecified
    val correctButtonColor = Color.Green

    println("correctaONEMULT: $correctAnswer isAccept: $isAcceptButtonClicked")

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        currentQuestion.opciones.forEach { opcion ->
            val isSelected = selectedAnswer?.contains(opcion) == true
            val isCorrect = opcion == correctAnswer
            val isDisabled = isAcceptButtonClicked && !isCorrect

            Button(
                onClick = {
                    // Solo permitimos cambiar la selección si no ha sido aceptada
                    if (!isAcceptButtonClicked) {
                        onSelectedAnswerChange(listOf(opcion)) // Seleccionamos la opción
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isAcceptButtonClicked && isCorrect -> correctButtonColor // Opción correcta se pone verde
                        isSelected -> selectedButtonColor // Respuesta seleccionada en naranja
                        else -> defaultButtonColor // Color por defecto
                    }
                ),
                enabled = !isDisabled, // Deshabilitar el botón si no es la respuesta correcta
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = opcion, color = Color.White)
            }
        }
    }
}





