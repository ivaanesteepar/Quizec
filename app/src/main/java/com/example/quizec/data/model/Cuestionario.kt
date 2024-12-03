
package com.example.quizec.data.model

// Clase que representa un cuestionario
data class Cuestionario(
    val id: String,             // ID único del cuestionario
    val titulo: String,         // Título del cuestionario
    val descripcion: String,    // Descripción del cuestionario
    val creadorId: String,      // ID del creador del cuestionario
    val imagen: String? = null, // Imagen opcional del cuestionario
    val preguntas: List<Pregunta>, // Lista de preguntas completas asociadas al cuestionario
    //NEW
    val immediateAccess: Boolean,
    val locationRestricted: Boolean,
    val immediateResults: Boolean
){
    constructor() : this("", "", "", "", null, emptyList(), false, false, false)

}
