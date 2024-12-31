
package com.example.quizec.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuestionsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    val quizViewModel = QuizViewModel()

    val _preguntas = mutableStateListOf<Pregunta>()
    val preguntas: List<Pregunta> get() = _preguntas

    private val _preguntasSeleccionadas = mutableStateOf<List<Pregunta>>(emptyList())
    val preguntasSeleccionadas get() = _preguntasSeleccionadas

    // Usamos una variable de tipo String? para almacenar solo un ID de cuestionario seleccionado
    private val _selectedCuestionario = mutableStateOf<String?>(null)
    val selectedCuestionario: String? get() = _selectedCuestionario.value

    private val _preguntasState = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntasState: StateFlow<List<Pregunta>> get() = _preguntasState

    private val _cuestionariosState = MutableStateFlow<List<Cuestionario>>(emptyList())
    val cuestionariosState: StateFlow<List<Cuestionario>> get() = _cuestionariosState

    fun obtenerCuestionarioPorId(cuestionarioId: String): Cuestionario? {
        return _cuestionariosState.value.find { it.id == cuestionarioId }
    }


    // Función para cargar todas las preguntas del usuario desde Firestore
    fun cargarPreguntasUsuario(userId: String) {
        if (_preguntasState.value.isNotEmpty()) return
        viewModelScope.launch {
            try {
                val preguntasList = mutableListOf<Pregunta>()
                val snapshot = db.collection("preguntas")
                    .whereEqualTo("user_id", userId)
                    .get()
                    .await()

                Log.d("QuestionsViewModel", "Número de preguntas obtenidas: ${snapshot.size()}")

                for (document in snapshot) {
                    val pregunta = document.toObject(Pregunta::class.java)
                    preguntasList.add(pregunta)
                }

                if (preguntasList.isNotEmpty()) {
                    _preguntasState.value = preguntasList
                } else {
                    Log.d("QuestionsViewModel", "No se encontraron preguntas para el usuario.")
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al cargar preguntas: ${e.message}")
            }
        }
    }


    // Función para cargar todos los cuestionarios de un usuario desde Firestore
    fun cargarCuestionariosUsuario(userId: String) {
        viewModelScope.launch {
            val cuestionariosList = cargarCuestionarios(userId)
            if (cuestionariosList.isNotEmpty()) {
                _cuestionariosState.value = cuestionariosList
            } else {
                Log.d("QuestionsViewModel", "No se encontraron cuestionarios para el usuario.")
            }
        }
    }


    suspend fun cargarPreguntasCuestionario(codigoQuiz: String): List<Pregunta> {
        println("Cargando preguntas del cuestionario con código: $codigoQuiz")
        val preguntasList = mutableListOf<Pregunta>()
        try {
            // Consulta para obtener el documento del cuestionario por código de cuestionario (codigoQuiz)
            val snapshot = db.collection("cuestionarios")
                .whereEqualTo("id", codigoQuiz) // Filtramos por el codigoQuiz del cuestionario
                .get()
                .await()

            Log.d("QuestionsViewModel", "Número de cuestionarios obtenidos: ${snapshot.size()}")

            // Iterar sobre los documentos obtenidos
            for (document in snapshot) {
                // Obtenemos el campo 'preguntas' que debe ser un array de mapas o documentos
                val preguntasArray = document.get("preguntas") as? List<Map<String, Any>>

                preguntasArray?.forEach { preguntaMap ->
                    // Aquí convertimos los datos del mapa en un objeto Pregunta
                    val pregunta = Pregunta(
                        id = preguntaMap["id"] as? String ?: UUID.randomUUID().toString(),
                        titulo = preguntaMap["titulo"] as? String ?: "",
                        tipo = TipoPregunta.valueOf(
                            preguntaMap["tipo"] as? String ?: "VERDADERO_FALSO"
                        ),
                        opciones = preguntaMap["opciones"] as? List<String> ?: listOf(),
                        imagen = preguntaMap["imagen"] as? String,
                        respuestasCorrectas = preguntaMap["respuestasCorrectas"] as? List<String>
                            ?: listOf(),
                        emparejamientos = preguntaMap["emparejamientos"] as? Map<String, String>
                            ?: mapOf(),
                        itemsOrdenados = preguntaMap["itemsOrdenados"] as? List<String> ?: listOf(),
                        user_id = preguntaMap["user_id"] as? String,
                        isSelected = preguntaMap["isSelected"] as? Boolean ?: false,
                        fraseCompletar = preguntaMap["fraseCompletar"] as? String ?: "",
                        opcionCorrecta = preguntaMap["opcionCorrecta"] as? String ?: "",
                        conceptosYDefiniciones = preguntaMap["conceptosYDefiniciones"] as? Map<String, String>
                            ?: mapOf(),
                        opcionesCorrectasCompletarPalabras = preguntaMap["opcionesCorrectasCompletarPalabras"] as? List<String>
                            ?: listOf(),
                        leftItems = preguntaMap["leftItems"] as? List<String> ?: listOf(),
                        rightItems = preguntaMap["rightItems"] as? List<String> ?: listOf(),
                        userAnswers = preguntaMap["userAnswers"] as? List<Map<String, Any>> ?: listOf()

                    )
                    preguntasList.add(pregunta)
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionsViewModel", "Error al cargar preguntas: ${e.message}")
        }
        return preguntasList
    }



    // Función para cargar los cuestionarios del usuario desde Firestore
    private suspend fun cargarCuestionarios(userId: String): List<Cuestionario> {
        val cuestionariosList = mutableListOf<Cuestionario>()
        try {
            val snapshot = db.collection("cuestionarios")
                .whereEqualTo("creadorId", userId)
                .get()
                .await()

            Log.d("QuestionsViewModel", "Número de cuestionarios obtenidos: ${snapshot.size()}")
            for (document in snapshot) {
                val cuestionario = document.toObject(Cuestionario::class.java)
                cuestionariosList.add(cuestionario)
                println("Cuestionario cargado: ${cuestionario.titulo}")
            }
        } catch (e: Exception) {
            Log.e("QuestionsViewModel", "Error al cargar cuestionarios: ${e.message}")
        }
        return cuestionariosList
    }

    // Función para agregar o eliminar una pregunta de la lista seleccionada
    fun togglePreguntaSeleccionada(pregunta: Pregunta) {
        _preguntasSeleccionadas.value = if (_preguntasSeleccionadas.value.contains(pregunta)) {
            _preguntasSeleccionadas.value - pregunta
        } else {
            _preguntasSeleccionadas.value + pregunta
        }
    }

    // Función para guardar las preguntas seleccionadas en quizViewModel, en las preg del cuestionario (no en Firestore)
    fun guardarPreguntasSeleccionadas(quizViewModel: QuizViewModel) {
        _preguntasSeleccionadas.value.forEach { pregunta ->
            quizViewModel.agregarPregunta(pregunta)
        }
        println("Preguntas seleccionadas guardadas en QuizViewModel: ${_preguntasSeleccionadas.value}")
    }


    // Función para manejar la selección/deselección de un cuestionario
    fun toggleCuestionarioSelection(cuestionarioId: String) {
        // Si el cuestionario seleccionado ya está marcado, lo desmarcamos (establecer null)
        _selectedCuestionario.value =
            if (_selectedCuestionario.value == cuestionarioId) null else cuestionarioId
    }


    // Función para eliminar una pregunta
    fun eliminarPregunta(preguntaToDelete: Pregunta, userId: String) {
        viewModelScope.launch {
            try {
                println("Pregunta a eliminar: $preguntaToDelete y userId: $userId")

                // Buscar el documento en la colección "preguntas" utilizando el campo "id" de la pregunta
                val querySnapshot = db.collection("preguntas")
                    .whereEqualTo(
                        "id",
                        preguntaToDelete.id
                    ) // Filtrar por el campo "id" dentro del documento
                    .get()
                    .await()

                // Comprobar si se encontró el documento
                if (querySnapshot.documents.isNotEmpty()) {
                    // Suponemos que solo hay un documento con ese "id"
                    val documentRef = querySnapshot.documents[0].reference

                    // Eliminar el documento
                    documentRef.delete().await()

                    // Actualizamos el estado local de las preguntas eliminando la pregunta de la lista
                    _preguntasState.value =
                        _preguntasState.value.filter { it.id != preguntaToDelete.id }

                    // Recargar las preguntas del usuario después de eliminar la pregunta
                    cargarPreguntasUsuario(userId) // Esto volverá a cargar las preguntas inmediatamente

                    Log.d(
                        "QuestionsViewModel",
                        "Pregunta eliminada correctamente: ${preguntaToDelete.id}"
                    )
                } else {
                    Log.e(
                        "QuestionsViewModel",
                        "No se encontró el documento con id: ${preguntaToDelete.id}"
                    )
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al eliminar la pregunta: ${e.message}")
            }
        }
    }

    // ERROR SE ELIMINA EL PREFIJO "IS" DE LOS CAMPOS DEL CUESTIONARIO DUPLICADO
    fun duplicarCuestionario(codigoQuiz: String) {
        // Obtener instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        // Buscar el cuestionario en la colección "cuestionarios"
        db.collection("cuestionarios")
            .whereEqualTo("id", codigoQuiz)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Obtener el primer documento que coincide con el id (suponemos que solo hay uno)
                    val documentSnapshot = querySnapshot.documents.first()

                    // Obtener el cuestionario original
                    val cuestionario = documentSnapshot.toObject(Cuestionario::class.java)

                    if (cuestionario != null) {
                        // Hacer una copia del cuestionario original
                        val cuestionarioDuplicado = cuestionario.copy()

                        // Crear una nueva instancia modificada solo con los valores necesarios
                        val cuestionarioActualizado = cuestionarioDuplicado.copy(
                            id = quizViewModel.generarClave(), // Generar un nuevo ID único
                            quizUsed = false, // Marcar como no usado
                            latitude = 0.0, // Poner la latitud a 0
                            longitude = 0.0, // Poner la longitud a 0
                            immediateAccess = false, // Acceso inmediato desactivado
                            immediateResults = false, // Resultados inmediatos desactivados
                            quizIniciado = false, // Marcar como no iniciado
                            radio = 0.0, // Poner radio a 0
                            preguntas = cuestionario.preguntas?.map { pregunta ->
                                // Iterar sobre las preguntas y vaciar el campo `userAnswers` de cada una
                                pregunta.copy(userAnswers = emptyList()) // Limpiar el campo `userAnswers`
                            } ?: emptyList() // Si no hay preguntas, se deja vacío
                        )

                        // Generar un nuevo documento con un ID único para el cuestionario duplicado
                        val cuestionarioRef = db.collection("cuestionarios").document()

                        // Subir el cuestionario duplicado a Firestore
                        cuestionarioRef.set(cuestionarioActualizado)
                            .addOnSuccessListener {
                                // Si la operación fue exitosa, mostramos un mensaje
                                Log.d("DuplicarCuestionario", "Cuestionario duplicado con éxito")
                                _cuestionariosState.value = _cuestionariosState.value + cuestionarioActualizado
                            }
                            .addOnFailureListener { e ->
                                // Si hubo un error, lo registramos
                                Log.e("DuplicarCuestionario", "Error al duplicar el cuestionario: $e")
                            }
                    } else {
                        // Si no se encontró el cuestionario, lo logueamos
                        Log.e("DuplicarCuestionario", "Cuestionario no encontrado en Firestore")
                    }
                } else {
                    // Si no se encontraron documentos que coincidan con el ID
                    Log.e("DuplicarCuestionario", "No se encontró el cuestionario con el ID: $codigoQuiz")
                }
            }
            .addOnFailureListener { e ->
                // Si hubo un error al obtener el documento, lo logueamos
                Log.e("DuplicarCuestionario", "Error al obtener el cuestionario: $e")
            }
    }





    // Función para duplicar una pregunta cambiando el userId al del usuario actual (HISTORIAL)
    fun duplicarPreguntaConUsuarioActual(preguntaToDuplicate: Pregunta, userIdActual: String) {
        viewModelScope.launch {
            try {
                // Crear una copia de la pregunta con un nuevo ID y el userId del usuario actual
                val nuevaPregunta = preguntaToDuplicate.copy(
                    id = UUID.randomUUID().toString(), // Generar un nuevo ID único
                    user_id = userIdActual // Cambiar el userId al del usuario actual
                )

                // Guardar la nueva pregunta duplicada en la base de datos
                db.collection("preguntas")
                    .add(nuevaPregunta)
                    .await()

                Log.d("QuestionsViewModel", "Pregunta duplicada correctamente: ${nuevaPregunta.id}")

                // Actualizar el estado local añadiendo la nueva pregunta
                _preguntasState.value = _preguntasState.value + nuevaPregunta

            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al duplicar la pregunta: ${e.message}")
            }
        }
    }

    // Función para duplicar una pregunta (SELECCIONAR PREGUNTA)
    fun duplicarPregunta(preguntaToDuplicate: Pregunta, userId: String) {
        viewModelScope.launch {
            try {
                // Crear una nueva pregunta con un nuevo ID
                val newPregunta = preguntaToDuplicate.copy(
                    id = UUID.randomUUID().toString() // Generamos un nuevo ID único
                )

                // Guardar la nueva pregunta duplicada en la base de datos
                db.collection("preguntas")
                    .add(newPregunta)
                    .await()

                Log.d("QuestionsViewModel", "Pregunta duplicada correctamente: ${newPregunta.id}")

                // Verificar si la pregunta ya está en el estado antes de agregarla
                if (!_preguntasState.value.contains(newPregunta)) {
                    // Actualizar la lista de preguntas localmente solo si no existe
                    _preguntasState.value = _preguntasState.value + newPregunta
                }

            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al duplicar la pregunta: ${e.message}")
            }
        }
    }

    // Función para obtener una pregunta desde Firestore por su ID
    suspend fun obtenerPregunta(preguntaId: String): Pregunta? {
        return try {
            // Realizamos la consulta a Firestore buscando la pregunta por su ID
            val snapshot = db.collection("preguntas")
                .whereEqualTo("id", preguntaId)
                .get()
                .await()

            // Si se encontró el documento, devolvemos la pregunta
            if (snapshot.documents.isNotEmpty()) {
                // Suponemos que hay solo un documento con ese ID
                snapshot.documents[0].toObject(Pregunta::class.java)
            } else {
                // Si no se encontró ninguna pregunta con ese ID, devolvemos null
                null
            }
        } catch (e: Exception) {
            // Si ocurre algún error, logueamos y devolvemos null
            Log.e("QuestionsViewModel", "Error al obtener la pregunta: ${e.message}")
            null
        }
    }


    fun eliminarCuestionario(id: String) {
        // Buscar el documento por el campo 'id' en la colección 'cuestionarios'
        db.collection("cuestionarios")
            .whereEqualTo("id", id)  // Filtramos por el campo 'id' del documento
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Verificar si la consulta devuelve documentos
                if (querySnapshot.isEmpty) {
                    Log.e("QuestionsViewModel", "No se encontró el cuestionario con id: $id")
                    return@addOnSuccessListener
                }

                // Si encontramos el cuestionario, eliminarlo
                val documentRef = querySnapshot.documents[0].reference
                documentRef.delete()
                    .addOnSuccessListener {
                        // Si la eliminación del cuestionario fue exitosa, actualizar el estado local
                        _cuestionariosState.value =
                            _cuestionariosState.value.filterNot { it.id == id }
                        Log.d("QuestionsViewModel", "Cuestionario eliminado correctamente: $id")

                    }
                    .addOnFailureListener { e ->
                        // Manejar el error si la eliminación falla
                        Log.e("QuestionsViewModel", "Error al eliminar el cuestionario: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                // Manejar el error si la consulta falla
                Log.e("QuestionsViewModel", "Error al buscar el cuestionario: ${e.message}")
            }
    }


    fun borrarCuestionarioHistorial(nombreUsuario: String, cuestionarioId: String) {
        // Obtén una instancia de Firestore
        val db = FirebaseFirestore.getInstance()

        // Accede al documento del usuario y a la colección de cuestionarios
        val usuarioRef = db.collection("usuarioHistorial")
            .document(nombreUsuario) // Nombre del usuario
            .collection("cuestionarios") // Colección de cuestionarios

        // Borrar el cuestionario especificado por su ID
        usuarioRef.document(cuestionarioId).delete()
            .addOnSuccessListener {
                // Operación exitosa, el cuestionario ha sido borrado
                println("Cuestionario con ID $cuestionarioId borrado correctamente")
            }
            .addOnFailureListener { exception ->
                // Ocurrió un error al borrar el cuestionario
                println("Error al borrar el cuestionario: ${exception.message}")
            }
    }

    fun actualizarPregunta(pregunta: Pregunta) {
        // Obtener una instancia de Firebase Firestore
        val firestore = FirebaseFirestore.getInstance()
        println("Pregunta a actualizar: $pregunta con id: ${pregunta.id}")

        // Referencia a la colección "preguntas"
        val preguntasCollection = firestore.collection("preguntas")

        // Verificar si el ID de la pregunta es válido
        if (pregunta.id.isEmpty()) {
            println("El ID de la pregunta no puede estar vacío.")
            return
        }

        // Paso 1: Buscar el documento cuyo campo `id` coincide con el valor de `pregunta.id`
        preguntasCollection
            .whereEqualTo("id", pregunta.id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Obtener el primer documento encontrado
                    val documentSnapshot = querySnapshot.documents[0]
                    val documentId = documentSnapshot.id // Obtener el verdadero `documentId`

                    println("Documento encontrado con documentId: $documentId")

                    // Paso 2: Actualizar los campos del documento encontrado
                    preguntasCollection.document(documentId)
                        .update(
                            mapOf(
                                "titulo" to pregunta.titulo,
                                "tipo" to pregunta.tipo.name,
                                "opciones" to pregunta.opciones,
                                "respuestasCorrectas" to pregunta.respuestasCorrectas,
                                "imagen" to pregunta.imagen,
                                "user_id" to pregunta.user_id,
                                "isSelected" to pregunta.isSelected,
                                "fraseCompletar" to pregunta.fraseCompletar,
                                "opcionCorrecta" to pregunta.opcionCorrecta,
                                "conceptosYDefiniciones" to pregunta.conceptosYDefiniciones,
                                "opcionesCorrectasCompletarPalabras" to pregunta.opcionesCorrectasCompletarPalabras,
                                "leftItems" to pregunta.leftItems,
                                "rightItems" to pregunta.rightItems
                            )
                        )
                        .addOnSuccessListener {
                            println("Pregunta actualizada correctamente con documentId: $documentId")
                        }
                        .addOnFailureListener { exception ->
                            println("Error al actualizar la pregunta: ${exception.message}")
                        }
                } else {
                    println("No se encontró un documento con el campo 'id' igual a: ${pregunta.id}")
                }
            }
            .addOnFailureListener { exception ->
                println("Error al buscar el documento: ${exception.message}")
            }
    }

    fun guardarPreguntasCuestionario(codigoQuiz: String, preguntasNuevas: List<Pregunta>) {
        // Creamos una referencia a la colección de cuestionarios en Firestore
        val db = FirebaseFirestore.getInstance()
        val cuestionariosRef = db.collection("cuestionarios") // La colección donde se encuentran los cuestionarios

        // Buscamos el documento cuyo campo "id" coincida con el "codigoQuiz"
        cuestionariosRef
            .whereEqualTo("id", codigoQuiz) // Comparar el campo "id" con el "codigoQuiz"
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Suponemos que hay un solo documento con este "id"
                    val cuestionarioDoc = querySnapshot.documents.first() // Obtenemos el primer (y único) documento

                    // Obtenemos la lista actual de preguntas en el documento
                    val preguntasActuales =
                        cuestionarioDoc.get("preguntas") as? List<Map<String, Any>> ?: emptyList()

                    // Convertimos las preguntas nuevas de List<Pregunta> a una lista de mapas (List<Map<String, Any>>)
                    val preguntasMapeadas = preguntasNuevas.map { pregunta ->
                        mapOf(
                            "id" to pregunta.id,
                            "titulo" to pregunta.titulo,
                            "tipo" to pregunta.tipo.name, // Convertir el enum TipoPregunta a string
                            "opciones" to pregunta.opciones,
                            "imagen" to (pregunta.imagen ?: ""),
                            "respuestasCorrectas" to pregunta.respuestasCorrectas,
                            "emparejamientos" to pregunta.emparejamientos,
                            "itemsOrdenados" to pregunta.itemsOrdenados,
                            "user_id" to (pregunta.user_id ?: ""),
                            "isSelected" to pregunta.isSelected,
                            "fraseCompletar" to pregunta.fraseCompletar,
                            "opcionCorrecta" to pregunta.opcionCorrecta,
                            "conceptosYDefiniciones" to pregunta.conceptosYDefiniciones,
                            "opcionesCorrectasCompletarPalabras" to pregunta.opcionesCorrectasCompletarPalabras,
                            "leftItems" to pregunta.leftItems,
                            "rightItems" to pregunta.rightItems
                        )
                    }

                    // Creamos la nueva lista combinando las preguntas actuales con las nuevas
                    val listaFinalPreguntas = preguntasActuales + preguntasMapeadas

                    // Actualizamos el campo "preguntas" en el documento del cuestionario
                    cuestionarioDoc.reference.update("preguntas", listaFinalPreguntas)
                        .addOnSuccessListener {
                            // Si la actualización fue exitosa, mostramos un mensaje
                            println("Las preguntas del cuestionario han sido actualizadas correctamente.")
                        }
                        .addOnFailureListener { exception ->
                            // Si algo sale mal, mostramos el error
                            println("Error al actualizar las preguntas: ${exception.message}")
                        }
                } else {
                    // Si no se encuentra un documento con ese "id"
                    println("No se encontró el cuestionario con id $codigoQuiz.")
                }
            }
            .addOnFailureListener { exception ->
                // Si hubo un error al buscar el documento, lo manejamos aquí
                println("Error al buscar el cuestionario: ${exception.message}")
            }
    }

    fun eliminarPreguntasCuestionario(codigoQuiz: String, preguntas: List<Pregunta>) {
        // Extraemos los IDs de las preguntas seleccionadas
        val idsPreguntas = preguntas.map { it.id }

        val db = FirebaseFirestore.getInstance()

        // Acceder a la colección 'cuestionarios'
        db.collection("cuestionarios")
            .whereEqualTo("id", codigoQuiz)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (document in snapshot.documents) {
                        val cuestionarioDocRef = db.collection("cuestionarios")
                            .document(document.id)
                        val cuestionarioData = document.data

                        if (cuestionarioData != null && cuestionarioData.containsKey("preguntas")) {
                            val preguntasActuales = cuestionarioData["preguntas"] as? List<Map<String, Any>>
                            val preguntasFiltradas = preguntasActuales?.filterNot { pregunta ->
                                pregunta["id"] in idsPreguntas
                            }

                            // Actualizar el campo 'preguntas' del documento
                            if (preguntasFiltradas != null) {
                                cuestionarioDocRef.update("preguntas", preguntasFiltradas)
                                    .addOnSuccessListener {
                                        Log.d("EliminarPreguntas", "Preguntas eliminadas correctamente del cuestionario con código '$codigoQuiz'.")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("EliminarPreguntas", "Error al actualizar el cuestionario '$codigoQuiz': ${e.message}")
                                    }
                            }
                        } else {
                            Log.e("EliminarPreguntas", "El cuestionario no contiene un campo 'preguntas' o es nulo.")
                        }
                    }
                } else {
                    Log.e("EliminarPreguntas", "No se encontraron documentos con el id '$codigoQuiz'.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("EliminarPreguntas", "Error al obtener el cuestionario: ${e.message}")
            }
    }



    private val _preguntasParaEliminar = mutableStateListOf<Pregunta>()

    fun marcarPreguntasParaEliminar(codigoQuiz: String, preguntas: List<Pregunta>) {
        _preguntasParaEliminar.clear()
        _preguntasParaEliminar.addAll(preguntas)
    }

    fun cancelarEliminacion() {
        _preguntasParaEliminar.clear()
    }

}
