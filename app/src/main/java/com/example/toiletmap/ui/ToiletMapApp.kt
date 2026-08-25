package com.example.toiletmap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.add.AddToiletScreen
import com.example.toiletmap.screen.listofuncleaned.ListOfUncleanedScreen
import com.example.toiletmap.screen.listofuncleaned.UncleanedToilet
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.ui.components.BottomNavigationBar
import org.maplibre.android.maps.MapView

@Composable
fun ToiletMapApp(
    mapView: MapView,
    toilets: List<Toilet>,
    selectedToilet: Toilet?,
    uncleanedToilets: List<UncleanedToilet>,
    onShowUncleanedToiletOnMap: (UncleanedToilet) -> Unit,
    onSearchToiletSelected: (Toilet) -> Unit,
    onDismissSelectedToilet: () -> Unit,
    onRequestCleaning: (Toilet) -> Unit,
    onMarkCleaned: (Toilet) -> Unit,
    onAddToilet: (Toilet) -> Unit,
    onCurrentLocationRequested: () -> Unit
) {
    /*
     * 0 = 未清掃一覧
     * 1 = レビュー・状態更新
     * 2 = Map
     * 3 = トイレ追加
     * 4 = アカウント
     */
    var selectedScreen by rememberSaveable {
        mutableIntStateOf(2)
    }

    var toiletName by rememberSaveable {
        mutableStateOf("")
    }

    var cleanliness by rememberSaveable {
        mutableIntStateOf(3)
    }

    var comment by rememberSaveable {
        mutableStateOf("")
    }

    var selectedLatitude by rememberSaveable {
        mutableStateOf<Double?>(null)
    }

    var selectedLongitude by rememberSaveable {
        mutableStateOf<Double?>(null)
    }

    var isSelectingLocation by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { screen ->
                    if (screen != 2) {
                        isSelectingLocation = false
                        onDismissSelectedToilet()
                    }

                    selectedScreen = screen
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
        ) {
            when (selectedScreen) {
                /* 未清掃一覧 */
                0 -> {
                    ListOfUncleanedScreen(
                        toilets = uncleanedToilets,
                        onShowOnMap = { uncleanedToilet ->
                            isSelectingLocation = false

                            /* 古い選択状態を消してから対象を選択する。 */
                            onDismissSelectedToilet()

                            /* Map画面へ戻す。 */
                            selectedScreen = 2

                            /* MainActivity側で対象を選択して地図を移動する。 */
                            onShowUncleanedToiletOnMap(uncleanedToilet)
                        }
                    )
                }

                /* レビュー・状態更新。現在は仮画面。 */
                1 -> {
                    StatusPlaceholderScreen()
                }

                /* Map */
                2 -> {
                    MapScreen(
                        mapView = mapView,
                        toilets = toilets,
                        onSearchToiletSelected = onSearchToiletSelected,
                        onCurrentLocationClick = onCurrentLocationRequested,
                        isSelectingLocation = isSelectingLocation,
                        selectedToilet = selectedToilet,
                        onDismissSelectedToilet = onDismissSelectedToilet,
                        onRequestCleaning = onRequestCleaning,
                        onMarkCleaned = onMarkCleaned,
                        onLocationSelected = { latitude, longitude ->
                            selectedLatitude = latitude
                            selectedLongitude = longitude
                            isSelectingLocation = false
                            selectedScreen = 3
                        },
                        onCancelLocationSelection = {
                            isSelectingLocation = false
                            selectedScreen = 3
                        }
                    )
                }

                /* トイレ追加 */
                3 -> {
                    AddToiletScreen(
                        toiletName = toiletName,
                        cleanliness = cleanliness,
                        comment = comment,
                        latitude = selectedLatitude,
                        longitude = selectedLongitude,
                        onToiletNameChange = { value ->
                            toiletName = value
                        },
                        onCleanlinessChange = { value ->
                            cleanliness = value
                        },
                        onCommentChange = { value ->
                            comment = value
                        },
                        onSelectLocation = {
                            onDismissSelectedToilet()
                            isSelectingLocation = true
                            selectedScreen = 2
                        },
                        onAddToilet = {
                            val latitude = selectedLatitude
                            val longitude = selectedLongitude

                            if (latitude != null && longitude != null) {
                                val toilet =
                                    Toilet(
                                        name = toiletName.trim(),
                                        latitude = latitude,
                                        longitude = longitude,
                                        cleanliness = cleanliness,
                                        comment = comment.trim()
                                    )

                                onAddToilet(toilet)

                                toiletName = ""
                                cleanliness = 3
                                comment = ""
                                selectedLatitude = null
                                selectedLongitude = null
                                selectedScreen = 2
                            }
                        }
                    )
                }

                /* アカウント */
                4 -> {
                    AccountScreen()
                }
            }
        }
    }
}

@Composable
private fun StatusPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "トイレのレビュー・状態更新\n\nこの画面は後で実装します",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
    }
}