package com.example.quizec.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.quizec.R
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.utils.AMovServer

@Composable
fun SelectQuestionsEditScreen(
    navController: NavHostController,
    userId: String,
    codigoQuiz: String,
    quizViewModel: QuizViewModel
) {
    val questionsViewModel: QuestionsViewModel = viewModel()
    //NEW
    var preguntasInicialesQuiz by remember { mutableStateOf(emptyList<Pregunta>()) }
    // Cargar preguntas (esto ya está configurado)
    LaunchedEffect(userId) {
        questionsViewModel.cargarPreguntasUsuario(userId)
        //NEW
        preguntasInicialesQuiz = questionsViewModel.cargarPreguntasCuestionario(codigoQuiz)
    }

    // Aquí debes asegurarte de que se actualicen automáticamente cuando las preguntas cambien.
    // Esto hará que se recarguen las preguntas cuando el estado de preguntas cambie.
    val preguntasState = questionsViewModel.preguntasState.collectAsState() // Obtiene las preguntas desde el ViewModel
    val preguntasSeleccionadas = questionsViewModel.preguntasSeleccionadas.value // Almacena las preguntas seleccionadas

    // Estado para manejar los diálogos de confirmación
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showDuplicateConfirmationDialog by remember { mutableStateOf(false) }
    var questionToDelete by remember { mutableStateOf<Pregunta?>(null) }
    var questionToDuplicate by remember { mutableStateOf<Pregunta?>(null) }


    Box(modifier = Modifier.fillMaxSize()) {
        if (preguntasState.value.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp)) // Indicador de carga
        } else {
            Text(
                text = stringResource(R.string.seleccionar_preguntas_titulo),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            Text(stringResource(R.string.preguntas_iniciales, preguntasInicialesQuiz.size))

            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .padding(top = 60.dp) // Menos espacio arriba
                    .padding(bottom = 110.dp) // Espacio reducido abajo para acercar más al botón
            ) {
                items(preguntasState.value) { pregunta -> // Muestra las preguntas
                    PreguntaItemEdit(
                        pregunta = pregunta,
                        isSelected = preguntasSeleccionadas.contains(pregunta),
                        //está bug, porque aunq preguntasInicialesQuiz tenga la pregunta, no deshabilita el btn.
                        //de todas formas, nunca se agregan las preguntas repetidas
                        isInQuiz = quizViewModel.preguntas.contains(pregunta) || preguntasInicialesQuiz.contains(pregunta),
                        onSelectionChange = { questionsViewModel.togglePreguntaSeleccionada(pregunta) },
                        onEdit = { preguntaToEdit ->
                            // Navegar a una pantalla de edición, o mostrar un cuadro de diálogo
                            navController.navigate("editPregunta/${preguntaToEdit.id}")
                        },
                        onDelete = { preguntaToDelete -> // Callback para eliminar
                            // Mostrar el diálogo de confirmación antes de eliminar
                            showDeleteConfirmationDialog = true
                            questionToDelete = preguntaToDelete
                        },
                        onDuplicate = { preguntaToDuplicate -> // Callback para duplicar
                            // Mostrar el diálogo de confirmación antes de duplicar
                            showDuplicateConfirmationDialog = true
                            questionToDuplicate = preguntaToDuplicate
                        }
                    )
                }
            }
        }

        // Diálogo de confirmación para eliminar
        if (showDeleteConfirmationDialog && questionToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text(stringResource(R.string.confirmar_eliminaci_n)) },
                text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_eliminar_esta_pregunta)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Asegúrate de que questionToDelete no es nulo antes de llamar a eliminarPregunta
                            questionToDelete?.let { pregunta ->
                                // Verifica si la pregunta tiene una imagen asociada
                                if (!pregunta.imagen.isNullOrEmpty()) {
                                    // Extrae el nombre del archivo de la URL
                                    val imageUrl = pregunta.imagen.toString()
                                    val trimmedUrl = imageUrl.trimEnd('/')
                                    val segments = trimmedUrl.split("/")
                                    val fileName = segments.lastOrNull() ?: ""

                                    // Llama a la función para eliminar el archivo en el servidor
                                    AMovServer.asyncDeleteFileFromServer(
                                        fileName = fileName,
                                        serverUrl = trimmedUrl,
                                        onResult = { result ->
                                            if (result) {
                                                // Elimina también la pregunta después de borrar la imagen
                                                questionsViewModel.eliminarPregunta(pregunta, userId)
                                            } else {
                                                // Manejo del error (por ejemplo, mostrando un mensaje al usuario)
                                                Log.e("DeleteError", "Error al eliminar la imagen: $imageUrl")
                                            }
                                        }
                                    )
                                } else {
                                    // Si no hay imagen, simplemente elimina la pregunta
                                    questionsViewModel.eliminarPregunta(pregunta, userId)
                                }
                            }

                            // Cerrar el diálogo y restablecer el estado
                            showDeleteConfirmationDialog = false
                            questionToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.si))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Cerrar el diálogo sin hacer nada
                            showDeleteConfirmationDialog = false
                            questionToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }

        // Diálogo de confirmación para duplicar
        if (showDuplicateConfirmationDialog && questionToDuplicate != null) {
            AlertDialog(
                onDismissRequest = { showDuplicateConfirmationDialog = false },
                title = { Text(stringResource(R.string.confirmar_duplicado)) },
                text = { Text(stringResource(R.string.est_s_seguro_de_que_quieres_duplicar_esta_pregunta)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Asegúrate de que questionToDuplicate no es nulo antes de llamar a duplicarPregunta
                            questionToDuplicate?.let {
                                // Llamada desde el composable (cuando se confirma la duplicación)
                                questionsViewModel.duplicarPregunta(questionToDuplicate!!, userId)
                            }

                            // Cerrar el diálogo y restablecer el estado
                            showDuplicateConfirmationDialog = false
                            questionToDuplicate = null
                        }
                    ) {
                        Text(stringResource(R.string.si))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Cerrar el diálogo sin hacer nada
                            showDuplicateConfirmationDialog = false
                            questionToDuplicate = null
                        }
                    ) {
                        Text(stringResource(R.string.no))
                    }
                }
            )
        }

        // Botones siempre visibles en la parte inferior
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp), // Espacio alrededor de los botones
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio horizontal entre los botones
        ) {
            Button(
                onClick = {
                    println("Guardando preguntas seleccionadas: ${questionsViewModel.preguntasSeleccionadas.value}")
                    // Guardar las preguntas seleccionadas en la lista de preguntas
                    questionsViewModel.guardarPreguntasSeleccionadas(quizViewModel)
                    navController.navigate("editCuestionario/$codigoQuiz")
                },
                modifier = Modifier.weight(1f) // Ambos botones tendrán el mismo ancho
            ) {
                Text(text = stringResource(R.string.guardar)) // Emoji para guardar
            }

            Button(
                onClick = {
                    val codigoQuiz = codigoQuiz
                    navController.navigate("editCuestionario/$codigoQuiz")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.volver)) // Emoji para volver
            }
        }
    }
}

