package com.example.quizec.ui.screens


import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.quizec.R
import com.example.quizec.data.model.Pregunta
import com.example.quizec.data.model.TipoPregunta
import com.example.quizec.ui.theme.buttonColor
import com.example.quizec.ui.viewmodel.QuestionsViewModel
import com.example.quizec.utils.AMovServer
import com.google.firebase.auth.FirebaseAuth


@Composable
fun EditarPreguntaScreen(preguntaMod: Pregunta, navController: NavHostController) {
    val questionsViewModel = QuestionsViewModel()
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
    var itemPairs by remember { mutableStateOf(mapOf<String, String>()) } // Mapa de item1 a item2
    //ORDENAR
    var itemsOrdenados by remember { mutableStateOf(preguntaMod.itemsOrdenados) } // Lista de ítems que deben ser ordenados
    //COMPLETAR ESPACIOS
    var fraseCompletar by remember { mutableStateOf(preguntaMod.fraseCompletar) }
    var opcionCorrecta by remember { mutableStateOf(preguntaMod.opcionCorrecta) } //la palabra que será el espacio en blanco

    var imageUri by remember { mutableStateOf(preguntaMod.imagen) }

    //ASOCIACION
    //tb usa itemPairs
    var conceptosYDefiniciones by remember { mutableStateOf(preguntaMod.conceptosYDefiniciones)}

    var opcionesCorrectasCompletarPalabras by remember { mutableStateOf(preguntaMod.opcionesCorrectasCompletarPalabras) }


    val context = LocalContext.current
    var error by remember { mutableStateOf<String?>(null) }

    val pickPicture = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("EditarPreguntaScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("EditarPreguntaScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            imageUri = result // Now you're setting a String?
                            error = null
                        } else {
                            Log.d("EditarPreguntaScreen", "Error al subir la imagen")
                            imageUri = null
                        }
                    }
                )
            }
        }
    )

    val pickPicture2 = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            Log.d("EditQuestionsScreen", "URI seleccionada: $uri")
            if (uri != null) {
                AMovServer.asyncUploadImage(
                    inputStream = context.contentResolver.openInputStream(uri)!!,
                    extension = "jpg",
                    onResult = { result ->
                        Log.d("EditQuestionsScreen", "Resultado de subir imagen: $result")
                        if (result != null) {
                            // Actualizar el concepto con la URL de la imagen
                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                // Encontrar un concepto vacío (con la clave vacía)
                                val conceptoVacio = this.keys.firstOrNull { it.isEmpty() }
                                if (conceptoVacio != null) {
                                    val definicion = this[conceptoVacio] ?: ""
                                    // Reemplazar el concepto vacío por la URL de la imagen
                                    this[result] = definicion
                                    this.remove(conceptoVacio) // Eliminar la entrada vacía
                                } else {
                                    Log.d("EditQuestionsScreen", "No se encontró un concepto vacío para actualizar")
                                }
                            }
                        } else {
                            Log.d("EditQuestionsScreen", "Error al subir la imagen")
                        }
                    }
                )
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .background(colorResource(id = R.color.background_color))
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(stringResource(R.string.modificar_pregunta), style = MaterialTheme.typography.titleLarge)
        }

        item {
            Text(stringResource(R.string.t_tulo_de_la_pregunta), style = MaterialTheme.typography.bodyMedium)
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
            // Texto explicativo
            Text(stringResource(R.string.subir_imagen_opcional), style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(10.dp))

            // Si no hay imagen seleccionada, centramos el botón de seleccionar imagen
            if (imageUri == null) {
                Button(
                    onClick = {
                        pickPicture.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor // Aplicamos el color de fondo del botón
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Ocupa tdo el ancho disponible
                        .padding(horizontal = 50.dp) // Márgenes laterales para que no esté pegado a los bordes
                ) {
                    Text(stringResource(R.string.seleccionar_imagen))
                }
            } else {
                // Si hay una imagen, alineamos los botones horizontalmente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center, // Centrar los botones
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón para seleccionar imagen
                    Button(
                        onClick = {
                            pickPicture.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        )
                    ) {
                        Text(stringResource(R.string.seleccionar_imagen))
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Espacio entre los botones

                    // Botón para eliminar imagen
                    Button(
                        onClick = {
                            // Eliminar la imagen
                            imageUri = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red // Aplicamos el color de fondo del botón
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.eliminar_imagen),
                            color = Color.White // Texto blanco para mayor visibilidad
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar la imagen seleccionada si existe
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri, // Ahora usando un String (imageUrl) en lugar de Uri
                    contentDescription = "Imagen cargada del servidor",
                    contentScale = ContentScale.Crop, // Ajusta la imagen para que aproveche tdo el tamaño
                    modifier = Modifier.size(100.dp)
                )
            }
        }


        item {
            Text(context.getString(R.string.tipo_de_pregunta), style = MaterialTheme.typography.bodyMedium)
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
                        stringResource(R.string.seleccione_la_respuesta_correcta),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = respuestaCorrectaVF,
                            onClick = { respuestaCorrectaVF = true }
                        )
                        Text(context.getString(R.string.verdadero))
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = !respuestaCorrectaVF,
                            onClick = { respuestaCorrectaVF = false }
                        )
                        stringResource(R.string.falso)
                    }
                }

                TipoPregunta.OPCION_MULTIPLE_UNA -> {
                    Text(stringResource(R.string.opciones_de_respuesta), style = MaterialTheme.typography.bodyMedium)

                    // Iterar sobre las opciones con índice para mostrar los campos de texto y el botón de eliminar
                    opciones.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newOption ->
                                    // Actualizar la opción al cambiar el texto
                                    opciones = opciones.toMutableList().apply { this[index] = newOption }
                                },
                                label = { Text(
                                    stringResource(
                                        R.string.opcionCreateQuestion,
                                        index + 1
                                    )) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp)
                            )

                            // Botón para eliminar la opción
                            IconButton(onClick = {
                                // Eliminar la opción de la lista
                                opciones = opciones.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.eliminar_opcion_createQuestions))
                            }
                        }
                    }

                    // Botón para agregar una nueva opción
                    Button(
                        onClick = { opciones = opciones + "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        )
                    ) {
                        Text(stringResource(R.string.agregar_opcion))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(stringResource(R.string.seleccione_la_respuesta_correcta), style = MaterialTheme.typography.bodyMedium)

                    // Mostrar las opciones y permitir seleccionar la respuesta correcta
                    opciones.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = respuestaCorrectaOpcionMultiple == index,
                                onClick = { respuestaCorrectaOpcionMultiple = index }
                            )
                            Text(stringResource(R.string.opcionCreateQuesitons2, index + 1, option))
                        }
                    }
                }

                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Text(stringResource(R.string.opciones_de_respuesta), style = MaterialTheme.typography.bodyMedium)

                        // Mostrar las opciones de respuesta
                        opciones.forEachIndexed { index, option ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = option,
                                    onValueChange = { newOption ->
                                        opciones = opciones.toMutableList().apply { this[index] = newOption }
                                    },
                                    label = { Text(stringResource(
                                        R.string.opcionCreateQuestion,
                                        index + 1
                                    )) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                )

                                // Botón de eliminar opción
                                IconButton(onClick = {
                                    // Eliminar la opción de la lista y sincronizar las respuestas correctas
                                    opciones = opciones.toMutableList().apply { removeAt(index) }
                                    respuestasCorrectasMultipleMultiples = respuestasCorrectasMultipleMultiples.filter { it in opciones }
                                }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(
                                        R.string.eliminar_opcion_createQuestions)
                                    )
                                }
                            }
                        }

                        // Botón para agregar más opciones
                        Button(
                            onClick = { opciones = opciones + "" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor // Aplicamos el color de fondo del botón
                            )
                        ) {
                            Text(stringResource(R.string.agregar_opcion))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            stringResource(R.string.seleccione_las_respuestas_correctas),
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
                                Text(
                                    stringResource(
                                        R.string.opcion_CreateQuestions3,
                                        index + 1,
                                        option
                                    )
                                ) // Mostrar la opción como texto
                            }
                        }
                    }
                }



                TipoPregunta.EMPAREJAR -> {
                    // Columna Izquierda
                    Text(
                        stringResource(R.string.ingresa_los_tems_de_la_columna_izquierda),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    leftItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    leftItems =
                                        leftItems.toMutableList().apply { this[index] = newValue }
                                },
                                label = { Text(stringResource(R.string.tem, index + 1)) },
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
                                    contentDescription = stringResource(R.string.eliminar_tem)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { leftItems = leftItems + "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.agregar_tem_a_la_columna_izquierda))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Columna Derecha
                    Text(
                        stringResource(R.string.ingresa_los_tems_de_la_columna_derecha),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    rightItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = item,
                                onValueChange = { newValue ->
                                    rightItems =
                                        rightItems.toMutableList().apply { this[index] = newValue }
                                },
                                label = { Text(stringResource(R.string.tem, index + 1)) },
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
                                    contentDescription = stringResource(R.string.eliminar_tem)
                                )
                            }
                        }
                    }
                    Button(
                        onClick = { rightItems = rightItems + "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.agregar_tem_a_la_columna_derecha))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }


                TipoPregunta.ORDENAR -> {
                    Text(
                        stringResource(R.string.ingresa_los_tems_en_el_orden_correcto),
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
                                label = { Text(stringResource(R.string.tem, index + 1)) },
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.agregar_tem))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                TipoPregunta.COMPLETAR_ESPACIOS -> {
                    // Paso 1: Campo para que el usuario introduzca la frase completa
                    Text(stringResource(R.string.escribe_la_frase_completa), style = MaterialTheme.typography.bodyMedium)
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
                        stringResource(R.string.escriba_la_palabra_que_ser_el_espacio_en_blanco),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = opcionCorrecta,
                        onValueChange = { nuevaPalabra ->
                            opcionCorrecta = nuevaPalabra
                        },
                        label = { Text(stringResource(R.string.palabra_en_blanco)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paso 3: Agregar opciones de palabras para completar el espacio en blanco seleccionado
                    Text(
                        stringResource(R.string.opciones_para_completar_el_espacio_en_blanco),
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
                                label = { Text(stringResource(R.string.opci_n, opcionIndex + 1)) },
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
                                    contentDescription = stringResource(R.string.eliminar_opcion_createQuestions)
                                )
                            }
                        }
                    }

                    // Botón para agregar más opciones
                    Button(
                        onClick = { opciones = opciones + "" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.agregar_opcion))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }


                TipoPregunta.ASOCIACION -> {

                    Text(stringResource(R.string.conceptos_y_definiciones), style = MaterialTheme.typography.bodyMedium)

                    conceptosYDefiniciones.forEach { (concepto, definicion) ->

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
                                        contentScale = ContentScale.Crop, // Ajusta la imagen para q aproveche tdo el tam
                                        modifier = Modifier
                                            .size(80.dp)
                                        //.clip(RoundedCornerShape(8.dp))
                                        //.background(Color.Gray)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Botón para eliminar la imagen
                                    Button(
                                        onClick = {
                                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                                this[concepto] = "" // Eliminar el valor de la imagen
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Red // Aplicamos el color de fondo del botón
                                        )
                                    ) {
                                        Text(stringResource(R.string.eliminar_imagen))
                                    }
                                } else {
                                    // Campo de texto para el concepto, si no hay imagen
                                    OutlinedTextField(
                                        value = concepto,
                                        onValueChange = { nuevoConcepto ->
                                            if (!nuevoConcepto.startsWith("http://")) {
                                                conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                                    // Actualizar el concepto en el Map, manteniendo la misma definición
                                                    this.remove(concepto)  // Eliminamos el antiguo concepto
                                                    this[nuevoConcepto] = definicion // Añadimos el nuevo concepto
                                                }
                                            }
                                        },
                                        label = { Text(stringResource(R.string.concepto)) },
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
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                                        ),
                                        enabled = concepto.isEmpty()
                                    ) {
                                        Text(stringResource(R.string.subir_imagen))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Campo de texto para la definición
                            OutlinedTextField(
                                value = definicion,
                                onValueChange = { nuevaDefinicion ->
                                    conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                        this[concepto] = nuevaDefinicion // Actualizamos la definición correspondiente
                                    }
                                },
                                label = { Text(stringResource(R.string.definicion)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Botón de eliminar par (concepto + definición)
                            IconButton(
                                onClick = {
                                    conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                        remove(concepto) // Eliminamos el concepto y su definición
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.eliminar_par_concepto_definici_n)
                                )
                            }
                        }
                    }

                    // Botón para agregar un nuevo par de concepto y definición
                    Button(
                        onClick = {
                            conceptosYDefiniciones = conceptosYDefiniciones.toMutableMap().apply {
                                this[""] = "" // Agregar un nuevo par vacío
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.agregar_concepto_y_definici_n))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }



                TipoPregunta.COMPLETAR_PALABRAS -> {
                    // Paso 1: Campo para que el usuario introduzca la frase completa
                    Text(stringResource(R.string.escribe_la_frase_completa), style = MaterialTheme.typography.bodyMedium)
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
                    Text(stringResource(R.string.escriba_las_palabras_que_ser_n_el_espacio_en_blanco), style = MaterialTheme.typography.bodyMedium)

                    // Iteramos sobre la lista de palabras que se deben completar
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        opcionesCorrectasCompletarPalabras.forEachIndexed { index, palabra ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = palabra,
                                    onValueChange = { nuevaPalabra ->
                                        opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras.toMutableList().apply {
                                            set(index, nuevaPalabra) // Actualizamos la palabra en la posición correspondiente
                                        }
                                    },
                                    label = { Text(
                                        stringResource(
                                            R.string.palabra_en_blanco_index1,
                                            index + 1
                                        )) },
                                    modifier = Modifier.weight(1f)
                                )

                                // Agregar botón de eliminación excepto para la primera opción
                                if (index > 0) {
                                    IconButton(
                                        onClick = {
                                            opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras.toMutableList().apply {
                                                removeAt(index) // Eliminamos la palabra en la posición correspondiente
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar palabra"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Agregar un botón para permitir agregar más palabras a completar
                    Button(
                        onClick = {
                            opcionesCorrectasCompletarPalabras = opcionesCorrectasCompletarPalabras + "" // Agrega una caja de texto vacía
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor // Aplicamos el color de fondo del botón
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.a_adir_palabra))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar las opciones correctas si es necesario
                    if (opcionesCorrectasCompletarPalabras.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.opciones_correctas_CompletarPalabras,
                                opcionesCorrectasCompletarPalabras.joinToString()
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
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
                        errorMessage = context.getString(R.string.por_favor_ingrese_el_t_tulo_de_la_pregunta)
                    } else {
                        errorMessage = "" //ya hay titulo
                        // Dependiendo del tipo de pregunta, realizar las validaciones
                        when (tipoPregunta) {
                            TipoPregunta.VERDADERO_FALSO -> {
                                // No hay validación adicional para este tipo
                            }
                            TipoPregunta.OPCION_MULTIPLE_MULTIPLES ->{
                                if (opciones.size !in 2..6) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_ingrese_entre_2_y_6_opciones)
                                }else if (opciones.any { it.isEmpty() }) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_complete_todas_las_opciones)
                                } else if (respuestasCorrectasMultipleMultiples.isEmpty()) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_seleccione_la_respuesta_correcta)
                                }else{
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.OPCION_MULTIPLE_UNA -> {
                                if (opciones.size !in 2..6) {
                                    errorMessage = context.getString(R.string.por_favor_ingrese_entre_2_y_6_opciones)
                                }else if (opciones.any { it.isEmpty() }) {
                                    errorMessage = context.getString(R.string.por_favor_complete_todas_las_opciones)
                                } else if (respuestaCorrectaOpcionMultiple == -1) {
                                    errorMessage = context.getString(R.string.por_favor_seleccione_la_respuesta_correcta)
                                }else{
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.EMPAREJAR -> {
                                if (leftItems.size !in 2..6 || rightItems.size !in 2..6) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_ingrese_entre_2_y_6_tems_en_ambas_columnas)
                                } else if (leftItems.size != rightItems.size) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_complete_ambas_columnas_con_el_mismo_n_mero_de_tems)
                                } else if (leftItems.any { it.isBlank() } || rightItems.any { it.isBlank() }) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_aseg_rese_de_que_todos_los_tems_contengan_texto_v_lido)
                                } else {
                                    errorMessage = ""
                                    // Emparejar los ítems si las validaciones son correctas
                                    itemPairs = leftItems.zip(rightItems).toMap() // Crear el mapa directamente
                                }
                            }

                            TipoPregunta.ORDENAR -> {
                                if (itemsOrdenados.size !in 2..6) {
                                    errorMessage = context.getString(R.string.por_favor_ingrese_entre_2_y_6_tems)
                                } else if (itemsOrdenados.any { it.isBlank() }) { // Verifica si algún elemento está en blanco
                                    errorMessage = context.getString(R.string.los_items_no_pueden_estar_vacios) // Mensaje de error adecuado
                                } else {
                                    errorMessage = ""
                                }
                            }

                            TipoPregunta.COMPLETAR_ESPACIOS -> {
                                if (fraseCompletar.isEmpty()) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_ingrese_la_frase_para_completar)
                                } else {
                                    // Eliminar caracteres especiales de la frase
                                    val fraseSinCaracteresEspeciales = fraseCompletar.replace(
                                        Regex("[^\\w\\s]"),
                                        ""
                                    )
                                    val palabrasFrase = fraseSinCaracteresEspeciales.split(" ")

                                    // Verificar si la palabra a completar está en la frase
                                    if (!palabrasFrase.contains(opcionCorrecta)) {
                                        errorMessage =
                                            context.getString(
                                                R.string.la_palabra_no_est_en_la_frase,
                                                opcionCorrecta
                                            )
                                    } else if (opciones.size < 1) {
                                        errorMessage =
                                            context.getString(R.string.por_favor_ingrese_al_menos_una_opci_n)
                                    } else if (opciones.any { it.isEmpty() }) {
                                        errorMessage =
                                            context.getString(R.string.por_favor_no_deje_opciones_en_blanco)
                                    } else {
                                        errorMessage = ""
                                    }
                                }
                            }


                            TipoPregunta.ASOCIACION -> {
                                // Comprobar si hay al menos dos conceptos y definiciones
                                if (conceptosYDefiniciones.size < 2) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_ingrese_al_menos_2_conceptos_y_definiciones)
                                } else if (conceptosYDefiniciones.any { (concepto, definicion) ->
                                        // Comprobar si algún concepto o definición está vacío o nulo
                                        concepto.isBlank() || definicion.isBlank()
                                    }) {
                                    errorMessage =
                                        context.getString(R.string.por_favor_aseg_rese_de_que_todos_los_conceptos_y_definiciones_no_est_n_vac_os)
                                } else {
                                    errorMessage = "" // No hay error si tdo está lleno
                                }
                            }


                            TipoPregunta.COMPLETAR_PALABRAS -> {
                                if (fraseCompletar.isEmpty()) {
                                    errorMessage = context.getString(R.string.por_favor_ingrese_la_frase_para_completar)
                                } else if (opcionesCorrectasCompletarPalabras.isEmpty() || opcionesCorrectasCompletarPalabras.any { it.isBlank() }) {
                                    errorMessage = context.getString(R.string.por_favor_ingrese_al_menos_una_palabra_a_completar)
                                } else {
                                    // Verificar que todas las palabras a completar están en la frase
                                    val fraseSinCaracteresEspeciales = fraseCompletar.replace(Regex("[^\\w\\s]"), "") // Eliminar caracteres especiales
                                    val palabrasFrase = fraseSinCaracteresEspeciales.split(" ")

                                    // Verificar si todas las palabras correctas están presentes en la frase
                                    val palabrasNoEncontradas = opcionesCorrectasCompletarPalabras.filter { palabra ->
                                        !palabrasFrase.contains(palabra)
                                    }

                                    if (palabrasNoEncontradas.isNotEmpty()) {
                                        errorMessage = context.getString(
                                            R.string.las_siguientes_palabras_no_est_n_en_la_frase,
                                            palabrasNoEncontradas.joinToString()
                                        )
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
                                TipoPregunta.VERDADERO_FALSO -> listOf(if (respuestaCorrectaVF) context.getString(R.string.verdadero) else context.getString(R.string.falso))
                                TipoPregunta.OPCION_MULTIPLE_UNA -> listOfNotNull(
                                    opciones.getOrNull(respuestaCorrectaOpcionMultiple)
                                )
                                TipoPregunta.OPCION_MULTIPLE_MULTIPLES -> respuestasCorrectasMultipleMultiples
                                TipoPregunta.COMPLETAR_ESPACIOS -> listOf(opcionCorrecta)

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

                            Toast.makeText(context,
                                context.getString(R.string.pregunta_guardada_exitosamente), Toast.LENGTH_SHORT).show()
                            // Navegar a la pantalla de quizzes
                            navController.navigate("select_questions/${user_id}")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.logo_pink) // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(stringResource(R.string.guardar_pregunta), color = Color.White)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor // Aplicamos el color de fondo del botón
                ),
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                Text(stringResource(R.string.volver))
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