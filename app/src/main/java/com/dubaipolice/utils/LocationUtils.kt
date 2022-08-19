package com.dubaipolice.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.dubaipolice.utils.Utils.isGooglePlayServicesAvailable
import com.dubaipolice.view.activity.MapLocationActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationUtils(private val activity: MapLocationActivity) {
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var onLocation: LocationEnable
    lateinit var locationManager: LocationManager
    init {
        if (isGooglePlayServicesAvailable(activity, AppConstants.Permissions.REQUEST_CHECK_GOOGLE_PLAY_SERVICE)) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

            createLocationRequest()
            createLocationCallback()
            checkLocationSettings()
            onLocation = activity

            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    AppConstants.Permissions.LOCATION_PERMISSION_REQUEST_CODE)
            }


            }
    }
    private fun checkLocationSettings() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { startLocationUpdates() }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    e.startResolutionForResult(activity, AppConstants.Permissions.REQUEST_CHECK_LOCATION_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                onLocation.onLocationFound(locationResult.lastLocation)
                stopLocationUpdates()
            }
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create()
        locationRequest.fastestInterval = 0
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

    }
    @SuppressLint("MissingPermission")
     fun startLocationUpdates() {
        fusedLocationClient!!.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }
     fun stopLocationUpdates() {
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }


    interface LocationEnable {
        fun onLocationFound(location: Location)
    }
}