package com.example.quizec.data.model

// Clase que representa un usuario en la aplicación
data class Usuario(
    val id: String,          // ID único del usuario
    val nombre: String,      // Nombre del usuario
    val correo: String,      // Correo electrónico del usuario
    val rol: Rol             // Rol del usuario (creador o participante)
)

// Enum para definir los posibles roles de los usuarios
enum class Rol {
    CREADOR, PARTICIPANTE
}
