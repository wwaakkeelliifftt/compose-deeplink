package com.example.compose_deeplink.notify

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.compose_deeplink.MainActivity
import com.example.compose_deeplink.R

class CounterNotificationService(
     private val context: Context
) {
    companion object {
        const val COUNTER_CHANNEL_ID = "counter_channel"
        const val NOTIFICATION_ID = 1 // for updating current notification
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(counter: Int) {
        val notiDeeplink = context.resources.getString(R.string.notify_app_deeplink)
        val detailDeeplink = context.resources.getString(R.string.detail_app_deeplink)

        val counterIntent = Intent(
            Intent.ACTION_VIEW,
            (notiDeeplink /** + Screen.Notify.route) ?? */ ).toUri(),
            context,
            MainActivity::class.java
        )
        val pendingIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(counterIntent)
            getPendingIntent(11,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else
                    PendingIntent.FLAG_CANCEL_CURRENT
            )
        }

        val gotoDetailIntent = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(
                Intent(
                    Intent.ACTION_VIEW,
//                    ("$deeplinkForDetail${Screen.Detail.route}/${counter * 2}").toUri(),
                    ("$detailDeeplink${counter * 2}").toUri(),
                    context,
                    MainActivity::class.java
                )
            )
            getPendingIntent(55, PendingIntent.FLAG_IMMUTABLE)
        }

        val incrementIntent = PendingIntent.getBroadcast(
            context,
            2,
            Intent(context, CounterNotificationBroadcastReceiver::class.java),
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
            .addAction(
                R.drawable.baseline_rabbit_24,
                "goto 'detail' deeplink",
                gotoDetailIntent
            )
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

}