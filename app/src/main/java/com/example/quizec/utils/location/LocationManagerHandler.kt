package com.example.listacontactos.utils.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationListenerCompat

class LocationManagerHandler(private val locationManager: LocationManager) : LocationHandler {
    override var locationEnabled = false
    override var onLocation: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates() { // Iniciar las actualizaciones de ubicación
        if (locationEnabled)
            return

        val notify = onLocation ?: return

        // Obtener la última ubicación conocida
        notify(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            ?: Location(null).apply { // Si no hay última ubicación, se crea una nueva
                latitude = 40.1925
                longitude = -8.4128

            }
        )

        // Solicitar actualizaciones de ubicación
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            100f,
            locationListener
        )
        locationEnabled = true
    }

    // Manejar la ubicación
    private val locationListener = LocationListenerCompat { location ->
        val notify = onLocation ?: return@LocationListenerCompat
        notify(location)
    }

    // Detener las actualizaciones de ubicación
    override fun stopLocationUpdates() {
        if (!locationEnabled)
            return
        locationManager.removeUpdates(locationListener)
        locationEnabled = false
    }

}