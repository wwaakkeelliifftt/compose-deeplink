import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose_deeplink.MainActivity
import com.example.compose_deeplink.proto_data_store.AppSettings
import com.example.compose_deeplink.proto_data_store.Language
import com.example.compose_deeplink.proto_data_store.Location
import com.example.compose_deeplink.proto_data_store.LocationViewModel
import com.example.compose_deeplink.proto_data_store.checkBackgroundLocation
import com.example.compose_deeplink.proto_data_store.checkLocationPermission
import com.example.compose_deeplink.proto_data_store.dataStore
import com.example.compose_deeplink.proto_data_store.locationCallback
import com.example.compose_deeplink.proto_data_store.locationRequest
import com.example.compose_deeplink.proto_data_store.requestLocationPermission
import com.example.compose_deeplink.proto_data_store.updateLanguage
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SerializeScreen(context: Context, provider: FusedLocationProviderClient) {
    val appSettings = context.dataStore.data
        .collectAsState(initial = AppSettings())
        .value

    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val activity = context as MainActivity

    var languageSelector by remember { mutableStateOf(appSettings.language) }

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = it) },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .background(Color.DarkGray),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0..2) {
                    val language = Language.values()[i]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(10.dp))
                            .clickable {
                                scope.launch {
                                    activity.updateLanguage(language)
                                }
                                languageSelector = language
                            }
                    ) {
                        RadioButton(
                            selected = language == appSettings.language,
                            onClick = {  }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = language.toString())
                        Spacer(modifier = Modifier.width(12.dp))

                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            PermissionScreen(activity = activity, provider = provider, scope = scope)
        }
    }

}

@Composable
fun PermissionScreen(activity: MainActivity, provider: FusedLocationProviderClient, scope: CoroutineScope) {

    val permAccessFine = ActivityCompat.checkSelfPermission(activity,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val permAccessCoarse = ActivityCompat.checkSelfPermission(activity,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val permAccessBackground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ActivityCompat.checkSelfPermission(activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else { true }

    var permissions by remember {
        mutableStateOf(
            listOf(
                Pair("ACCESS_FINE_LOCATION", permAccessFine),
                Pair("ACCESS_COARSE_LOCATION", permAccessCoarse),
                Pair("ACCESS_COARSE_LOCATION", permAccessBackground)
            )
        )
    }

    val vm = viewModel<LocationViewModel>(viewModelStoreOwner = activity)
    val loc = vm.l.collectAsState()
    val location by remember { loc }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(42.dp))
        Text(text = "Location permissions checkbox:", fontWeight = FontWeight.ExtraLight, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(12.dp))

        permissions.forEach { permission ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "-   ${permission.first}", fontWeight = FontWeight.Light)

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        tint = if (permission.second) Color.Green else Color.Transparent,
                        contentDescription = "done check"
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Button(
                        onClick = {
                            when (permission.first) {
                                "ACCESS_FINE_LOCATION" -> {
                                    activity.checkLocationPermission()
                                    Toast.makeText(activity, "ask FINE_LOCATION", Toast.LENGTH_SHORT).show()
                                }
                                "ACCESS_BACKGROUND_LOCATION" -> {
                                    activity.checkBackgroundLocation()
                                    Toast.makeText(activity, "ask BACKGROUND_LOCATION", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    activity.checkLocationPermission()
                                    Toast.makeText(activity, "ask COARSE_LOCATION", Toast.LENGTH_SHORT).show()
                                }
                            }
                                  },
                        enabled = !permission.second
                    ) {
                        Text(text = "get")
                    }
                }
            }
        }
        Button(
            onClick = { activity.checkLocationPermission() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(text = "request all permissions")
        }
        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier
            .fillMaxSize()
            .border(3.dp, Color.Cyan)
            .padding(12.dp)
        ) {

            val gps = vm.gpsEnabled.collectAsState()
            val gpsR = remember { gps }

            fun updateLocation() {
                activity.requestLocationPermission()
                try {
                    provider.requestLocationUpdates(
                        activity.locationRequest,
                        activity.locationCallback,
                        Looper.getMainLooper()
                    )
                    provider.lastLocation.addOnSuccessListener { point ->
                        point?.let {
                            vm.updateLocation(point.latitude, point.latitude)
                            Toast.makeText(activity, "lat: ${point.latitude}, lng: ${point.longitude}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    Toast.makeText(activity, "IllegalStateEx", Toast.LENGTH_SHORT).show()
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                    Toast.makeText(activity, "RuntimeEx", Toast.LENGTH_SHORT).show()
                }
            }

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Coordinates CURRENT:", fontWeight = FontWeight.ExtraLight, fontSize = 22.sp)
                Text(text = "Coordinates LAST:", fontWeight = FontWeight.ExtraLight, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "latitude - ${location.lat}", fontWeight = FontWeight.Light)
                Text(text = "latitude - ${location.lat}", fontWeight = FontWeight.Light)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "longitude - ${location.lng}", fontWeight = FontWeight.Light)
                Text(text = "longitude - ${location.lng}", fontWeight = FontWeight.Light)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "GPS enabled: ${gpsR.value}")


            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {

                var openDialog by remember { mutableStateOf(false) }
                if (openDialog) {
                    AlertDialog(
                        onDismissRequest = { openDialog = false },
                        title = { Text(text = "Attention") },
                        text = { 
                            Column {
                                Text(text = "Please, turn ON your gps and network") 
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "GPS status: ${vm.gpsEnabled}", fontWeight = FontWeight.Light)
                            } },
                        buttons = {
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = { openDialog = false }, modifier = Modifier.fillMaxWidth(0.7f)) {
                                    Text(text = "close")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                Button(onClick = {
                                    activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                }, modifier = Modifier.fillMaxWidth(0.7f)) {
                                    Text(text = "check")
                                }
                            }

                        }
                    )
                }
                Column {
                    Button(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onClick = { openDialog = true }
                    ) {
                        Text(text = "open check dialog")
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        onClick = { updateLocation() }
                    ) {
                        Text(text = "update location")
                    }
                }
            }

        }

    }
}
