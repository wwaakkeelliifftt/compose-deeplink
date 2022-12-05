package com.example.compose_deeplink.proto_data_store

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.dataStore
import com.example.compose_deeplink.MainActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.collections.immutable.mutate

internal const val MY_PERMISSIONS_REQUEST_LOCATION = 99
internal const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66

internal val Context.dataStore by dataStore("app-settings.json", AppSettingsSerializer)

suspend fun MainActivity.updateLanguage(language: Language) {
    dataStore.updateData {
        it.copy(language = language)
    }
}

suspend fun MainActivity.updateLocation(location: Location) {
    dataStore.updateData {
        it.copy(
            knownLocation = it.knownLocation.mutate { kl ->
                kl.add(location)
            }
        )
    }
}

val MainActivity.locationRequest: LocationRequest
    get() = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 100
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime = 60000
    }

val MainActivity.locationCallback: LocationCallback
    get() = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Toast.makeText(applicationContext, "Location at: $location", Toast.LENGTH_SHORT).show()
            }
        }
    }


fun MainActivity.checkLocationPermission() {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton(
                    "OK"
                ) { _, _ ->
                    //Prompt the user once explanation has been shown
                    requestLocationPermission()
                }
                .create()
                .show()
        } else {
            // No explanation needed, we can request the permission.
            requestLocationPermission()
        }
    } else {
        checkBackgroundLocation()
    }
}

fun MainActivity.checkBackgroundLocation() {
    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        requestBackgroundLocationPermission()
    }
}

fun MainActivity.requestLocationPermission() {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
        ),
        MY_PERMISSIONS_REQUEST_LOCATION
    )
}

fun MainActivity.requestBackgroundLocationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
        )
    } else {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }
}
