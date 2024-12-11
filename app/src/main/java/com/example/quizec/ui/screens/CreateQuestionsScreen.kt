package com.example.quizec.ui.screens

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.viewmodel.QuizViewModel
import com.example.quizec.utils.AMovServer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID


@Composable
fun CreateQuestionsScreen(navController: NavHostController, quizViewModel: QuizViewModel) {
    val db = FirebaseFirestore.getInstance()

    val user_id = FirebaseAuth.getInstance().currentUser?.uid   // Obtener el ID del usuario autenticado

    var errorMessage by remember { mutableStateOf("") }
    var titulo by remember { mutableStateOf("") }
    var tipoPregunta by remember { mutableStateOf(TipoPregunta.VERDADERO_FALSO) }

    // Nueva variable para almacenar las respuestas correctas
    var respuestasCorrectasMultipleMultiples by remember { mutableStateOf(listOf<String>()) }

    var opciones by remember { mutableStateOf(listOf("")) } //COMPLETAR ESPACIOS
    var respuestasCorrectas by remember { mutableStateOf(listOf("")) }
    var respuestaCorrectaVF by remember { mutableStateOf(true) }
    var respuestaCorrectaOpcionMultiple by remember { mutableStateOf(-1) } // Índice de la opción correcta seleccionada

    //EMPAREJAR
    var leftItems by remember { mutableStateOf(listOf("")) }
    var rightItems by remember { mutableStateOf(listOf("")) }
    var itemPairs by remember { mutableStateOf(listOf<Map<String, String>>())}  // Lista de pares
    //ORDENAR
    var itemsOrdenados by remember { mutableStateOf(listOf("")) } // Lista de ítems que deben ser ordenados
    //COMPLETAR ESPACIOS
    var fraseCompletar by remember { mutableStateOf("") }
    var opcionCorrecta by remember { mutableStateOf("") } //la palabra que será el espacio en blanco
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    //ASOCIACION
    // Listas para que el profesor ingrese conceptos y definiciones
    var conceptos by remember { mutableStateOf(listOf("")) }
    var definiciones by remember { mutableStateOf(listOf("")) }
    //tb usa itemPairs
    var conceptosYDefiniciones by remember { mutableStateOf(listOf<Map<String, String>>())}
    var opcionesCorrectasCompletarPalabras by remember { mutableStateOf(listOf<String>()) }

    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    val pickPicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("CreateQuizScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("CreateQuizScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            imageUrl = result
                            error = null
                        } else {
                            Log.d("CreateQuizScreen", "Error al subir la imagen")
                            imageUrl = null
                        }
                    }
                )
            }
        }
    )


    // Para agregar img como concepto
    val pickPicture2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("CreateQuizScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("CreateQuizScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
                                val indexToUpdate = conceptosYDefiniciones.indexOfFirst { it["concepto"] == "" }
                                if (indexToUpdate >= 0) {
                                    this[indexToUpdate] = mapOf(
                                        "concepto" to result,
                                        //"definicion" to this[indexToUpdate]["definicion"] ?: ""
                                    )
                                }
                            }
                        } else {
                            Log.d("CreateQuizScreen", "Error al subir la imagen")
                        }
                    }
                )
            }
        }
    )


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Crear Pregunta", style = MaterialTheme.typography.titleLarge)
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
            // Botón para elegir imagen
            Text("Subir Imagen (Opcional)", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = {
                    pickPicture.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            ) {
                Text("Seleccionar Imagen")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show selected image if available
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen cargada del servidor",
                    modifier = Modifier.size(200.dp)
                )
            } else {
                Text(text = "No hay imagen para mostrar.")
            }
        }

        item {
            Text("Tipo de Pregunta", style = MaterialTheme.typography.bodyMedium)
            DropdownMenuQuestionType(tipoPregunta) { selectedTipo ->
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newOption ->
                                    // Actualizar la opción al cambiar el texto
                                    opciones =
                                        opciones.toMutableList().apply { this[index] = newOption }
                                },
                                label = { Text("Opción ${index + 1}") },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                            )

                            // Botón para eliminar la opción
                            IconButton(onClick = {
                                // Eliminar la opción de la lista
                                opciones = opciones.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar Opción"
                                )
                            }
                        }
                    }

                    // Botón para agregar una nueva opción
                    Button(onClick = { opciones = opciones + "" }) {
                        Text("Agregar Opción")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Seleccione la respuesta correcta:",
                        style = MaterialTheme.typography.bodyMedium
                    )

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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { newOption ->
                                        opciones = opciones.toMutableList()
                                            .apply { this[index] = newOption }
                                    },
                                    label = { Text("Opción ${index + 1}") },
                                    modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                )

                                // Botón de eliminar opción
                                IconButton(onClick = {
                                    // Eliminar la opción de la lista y sincronizar las respuestas correctas
                                    opciones = opciones.toMutableList().apply { removeAt(index) }
                                    respuestasCorrectasMultipleMultiples =
                                        respuestasCorrectasMultipleMultiples.filter { it in opciones }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar Opción"
                                    )
                                }
                            }
                        }

                        // Botón para agregar más opciones
                        Button(onClick = { opciones = opciones + "" }) {
                            Text("Agregar Opción")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Seleccione las respuestas correctas:",
                            style = MaterialTheme.typography.bodyMedium
                        )

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


