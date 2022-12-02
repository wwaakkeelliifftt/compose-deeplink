package com.example.compose_deeplink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose_deeplink.notify.Counter
import com.example.compose_deeplink.notify.CounterNotificationService
import com.example.compose_deeplink.notify.NotificationScreen
import com.example.compose_deeplink.ui.theme.ComposedeeplinkTheme

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail")
    object Notify : Screen("notification")
}

object Arg {
    const val COUNTER = "counter"
    const val NAME = "name"
}


class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deeplink = "https://" + resources.getString(R.string.app_deeplink) + "/"

        val notifyDeeplink = resources.getString(R.string.notify_app_deeplink)
        val detailDeeplink = resources.getString(R.string.detail_app_deeplink)

        val notificationService = CounterNotificationService(applicationContext)

        setContent {
            ComposedeeplinkTheme {
                var counter by remember { mutableStateOf(0) }
                Counter.val_ld.observe(this) {
                    counter = it
                }

                navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Home.route) {
                    composable(route = Screen.Home.route) {
                        HomeScreen(navController)
                    }
                    composable(
                        route = Screen.Detail.route,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = "$detailDeeplink/{counter}" // Arg.COUNTER == counter
                                action = Intent.ACTION_VIEW
                            }),
                        arguments = listOf(
                            navArgument("path") {
                                type = NavType.StringType
                                defaultValue = "DEFAULT"
                            },
                            navArgument(Arg.COUNTER) {
                                type = NavType.IntType
                                defaultValue = -44
                            }
                        )
                    ) { navBackStackEntry ->
                        val pathFrom = navBackStackEntry.arguments?.getString(Arg.NAME) ?: "empty_nav_back_stack"
                        val cnt = navBackStackEntry.arguments?.getInt(Arg.COUNTER) ?: (counter * 2)
                        DetailScreen(counter = cnt, screenName = pathFrom)
                    }
                    composable(
                        route = Screen.Notify.route,
                        deepLinks = listOf(
                            navDeepLink {
                                uriPattern = notifyDeeplink
                                action = Intent.ACTION_VIEW
                            })
                    ) {
                        NotificationScreen(service = notificationService, cntx = LocalContext.current, counter)
                    }
                }

            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Toast.makeText(applicationContext, "HANDLE INTENT", Toast.LENGTH_SHORT).show()
    }

}

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                navController.navigate(Screen.Detail.route)
            }) {
                Text(text = "To detail screen")
            }
            Button(onClick = {
                navController.navigate(Screen.Notify.route)
            }) {
                Text(text = "GoTo Notification Screen")
            }
        }

    }
}

@Composable
fun DetailScreen(screenName: String, counter: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(text = "CURRENT SCREED: $screenName")
            Row {
                Text(text = "The current count is: ")
                Text(text = "$counter", fontWeight = FontWeight.SemiBold)
            }
        }

    }
}
