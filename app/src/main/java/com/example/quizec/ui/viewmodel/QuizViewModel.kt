package com.example.quizec.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.data.model.UserResponse
import com.example.quizec.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuizViewModel : ViewModel() {
    var usuario = mutableStateOf<Usuario?>(null)
    private val firestore = FirebaseFirestore.getInstance()
    private val _nombreUsuario = MutableStateFlow<String?>(null)
    val nombreUsuario: StateFlow<String?> = _nombreUsuario
    private val _preguntas = mutableStateListOf<Pregunta>()
    var preguntas: List<Pregunta> = _preguntas // preguntas del quiz
    var contadorPreguntas = mutableStateOf(0)
    // StateFlow para almacenar el rol del usuario
    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    // Estado para almacenar los cuestionarios
    private val _cuestionarios = mutableStateOf<List<Cuestionario>>(emptyList())
    val cuestionarios: State<List<Cuestionario>> = _cuestionarios

    //var imageUri = mutableStateOf<Uri?>(null) NO SIRVE
    var imageUri by mutableStateOf<Uri?>(null)

    // State para almacenar las URLs de las imágenes
    var imagenesState = mutableStateOf<List<String>>(emptyList())

    // Flujo para el mensaje de error
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _codigoQuiz = MutableStateFlow<String?>(null)
    val codigoQuiz: StateFlow<String?> get() = _codigoQuiz

    // Declara el tipo del Map explícitamente
    val _respuestas = MutableStateFlow<Map<String, Any>>(emptyMap())
    val respuestas: StateFlow<Map<String, Any>> get() = _respuestas

    // En tu ViewModel o en un lugar adecuado
    val correctAnswersMap = mutableMapOf<String, Int>()

    // Mapa de respuestas de todos los usuarios (usamos StateFlow para ser reactivos)
    private val _respuestasUsuario = MutableStateFlow<Map<String, Pair<String, Int>>>(emptyMap())
    val respuestasUsuario: StateFlow<Map<String, Pair<String, Int>>> = _respuestasUsuario


    init {
        obtenerRolUsuario()
    }

    fun actualizarRespuestasCorrectas(userId: String, userName: String, respuestasCorrectas: Int) {
        val updatedMap = _respuestasUsuario.value.toMutableMap()
        updatedMap[userId] = Pair(userName, respuestasCorrectas)
        _respuestasUsuario.value = updatedMap
    }

    // Función para agregar un nuevo usuario al mapa si no está presente
    fun agregarNuevoUsuario(userId: String, userName: String) {
        val updatedMap = _respuestasUsuario.value.toMutableMap()
        if (!updatedMap.containsKey(userId)) {
            updatedMap[userId] = Pair(userName, 0)  // 0 respuestas correctas inicialmente
        }
        _respuestasUsuario.value = updatedMap
    }


    // Obtener las respuestas correctas de un usuario
    fun obtenerRespuestasCorrectas(userId: String): Int {
        return correctAnswersMap.getOrDefault(userId, 0)
    }


    // Función para cargar las imágenes de los cuestionarios creados por el usuario
    suspend fun cargarImagenesCuestionariosUsuario(userId: String) {
        try {
            val cuestionariosRef = firestore.collection("cuestionarios")
                .whereEqualTo("creadorId", userId) // Filtramos por el ID del creador

            val result = cuestionariosRef.get().await()

            // Extraemos las URLs de las imágenes de los cuestionarios
            val imagenesList = result.documents.mapNotNull { document ->
                document.getString("imagen") // Suponiendo que el campo "imagen" contiene la URL
            }

            // Actualizamos el estado con las URLs de las imágenes
            imagenesState.value = imagenesList
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al cargar imágenes de los cuestionarios: ${e.message}")
        }
    }

    // Función para obtener el rol del usuario
    private fun obtenerRolUsuario() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usuarioRef = firestore.collection("users").document(userId)

        usuarioRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("QuizViewModel", "Error al escuchar cambios en el rol: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val rol = snapshot.getString("rol")
                _userRole.value = rol // Actualiza el flujo con el nuevo rol
                Log.d("QuizViewModel", "Rol del usuario actualizado: $rol")
            } else {
                Log.e("QuizViewModel", "El documento del usuario no existe o está vacío.")
            }
        }
    }

    // Función para actualizar el rol del usuario en Firestore
    fun actualizarRolUsuario(nuevoRol: String, onComplete: (String?) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usuarioRef = firestore.collection("users").document(userId)

        usuarioRef.update("rol", nuevoRol)
            .addOnSuccessListener {
                Log.d("QuizViewModel", "Rol actualizado a $nuevoRol")
                onComplete(null) // Llamar con null si no hubo errores
            }
            .addOnFailureListener { e ->
                Log.e("QuizViewModel", "Error al actualizar el rol: ${e.message}")
                onComplete(e.message) // Llamar con el mensaje de error
            }
    }

    // Función para agregar una pregunta a la lista
    fun agregarPregunta(pregunta: Pregunta) {
        if (!_preguntas.contains(pregunta)) {
            _preguntas.add(pregunta) // Agregar la pregunta solo si no está en la lista
            contadorPreguntas.value += 1 // Aumentar el contador solo si la pregunta es nueva
        }
    }

    // Función para cargar los cuestionarios del usuario
    suspend fun cargarCuestionariosPorUsuario(userId: String) {
        try {
            val cuestionariosRef = firestore.collection("cuestionarios")
                .whereEqualTo("creadorId", userId)

            val result = cuestionariosRef.get().await()

            // Transformar los resultados a una lista de Cuestionario
            val cuestionarios = result.documents.mapNotNull { document ->
                document.toObject(Cuestionario::class.java)
            }

            // Actualizar el estado con los cuestionarios cargados
            _cuestionarios.value = cuestionarios
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al cargar cuestionarios: ${e.message}")
        }
    }


    suspend fun cargarPreguntasPorCodigo(codigoQuiz: String) {
        try {
            // Realizar una consulta para buscar el documento cuyo campo 'id' sea igual al código del cuestionario
            val cuestionariosQuery = firestore.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz)

            // Obtener los resultados de la consulta
            val documentos = cuestionariosQuery.get().await()

            println("Documentos encontrados: ${documentos.documents}")

            if (documentos.isEmpty) {
                Log.e("QuizViewModel", "No se encontró ningún cuestionario con el código $codigoQuiz.")
            } else {
                // Suponemos que solo hay un documento con ese 'id', por lo que obtenemos el primer documento
                val documento = documentos.documents.first()

                // Obtener las preguntas como una lista de mapas
                val preguntasData = documento.get("preguntas") as? List<Map<String, Any>> ?: emptyList()

                // Limpiar la lista de preguntas antes de agregar las nuevas
                _preguntas.clear()

                // Convertir cada mapa en un objeto Pregunta
                preguntasData.forEach { preguntaData ->
                    val id = preguntaData["id"] as? String ?: ""
                    val titulo = preguntaData["titulo"] as? String ?: ""
                    val tipo = (preguntaData["tipo"] as? String)?.let { TipoPregunta.valueOf(it) } ?: TipoPregunta.VERDADERO_FALSO
                    val opciones = (preguntaData["opciones"] as? List<String>) ?: emptyList()
                    val imagen = preguntaData["imagen"] as? String
                    val respuestasCorrectas = (preguntaData["respuestasCorrectas"] as? List<String>) ?: emptyList()
                    val emparejamientos = (preguntaData["emparejamientos"] as? List<Map<String, String>>) ?: emptyList()
                    val itemsOrdenados = (preguntaData["itemsOrdenados"] as? List<String>) ?: emptyList()
                    val fraseCompletar = preguntaData["fraseCompletar"] as? String ?: ""
                    val opcionCorrecta = preguntaData["opcionCorrecta"] as? String ?: ""
                    val conceptosYDefiniciones = (preguntaData["conceptosYDefiniciones"] as? List<Map<String, String>>) ?: emptyList()
                    val user_id = preguntaData["user_id"] as? String
                    val isSelected = preguntaData["isSelected"] as? Boolean ?: false
                    val opcionesCorrectasCompletarPalabras = preguntaData["opcionesCorrectasCompletarPalabras"] as? List<String> ?: emptyList()
                    val leftItems = preguntaData["leftItems"] as? List<String> ?: emptyList()
                    val rightItems = preguntaData["rightItems"] as? List<String> ?: emptyList()

                    // Crear el objeto Pregunta
                    val pregunta = Pregunta(
                        id = id,
                        titulo = titulo,
                        tipo = tipo,
                        opciones = opciones,
                        imagen = imagen,
                        respuestasCorrectas = respuestasCorrectas,
                        emparejamientos = emparejamientos,
                        itemsOrdenados = itemsOrdenados,
                        fraseCompletar = fraseCompletar,
                        opcionCorrecta = opcionCorrecta,
                        conceptosYDefiniciones = conceptosYDefiniciones,
                        user_id = user_id,
                        isSelected = isSelected,
                        opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras,
                        leftItems = leftItems,
                        rightItems = rightItems
                    )

                    // Añadir la pregunta a la lista
                    _preguntas.add(pregunta)
                }

                // Actualizar el contador de preguntas
                contadorPreguntas.value = _preguntas.size
                println("Preguntas cargadas: ${_preguntas.size}") // BIEN
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al obtener las preguntas de Firestore: ${e.message}")
        }
    }

    // Función para obtener el código del quiz
    fun obtenerCodigoQuiz() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val cuestionariosRef = firestore.collection("cuestionarios")
            .whereEqualTo("creadorId", userId) // Filtramos por el ID del creador

        // Realiza la consulta
        viewModelScope.launch {
            try {
                val result = cuestionariosRef.get().await()
                if (!result.isEmpty) {
                    // Suponemos que solo hay un cuestionario por usuario, así que obtenemos el primer documento
                    val cuestionario = result.documents.first()
                    val codigo = cuestionario.getString("id") // Suponemos que el campo 'id' es el código
                    _codigoQuiz.value = codigo // Asignamos el valor al MutableStateFlow
                    println("EL CODIGO DEL QUIZ ES: $codigo")
                } else {
                    Log.e("QuizViewModel", "No se encontró ningún cuestionario para el usuario con ID $userId")
                }
            } catch (e: Exception) {
                Log.e("QuizViewModel", "Error al obtener el código del quiz: ${e.message}")
            }
        }
    }

    // Función para generar un código de 6 dígitos
    fun generarClave(): String {
        return (100000..999999).random().toString()  // Genera un código de 6 dígitos
    }

    // Función para guardar un cuestionario en Firestore
    fun guardarCuestionarioEnFirestore(cuestionario: Map<String, Any>, onComplete: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val cuestionariosRef = db.collection("cuestionarios") // Cambia "cuestionarios" por el nombre de tu colección

        cuestionariosRef.add(cuestionario)
            .addOnSuccessListener { documentReference ->
                Log.d("QuizViewModel", "Cuestionario creado con ID: ${documentReference.id}")
                onComplete(null)  // Llama al onComplete con null indicando éxito
            }
            .addOnFailureListener { e ->
                Log.w("QuizViewModel", "Error al agregar el cuestionario", e)
                onComplete(e.message)  // Llama al onComplete con el mensaje de error
            }
    }


    // Función para verificar si el código del quiz existe en la base de datos
    fun verificarCodigoQuiz(codigoQuiz: String, onComplete: (Boolean) -> Unit) {
        val cuestionarioRef = firestore.collection("cuestionarios").whereEqualTo("id", codigoQuiz)

        cuestionarioRef.get()
            .addOnSuccessListener { querySnapshot ->
                // Si hay al menos un documento, el código es válido
                if (!querySnapshot.isEmpty) {
                    onComplete(true)  // El código es válido, existe un cuestionario con ese código
                } else {
                    onComplete(false)  // El código no es válido
                }
            }
            .addOnFailureListener { exception ->
                onComplete(false)  // Error al verificar el código
                println("Error al verificar el código del quiz: ${exception.message}")
            }
    }

}