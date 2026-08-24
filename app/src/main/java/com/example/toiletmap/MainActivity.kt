package com.example.toiletmap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toiletmap.model.Toilet
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.ui.components.BottomNavigationBar
import com.example.toiletmap.ui.screen.AddToiletScreen
import org.maplibre.android.maps.MapView

@Composable
fun ToiletMapApp(

    mapView:
    MapView,

    selectedToilet:
    Toilet?,

    onDismissSelectedToilet:
        () -> Unit,

    onRequestCleaning:
        (Toilet) -> Unit,

    onMarkCleaned:
        (Toilet) -> Unit,

    onAddToilet:
        (Toilet) -> Unit

) {

    /*
     * 0 = マップ
     * 1 = アカウント
     * 2 = 追加
     */
    var selectedScreen by

    rememberSaveable {

        mutableIntStateOf(
            0
        )
    }

    var toiletName by

    rememberSaveable {

        mutableStateOf(
            ""
        )
    }

    var cleanliness by

    rememberSaveable {

        mutableIntStateOf(
            3
        )
    }

    var comment by

    rememberSaveable {

        mutableStateOf(
            ""
        )
    }

    var selectedLatitude by

    rememberSaveable {

        mutableStateOf<
                Double?
                >(
            null
        )
    }

    var selectedLongitude by

    rememberSaveable {

        mutableStateOf<
                Double?
                >(
            null
        )
    }

    var isSelectingLocation by

    rememberSaveable {

        mutableStateOf(
            false
        )
    }

    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                selectedScreen =
                    selectedScreen,

                onScreenSelected = {
                        screen ->

                    if (
                        screen != 0
                    ) {

                        isSelectingLocation =
                            false

                        onDismissSelectedToilet()
                    }

                    selectedScreen =
                        screen
                }
            )
        }

    ) {
            innerPadding ->

        Box(

            modifier =
                Modifier
                    .fillMaxSize()

                    .padding(
                        innerPadding
                    )

        ) {

            when (
                selectedScreen
            ) {

                /*
                 * =================================
                 * マップ
                 * =================================
                 */
                0 -> {

                    MapScreen(

                        mapView =
                            mapView,

                        isSelectingLocation =
                            isSelectingLocation,

                        selectedToilet =
                            selectedToilet,

                        onDismissSelectedToilet =
                            onDismissSelectedToilet,

                        onRequestCleaning =
                            onRequestCleaning,

                        onMarkCleaned =
                            onMarkCleaned,

                        onLocationSelected = {
                                latitude,
                                longitude ->

                            selectedLatitude =
                                latitude

                            selectedLongitude =
                                longitude

                            isSelectingLocation =
                                false

                            selectedScreen =
                                2
                        },

                        onCancelLocationSelection = {

                            isSelectingLocation =
                                false

                            selectedScreen =
                                2
                        }
                    )
                }

                /*
                 * =================================
                 * アカウント
                 * =================================
                 */
                1 -> {

                    AccountScreen()
                }

                /*
                 * =================================
                 * トイレ追加
                 * =================================
                 */
                2 -> {

                    AddToiletScreen(

                        toiletName =
                            toiletName,

                        cleanliness =
                            cleanliness,

                        comment =
                            comment,

                        latitude =
                            selectedLatitude,

                        longitude =
                            selectedLongitude,

                        onToiletNameChange = {

                            toiletName =
                                it
                        },

                        onCleanlinessChange = {

                            cleanliness =
                                it
                        },

                        onCommentChange = {

                            comment =
                                it
                        },

                        onSelectLocation = {

                            onDismissSelectedToilet()

                            isSelectingLocation =
                                true

                            selectedScreen =
                                0
                        },

                        onAddToilet = {

                            val latitude =
                                selectedLatitude

                            val longitude =
                                selectedLongitude

                            if (
                                latitude != null &&
                                longitude != null
                            ) {

                                val toilet =

                                    Toilet(

                                        name =
                                            toiletName
                                                .trim(),

                                        latitude =
                                            latitude,

                                        longitude =
                                            longitude,

                                        cleanliness =
                                            cleanliness,

                                        comment =
                                            comment
                                                .trim()
                                    )

                                onAddToilet(
                                    toilet
                                )

                                /*
                                 * 入力欄初期化
                                 */
                                toiletName =
                                    ""

                                cleanliness =
                                    3

                                comment =
                                    ""

                                selectedLatitude =
                                    null

                                selectedLongitude =
                                    null

                                /*
                                 * マップへ戻る
                                 */
                                selectedScreen =
                                    0
                            }
                        }
                    )
                }
            }
        }
    }
}