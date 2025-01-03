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
import com.example.quizec.ui.theme.JetpackComposePracTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackComposePracTheme {
            Scaffold { padding ->
                QuizecApp()
            }
        }
    }

        // Solicitar permisos necesarios
        requestNecessaryPermissions()
    }

    /**
     * Solicitar permisos necesarios para la aplicación.
     */
    private fun requestNecessaryPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Permisos de ubicación
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        // Permiso de cámara
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }

        // Permisos para medios (Android 13+) o almacenamiento (Android < 13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            // Para versiones anteriores
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // Solicitar permisos si es necesario
        if (permissionsToRequest.isNotEmpty()) {
            askMultiplePermissions.launch(permissionsToRequest.toTypedArray())
        }

        // Log de permisos solicitados y estado actual
        logRequestedPermissions()
    }

    /**
     * Registro de permisos declarados en el manifiesto y su estado actual.
     */
    private fun logRequestedPermissions() {
        val requestedPermissions = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_PERMISSIONS
        ).requestedPermissions
        requestedPermissions?.forEach { permission ->
            val isGranted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            Log.d("Permissions", "Permiso: $permission, Otorgado: $isGranted")
        } ?: run {
            Log.d("Permissions", "No se encontraron permisos solicitados en el manifiesto.")
        }
    }

    /**
     * Manejo del resultado de solicitudes de permisos múltiples.
     */
    private val askMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            if (isGranted) {
                Log.d("Permissions", "Permiso otorgado: $permission")
            } else {
                Log.e("Permissions", "Permiso denegado: $permission")
            }
        }
    }
}
