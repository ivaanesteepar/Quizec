package com.example.quizec.data.model

// Clase que representa un cuestionario
data class Cuestionario(
    val id: String,             // ID único del cuestionario
    val titulo: String,         // Título del cuestionario
    val descripcion: String,    // Descripción del cuestionario
    val creadorId: String,      // ID del creador del cuestionario
    val imagen: String? = null, // Imagen opcional del cuestionario
    val preguntas: List<Pregunta>, // Lista de preguntas completas asociadas al cuestionario
    val immediateAccess: Boolean,
    val locationRestricted: Boolean,
    val immediateResults: Boolean,
    val quizIniciado : Boolean,
    val quizUsed : Boolean,
    val latitude: Double,
    val longitude: Double,
    val radio: Double,           // Radio de ubicación
    val questionsTime: Int
) {
    // Constructor secundario para inicialización por defecto
    constructor() : this(
        "", "", "", "", null, emptyList(),
        false, false, false, false, false,  0.0, 0.0, 0.0, 0
    )
}
