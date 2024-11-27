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
fun MultChoicesScreen(
    currentQuestion: Pregunta,
    selectedAnswer: List<String>?,
    onSelectedAnswerChange: (List<String>) -> Unit,
    isAcceptButtonClicked: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        currentQuestion.opciones.forEach { opcion ->
            val isSelected = selectedAnswer?.contains(opcion) == true

            Button(
                onClick = {
                    if (selectedAnswer != null) {
                        if (isSelected) {
                            onSelectedAnswerChange(selectedAnswer.filter { it != opcion })

                        } else {
                            onSelectedAnswerChange(selectedAnswer + opcion)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Color(
                        0xFFFFA500
                    ) else Color(0xFF2196F3)
                ),
                enabled = !isAcceptButtonClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = opcion, color = Color.White)
            }
        }
    }
}