package com.example.compose_deeplink.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class CounterNotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = CounterNotificationService(context)
        if (intent.getIntExtra(Counter.FLAG_RESET, -1) == 0) {
            Counter.value = 0
            Toast.makeText(context, "FLAG --- GOT", Toast.LENGTH_SHORT).show()
            service.showNotification(0)
        } else {
            service.showNotification(++Counter.value)
        }
    }
}