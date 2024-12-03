package com.example.quizec.utils

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.CancellationTokenSource

object LocationUtils {

    @SuppressLint("MissingPermission")
    fun fetchLocation(
        fusedLocationClient: FusedLocationProviderClient,
        onLocationFetched: (String) -> Unit
    ) {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, // Usar el nuevo valor constante
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
