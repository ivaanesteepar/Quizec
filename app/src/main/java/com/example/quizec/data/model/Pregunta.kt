package com.example.quizec.data.model

// Clase que representa una pregunta
data class Pregunta(
    val id: String = "",            // ID único de la pregunta, con valor por defecto
    val titulo: String = "",        // Título o enunciado de la pregunta, con valor por defecto
    val tipo: TipoPregunta = TipoPregunta.VERDADERO_FALSO,    // Tipo de pregunta, valor por defecto
    val opciones: List<String> = listOf(), // Opciones de respuesta con valor por defecto
    val imagen: String? = null, // Imagen opcional
    val respuestasCorrectas: List<String> = listOf(), // Respuestas correctas
    val emparejamientos: List<Map<String, String>> = listOf(),
    val itemsOrdenados: List<String> = listOf(),
    val user_id: String? = null, // ID del usuario que creó la pregunta
    var isSelected: Boolean = false,
    val fraseCompletar: String = "",
    val opcionCorrecta: String = "",
    val conceptosYDefiniciones: List<Map<String, String>> = listOf(),
    val opcionesCorrectasCompletarPalabras: List<String> = listOf(),
    val leftItems: List<String> = listOf(),
    val rightItems: List<String> = listOf(),

) {
    // Constructor sin argumentos
    constructor() : this("", "", TipoPregunta.VERDADERO_FALSO)
}


// Enum para definir los tipos de preguntas
enum class TipoPregunta {
    VERDADERO_FALSO,           // Sí/No, Verdadero/Falso
    OPCION_MULTIPLE_UNA,       // Opción múltiple (una respuesta correcta)
    OPCION_MULTIPLE_MULTIPLES, // Opción múltiple (múltiples respuestas correctas)
    EMPAREJAR,                 // Pregunta de emparejar (Matching)
    ORDENAR,                   // Pregunta de ordenar
    COMPLETAR_ESPACIOS,        // Pregunta de completar el espacio en blanco
    ASOCIACION,                // Pregunta de asociación (imagen o concepto con descripción)
    COMPLETAR_PALABRAS         // Pregunta de completar múltiples palabras
}
