package com.example.toiletmap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.ui.components.BottomNavigationBar
import com.example.toiletmap.ui.screen.AddToiletScreen
import org.maplibre.android.maps.MapView

@Composable
fun ToiletMapApp(
    mapView: MapView
) {

    /*
     * 0 = マップ
     * 1 = アカウント
     * 2 = 追加
     */
    var selectedScreen by rememberSaveable {
        mutableIntStateOf(0)
    }


    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                selectedScreen = selectedScreen,

                onScreenSelected = { screen ->

                    selectedScreen = screen
                }
            )
        }

    ) { innerPadding ->


        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            when (selectedScreen) {

                // マップ
                0 -> {
                    MapScreen(
                        mapView = mapView
                    )
                }


                // アカウント
                1 -> {
                    AccountScreen()
                }


                // トイレ追加
                2 -> {
                    AddToiletScreen()
                }
            }
        }
    }
}