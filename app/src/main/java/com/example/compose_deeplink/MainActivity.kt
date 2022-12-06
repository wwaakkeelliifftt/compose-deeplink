package com.example.compose_deeplink

import SerializeScreen
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.compose_deeplink.proto_data_store.Location
import com.example.compose_deeplink.proto_data_store.LocationViewModel
import com.example.compose_deeplink.proto_data_store.MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
import com.example.compose_deeplink.proto_data_store.MY_PERMISSIONS_REQUEST_LOCATION
import com.example.compose_deeplink.proto_data_store.checkBackgroundLocation
import com.example.compose_deeplink.proto_data_store.locationCallback
import com.example.compose_deeplink.proto_data_store.locationRequest
import com.example.compose_deeplink.proto_data_store.requestLocationPermission
import com.example.compose_deeplink.ui.theme.ComposedeeplinkTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail")
    object Notify : Screen("notification")
    object Serialize : Screen("serialize")
}

object Arg {
    const val COUNTER = "counter"
    const val NAME = "name"
}


class MainActivity : ComponentActivity() {

    private var fusedLocationProvider: FusedLocationProviderClient? = null
    lateinit var locationManager: LocationManager
    lateinit var vm: LocationViewModel

    private lateinit var navController: NavHostController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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
                vm = viewModel()
                val location = vm.location.observeAsState()

                navController = rememberNavController()
                NavHost(navController = navController, startDestination = Screen.Home.route) {
                    composable(route = Screen.Home.route) {
                        val l by remember { location }
                        HomeScreen(navController, l ?: Location(99.11, 88.22), LocalContext.current)
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
                    composable(route = Screen.Serialize.route) {
                        SerializeScreen(
                            context = LocalContext.current,
                            provider = fusedLocationProvider!!
                        )
                    }
                }

            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val gps = LocationManagerCompat.isLocationEnabled(locationManager)
        Toast.makeText(this, "GPS=$gps", Toast.LENGTH_LONG).show()
        vm.checkLocationStatus(gps)
        return gps
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Toast.makeText(applicationContext, "HANDLE INTENT", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProvider?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProvider?.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProvider = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                Toast.makeText(this, "MY_PERMISSIONS_REQUEST_LOCATION", Toast.LENGTH_SHORT).show()
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                        fusedLocationProvider?.lastLocation?.addOnSuccessListener(this) { point ->
                            point?.let {
                                vm.updateLocation(point.latitude, point.longitude)
                                Toast.makeText(this, "Catch. LAT:${it.latitude} LNG: ${it.longitude}", Toast.LENGTH_LONG).show()
                            }
                        }
                        // Now check background location
                        checkBackgroundLocation()
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                    // Check if we are in a state where the user has denied the permission and
                    // selected Don't ask again
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", this.packageName, null),
                            ),
                        )
                    }
                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                Toast.makeText(this, "MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION", Toast.LENGTH_SHORT).show()
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )

                        Toast.makeText(
                            this,
                            "Granted Background Location Permission",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return

            }
        }
    }

}

@Composable
fun HomeScreen(navController: NavController, location: Location, cntx: Context) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(12.dp), contentAlignment = Alignment.Center) {
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
            Button(onClick = {
                navController.navigate(Screen.Serialize.route)
            }) {
                Text(text = "Settings/serialize")
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color.Magenta, RoundedCornerShape(16.dp)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Current lat/lng: ${location.lat} :: ${location.lng}", fontWeight = FontWeight.Normal)
                Button(
                    onClick = { (cntx as? MainActivity)?.requestLocationPermission() }
                ) {
                    Text(text = "update")
                }
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
