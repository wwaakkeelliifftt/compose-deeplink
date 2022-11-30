package com.example.compose_deeplink

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose_deeplink.ui.theme.ComposedeeplinkTheme

object Route {
    const val HOME = "home"
    const val DETAIL = "detail"
}

object Arg {
    const val ID = "id"
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposedeeplinkTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = Route.HOME) {
                    composable(route = Route.HOME) {
                        HomeScreen(navController)
                    }
                    composable(
                        route = Route.DETAIL,
                        deepLinks = listOf(
                            navDeepLink {
                                val deeplink = resources.getString(R.string.app_deeplink)
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
                }

            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = {
            navController.navigate(Route.DETAIL)
        }) {
            Text(text = "To detail screen")
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
