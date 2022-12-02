package com.example.compose_deeplink.notify

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationScreen(service: CounterNotificationService, cntx: Context, counter: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Button(
                onClick = {
                    service.showNotification(counter)
                }) {
                Text(text = "Run counter")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "current count: $counter", fontSize = 16.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                val pi = PendingIntent.getBroadcast(
                    cntx,
                    2,
                    Intent(cntx, CounterNotificationBroadcastReceiver::class.java).apply {
                        putExtra(Counter.FLAG_RESET, 0)
                        Toast.makeText(cntx, "FLAG_RESET", Toast.LENGTH_SHORT).show()
                    },
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_MUTABLE else 0
                )
                pi.send()
//                counter = 0
                Counter.reset()
            }) {
                Text(text = "RESET COUNTER")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                service.showNotification(counter + 1)
                Counter.increment()
            }) {
                Text(text = "++1")
            }

        }

    }
}