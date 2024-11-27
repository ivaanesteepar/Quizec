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
import com.example.quizec.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val _cuestionario = mutableStateOf<Cuestionario?>(null)
    val cuestionario: State<Cuestionario?> get() = _cuestionario

    //var imageUri = mutableStateOf<Uri?>(null) NO SIRVE
    var imageUri by mutableStateOf<Uri?>(null)

    // State para almacenar las URLs de las imágenes
    var imagenesState = mutableStateOf<List<String>>(emptyList())

    // Flujo para el mensaje de error
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    // Declara el tipo del Map explícitamente
    val _respuestas = MutableStateFlow<Map<String, Any>>(emptyMap())
    val respuestas: StateFlow<Map<String, Any>> get() = _respuestas

    // Mapa de respuestas de todos los usuarios (usamos StateFlow para ser reactivos)
    private val _respuestasUsuario = MutableStateFlow<Map<String, Pair<String, Int>>>(emptyMap())
    val respuestasUsuario: StateFlow<Map<String, Pair<String, Int>>> = _respuestasUsuario

    private val _totalTime = MutableStateFlow(600) // Tiempo total global inicial (10 minutos)
    val totalTime: StateFlow<Int> get() = _totalTime // Exponer el tiempo total como StateFlow

    private val _usuariosTiempo = mutableStateOf<Map<String, Int>>(emptyMap())
    val usuariosTiempo: State<Map<String, Int>> get() = _usuariosTiempo


    private val _remainingTime = mutableStateOf(60) // Tiempo restante para la pregunta
    val remainingTime: State<Int> = _remainingTime // Exponer el tiempo restante como un estado observable


    init {
        obtenerRolUsuario()
    }


    suspend fun obtenerCuestionarioPorCodigo(codigoQuiz: String, userId: String) {
        try {
            // Buscar el cuestionario en la colección "cuestionarios" por su código
            val cuestionariosQuery = firestore.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz)

            // Obtener los documentos del cuestionario
            val documentos = cuestionariosQuery.get().await()

            if (documentos.isEmpty) {
                Log.e("QuizViewModel", "No se encontró ningún cuestionario con el código $codigoQuiz.")
            } else {
                // Suponemos que solo hay un documento con ese 'id', por lo que obtenemos el primer documento
                val documento = documentos.documents.first()

                // Obtener los datos del cuestionario
                val cuestionarioData = documento.data ?: emptyMap<String, Any>()

                // Mapeo de las preguntas desde Firestore
                val preguntasData = (cuestionarioData["preguntas"] as? List<Map<String, Any>>) ?: emptyList()

                // Convertir cada pregunta en un objeto Pregunta
                val preguntas = preguntasData.map { preguntaData ->
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

                    // Crear y devolver el objeto Pregunta
                    Pregunta(
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
                }

                // Crear el objeto Cuestionario con las preguntas mapeadas
                val cuestionario = Cuestionario(
                    id = documento.id,
                    titulo = cuestionarioData["titulo"] as? String ?: "",
                    descripcion = cuestionarioData["descripcion"] as? String ?: "",
                    creadorId = cuestionarioData["creadorId"] as? String ?: "",
                    imagen = cuestionarioData["imagen"] as? String,
                    preguntas = preguntas // Asignar la lista de preguntas obtenidas
                )

                // Crear un historialData con la información del cuestionario
                val historialData = mapOf(
                    "codigoQuiz" to cuestionario.id,
                    "titulo" to cuestionario.titulo,
                    "descripcion" to cuestionario.descripcion,
                    "preguntas" to cuestionario.preguntas
                )

                // Guardar el cuestionario en el historial del usuario con su códigoQuiz
                guardarCuestionarioEnHistorial(userId, codigoQuiz)
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al obtener el cuestionario de Firestore: ${e.message}")
        }
    }

    // Función para obtener el nombre del usuario actual
    fun obtenerNombreUsuario(userId: String, callback: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    callback(document.getString("nombre")) // Si el campo existe, devuelve su valor
                } else {
                    callback(null) // Si no existe, devuelve null
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el nombre del usuario", e)
                callback(null) // Si hay error, devuelve null
            }
    }


    fun guardarCuestionarioEnHistorial(userId: String, codigoQuiz: String) {
        // Llamamos a la función para obtener el nombre del usuario
        obtenerNombreUsuario(userId) { userName ->
            if (userName != null) {
                val db = FirebaseFirestore.getInstance()
                // Referencia al documento de historial del usuario con el codigoQuiz como el nombre del documento
                val historialRef = db.collection("usuarioHistorial")
                    .document(userName) // Documento identificado por el nombre de usuario
                    .collection("cuestionarios") // Subcolección de 'cuestionarios' para cada usuario
                    .document(codigoQuiz) // Documento con el nombre igual al codigoQuiz

                // Recuperar el cuestionario por su 'codigoQuiz'
                val cuestionariosQuery = db.collection("cuestionarios")
                    .whereEqualTo("id", codigoQuiz)

                cuestionariosQuery.get()
                    .addOnSuccessListener { documentos ->
                        if (documentos.documents.isNotEmpty()) {
                            val documento = documentos.documents.first()

                            // Obtener los datos del cuestionario
                            val cuestionarioData = documento.data ?: emptyMap<String, Any>()
                            val preguntasData = (cuestionarioData["preguntas"] as? List<Map<String, Any>>) ?: emptyList()

                            // Mapeo de las preguntas desde Firestore
                            val preguntas = preguntasData.map { preguntaData ->
                                Pregunta(
                                    id = (preguntaData["id"] as? String ?: ""),
                                    titulo = (preguntaData["titulo"] as? String ?: ""),
                                    tipo = TipoPregunta.valueOf((preguntaData["tipo"] as? String ?: "VERDADERO_FALSO")),
                                    opciones = (preguntaData["opciones"] as? List<String>) ?: listOf(),
                                    imagen = (preguntaData["imagen"] as? String),
                                    respuestasCorrectas = (preguntaData["respuestasCorrectas"] as? List<String>) ?: listOf(),
                                    emparejamientos = (preguntaData["emparejamientos"] as? List<Map<String, String>>) ?: listOf(),
                                    itemsOrdenados = (preguntaData["itemsOrdenados"] as? List<String>) ?: listOf(),
                                    user_id = (preguntaData["user_id"] as? String),
                                    isSelected = (preguntaData["isSelected"] as? Boolean ?: false),
                                    fraseCompletar = (preguntaData["fraseCompletar"] as? String ?: ""),
                                    opcionCorrecta = (preguntaData["opcionCorrecta"] as? String ?: ""),
                                    conceptosYDefiniciones = (preguntaData["conceptosYDefiniciones"] as? List<Map<String, String>>) ?: listOf(),
                                    opcionesCorrectasCompletarPalabras = (preguntaData["opcionesCorrectasCompletarPalabras"] as? List<String>) ?: listOf(),
                                    leftItems = (preguntaData["leftItems"] as? List<String>) ?: listOf(),
                                    rightItems = (preguntaData["rightItems"] as? List<String>) ?: listOf()
                                )
                            }

                            // Creamos un mapa con los datos del cuestionario
                            val cuestionarioMap = mapOf(
                                "codigoQuiz" to codigoQuiz,
                                "titulo" to (cuestionarioData["titulo"] as? String ?: ""),
                                "descripcion" to (cuestionarioData["descripcion"] as? String ?: ""),
                                "creadorId" to (cuestionarioData["creadorId"] as? String ?: ""),
                                "imagen" to cuestionarioData["imagen"], // Imagen es opcional
                                "preguntas" to preguntas // Lista de preguntas mapeadas
                            )

                            // Guardamos los datos en Firestore
                            historialRef.set(cuestionarioMap)
                                .addOnSuccessListener {
                                    Log.d("Historial", "Cuestionario guardado exitosamente para $userName con código $codigoQuiz")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Historial", "Error al guardar el cuestionario", e)
                                }
                        } else {
                            Log.e("Historial", "No se encontró el cuestionario con código $codigoQuiz")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Historial", "Error al obtener el cuestionario", e)
                    }
            } else {
                Log.w("Historial", "No se pudo obtener el nombre del usuario")
            }
        }
    }


    // Carga los cuestionarios del historial del usuario
    fun cargarCuestionariosDeUsuario(
        userId: String,
        onSuccess: (List<Cuestionario>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        obtenerNombreUsuario(userId) { nombreUsuario ->
            if (nombreUsuario != null) {
                val cuestionariosRef = db.collection("usuarioHistorial")
                    .document(nombreUsuario)
                    .collection("cuestionarios")

                cuestionariosRef.get()
                    .addOnSuccessListener { documents ->
                        if (documents.documents.isNotEmpty()) {
                            val cuestionarios = documents.documents.mapNotNull { document ->
                                val cuestionarioData = document.data
                                println("Cuestionario data: $cuestionarioData")

                                // Recuperar las preguntas de la subcolección 'preguntas' si existe
                                val preguntasRef = document.reference.collection("preguntas")
                                preguntasRef.get()
                                    .addOnSuccessListener { preguntasDocuments ->
                                        val preguntas = preguntasDocuments.documents.mapNotNull { preguntaDocument ->
                                            val preguntaData = preguntaDocument.data
                                            preguntaData?.let {
                                                // Asignación correcta a la clase Pregunta
                                                Pregunta(
                                                    id = it["codigoQuiz"] as? String ?: "",
                                                    titulo = it["titulo"] as? String ?: "",
                                                    tipo = TipoPregunta.valueOf(it["tipo"] as? String ?: "VERDADERO_FALSO"), // Convertimos el tipo
                                                    opciones = it["opciones"] as? List<String> ?: listOf(),
                                                    imagen = it["imagen"] as? String,
                                                    respuestasCorrectas = it["respuestasCorrectas"] as? List<String> ?: listOf(),
                                                    emparejamientos = it["emparejamientos"] as? List<Map<String, String>> ?: listOf(),
                                                    itemsOrdenados = it["itemsOrdenados"] as? List<String> ?: listOf(),
                                                    user_id = it["user_id"] as? String,
                                                    isSelected = it["isSelected"] as? Boolean ?: false,
                                                    fraseCompletar = it["fraseCompletar"] as? String ?: "",
                                                    opcionCorrecta = it["opcionCorrecta"] as? String ?: "",
                                                    conceptosYDefiniciones = it["conceptosYDefiniciones"] as? List<Map<String, String>> ?: listOf(),
                                                    opcionesCorrectasCompletarPalabras = it["opcionesCorrectasCompletarPalabras"] as? List<String> ?: listOf(),
                                                    leftItems = it["leftItems"] as? List<String> ?: listOf(),
                                                    rightItems = it["rightItems"] as? List<String> ?: listOf()
                                                )
                                            }
                                        }

                                        // Ahora incluimos las preguntas en el cuestionario
                                        val cuestionario = Cuestionario(
                                            id = cuestionarioData?.get("codigoQuiz") as? String ?: "", // Código del cuestionario
                                            titulo = cuestionarioData?.get("titulo") as? String ?: "Sin título",
                                            descripcion = cuestionarioData?.get("descripcion") as? String ?: "",
                                            creadorId = cuestionarioData?.get("creadorId") as? String ?: "",
                                            imagen = cuestionarioData?.get("imagen") as? String,
                                            preguntas = preguntas // Asignamos las preguntas recuperadas
                                        )

                                        // Pasamos el cuestionario con las preguntas
                                        onSuccess(listOf(cuestionario))
                                    }
                            }
                        } else {
                            onFailure(Exception("No se encontraron cuestionarios"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } else {
                onFailure(Exception("No se pudo obtener el nombre del usuario"))
            }
        }
    }












    // Función para decrementar el tiempo total
    fun tick() {
        // Solo decrementa si el tiempo no es 0
        if (_totalTime.value > 0) {
            _totalTime.value -= 1
            println("El tiempo total es ahora: ${_totalTime.value}")
        } else {
            // El tiempo total es 0, no hace nada.
            println("El tiempo ya ha llegado a 0, no se puede decrementar más.")
        }
    }


    // Función que permite iniciar el quiz
    fun iniciarQuiz(codigoQuiz: String, usuarioUid: String) {
        val db = FirebaseFirestore.getInstance()

        // Referencia al documento del código del cuestionario dentro de la colección usuariosEspera
        val usuariosEsperaRef = db.collection("usuariosEspera")
            .document(codigoQuiz)

        println("Usuarios espera ref: $usuariosEsperaRef")

        // Recuperar el documento del código del cuestionario
        usuariosEsperaRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Accedemos al mapa 'usuarios' dentro del documento
                val usuariosMap = document.get("usuarios") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener

                println("Usuarios map: $usuariosMap")

                // Verificamos si el UID del usuario está presente en el mapa
                val usuarioData = usuariosMap[usuarioUid]
                if (usuarioData != null) {
                    // Creamos la referencia al documento del usuario
                    val usuarioRef = usuariosEsperaRef.collection("usuarios").document(usuarioUid)

                    println("Usuario ref iniciarquiz: $usuarioRef")

                    // Actualizamos el campo 'quizTerminado' a false para el usuario específico
                    usuarioRef.update("quizTerminado", false).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("QuizViewModel", "El campo quizTerminado para el usuario $usuarioUid ha sido actualizado a false")
                        } else {
                            Log.e("QuizViewModel", "Error al actualizar el campo quizTerminado para el usuario $usuarioUid.")
                        }
                    }
                } else {
                    Log.e("QuizViewModel", "El usuario con UID $usuarioUid no se encontró en el mapa de usuarios.")
                }
            } else {
                Log.e("QuizViewModel", "No se encontró el documento para el código del cuestionario $codigoQuiz.")
            }
        }.addOnFailureListener { exception ->
            Log.e("QuizViewModel", "Error al acceder al documento: ${exception.message}")
        }
    }



    // Pone el campo de 'quizTerminado' en true para todos los usuarios y acaba la partida
    fun endQuiz(codigoQuiz: String) {
        val db = FirebaseFirestore.getInstance()

        // Referencia al documento del código del cuestionario dentro de la colección usuariosEspera
        val usuariosEsperaRef = db.collection("usuariosEspera")
            .document(codigoQuiz)

        println("Usuarios espera ref: $usuariosEsperaRef")

        // Recuperar el documento del código del cuestionario
        usuariosEsperaRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Accedemos al mapa 'usuarios' dentro del documento
                val usuariosMap = document.get("usuarios") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener

                println("Usuarios map: $usuariosMap")

                // Iteramos sobre los usuarios y actualizamos el campo 'quizTerminado' para cada uno
                val batch = db.batch()

                // Para cada usuario, obtenemos el UID y actualizamos el campo 'quizTerminado'
                usuariosMap.forEach { (uid, usuarioData) ->
                    // Referencia al mapa del usuario dentro del campo 'usuarios'
                    val usuarioRef = usuariosEsperaRef

                    // Si 'quizTerminado' no existe en el mapa, lo actualizamos
                    batch.update(usuarioRef, "usuarios.$uid.quizTerminado", true)
                }

                // Commit de la operación de actualización en batch
                batch.commit().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("QuizViewModel", "Todos los usuarios han sido actualizados con quizTerminado = true")
                    } else {
                        Log.e("QuizViewModel", "Error al actualizar el campo quizTerminado para los usuarios.")
                    }
                }

            } else {
                Log.e("QuizViewModel", "No se encontró el documento para el código del cuestionario $codigoQuiz.")
            }
        }.addOnFailureListener { exception ->
            Log.e("QuizViewModel", "Error al acceder al documento: ${exception.message}")
        }
    }




    // Método para restablecer los tiempos cuando se inicia un nuevo quiz o pregunta
    fun resetTimes() {
        _totalTime.value = 600  // Restablece el tiempo total a 10 minutos
        _remainingTime.value = 30 // Restablece el tiempo por pregunta a 30 segundos
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