//                //!! FALTA QUE SE PUEDE ELEGIR COMO CONCEPTO UNA IMAGEN
//                TipoPregunta.ASOCIACION -> {
//                    Text("Conceptos y definiciones:", style = MaterialTheme.typography.bodyMedium)
//
//                    conceptosYDefiniciones.forEachIndexed { index, mapConceptoDefinicion ->
//                        val concepto = mapConceptoDefinicion["concepto"] ?: ""
//                        val definicion = mapConceptoDefinicion["definicion"] ?: ""
//
//                        // Cada par concepto/definición estará en una columna
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                        ) {
//                            // Campo de texto para el concepto
//                            OutlinedTextField(
//                                value = concepto,
//                                onValueChange = { nuevaConcepto ->
//                                    conceptosYDefiniciones =
//                                        conceptosYDefiniciones.toMutableList().apply {
//                                            this[index] = mapOf(
//                                                "concepto" to nuevaConcepto,
//                                                "definicion" to definicion
//                                            )
//                                        }
//                                },
//                                label = { Text("Concepto ${index + 1}") },
//                                modifier = Modifier.fillMaxWidth()
//                            )
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Campo de texto para la definición
//                            OutlinedTextField(
//                                value = definicion,
//                                onValueChange = { nuevaDefinicion ->
//                                    conceptosYDefiniciones =
//                                        conceptosYDefiniciones.toMutableList().apply {
//                                            this[index] = mapOf(
//                                                "concepto" to concepto,
//                                                "definicion" to nuevaDefinicion
//                                            )
//                                        }
//                                },
//                                label = { Text("Definición ${index + 1}") },
//                                modifier = Modifier.fillMaxWidth()
//                            )
//
//                            // Botón de eliminar par (concepto + definición)
//                            IconButton(
//                                onClick = {
//                                    conceptosYDefiniciones =
//                                        conceptosYDefiniciones.toMutableList().apply {
//                                            removeAt(index)
//                                        }
//                                }
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Delete,
//                                    contentDescription = "Eliminar par concepto/definición"
//                                )
//                            }
//                        }
//                    }
//
//                    // Botón para agregar un nuevo par de concepto y definición
//                    Button(
//                        onClick = {
//                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
//                                add(mapOf("concepto" to "", "definicion" to ""))
//                            }
//                        },
//                        modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp)
//                    ) {
//                        Text("Agregar concepto y definición")
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//                }



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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (concepto.startsWith("http://")) {
                                    // Mostrar imagen como concepto
                                    AsyncImage(
                                        model = concepto,
                                        contentDescription = "Imagen del concepto",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botón para eliminar la imagen
                                    Button(
                                        onClick = {
                                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
                                                this[index] = mapOf(
                                                    "concepto" to "",
                                                    "definicion" to definicion
                                                )
                                            }
                                        }
                                    ) {
                                        Text("Eliminar imagen")
                                    }
                                } else {
                                    // Campo de texto para el concepto, si no hay imagen
                                    OutlinedTextField(
                                        value = concepto,
                                        onValueChange = { nuevoConcepto ->
                                            if (!nuevoConcepto.startsWith("http://")) {
                                                conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
                                                    this[index] = mapOf(
                                                        "concepto" to nuevoConcepto,
                                                        "definicion" to definicion
                                                    )
                                                }
                                            }
                                        },
                                        label = { Text("Concepto ${index + 1}") },
                                        modifier = Modifier.weight(1f),
                                        enabled = !concepto.startsWith("content://")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botón para seleccionar una imagen
                                    Button(
                                        onClick = {
                                            if (concepto.isEmpty()) {
                                                pickPicture2.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                        },
                                        enabled = concepto.isEmpty()
                                    ) {
                                        Text("Subir imagen")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Campo de texto para la definición
                            OutlinedTextField(
                                value = definicion,
                                onValueChange = { nuevaDefinicion ->
                                    conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
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
                                    conceptosYDefiniciones = conceptosYDefiniciones.toMutableList().apply {
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



//////////////////////////////////////

                TipoPregunta.COMPLETAR_PALABRAS -> {
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

                            // Crear la pregunta
                            val pregunta = Pregunta(
                                id = UUID.randomUUID().toString(),
                                user_id = user_id ?: "",
                                titulo = titulo,
                                tipo = tipoPregunta,
                                opciones = opciones,
                                imagen = imageUri?.toString(), // Convertir Uri a String
                                respuestasCorrectas = respuestasCorrectas,
                                emparejamientos = itemPairs,
                                itemsOrdenados = itemsOrdenados,
                                fraseCompletar = fraseCompletar,
                                opcionCorrecta = opcionCorrecta,
                                conceptosYDefiniciones = conceptosYDefiniciones,
                                opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras,
                                leftItems = leftItems,
                                rightItems = rightItems
                                //userAnswers = userAnswers
                            )
                            // Guardar la pregunta en Firestore
                            savePreguntaToFirestore(pregunta, db)

                            Toast.makeText(context, "Pregunta guardada exitosamente!", Toast.LENGTH_SHORT).show()
                            // Navegar a la pantalla de quizzes
                            navController.navigate("createQuiz")
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
                onClick = { navController.navigate("createQuiz") },
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Text("Volver")
            }
        }
    }
}

@Composable
fun DropdownMenuQuestionType(selectedTipo: TipoPregunta, onTipoSelected: (TipoPregunta) -> Unit) {
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

private fun savePreguntaToFirestore(pregunta: Pregunta, db: FirebaseFirestore) {
    val preguntaMap = hashMapOf(
        "id" to pregunta.id,
        "user_id" to pregunta.user_id, // Asegúrate de tener una propiedad "userId"
        "titulo" to pregunta.titulo,
        "tipo" to pregunta.tipo.name,
        "opciones" to pregunta.opciones,
        "imagen" to pregunta.imagen,
        "respuestasCorrectas" to pregunta.respuestasCorrectas,
        "emparejamientos" to pregunta.emparejamientos,
        "itemsOrdenados" to pregunta.itemsOrdenados,
        "fraseCompletar" to pregunta.fraseCompletar,
        "opcionCorrecta" to pregunta.opcionCorrecta,
        "conceptosYDefiniciones" to pregunta.conceptosYDefiniciones,
        "opcionesCorrectasCompletarPalabras" to pregunta.opcionesCorrectasCompletarPalabras,
        "userAnswers" to pregunta.userAnswers
    )

    db.collection("preguntas")
        .document(pregunta.id)
        .set(preguntaMap)
        .addOnSuccessListener {
            Log.d("Firestore", "Pregunta guardada exitosamente")
        }
        .addOnFailureListener { e ->
            Log.w("Firestore", "Error al guardar la pregunta", e)
        }
}



@Preview (showBackground = true)
@Composable
fun CreateQuestionsScreenPreview() {
    val navController = rememberNavController()
    CreateQuestionsScreen(navController, quizViewModel = viewModel())
}