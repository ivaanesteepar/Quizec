
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
import com.example.quizec.data.model.Rol
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
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

    var _preguntas = mutableStateListOf<Pregunta>()
    var preguntas: List<Pregunta> = _preguntas

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
    private val _userAnswers = MutableStateFlow<Map<String, Any>>(emptyMap())
    val userAnswers: StateFlow<Map<String, Any>> get() = _userAnswers

    //OJO Q LO CAMBIE Y NS SI AFECTA!!!!!!!!!!!
    // Mapa de respuestas de todos los usuarios (usamos StateFlow para ser reactivos)
    private val _respuestas = MutableStateFlow<Map<String, Pair<String, Int>>>(emptyMap())
    val respuestas: StateFlow<Map<String, Pair<String, Int>>> = _respuestas

    private val _totalTime = MutableStateFlow(600) // Tiempo total global inicial (10 minutos)
    val totalTime: StateFlow<Int> get() = _totalTime // Exponer el tiempo total como StateFlow

    private val _usuariosTiempo = mutableStateOf<Map<String, Int>>(emptyMap())
    val usuariosTiempo: State<Map<String, Int>> get() = _usuariosTiempo

    private val _remainingTime = mutableStateOf(60) // Tiempo restante para la pregunta
    val remainingTime: State<Int> = _remainingTime // Exponer el tiempo restante como un estado observable

    // MutableState para almacenar el valor de immediateAccess
    private val _immediateAccess = mutableStateOf(false)  // Iniciar en false
    val immediateAccess: State<Boolean> = _immediateAccess

    private val _isQuizIniciado = MutableStateFlow(false)
    val isQuizIniciado: StateFlow<Boolean> = _isQuizIniciado

    init {
        obtenerRolUsuario()
    }

    // Función para eliminar una pregunta
    fun eliminarPreguntaCuestionario(cuestionarioId: String, preguntaId: String, onSuccess: () -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("cuestionarios")
            .whereEqualTo("id", cuestionarioId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val documento = querySnapshot.documents[0]
                    val cuestionarioRef = documento.reference
                    val preguntas = documento.get("preguntas") as? MutableList<Map<String, Any>> ?: mutableListOf()

                    // Filtra el array para eliminar la pregunta
                    val preguntasActualizadas = preguntas.filter { it["id"] != preguntaId }

                    // Actualiza Firestore
                    cuestionarioRef.update("preguntas", preguntasActualizadas)
                        .addOnSuccessListener {
                            // Aquí también actualizamos la lista local de preguntas en el ViewModel
                            _preguntas.removeIf { it.id == preguntaId }

                            // Llama al callback de éxito
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            println("Error al eliminar la pregunta: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                println("Error al obtener el cuestionario: ${exception.message}")
            }
    }


    fun obtenerCuestionario(cuestionarioId: String, onResult: (Cuestionario?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val cuestionariosRef = db.collection("cuestionarios")

        // Hacer una consulta para buscar el documento cuyo campo "id" coincida con el "cuestionarioId"
        cuestionariosRef
            .whereEqualTo("id", cuestionarioId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Si la consulta encuentra documentos, obtenemos el primero
                    val document = querySnapshot.documents[0]
                    val cuestionario = document.toObject(Cuestionario::class.java)
                    onResult(cuestionario) // Retorna el cuestionario encontrado
                } else {
                    onResult(null) // No se encontró el cuestionario
                }
            }
            .addOnFailureListener { exception ->
                onResult(null) // Si ocurre un error, retornamos null
                Log.e("obtenerCuestionario", "Error al obtener el cuestionario: ${exception.message}")
            }
    }


    fun actualizarCuestionario(
        cuestionario: Cuestionario,
        codigoQuiz: String,
        onError: (String?) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()

        // Buscar el documento usando whereEqualTo
        db.collection("cuestionarios")
            .whereEqualTo("id", codigoQuiz)  // Filtrar por el campo 'id' del cuestionario
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Si encontramos el documento con el id correcto
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]  // Obtener el primer documento que coincide

                    // Crear un mapa con los campos que deseas actualizar
                    val camposActualizados = hashMapOf<String, Any>(
                        "titulo" to cuestionario.titulo,
                        "descripcion" to cuestionario.descripcion,
                        "creadorId" to cuestionario.creadorId,
                        "immediateAccess" to cuestionario.immediateAccess,
                        "locationRestricted" to cuestionario.locationRestricted,
                        "immediateResults" to cuestionario.immediateResults,
                        "isQuizIniciado" to cuestionario.isQuizIniciado,
                        "latitude" to cuestionario.latitude,
                        "longitude" to cuestionario.longitude,
                        "radio" to cuestionario.radio
                    )

                    // Si la imagen ha cambiado, también actualizamos ese campo
                    cuestionario.imagen?.let {
                        camposActualizados["imagen"] = it
                    }

                    // Actualizar las preguntas del cuestionario si es necesario
                    camposActualizados["preguntas"] = cuestionario.preguntas

                    // Realizar la actualización de los campos seleccionados en Firestore
                    document.reference.update(camposActualizados)
                        .addOnSuccessListener {
                            onError(null) // No error, actualizaciones exitosas
                        }
                        .addOnFailureListener { exception ->
                            onError(exception.message) // En caso de error, pasa el mensaje
                        }
                } else {
                    // Si no se encuentra ningún documento con el id
                    onError("No se encontró el cuestionario con el código proporcionado.")
                }
            }
            .addOnFailureListener { exception ->
                onError("Error al obtener el cuestionario: ${exception.message}")
            }
    }

    suspend fun obtenerImmediateAccess(codigoQuiz: String): Boolean? {
        val db = FirebaseFirestore.getInstance()

        return try {
            val cuestionarioSnapshot = db.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz)  // Filtrar por el campo "id"
                .get()
                .await()  // Esperar la respuesta de Firestore

            // Imprimir el snapshot para depuración
            Log.d("QuizViewModel", "Cuestionario snapshot: $cuestionarioSnapshot")

            if (!cuestionarioSnapshot.isEmpty) {
                // Verificar que se obtienen documentos
                val cuestionario = cuestionarioSnapshot.documents[0]
                val immediateAccess = cuestionario.getBoolean("immediateAccess")

                // Imprimir el valor de immediateAccess
                Log.d("QuizViewModel", "Valor de immediateAccess: $immediateAccess")

                immediateAccess  // Devolver el valor de immediateAccess (true o false)
            } else {
                Log.d("QuizViewModel", "No se encontró el cuestionario con el código: $codigoQuiz")
                null
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al obtener el cuestionario: ${e.localizedMessage}")
            null  // En caso de error, devolver null
        }
    }

    suspend fun obtenerLocationRestricted(codigoQuiz: String): Boolean? {
        val db = FirebaseFirestore.getInstance()

        return try {
            val cuestionarioSnapshot = db.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz)  // Filtrar por el campo "id"
                .get()
                .await()  // Esperar la respuesta de Firestore

            // Imprimir el snapshot para depuración
            Log.d("QuizViewModel", "Cuestionario snapshot: $cuestionarioSnapshot")

            if (!cuestionarioSnapshot.isEmpty) {
                // Verificar que se obtienen documentos
                val cuestionario = cuestionarioSnapshot.documents[0]
                val locationRestricted = cuestionario.getBoolean("locationRestricted")

                locationRestricted  // Devolver el valor de immediateAccess (true o false)
            } else {
                Log.d("QuizViewModel", "No se encontró el cuestionario con el código: $codigoQuiz")
                null
            }
        } catch (e: Exception) {
            Log.e("QuizViewModel", "Error al obtener el cuestionario: ${e.localizedMessage}")
            null  // En caso de error, devolver null
        }
    }


    // Esta función obtiene el título del cuestionario por su código (codigoQuiz)
    fun obtenerTitulo(codigoQuiz: String, onResult: (String?) -> Unit) {
        firestore.collection("cuestionarios") // Suponiendo que tienes una colección llamada "cuestionarios"
            .whereEqualTo("id", codigoQuiz) // Buscamos por el campo 'id' que debe coincidir con 'codigoQuiz'
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Si se encuentra un documento que cumple con la condición
                    val document = querySnapshot.documents.first()
                    val titulo = document.getString("titulo") // Obtenemos el campo 'titulo' del documento
                    onResult(titulo) // Llamamos al callback con el título encontrado
                } else {
                    onResult(null) // Si no se encuentra ningún documento, retornamos null
                }
            }
            .addOnFailureListener { exception ->
                Log.e("QuizViewModel", "Error al obtener el título: ", exception)
                onResult(null) // Si hay un error en la consulta, retornamos null
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
                                    rightItems = (preguntaData["rightItems"] as? List<String>) ?: listOf(),
                                    userAnswers = (preguntaData["userAnswers"] as? List<Map<String, Any>>) ?: listOf()
                                )
                            }

                            // Creamos un mapa con los datos del cuestionario
                            val cuestionarioMap = mapOf(
                                "codigoQuiz" to codigoQuiz,
                                "titulo" to (cuestionarioData["titulo"] as? String ?: ""),
                                "descripcion" to (cuestionarioData["descripcion"] as? String ?: ""),
                                "creadorId" to (cuestionarioData["creadorId"] as? String ?: ""),
                                "imagen" to cuestionarioData["imagen"], // Imagen es opcional
                                "preguntas" to preguntas, // Lista de preguntas mapeadas
                                "immediateAccess" to (cuestionarioData["immediateAccess"] as? Boolean ?: false),
                                "locationRestricted" to (cuestionarioData["locationRestricted"] as? Boolean ?: false),
                                "immediateResults" to (cuestionarioData["immediateResults"] as? Boolean ?: false),
                                "isQuizIniciado" to (cuestionarioData["isQuizIniciado"] as? Boolean ?: false)

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
                            val cuestionarios = mutableListOf<Cuestionario>() // Lista mutable para almacenar los cuestionarios

                            // Recorremos los documentos (cuestionarios)
                            documents.forEach { document ->
                                val cuestionarioData = document.data
                                println("Cuestionario data: $cuestionarioData")

                                // Recuperamos las preguntas de la subcolección 'preguntas' si existe
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
                                                    tipo = TipoPregunta.valueOf(it["tipo"] as? String ?: "VERDADERO_FALSO"),
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
                                                    rightItems = it["rightItems"] as? List<String> ?: listOf(),
                                                    userAnswers = it["userAnswers"] as? List<Map<String, Any>> ?: listOf()
                                                )
                                            }
                                        }

                                        // Crear el cuestionario y agregarlo a la lista
                                        val cuestionario = Cuestionario(
                                            id = cuestionarioData?.get("codigoQuiz") as? String ?: "",
                                            titulo = cuestionarioData?.get("titulo") as? String ?: "Sin título",
                                            descripcion = cuestionarioData?.get("descripcion") as? String ?: "",
                                            creadorId = cuestionarioData?.get("creadorId") as? String ?: "",
                                            imagen = cuestionarioData?.get("imagen") as? String, // Deja que sea nulo si no está presente
                                            preguntas = preguntas, // Asignamos las preguntas recuperadas
                                            immediateAccess = cuestionarioData?.get("immediateAccess") as? Boolean ?: false,
                                            isQuizIniciado = cuestionarioData?.get("isQuizIniciado") as? Boolean ?: false,
                                            isUsed = cuestionarioData?.get("isUsed") as? Boolean ?: false,
                                            locationRestricted = cuestionarioData?.get("locationRestricted") as? Boolean ?: false,
                                            immediateResults = cuestionarioData?.get("immediateResults") as? Boolean ?: false,
                                            latitude = (cuestionarioData?.get("latitude") as? String)?.toDoubleOrNull() ?: 0.0,
                                            longitude = (cuestionarioData?.get("longitude") as? String)?.toDoubleOrNull() ?: 0.0,
                                            // Conversión de radio a Double (si es necesario)
                                            radio = (cuestionarioData?.get("radio") as? String)?.toDoubleOrNull() ?: 0.0 // Valor por defecto en caso de error
                                        )

                                        // Añadir el cuestionario a la lista
                                        cuestionarios.add(cuestionario)

                                        // Una vez se procesen todos los cuestionarios, llamar a onSuccess
                                        if (cuestionarios.size == documents.size()) {
                                            onSuccess(cuestionarios) // Devolver la lista completa
                                        }
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

    suspend fun obtenerEstadosIsUsed(creadorId: String): Map<String, Boolean> {
        val estados = mutableMapOf<String, Boolean>()
        try {
            val cuestionariosSnapshot = FirebaseFirestore.getInstance()
                .collection("cuestionarios")
                .whereEqualTo("creadorId", creadorId) // Filtrar por creadorId
                .get()
                .await()

            for (document in cuestionariosSnapshot.documents) {
                // Obtener el valor de codigoQuiz y el estado de isUsed
                val codigoQuiz = document.getString("id") ?: "" // Asegúrate de que "codigoQuiz" sea el campo correcto
                val isUsed = document.getBoolean("isUsed") ?: false

                // Usar codigoQuiz como la clave en el mapa
                if (codigoQuiz.isNotEmpty()) {
                    estados[codigoQuiz] = isUsed
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener los estados isUsed: ${e.message}")
        }
        return estados
    }



    fun actualizarIsUsed(quizId: String, isUsed: Boolean, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("cuestionarios")

        // Buscar el documento donde el campo "id" es igual al quizId
        collectionRef.whereEqualTo("id", quizId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0] // Obtén el primer documento que coincida
                    document.reference.update("isUsed", isUsed)
                        .addOnSuccessListener {
                            callback(true) // La actualización fue exitosa
                        }
                        .addOnFailureListener { exception ->
                            println("Error al actualizar isUsed: ${exception.message}")
                            callback(false) // La actualización falló
                        }
                } else {
                    println("No se encontró un documento con id igual a $quizId")
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                println("Error al buscar documento: ${exception.message}")
                callback(false)
            }
    }

    fun actualizarCoordenadasCuestionario(
        codigoQuiz: String,
        nuevaLatitud: Double?,
        nuevaLongitud: Double?,
        onComplete: (String?) -> Unit // Callback para manejar el resultado
    ) {
        if (nuevaLatitud == null || nuevaLongitud == null) {
            onComplete("La ubicación no es válida.")
            return
        }
        val db = FirebaseFirestore.getInstance()
        // Realizamos una consulta para encontrar el cuestionario cuyo campo "id" sea igual a "codigoQuiz"
        db.collection("cuestionarios")
            .whereEqualTo("id", codigoQuiz)  // Filtro por el campo "id"
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Si no se encontró ningún documento con ese id
                    onComplete("No se encontró el cuestionario con el id especificado.")
                    return@addOnSuccessListener
                }

                // Obtener el primer documento de la consulta
                val cuestionarioDoc = querySnapshot.documents.first()

                // Referencia al documento que vamos a actualizar
                val cuestionarioRef = cuestionarioDoc.reference

                // Datos a actualizar
                val updatedData = mapOf(
                    "latitude" to nuevaLatitud,
                    "longitude" to nuevaLongitud
                )

                // Llamar a `update` para actualizar las coordenadas
                cuestionarioRef.update(updatedData)
                    .addOnSuccessListener {
                        onComplete(null) // No hubo error, la actualización fue exitosa
                    }
                    .addOnFailureListener { e ->
                        onComplete("Error al actualizar las coordenadas: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                onComplete("Error al realizar la consulta: ${e.message}")
            }
    }


    // Función para obtener los datos del quiz desde Firestore (latitud, longitud y radio)
    suspend fun obtenerDatosDelQuiz(codigoQuiz: String): Triple<Double, Double, Float>? {
        try {
            // Hacemos la consulta para obtener el documento del quiz con el código indicado
            val quizQuerySnapshot = firestore.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz)
                .get()
                .await() // Esperamos el resultado de la consulta asincrónica

            // Verificamos si encontramos algún documento que coincida con el código del quiz
            if (!quizQuerySnapshot.isEmpty) {
                val quizDoc = quizQuerySnapshot.documents.first() // Obtenemos el primer (y único) documento

                // Obtenemos la latitud, longitud y radio del documento
                val latitude = quizDoc.getDouble("latitude") ?: return null
                val longitude = quizDoc.getDouble("longitude") ?: return null
                val radius = quizDoc.getDouble("radio")?.toFloat() ?: return null

                // Retornamos los datos en un Triple (latitud, longitud, radio)
                return Triple(latitude, longitude, radius)
            }

            return null // Si no se encuentra ningún documento con el código de quiz
        } catch (e: Exception) {
            // Manejo de errores si hay un problema al obtener los datos
            Log.e("FirebaseError", "Error al obtener datos del quiz: ${e.message}")
            return null
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
            println("Pregunta agregada en la lista $preguntas")
        }
        else {
            println("La pregunta ya está en la lista")
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
                    val userAnswers = preguntaData["userAnswers"] as? List<Map<String, Any>> ?: emptyList()

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
                        rightItems = rightItems,
                        userAnswers = userAnswers

                    )
                    // Añadir la pregunta a la lista
                    _preguntas.add(pregunta)
                }
                // Actualizar el contador de preguntas
                contadorPreguntas.value = _preguntas.size
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

    // Función para actualizar el rol del usuario en Firestore  (USADA EN CREATE QUIZ Y HOME)
    fun actualizarRolUsuario2(userUid: String, rol: Rol) {
        val db = FirebaseFirestore.getInstance()
        val usuarioRef = db.collection("users").document(userUid)

        // Actualizar el campo 'rol' en Firestore
        usuarioRef.update("rol", rol)
            .addOnSuccessListener {
                Log.d("CrearCuestionario", "Rol de usuario actualizado a '${rol.name}'.")
            }
            .addOnFailureListener { e ->
                Log.w("CrearCuestionario", "Error al actualizar el rol del usuario", e)
            }
    }

    // Función para resetear las preguntas en CrearQuizScreen
    fun resetearPreguntas() {
        _preguntas.clear()  // Limpiar la lista de preguntas
        contadorPreguntas.value = _preguntas.size  // Restablecer el contador de preguntas
    }


    fun getIsQuizIniciado(quizId: String) {
        val quizRef = firestore.collection("cuestionarios").whereEqualTo("id", quizId)

        quizRef.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                Log.e("QuizViewModel", "Error al escuchar cambios en isQuizIniciado: ${error.message}")
                return@addSnapshotListener
            }

            if (querySnapshot != null && !querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0] // Obtiene el primer documento que coincide
                val isQuizIniciado = document.getBoolean("isQuizIniciado") ?: false
                _isQuizIniciado.value = isQuizIniciado // Actualiza el flujo con el estado del quiz

                Log.d("QuizViewModel", "Estado de isQuizIniciado actualizado: $isQuizIniciado")
            } else {
                Log.e("QuizViewModel", "No se encontró un cuestionario con el ID proporcionado: $quizId")
            }
        }
    }


    fun actualizarIsQuizIniciado(codigoQuiz: String?, onComplete: (Boolean) -> Unit) {
        _isQuizIniciado.value = true
        Log.d("QuizViewModel", "Estado de _isQuizIniciado actualizado a: ${_isQuizIniciado.value}")

        if (codigoQuiz.isNullOrEmpty()) {
            Log.e("QuizViewModel", "Código de cuestionario no válido")
            onComplete(false) // Llamar con false si el código no es válido
            return
        }

        val cuestionariosRef = firestore.collection("cuestionarios").whereEqualTo("id", codigoQuiz)

        cuestionariosRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id // Obtiene el ID del primer documento que coincide
                    firestore.collection("cuestionarios").document(documentId)
                        .update("isQuizIniciado", true)
                        .addOnSuccessListener {
                            Log.d("QuizViewModel", "Campo isQuizIniciado actualizado a true para el cuestionario con ID: $codigoQuiz")
                            onComplete(true) // Llama al callback indicando éxito
                        }
                        .addOnFailureListener { e ->
                            Log.e("QuizViewModel", "Error al actualizar isQuizIniciado: ${e.message}")
                            onComplete(false) // Llama al callback indicando fallo
                        }
                } else {
                    Log.e("QuizViewModel", "No se encontró un cuestionario con el código proporcionado: $codigoQuiz")
                    onComplete(false) // Llama al callback indicando que no se encontró el documento
                }
            }
            .addOnFailureListener { e ->
                Log.e("QuizViewModel", "Error al obtener el documento del cuestionario: ${e.message}")
                onComplete(false) // Llama al callback indicando error
            }
    }

    fun actualizarUserAnswers(
        codigoQuiz: String,
        indicePregunta: Int,   // Recibimos el índice de la pregunta
        userId: String,
        respuesta: Any
    ) {
        val db = FirebaseFirestore.getInstance()
        // Paso 1: Obtener el cuestionario por el código del cuestionario (codigoQuiz)
        db.collection("cuestionarios")
            .whereEqualTo("id", codigoQuiz)
            .get()
            .addOnSuccessListener { snapshot ->
                // Paso 2: Buscar el cuestionario que contiene las preguntas
                for (document in snapshot) {
                    val preguntasData = document.get("preguntas") as? List<Map<String, Any>>

                    preguntasData?.let { preguntas ->
                        // Paso 3: Verificamos que el índice es válido
                        if (indicePregunta in preguntas.indices) {
                            // Paso 4: Obtener la pregunta en el índice proporcionado
                            val pregunta = preguntas[indicePregunta]

                            // Obtener el campo userAnswers y crear el mapa de la nueva respuesta
                            val userAnswerMap = mapOf(
                                "userId" to userId,
                                "respuesta" to respuesta
                            )

                            // Paso 5: Obtener la lista de respuestas actuales de la pregunta
                            val currentUserAnswers = (pregunta["userAnswers"] as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()

                            // Paso 6: Agregar la nueva respuesta a la lista de respuestas
                            currentUserAnswers.add(userAnswerMap)

                            // Paso 7: Actualizar el campo userAnswers en el mapa de la pregunta
                            val updatedPregunta = pregunta.toMutableMap().apply {
                                this["userAnswers"] = currentUserAnswers
                            }

                            // Paso 8: Actualizar el array de preguntas con la pregunta modificada
                            val updatedPreguntas = preguntas.toMutableList().apply {
                                this[indicePregunta] = updatedPregunta
                            }

                            // Paso 9: Guardar la actualización en Firestore
                            db.collection("cuestionarios")
                                .document(document.id)
                                .update("preguntas", updatedPreguntas)
                                .addOnSuccessListener {
                                    println("Respuesta del usuario actualizada correctamente")
                                }
                                .addOnFailureListener { e ->
                                    println("Error al actualizar la respuesta: $e")
                                }
                        } else {
                            println("Índice de pregunta inválido: $indicePregunta")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                println("Error al obtener el cuestionario: $e")
            }
    }



}



