package com.example.toiletmap.screen.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.maps.MapView

@Composable
fun MapScreen(
    mapView: MapView
) {

    AndroidView(
        factory = {
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )
}