@Composable
fun PreguntaItemEdit(
    pregunta: Pregunta,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    isInQuiz: Boolean, // Nueva propiedad para indicar si ya está en el quiz
    onEdit: (Pregunta) -> Unit, // Callback para editar
    onDelete: (Pregunta) -> Unit, // Callback para eliminar
    onDuplicate: (Pregunta) -> Unit // Callback para duplicar
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp) // Espaciado entre elementos
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Primera fila: Checkbox y título de la pregunta
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { if (!isInQuiz) onSelectionChange(it) }, // Evitar cambio si ya está en el quiz
                    enabled = !isInQuiz // Deshabilitar si ya está en el quiz
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Usar stringResource para traducir el tipo de pregunta
                val tipoPregunta = when (pregunta.tipo) {
                    TipoPregunta.VERDADERO_FALSO -> stringResource(R.string.true_false)
                    TipoPregunta.OPCION_MULTIPLE_UNA -> stringResource(R.string.multiple_choice_single)
                    TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> stringResource(R.string.multiple_choice_multiple)
                    TipoPregunta.EMPAREJAR -> stringResource(R.string.matching)
                    TipoPregunta.ORDENAR -> stringResource(R.string.ordering)
                    TipoPregunta.COMPLETAR_ESPACIOS -> stringResource(R.string.fill_in_blanks)
                    TipoPregunta.ASOCIACION -> stringResource(R.string.association)
                    TipoPregunta.COMPLETAR_PALABRAS -> stringResource(R.string.fill_in_words)
                }

                Text(
                    text = "${pregunta.titulo} - $tipoPregunta",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        // Segunda fila: Botones de acción
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onEdit(pregunta) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "✏️") // Emoji para editar
            }

            Button(
                onClick = { onDelete(pregunta) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "🗑️") // Emoji para eliminar
            }

            Button(
                onClick = { onDuplicate(pregunta) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "🔁") // Emoji para duplicar
            }
        }
    }
}

