package com.example.quizec.ui.screens.UserQuestionTypes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.quizec.R


@Composable
fun TrueFalseQuestionScreen(
    onSelectedAnswerChange: (List<String>?) -> Unit, // Lambda para actualizar el estado
    falseButtonColor: Color,
    trueButtonColor: Color,
    isAcceptButtonClicked: Boolean,
    correctAnswer: String // La respuesta correcta ("Verdadero" o "Falso")
) {

    val context = LocalContext.current

    Row(
        horizontalArrangement = Arrangement.Center, // Centra los botones horizontalmente
        verticalAlignment = Alignment.CenterVertically, // Centra los botones verticalmente
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Button(
            onClick = {

                onSelectedAnswerChange(listOf(context.getString(R.string.verdadero)))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAcceptButtonClicked && correctAnswer == stringResource(R.string.verdadero)) Color.Green else trueButtonColor
            ),
            enabled = !(isAcceptButtonClicked && correctAnswer != stringResource(R.string.verdadero)), // Deshabilitar si no es la respuesta correcta
            modifier = Modifier.weight(1f) // Ocupa el mismo espacio que el botón de Falso
        ) {
            Text(text = stringResource(R.string.verdadero), color = Color.White)
        }

        Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

        Button(
            onClick = {
                onSelectedAnswerChange(listOf(context.getString(R.string.falso)))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAcceptButtonClicked && correctAnswer == stringResource(R.string.falso)) Color.Green else falseButtonColor
            ),
            enabled = !(isAcceptButtonClicked && correctAnswer != stringResource(R.string.falso)), // Deshabilitar si no es la respuesta correcta
            modifier = Modifier.weight(1f) // Ocupa el mismo espacio que el botón de Verdadero
        ) {
            Text(text = stringResource(R.string.falso), color = Color.White)
        }
    }
}
