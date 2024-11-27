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
fun MultChoicesScreen(
    currentQuestion: Pregunta,
    selectedAnswer: List<String>?,
    onSelectedAnswerChange: (List<String>?) -> Unit,
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
                    val updatedAnswer = if (isSelected) {
                        // Si está seleccionada, la eliminamos
                        selectedAnswer?.filter { it != opcion }
                    } else {
                        // Si no está seleccionada, la agregamos
                        (selectedAnswer ?: emptyList()) + opcion
                    }
                    onSelectedAnswerChange(updatedAnswer)
                    Log.d("MultChoicesScreen", "selectedAnswer: $updatedAnswer")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) selectedButtonColor
                    else defaultButtonColor
                ),
                enabled = !isAcceptButtonClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = opcion, color = Color.White)
            }
        }
    }
}