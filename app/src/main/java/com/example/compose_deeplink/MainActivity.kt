package com.example.compose_deeplink

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose_deeplink.notify.Counter
import com.example.compose_deeplink.notify.CounterNotificationBroadcastReceiver
import com.example.compose_deeplink.notify.CounterNotificationService
import com.example.compose_deeplink.ui.theme.ComposedeeplinkTheme

object Route {
    const val HOME = "home"
    const val DETAIL = "detail"

    const val NOTIFY = "notification"
}

object Arg {
    const val ID = "id"
    const val COUNTER = "counter"
}


class MainActivity : ComponentActivity() {

    lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deeplink = resources.getString(R.string.app_deeplink)
        val notificationService = CounterNotificationService(applicationContext)

        setContent {
            ComposedeeplinkTheme {
                navController = rememberNavController()
                NavHost(navController = navController, startDestination = Route.HOME) {
                    composable(route = Route.HOME) {
                        HomeScreen(navController)
                    }
                    composable(
                        route = Route.DETAIL,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "https://$deeplink/{${Arg.ID}}"
                                action = Intent.ACTION_VIEW
                            }
                        ),
                        arguments = listOf(
                            navArgument(Arg.ID) {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { navBackStackEntry ->
                        val id = navBackStackEntry.arguments?.getInt(Arg.ID)
                        DetailScreen(id = id ?: -666)
                    }
                    composable(
                        route = Route.NOTIFY,
                        arguments = listOf(
                            navArgument(Arg.COUNTER) {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        ),
                        deepLinks = listOf(navDeepLink {
                            uriPattern = "https://$deeplink/{${Route.NOTIFY}}"
                            action = Intent.ACTION_VIEW
                        })
                    ) {
                        NotificationScreen(service = notificationService, cntx = LocalContext.current)
                    }
                }

            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                navController.navigate(Route.DETAIL)
            }) {
                Text(text = "To detail screen")
            }
            Button(onClick = {
                navController.navigate(Route.NOTIFY)
            }) {
                Text(text = "GoTo Notification Screen")
            }
        }

    }
}

@Composable
fun DetailScreen(id: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Row {
            Text(text = "The ID is: ")
            Text(text = "$id", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NotificationScreen(service: CounterNotificationService, cntx: Context) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Button(
                onClick = {
                    service.showNotification(Counter.value)
            }) {
                Text(text = "Run counter")
            }
            Text(text = "current count: ${Counter.value}")
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
            }) {
                Text(text = "RESET COUNTER")
            }

        }

    }
}