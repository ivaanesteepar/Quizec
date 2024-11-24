package com.example.quizec.data.model

data class UserResponse(
    val questionId: String,         // ID de la pregunta
    val selectedAnswers: List<String>, // Respuestas seleccionadas por el usuario
    val isCorrect: Boolean       // Si la respuesta es correcta o no
)

