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
import com.example.quizec.ui.theme.defaultButtonColor
import com.example.quizec.ui.theme.selectedButtonColor

@Composable
fun OneMultChoicesScreen(
    currentQuestion: Pregunta,
    selectedAnswer: List<String>?,
    onSelectedAnswerChange: (List<String>) -> Unit,
    isAcceptButtonClicked: Boolean,
    buttonColors: List<Color> // Este parámetro aceptará los colores de los botones
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        currentQuestion.opciones.forEachIndexed { index, opcion ->
            val isSelected = selectedAnswer?.contains(opcion) == true
            Button(
                onClick = {
                    if (!isSelected && !isAcceptButtonClicked) {
                        onSelectedAnswerChange(listOf(opcion)) // Solo seleccionamos una opción si no está seleccionada
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColors.getOrElse(index) { defaultButtonColor } // Asigna el color del botón
                ),
                enabled = !isAcceptButtonClicked, // Deshabilitar botones después de la selección
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = opcion, color = Color.White)
            }
        }
    }
}
