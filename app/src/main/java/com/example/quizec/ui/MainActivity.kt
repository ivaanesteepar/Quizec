package com.example.quizec.ui

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Scaffold
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold { padding -> //arreglar
                QuizecApp()
            }
        }



        // Verificar permisos y solicitar los necesarios
        if (ContextCompat.checkSelfPermission( // Verificar si se tienen los permisos para la cámara
                this,
                android.Manifest.permission.CAMERA // Permiso para la cámara
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askSinglePermissions.launch(android.Manifest.permission.CAMERA)
        }

        // Solicitar permisos para leer imágenes en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission( // Verificar si se tienen los permisos
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES // Permiso para leer imágenes
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askSinglePermissions.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Solicitar permisos para acceder a almacenamiento en versiones anteriores a Android 10
            if (ContextCompat.checkSelfPermission( // Verificar si se tienen los permisos
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE // Permiso para leer archivos
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE // Permiso para escribir archivos
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askMultiplePermissions.launch(
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }

        // Obtener la lista de permisos solicitados
        val requestedPermissions = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions

        // Verificar si los permisos están otorgados
        if (requestedPermissions != null) {
            for (permission in requestedPermissions) {
                val isGranted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
                // Imprimir si el permiso está otorgado o no
                Log.d("Permissions", "Permiso: $permission, Otorgado: $isGranted")
            }
        } else {
            Log.d("Permissions", "No se encontraron permisos solicitados en el manifiesto.")
        }
    }


    // Manejo de permisos múltiples
    val askMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { permission, isGranted ->
            if (isGranted) {
                // Imprimir un mensaje cuando se otorgue el permiso
                if (permission == android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                    println("Permiso de lectura de almacenamiento otorgado.")
                }
                if (permission == android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    println("Permiso de escritura de almacenamiento otorgado.")
                }
            } else {
                // Imprimir un mensaje cuando se deniegue el permiso
                println("Permiso de almacenamiento DENEGADO: $permission")
            }
        }
    }

    // Manejo de permisos individuales
    val askSinglePermissions = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Imprimir un mensaje cuando se otorgue el permiso
            println("Permiso otorgado.")
        } else {
            // Imprimir un mensaje cuando se deniegue el permiso
            println("Permiso DENEGADO.")
        }
    }
}
