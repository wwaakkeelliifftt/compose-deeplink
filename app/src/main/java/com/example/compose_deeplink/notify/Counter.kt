package com.example.compose_deeplink.notify

import androidx.lifecycle.MutableLiveData

object Counter {
    val val_ld = MutableLiveData(0)

    fun increment() {
        val_ld.postValue(val_ld.value?.plus(1))
    }

    fun reset() {
        val_ld.postValue(0)
    }


    const val FLAG_RESET = "zero"
}