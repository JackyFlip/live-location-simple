package fr.ubalia.livelocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import fr.ubalia.livelocation.ui.theme.LiveLocationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val LOCATION_TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkForLocationPermission()

        setContent {
            LiveLocationTheme {

                val scope = rememberCoroutineScope()
                fusedLocationClient = remember {
                    LocationServices.getFusedLocationProviderClient(this)
                }

                var lat by remember {
                    mutableDoubleStateOf(0.0)
                }

                var lon by remember {
                    mutableDoubleStateOf(0.0)
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GetLocation(
                        lat = lat,
                        lon = lon,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val result = fusedLocationClient.lastLocation.await()
                                result?.let {
                                    lat = it.latitude
                                    lon = it.longitude
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkForLocationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions
                    .getOrDefault(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        false
                    ) -> {
                    // Accès à la localisation fine
                    Log.d(LOCATION_TAG, "Fine location success")
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location ->
                            Log.d(LOCATION_TAG, location.toString())
                        }
                }
                permissions
                    .getOrDefault(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        false
                    ) -> {
                    // Accès à la localisation partielle
                    Log.d(LOCATION_TAG, "Coarse location success")
                } else -> {
                // Pas d'accès à la localisation
                Log.d(LOCATION_TAG, "Location failure")
            }
            }

        }

        if (isLocationPermissionGranted()) {
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }


}

@SuppressLint("MissingPermission")
@Composable
fun GetLocation(lat: Double, lon: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onClick) {
            Text(text = "Get location")
        }
        Text(text = "lat: $lat")
        Text(text = "lon: $lon")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    LiveLocationTheme {
        // GetLocation(onClick = {}, modifier = Modifier.fillMaxSize())
    }
}