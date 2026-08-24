package com.example.toiletmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.toiletmap.map.MapLibreMapController
import com.example.toiletmap.ui.ToiletMapApp
import com.example.toiletmap.ui.theme.ToiletMapTheme

class MainActivity : ComponentActivity() {

    private lateinit var mapController: MapLibreMapController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MapLibreの準備
        mapController = MapLibreMapController(
            activity = this,
            savedInstanceState = savedInstanceState
        )

        // Compose画面を表示
        setContent {
            ToiletMapTheme {
                ToiletMapApp(
                    mapView = mapController.mapView
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapController.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapController.onLowMemory()
    }
}
