package com.example.quizec.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


@Composable
fun EditarPreguntaScreen(preguntaMod: Pregunta, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val questionsViewModel = QuestionsViewModel()
    val context = LocalContext.current
    val user_id = FirebaseAuth.getInstance().currentUser?.uid   // Obtener el ID del usuario autenticado

    println("id pregunta: ${preguntaMod.id}")

    var errorMessage by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf(preguntaMod.titulo) }
    var tipoPregunta by remember { mutableStateOf(preguntaMod.tipo) }

    // Nueva variable para almacenar las respuestas correctas
    var respuestasCorrectasMultipleMultiples by remember { mutableStateOf(listOf<String>()) }

    var opciones by remember { mutableStateOf(preguntaMod.opciones) } //COMPLETAR ESPACIOS
    var respuestasCorrectas by remember { mutableStateOf(preguntaMod.respuestasCorrectas) }
    var respuestaCorrectaVF by remember { mutableStateOf(true) }
    var respuestaCorrectaOpcionMultiple by remember { mutableStateOf(-1) } // Índice de la opción correcta seleccionada

    //EMPAREJAR
    var leftItems by remember { mutableStateOf(preguntaMod.leftItems) }
    var rightItems by remember { mutableStateOf(preguntaMod.rightItems) }
    var itemPairs by remember { mutableStateOf(listOf<Map<String, String>>())}  // Lista de pares
    //ORDENAR
    var itemsOrdenados by remember { mutableStateOf(preguntaMod.itemsOrdenados) } // Lista de ítems que deben ser ordenados
    //COMPLETAR ESPACIOS
    var fraseCompletar by remember { mutableStateOf(preguntaMod.fraseCompletar) }
    var opcionCorrecta by remember { mutableStateOf(preguntaMod.opcionCorrecta) } //la palabra que será el espacio en blanco

    //ASOCIACION
    // Listas para que el profesor ingrese conceptos y definiciones
    var conceptos by remember { mutableStateOf(listOf("")) }
    var definiciones by remember { mutableStateOf(listOf("")) }
    //tb usa itemPairs
    var conceptosYDefiniciones by remember { mutableStateOf(listOf<Map<String, String>>())}

    var opcionesCorrectasCompletarPalabras by remember { mutableStateOf(preguntaMod.opcionesCorrectasCompletarPalabras) }


    //ARREGLAR LO DE LAS IMAGENES (hacer q se pueda poner una imagen)
    var imageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Modificar Pregunta", style = MaterialTheme.typography.titleLarge)
        }

        item {
            Text("Título de la Pregunta", style = MaterialTheme.typography.bodyMedium)
            BasicTextField(
                value = titulo,
                onValueChange = { titulo = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, Color.Gray)
                    .padding(12.dp)
            )
        }

        item {
            Text("Tipo de Pregunta", style = MaterialTheme.typography.bodyMedium)
            DropdownMenuQuestionType2(tipoPregunta) { selectedTipo ->
                tipoPregunta = selectedTipo
                opciones = when (selectedTipo) {
                    TipoPregunta.OPCION_MULTIPLE_UNA, TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> listOf("", "")
                    else -> listOf()
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            when (tipoPregunta) {
                TipoPregunta.VERDADERO_FALSO -> {
                    Text(
                        "Seleccione la respuesta correcta:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = respuestaCorrectaVF,
                            onClick = { respuestaCorrectaVF = true }
                        )
                        Text("Verdadero")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = !respuestaCorrectaVF,
                            onClick = { respuestaCorrectaVF = false }
                        )
                        Text("Falso")
                    }
                }

                TipoPregunta.OPCION_MULTIPLE_UNA -> {
                    Text("Opciones de Respuesta", style = MaterialTheme.typography.bodyMedium)

                    // Iterar sobre las opciones con índice para mostrar los campos de texto y el botón de eliminar
                    opciones.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newOption ->
                                    // Actualizar la opción al cambiar el texto
                                    opciones = opciones.toMutableList().apply { this[index] = newOption }
                                },
                                label = { Text("Opción ${index + 1}") },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                            )

                            // Botón para eliminar la opción
                            IconButton(onClick = {
                                // Eliminar la opción de la lista
                                opciones = opciones.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Opción")
                            }
                        }
                    }

                    // Botón para agregar una nueva opción
                    Button(onClick = { opciones = opciones + "" }) {
                        Text("Agregar Opción")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Seleccione la respuesta correcta:", style = MaterialTheme.typography.bodyMedium)

                    // Mostrar las opciones y permitir seleccionar la respuesta correcta
                    opciones.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = respuestaCorrectaOpcionMultiple == index,
                                onClick = { respuestaCorrectaOpcionMultiple = index }
                            )
                            Text("Opción ${index + 1}: $option")
                        }
                    }
                }

                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Opciones de Respuesta", style = MaterialTheme.typography.bodyMedium)

                        // Mostrar las opciones de respuesta
                        opciones.forEachIndexed { index, option ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { newOption ->
                                        opciones = opciones.toMutableList().apply { this[index] = newOption }
                                    },
                                    label = { Text("Opción ${index + 1}") },
                                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                )

                                // Botón de eliminar opción
                                IconButton(onClick = {
                                    // Eliminar la opción de la lista y sincronizar las respuestas correctas
                                    opciones = opciones.toMutableList().apply { removeAt(index) }
                                    respuestasCorrectasMultipleMultiples = respuestasCorrectasMultipleMultiples.filter { it in opciones }
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Opción")
                                }
                            }
                        }

                        // Botón para agregar más opciones
                        Button(onClick = { opciones = opciones + "" }) {
                            Text("Agregar Opción")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Seleccione las respuestas correctas:", style = MaterialTheme.typography.bodyMedium)

                        // Lista de opciones con múltiples respuestas correctas
                        opciones.forEachIndexed { index, option ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Usar un Checkbox para permitir seleccionar múltiples respuestas correctas
                                Checkbox(
                                    checked = respuestasCorrectasMultipleMultiples.contains(option), // Verifica si la opción está seleccionada como correcta
                                    onCheckedChange = { isChecked ->
                                        // Modificar la lista de respuestas correctas directamente con opciones
                                        respuestasCorrectasMultipleMultiples = if (isChecked) {
                                            respuestasCorrectasMultipleMultiples + option // Añadir la opción si está marcada
                                        } else {
                                            respuestasCorrectasMultipleMultiples - option // Eliminar la opción si se desmarca
                                        }
                                    }
                                )
                                Text("Opción ${index + 1}: $option") // Mostrar la opción como texto
                            }
                        }
                    }
                }



                TipoPregunta.EMPAREJAR -> {
                    // Columna Izquierda
                    Text(
                        "Ingresa los ítems de la columna izquierda:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    leftItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    leftItems =
                                        leftItems.toMutableList().apply { this[index] = newValue }
                                },
                                label = { Text("Ítem ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            // Botón de eliminar
                            IconButton(
                                onClick = {
                                    leftItems = leftItems.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar ítem"
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { leftItems = leftItems + "" },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
                    ) {
                        Text("Agregar Ítem a la Columna Izquierda")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Columna Derecha
                    Text(
                        "Ingresa los ítems de la columna derecha:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    rightItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    rightItems =
                                        rightItems.toMutableList().apply { this[index] = newValue }
                                },
                                label = { Text("Ítem ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            // Botón de eliminar
                            IconButton(
                                onClick = {
                                    rightItems =
                                        rightItems.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar ítem"
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { rightItems = rightItems + "" },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
                    ) {
                        Text("Agregar Ítem a la Columna Derecha")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                TipoPregunta.ORDENAR -> {
                    Text(
                        "Ingresa los ítems en el orden correcto, siendo el 1er elemento el mayor o el primero:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    itemsOrdenados.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    itemsOrdenados = itemsOrdenados.toMutableList()
                                        .apply { this[index] = newValue }
                                },
                                label = { Text("Ítem ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            // Botón de eliminar
                            IconButton(
                                onClick = {
                                    itemsOrdenados =
                                        itemsOrdenados.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar ítem"
                                )
                            }
                        }
                    }
                    // Botón para agregar más ítems
                    Button(
                        onClick = { itemsOrdenados = itemsOrdenados + "" },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
                    ) {
                        Text("Agregar ítem")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                TipoPregunta.COMPLETAR_ESPACIOS -> {
                    // Paso 1: Campo para que el usuario introduzca la frase completa
                    Text("Escribe la frase completa:", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = fraseCompletar,
                        onValueChange = { nuevaFrase ->
                            fraseCompletar = nuevaFrase
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paso 2: Seleccionar la palabra en la frase que será el espacio en blanco
                    Text(
                        "Escriba la palabra que será el espacio en blanco:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = opcionCorrecta,
                        onValueChange = { nuevaPalabra ->
                            opcionCorrecta = nuevaPalabra
                        },
                        label = { Text("Palabra en blanco") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paso 3: Agregar opciones de palabras para completar el espacio en blanco seleccionado
                    Text(
                        "Opciones para completar el espacio en blanco:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Si ya hay opciones, mostrarlas en campos de texto
                    opciones.forEachIndexed { opcionIndex, opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = opcion,
                                onValueChange = { nuevaOpcion ->
                                    opciones = opciones.toMutableList()
                                        .apply { this[opcionIndex] = nuevaOpcion }
                                },
                                label = { Text("Opción ${opcionIndex + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                            // Botón de eliminar
                            IconButton(
                                onClick = {
                                    opciones =
                                        opciones.toMutableList().apply { removeAt(opcionIndex) }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar opción"
                                )
                            }
                        }
                    }

                    // Botón para agregar más opciones
                    Button(
                        onClick = { opciones = opciones + "" },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
                    ) {
                        Text("Agregar opción")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }


                //!! FALTA QUE SE PUEDE ELEGIR COMO CONCEPTO UNA IMAGEN
                TipoPregunta.ASOCIACION -> {
                    Text("Conceptos y definiciones:", style = MaterialTheme.typography.bodyMedium)

                    conceptosYDefiniciones.forEachIndexed { index, mapConceptoDefinicion ->
                        val concepto = mapConceptoDefinicion["concepto"] ?: ""
                        val definicion = mapConceptoDefinicion["definicion"] ?: ""

                        // Cada par concepto/definición estará en una columna
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            // Campo de texto para el concepto
                            OutlinedTextField(
                                value = concepto,
                                onValueChange = { nuevaConcepto ->
                                    conceptosYDefiniciones =
                                        conceptosYDefiniciones.toMutableList().apply {
                                            this[index] = mapOf(
                                                "concepto" to nuevaConcepto,
                                                "definicion" to definicion
                                            )
                                        }
                                },
                                label = { Text("Concepto ${index + 1}") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Campo de texto para la definición
                            OutlinedTextField(
                                value = definicion,
                                onValueChange = { nuevaDefinicion ->
                                    conceptosYDefiniciones =
                                        conceptosYDefiniciones.toMutableList().apply {
                                            this[index] = mapOf(
                                                "concepto" to concepto,
                                                "definicion" to nuevaDefinicion
                                            )
                                        }
                                },
                                label = { Text("Definición ${index + 1}") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Botón de eliminar par (concepto + definición)
                            IconButton(
                                onClick = {
                                    conceptosYDefiniciones =
                                        conceptosYDefiniciones.toMutableList().apply {
                                            removeAt(index)
                                        }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar par concepto/definición"
                                )
                            }
                        }
                    }

                    // Botón para agregar un nuevo par de concepto y definición
                    Button(
                        onClick = {
                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
                                add(mapOf("concepto" to "", "definicion" to ""))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
                    ) {
                        Text("Agregar concepto y definición")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                TipoPregunta.COMPLETAR_PALABRAS -> {
                    // Paso 1: Campo para que el usuario introduzca la frase completa
                    Text("Escribe la frase completa:", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = fraseCompletar,
                        onValueChange = { nuevoValor ->
                            fraseCompletar = nuevoValor // Actualiza el estado con el nuevo texto ingresado
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    // Paso 2: Seleccionar las palabras que serán completadas
                    Text("Escriba las palabras que serán el espacio en blanco:", style = MaterialTheme.typography.bodyMedium)

                    // Iteramos sobre la lista de palabras que se deben completar
                    opcionesCorrectasCompletarPalabras.forEachIndexed { index, palabra ->
                        OutlinedTextField(
                            value = palabra,
                            onValueChange = { nuevaPalabra ->
                                opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras.toMutableList().apply {
                                    set(index, nuevaPalabra)  // Actualizamos la palabra en la posición correspondiente
                                }
                            },
                            label = { Text("Palabra en blanco #${index + 1}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    // Agregar un botón para permitir agregar más palabras a completar
                    Button(
                        onClick = {
                            opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras + "" // Agrega una caja de texto vacía
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Añadir palabra")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar las opciones correctas si es necesario
                    if (opcionesCorrectasCompletarPalabras.isNotEmpty()) {
                        Text("Opciones correctas: ${opcionesCorrectasCompletarPalabras.joinToString()}", style = MaterialTheme.typography.bodySmall)
                    }
                }

            }

        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Button(
                onClick = {
                    // Verificar si el título está vacío
                    if (titulo.isEmpty()) {
                        errorMessage = "Por favor, ingrese el título de la pregunta."
                    } else {
                        errorMessage = "" //ya hay titulo
                        // Dependiendo del tipo de pregunta, realizar las validaciones
                        when (tipoPregunta) {
                            TipoPregunta.VERDADERO_FALSO -> {
                                // No hay validación adicional para este tipo
                            }
                            TipoPregunta.OPCION_MULTIPLE_MULTIPLES ->{
                                if (opciones.size !in 2..6) {
                                    errorMessage = "Por favor, ingrese entre 2 y 6 opciones."
                                }else if (opciones.any { it.isEmpty() }) {
                                    errorMessage = "Por favor, complete todas las opciones."
                                } else if (respuestasCorrectasMultipleMultiples.isEmpty()) {
                                    errorMessage = "Por favor, seleccione la respuesta correcta."
                                }else{
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.OPCION_MULTIPLE_UNA -> {
                                if (opciones.size !in 2..6) {
                                    errorMessage = "Por favor, ingrese entre 2 y 6 opciones."
                                }else if (opciones.any { it.isEmpty() }) {
                                    errorMessage = "Por favor, complete todas las opciones."
                                } else if (respuestaCorrectaOpcionMultiple == -1) {
                                    errorMessage = "Por favor, seleccione la respuesta correcta."
                                }else{
                                    errorMessage = ""
                                }
                            }
                            //ME FALTA QUE LOS ITEMS SEAN != "". YA Q SI NO PONES NADA, T DEJA CREAR LA PREG
                            TipoPregunta.EMPAREJAR -> {
                                if (leftItems.size !in 2..6 || rightItems.size !in 2..6) {
                                    errorMessage = "Por favor, ingrese entre 2 y 6 ítems en ambas columnas."
                                } else if (leftItems.size != rightItems.size) {
                                    errorMessage = "Por favor, complete ambas columnas con el mismo número de ítems."
                                } else {
                                    errorMessage = "" //ya esta bien
                                    // Emparejar los ítems si las validaciones son correctas
                                    itemPairs = leftItems.zip(rightItems) { leftItem, rightItem ->
                                        mapOf(leftItem to rightItem)
                                    }
                                }
                            }

                            TipoPregunta.ORDENAR -> {
                                if (itemsOrdenados.size !in 2..6 ) {
                                    errorMessage = "Por favor, ingrese entre 2 y 6 ítems."
                                }else {
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.COMPLETAR_ESPACIOS -> {
                                if (fraseCompletar.isEmpty()){
                                    errorMessage = "Por favor, ingrese la frase para completar."
                                    //NO COGE LA PALABRA SI VA SEGUIDA DE CARACTERES ESPECIALES (SYMBOLS) '¡?()
                                }else if (!fraseCompletar.split(" ").contains(opcionCorrecta)) {  // Verificar si la palabra a completar está en la frase
                                    errorMessage = "La palabra no está en la frase."
                                }else if (opciones.size < 1 ) {
                                    errorMessage = "Por favor, ingrese al menos una opción."
                                }else if(opciones.any { it.isEmpty() }){
                                    errorMessage = "Por favor, no deje opciones en blanco."
                                }else {
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.ASOCIACION -> {
                                if (conceptosYDefiniciones.size < 2) {
                                    errorMessage = "Por favor, ingrese al menos 2 conceptos y definiciones."
                                } else if (conceptosYDefiniciones.any { it["concepto"].isNullOrBlank() || it["definicion"].isNullOrBlank() }) {
                                    errorMessage = "Por favor, asegúrese de que todos los conceptos y definiciones no estén vacíos."
                                } else {
                                    errorMessage = ""
                                }

                            }

                            TipoPregunta.COMPLETAR_PALABRAS -> {
                                if (fraseCompletar.isEmpty()) {
                                    errorMessage = "Por favor, ingrese la frase para completar."
                                } else {
                                    // Verificar que todas las palabras a completar están en la frase
                                    val fraseSinCaracteresEspeciales = fraseCompletar.replace(Regex("[^\\w\\s]"), "") // Eliminar caracteres especiales
                                    val palabrasFrase = fraseSinCaracteresEspeciales.split(" ")

                                    // Verificar si todas las palabras correctas están presentes en la frase
                                    val palabrasNoEncontradas = opcionesCorrectasCompletarPalabras.filter { palabra ->
                                        !palabrasFrase.contains(palabra)
                                    }

                                    if (palabrasNoEncontradas.isNotEmpty()) {
                                        errorMessage = "Las siguientes palabras no están en la frase: ${palabrasNoEncontradas.joinToString()}"
                                    } else {
                                        errorMessage = ""
                                    }
                                }
                            }
                        }

                        // Si no hay errores, proceder a guardar la pregunta en Firebase y navegar
                        if (errorMessage.isEmpty()) {
                            // Establecer las respuestas correctas dependiendo del tipo de pregunta
                            respuestasCorrectas = when (tipoPregunta) {
                                TipoPregunta.VERDADERO_FALSO -> listOf(if (respuestaCorrectaVF) "Verdadero" else "Falso")
                                TipoPregunta.OPCION_MULTIPLE_UNA -> listOfNotNull(
                                    opciones.getOrNull(respuestaCorrectaOpcionMultiple)
                                )
                                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> respuestasCorrectasMultipleMultiples
                                TipoPregunta.COMPLETAR_ESPACIOS -> listOf(opcionCorrecta)
                                //TipoPregunta.EMPAREJAR -> itemPairs.map { it.values.first() }

                                else -> listOf()
                            }
                            questionsViewModel.actualizarPregunta(
                                pregunta = Pregunta(
                                    id = preguntaMod.id,  // Genera un nuevo ID único
                                    user_id = user_id ?: "",            // Asigna el ID del usuario o un valor vacío
                                    titulo = titulo,                    // Título de la pregunta
                                    tipo = tipoPregunta,                // Tipo de pregunta seleccionado
                                    opciones = opciones,                // Opciones para las respuestas
                                    imagen = imageUri?.toString(),                    // Imagen opcional (puede ser null)
                                    respuestasCorrectas = respuestasCorrectas, // Respuestas correctas
                                    emparejamientos = itemPairs,  // Lista de emparejamientos si aplica
                                    itemsOrdenados = itemsOrdenados,    // Lista de ítems para ordenar si aplica
                                    isSelected = false,                 // Inicializa con el valor predeterminado
                                    fraseCompletar = fraseCompletar,    // Frase para completar espacios
                                    opcionCorrecta = opcionCorrecta,    // Opción correcta en caso de una respuesta única
                                    conceptosYDefiniciones = conceptosYDefiniciones, // Conceptos y definiciones si aplica
                                    opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras, // Opciones correctas para completar palabras
                                    leftItems = leftItems,              // Ítems del lado izquierdo para emparejar
                                    rightItems = rightItems             // Ítems del lado derecho para emparejar
                                )
                            )

                            Toast.makeText(context, "Pregunta actualizada exitosamente!", Toast.LENGTH_SHORT).show()
                            // Navegar a la pantalla de quizzes
                            navController.navigate("select_questions/${user_id}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Guardar Pregunta", color = Color.White)
            }

        }

        item {
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
        }

        item {
            Button(
                onClick = { navController.navigate("select_questions/${user_id}") },
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun DropdownMenuQuestionType2(selectedTipo: TipoPregunta, onTipoSelected: (TipoPregunta) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedTipo.name.replace('_', ' '))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            TipoPregunta.entries.forEach { tipo ->
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onTipoSelected(tipo)
                    },
                    text = { Text(tipo.name.replace('_', ' ')) }
                )
            }
        }
    }
}