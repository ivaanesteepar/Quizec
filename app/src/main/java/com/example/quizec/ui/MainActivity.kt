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
            Scaffold { padding ->
                QuizecApp()
            }
        }

        // Permisos de localización
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION // Permiso para ubicación precisa
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askSinglePermissions.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION // Permiso para ubicación aproximada
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askSinglePermissions.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Verificar y solicitar otros permisos (cámara, almacenamiento) como ya tienes en tu código
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askSinglePermissions.launch(android.Manifest.permission.CAMERA)
        }

        // Solicitar permisos para leer imágenes en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                askSinglePermissions.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Solicitar permisos para acceder a almacenamiento en versiones anteriores a Android 10
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
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
