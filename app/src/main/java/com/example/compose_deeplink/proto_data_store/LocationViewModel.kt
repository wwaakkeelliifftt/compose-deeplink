package com.example.compose_deeplink.proto_data_store


import android.net.Network
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class LocationViewModel: ViewModel() {

    private var _gpsEnabled = MutableStateFlow(false)
    val gpsEnabled: StateFlow<Boolean> get() = _gpsEnabled

    fun checkLocationStatus(isGps: Boolean) = flow<Boolean>{
        viewModelScope.launch {
            update(isGps)
        }
    }
    private fun update(gps: Boolean) = flow<Boolean> {
        _gpsEnabled.emit(gps)
    }

    private var _location = MutableLiveData(Location(11.11, 4.4))
    val location: LiveData<Location> get() = _location

    suspend fun loc(newLat: Double, newLng: Double) {
        _l.emit(Location(newLat, newLng))
    }
    private val _l = MutableStateFlow<Location>(Location(22.22, 44.44))
    val l: StateFlow<Location> get() = _l

    fun updateLocation(lat: Double, lng: Double) = viewModelScope.launch {
        _location.postValue(Location(lat, lng))
        loc(lat, lng)
    }



}