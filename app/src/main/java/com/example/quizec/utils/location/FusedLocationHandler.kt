package com.example.listacontactos.utils.location

import android.annotation.SuppressLint
import android.location.Location

import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

class FusedLocationHandler(private val locationProvider: FusedLocationProviderClient) :
    LocationHandler {
    override var locationEnabled = false
    override var onLocation: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    // Iniciar las actualizaciones de ubicación
    override fun startLocationUpdates() {
        if (locationEnabled)
            return

        val notify = onLocation ?: return

        // Si no hay última ubicación, se crea una nueva
        notify(Location(null).apply {
            latitude = 40.1925
            longitude = -8.4128
        })

        // Obtener la última ubicación conocida
        locationProvider.lastLocation
            .addOnSuccessListener { location ->
                location?.let(notify)
                Log.i("Teste", "startLocationUpdates: $location")
            }
        // Solicitar actualizaciones de ubicación
        val locationRequest =
            LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 2000)
                //.setMinUpdateDistanceMeters(100f)
                //.setMinUpdateIntervalMillis(1000)
                //.setMaxUpdateDelayMillis(10000)
                //.setPriority(PRIORITY_HIGH_ACCURACY)
                //.setIntervalMillis(1000)
                //.setMaxUpdates(10)
                .build()
        /*old:val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }*/

        // Solicitar actualizaciones de ubicación
        locationProvider.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())

        locationEnabled = true
    }

    // Crear un callback para manejar las actualizaciones de ubicacion
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach(onLocation)
        }
    }

    // Detener las actualizaciones de ubicación
    override fun stopLocationUpdates() {
        if (!locationEnabled)
            return
        locationProvider.removeLocationUpdates(locationCallback)
        locationEnabled = false
    }

}