package com.example.compose_deeplink.notify

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.compose_deeplink.MainActivity
import com.example.compose_deeplink.R
import com.example.compose_deeplink.Route

class CounterNotificationService(
     private val context: Context
) {
    companion object {
        const val COUNTER_CHANNEL_ID = "counter_channel"
        const val NOTIFICATION_ID = 1 // for updating current notification
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(counter: Int) {
        val activityIntent = Intent(
            Intent.ACTION_VIEW,
            Route.NOTIFY.toUri(),
            context,
            MainActivity::class.java
        )
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val incrementIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, CounterNotificationBroadcastReceiver::class.java).apply {
                putExtra(Counter.FLAG_RESET, 11)
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, COUNTER_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_rabbit_24)
            .setContentTitle("Increment counter")
            .setContentText("The count is $counter")
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.baseline_rabbit_24,
                "inc +1",
                incrementIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}