package com.example.quizec.utils

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.content.Context
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource

object LocationUtils {

    fun fetchLocation(
        context: Context,  // Recibimos el contexto como parámetro
        fusedLocationClient: FusedLocationProviderClient,
        onLocationFetched: (String) -> Unit
    ) {
        // Verificar si se tiene el permiso de ubicación
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("Geolocalización", "Permiso de ubicación no concedido.")
            // Si no se tiene el permiso, retornamos y dejamos la solicitud del permiso
            return
        }

        // Si se tienen los permisos, obtener la ubicación
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val locationString = "Lat: ${location.latitude}, Lng: ${location.longitude}"
                onLocationFetched(locationString)
            } else {
                Log.e("Geolocalización", "Ubicación no disponible.")
            }
        }.addOnFailureListener { exception ->
            Log.e("Geolocalización", "Error obteniendo la ubicación: ${exception.message}")
        }
    }
}
