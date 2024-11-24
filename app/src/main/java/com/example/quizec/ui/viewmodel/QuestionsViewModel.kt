package com.example.quizec.ui.viewmodel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizec.data.model.Cuestionario
import com.example.quizec.data.model.Pregunta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuestionsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _preguntasSeleccionadas = mutableStateOf<List<Pregunta>>(emptyList())
    val preguntasSeleccionadas get() = _preguntasSeleccionadas

    // Usamos una variable de tipo String? para almacenar solo un ID de cuestionario seleccionado
    private val _selectedCuestionario = mutableStateOf<String?>(null)
    val selectedCuestionario: String? get() = _selectedCuestionario.value

    private val _preguntasState = MutableStateFlow<List<Pregunta>>(emptyList())
    val preguntasState: StateFlow<List<Pregunta>> get() = _preguntasState

    private val _cuestionariosState = MutableStateFlow<List<Cuestionario>>(emptyList())
    val cuestionariosState: StateFlow<List<Cuestionario>> get() = _cuestionariosState


    // Función para cargar todas las preguntas del usuario desde Firestore
    fun cargarPreguntasUsuario(userId: String) {
        if (_preguntasState.value.isNotEmpty()) return
        viewModelScope.launch {
            val preguntasList = cargarPreguntas(userId)
            if (preguntasList.isNotEmpty()) {
                _preguntasState.value = preguntasList
            } else {
                Log.d("QuestionsViewModel", "No se encontraron preguntas para el usuario.")
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

    // Función para cargar todas las preguntas del usuario desde Firestore
    private suspend fun cargarPreguntas(userId: String): List<Pregunta> {
        val preguntasList = mutableListOf<Pregunta>()
        try {
            val snapshot = db.collection("preguntas")
                .whereEqualTo("user_id", userId)
                .get()
                .await()

            Log.d("QuestionsViewModel", "Número de preguntas obtenidas: ${snapshot.size()}")

            for (document in snapshot) {
                val pregunta = document.toObject(Pregunta::class.java)
                preguntasList.add(pregunta)
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

    // Función para guardar las preguntas seleccionadas en una lista (no en Firestore)
    fun guardarPreguntasSeleccionadas(quizViewModel: QuizViewModel) {
        _preguntasSeleccionadas.value.forEach { pregunta ->
            quizViewModel.agregarPregunta(pregunta)
        }
        println("Preguntas seleccionadas guardadas en QuizViewModel: ${_preguntasSeleccionadas.value}")
    }

    // Función para manejar la selección/deselección de un cuestionario
    fun toggleCuestionarioSelection(cuestionarioId: String) {
        // Si el cuestionario seleccionado ya está marcado, lo desmarcamos (establecer null)
        _selectedCuestionario.value = if (_selectedCuestionario.value == cuestionarioId) null else cuestionarioId
    }


    // Función para eliminar una pregunta
    fun eliminarPregunta(preguntaToDelete: Pregunta, userId: String) {
        viewModelScope.launch {
            try {
                println("Pregunta a eliminar: $preguntaToDelete y userId: $userId")

                // Buscar el documento en la colección "preguntas" utilizando el campo "id" de la pregunta
                val querySnapshot = db.collection("preguntas")
                    .whereEqualTo("id", preguntaToDelete.id) // Filtrar por el campo "id" dentro del documento
                    .get()
                    .await()

                // Comprobar si se encontró el documento
                if (querySnapshot.documents.isNotEmpty()) {
                    // Suponemos que solo hay un documento con ese "id"
                    val documentRef = querySnapshot.documents[0].reference

                    // Eliminar el documento
                    documentRef.delete().await()

                    // Actualizamos el estado local de las preguntas eliminando la pregunta de la lista
                    _preguntasState.value = _preguntasState.value.filter { it.id != preguntaToDelete.id }

                    // Recargar las preguntas del usuario después de eliminar la pregunta
                    cargarPreguntasUsuario(userId) // Esto volverá a cargar las preguntas inmediatamente

                    Log.d("QuestionsViewModel", "Pregunta eliminada correctamente: ${preguntaToDelete.id}")
                } else {
                    Log.e("QuestionsViewModel", "No se encontró el documento con id: ${preguntaToDelete.id}")
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al eliminar la pregunta: ${e.message}")
            }
        }
    }


    // Función para duplicar una pregunta
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

                // No es necesario recargar las preguntas si ya las tienes actualizadas
                // cargarPreguntasUsuario(userId) // Solo llama a esta función si realmente es necesario
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

    fun modificarPregunta(preguntaModificada: Pregunta, userId: String) {
        println("Pregunta a modificar cuyo titulo eees: $preguntaModificada.titulo")

        // Asegurarse de que las preguntas estén cargadas antes de intentar modificar
        if (_preguntasState.value.isEmpty()) {
            cargarPreguntasUsuario(userId) // Cargar preguntas si aún no están cargadas
        }

        // Continuar con la modificación de la pregunta
        viewModelScope.launch {
            try {
                // Buscar el documento de la pregunta en Firestore
                val querySnapshot = db.collection("preguntas")
                    .whereEqualTo("id", preguntaModificada.id) // Filtrar por el campo "id"
                    .get()
                    .await()

                println("QuerySnapshot size: ${querySnapshot.size()}")
                println("QuerySnapshot documents: ${querySnapshot.documents}")
                println("QuerySnapshot isEmpty: ${querySnapshot.isEmpty}")

                // Comprobar si se encontró el documento
                if (querySnapshot.documents.isNotEmpty()) {
                    // Suponemos que solo hay un documento con ese "id"
                    val documentRef = querySnapshot.documents[0].reference

                    // Actualizar los campos necesarios de la pregunta en Firestore
                    documentRef.set(preguntaModificada)
                        .await()

                    // Actualizar la lista de preguntas localmente con la pregunta modificada
                    _preguntasState.value = _preguntasState.value.map {
                        if (it.id == preguntaModificada.id) preguntaModificada else it
                    }

                    // Verificar que la lista de preguntas se haya actualizado correctamente
                    println("Lista de preguntas actualizada: ${_preguntasState.value}")

                    Log.d("QuestionsViewModel", "Pregunta modificada correctamente: ${preguntaModificada.id}")
                } else {
                    Log.e("QuestionsViewModel", "No se encontró el documento con id: ${preguntaModificada.id}")
                }
            } catch (e: Exception) {
                Log.e("QuestionsViewModel", "Error al modificar la pregunta: ${e.message}")
            }
        }
    }
}