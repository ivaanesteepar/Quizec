package com.example.quizec.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsersViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _usuariosEnEspera = MutableStateFlow<List<String>>(emptyList())
    val usuariosEnEspera: StateFlow<List<String>> = _usuariosEnEspera

    private val _usuariosConRespuestas = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val usuariosConRespuestas: StateFlow<List<Pair<String, Int>>> = _usuariosConRespuestas


    suspend fun obtenerNombreUsuario(userId: String, codigoQuiz: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Referencia al documento del quiz
                val quizRef = db.collection("usuariosEspera").document(codigoQuiz)
                val quizSnapshot = quizRef.get().await()

                if (quizSnapshot.exists()) {
                    // Obtiene el mapa de usuarios
                    val usuarios = quizSnapshot.get("usuarios") as? Map<String, Map<String, Any>>
                    val usuario = usuarios?.get(userId)

                    // Retorna el nombre del usuario si existe, de lo contrario, "Desconocido"
                    usuario?.get("nombre") as? String ?: "Desconocido"
                } else {
                    "Desconocido" // Si el documento no existe
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "Desconocido" // En caso de error
            }
        }
    }

    // Agrega un usuario a un quiz
    fun agregarUsuarioAQuiz(codigoQuiz: String) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val nombreUsuario = document.getString("nombre") ?: "Desconocido"
                        val rolUsuario = document.getString("rol") ?: "Desconocido"

                        // Añadimos respuestasCorrectas con valor 0
                        val usuario = mapOf(
                            "id" to userId,
                            "nombre" to nombreUsuario,
                            "rol" to rolUsuario,
                            "respuestasCorrectas" to 0,  // Se inicializa con 0
                            "quizTerminado" to false
                        )

                        val usuariosEsperaRef = db.collection("usuariosEspera")
                        val quizRef = usuariosEsperaRef.document(codigoQuiz)

                        quizRef.get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    // Obtener los usuarios actuales del documento
                                    val usuariosExistentes = doc.get("usuarios") as? MutableMap<String, Map<String, Any>> ?: mutableMapOf()

                                    // Añadir al nuevo usuario si no está ya en la lista
                                    if (!usuariosExistentes.containsKey(userId)) {
                                        usuariosExistentes[userId] = usuario
                                        println("Usuario agregado correctamente al quiz.")
                                    }

                                    // Actualizamos el documento con los usuarios existentes
                                    quizRef.update("usuarios", usuariosExistentes)
                                        .addOnSuccessListener {
                                            println("Usuarios actualizados correctamente.")
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error al actualizar el documento: ${e.message}")
                                        }

                                } else {
                                    // Si el documento no existe, creamos uno nuevo con el usuario
                                    val usuariosMap = mutableMapOf(userId to usuario)
                                    quizRef.set(mapOf("usuarios" to usuariosMap))
                                        .addOnSuccessListener {
                                            println("Documento creado y usuario agregado correctamente.")
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error al crear el documento: ${e.message}")
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                println("Error al verificar documento: ${e.message}")
                            }
                    }
                    .addOnFailureListener { e ->
                        println("Error al obtener el nombre del usuario: ${e.message}")
                    }
            }
        }
    }

    // Escuchar los cambios en los usuarios y sus respuestas correctas
    fun escucharNombreYRespuestasCorrectas(codigoQuiz: String, callback: (List<Pair<String, Int>>) -> Unit) {
        val quizRef = db.collection("usuariosEspera").document(codigoQuiz)

        quizRef.addSnapshotListener { doc, e ->
            if (e != null) {
                println("Error al escuchar cambios: ${e.message}")
                return@addSnapshotListener
            }

            if (doc != null && doc.exists()) {
                // Obtener todos los usuarios del documento
                val usuariosExistentes = doc.get("usuarios") as? Map<String, Map<String, Any>> ?: emptyMap()

                // Obtener la lista de nombres y respuestas correctas de todos los usuarios
                val usuarios = usuariosExistentes.map { (userId, usuario) ->
                    val nombre = usuario["nombre"] as? String ?: "Desconocido"
                    val respuestasCorrectas = (usuario["respuestasCorrectas"] as? Long)?.toInt() ?: 0
                    nombre to respuestasCorrectas  // Retornamos un par (nombre, respuestasCorrectas)
                }

                // Llamamos al callback con la lista de usuarios
                callback(usuarios)
            } else {
                callback(emptyList())  // Si no hay usuarios, devolvemos una lista vacía
            }
        }
    }


    fun obtenerRespuestasCorrectas(userId: String, codigoQuiz: String, callback: (Int) -> Unit) {
        val quizRef = db.collection("usuariosEspera").document(codigoQuiz)
        quizRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val usuariosExistentes = doc.get("usuarios") as? Map<String, Map<String, Any>> ?: emptyMap()
                    val usuarioExistente = usuariosExistentes[userId]
                    println("Usuario existente: $usuarioExistente")
                    val respuestasCorrectas = (usuarioExistente?.get("respuestasCorrectas") as? Long)?.toInt() ?: 0
                    println("Respuestas correctas obtenidas: $respuestasCorrectas")
                    callback(respuestasCorrectas)
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener respuestas correctas: ${e.message}")
                callback(0)  // En caso de error, devolvemos 0
            }
    }


    fun actualizarRespuestasCorrectas(userId: String, codigoQuiz: String, respuestasCorrectas: Int) {
        val quizRef = db.collection("usuariosEspera").document(codigoQuiz)

        quizRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val usuariosExistentes = doc.get("usuarios") as? MutableMap<String, Map<String, Any>> ?: mutableMapOf()
                    val usuarioExistente = usuariosExistentes[userId] as? MutableMap<String, Any>

                    if (usuarioExistente != null) {
                        // Actualizamos el número de respuestas correctas
                        usuarioExistente["respuestasCorrectas"] = respuestasCorrectas

                        // Actualizamos el documento con la lista de usuarios
                        quizRef.update("usuarios", usuariosExistentes)
                            .addOnSuccessListener {
                                println("Respuestas correctas actualizadas correctamente.")
                            }
                            .addOnFailureListener { e ->
                                println("Error al actualizar respuestas correctas: ${e.message}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener documento: ${e.message}")
            }
    }



    // Escuchar los cambios en los usuarios en espera
    fun escucharUsuariosEnEspera(codigoQuiz: String) {
        db.collection("usuariosEspera").document(codigoQuiz)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    println("Error al escuchar cambios: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val usuariosMap = snapshot.get("usuarios") as? Map<String, Map<String, Any>> ?: emptyMap()

                    val nombresUsuarios = usuariosMap.values.map { usuario ->
                        usuario["nombre"] as? String ?: "Desconocido"
                    }

                    _usuariosEnEspera.value = nombresUsuarios
                }
            }
    }


    // Eliminar al usuario de la lista en la base de datos y si no hay más usuarios, eliminar el documento
    fun eliminarUsuarioDeQuiz(codigoQuiz: String) {
        viewModelScope.launch {
            // Obtener el usuario actual desde Firebase Authentication
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid

            if (userId != null) {
                // Referencia a la colección "usuariosEspera"
                val usuariosEsperaRef = db.collection("usuariosEspera")

                // Obtener el documento de la colección "usuariosEspera" con el código del quiz
                val quizRef = usuariosEsperaRef.document(codigoQuiz)

                // Verificar si el documento con el código del quiz ya existe
                quizRef.get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            // Obtener los usuarios existentes del documento
                            val usuariosExistentes = doc.get("usuarios") as? MutableMap<String, Map<String, Any>> ?: mutableMapOf()

                            // Verificar si el usuario está en la lista de espera
                            if (usuariosExistentes.containsKey(userId)) {
                                // Eliminar el usuario de la lista
                                usuariosExistentes.remove(userId)

                                // Actualizamos el documento con la lista de usuarios actualizada
                                quizRef.update("usuarios", usuariosExistentes)
                                    .addOnSuccessListener {
                                        println("Usuario eliminado correctamente de la lista de espera.")

                                        // Verificar si no hay más usuarios en el quiz
                                        if (usuariosExistentes.isEmpty()) {
                                            // Si no hay más usuarios, eliminar el documento del quiz
                                            quizRef.delete()
                                                .addOnSuccessListener {
                                                    println("Documento del quiz eliminado, ya no hay usuarios.")
                                                }
                                                .addOnFailureListener { e ->
                                                    println("Error al eliminar el documento del quiz: ${e.message}")
                                                }
                                        }

                                        // Actualizar la lista local de usuarios en espera
                                        actualizarUsuariosEnEspera(codigoQuiz)
                                    }
                                    .addOnFailureListener { e ->
                                        println("Error al eliminar el usuario: ${e.message}")
                                    }
                            } else {
                                println("El usuario no está en la lista de espera.")
                            }
                        } else {
                            println("El documento del quiz no existe.")
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Error al obtener el documento del quiz: ${e.message}")
                    }
            }
        }
    }

    // Actualiza la lista de usuarios de un quiz
    fun actualizarUsuariosEnEspera(codigoQuiz: String) {
        db.collection("usuariosEspera").document(codigoQuiz)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Obtener los usuarios del documento
                    val usuariosMap = document.get("usuarios") as? Map<String, Map<String, Any>> ?: emptyMap()

                    // Extraer los nombres de los usuarios
                    val nombresUsuarios = usuariosMap.values.map { usuario ->
                        usuario["nombre"] as? String ?: "Desconocido"
                    }

                    // Actualizar el estado local
                    _usuariosEnEspera.value = nombresUsuarios
                }
            }
            .addOnFailureListener { e ->
                println("Error al actualizar los usuarios en espera: ${e.message}")
            }
    }
}