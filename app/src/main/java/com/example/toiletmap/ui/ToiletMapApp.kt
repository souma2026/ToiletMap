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
import com.example.toiletmap.screen.account.AccountScreen
import com.example.toiletmap.screen.map.MapScreen
import com.example.toiletmap.ui.components.BottomNavigationBar
import com.example.toiletmap.ui.screen.AddToiletScreen
import org.maplibre.android.maps.MapView
import com.example.toiletmap.model.Toilet

@Composable
fun ToiletMapApp(

    mapView: MapView,

    selectedToilet: Toilet?,

    onDismissSelectedToilet: () -> Unit,

    onRequestCleaning: (Toilet) -> Unit,

    onMarkCleaned: (Toilet) -> Unit,

    onAddToilet: (Toilet) -> Unit
) {

    /*
     * 0 = マップ
     * 1 = アカウント
     * 2 = 追加
     */
    var selectedScreen by rememberSaveable {
        mutableIntStateOf(0)
    }

    /*
     * =====================================
     * トイレ追加画面の入力内容
     * =====================================
     */

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

    /*
     * true
     * → 地図上で新しいトイレの場所を選択中
     */
    var isSelectingLocation by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                selectedScreen = selectedScreen,

                onScreenSelected = { screen ->

                    /*
                     * マップ以外に移動した場合
                     * 場所選択モードを終了
                     */
                    if (screen != 0) {

                        isSelectingLocation = false

                        onDismissSelectedToilet()
                    }

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

                /*
                 * =====================================
                 * マップ
                 * =====================================
                 */
                0 -> {

                    MapScreen(

                        mapView = mapView,

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

                        /*
                         * 地図をタップして
                         * 新しいトイレの場所を選択
                         */
                        onLocationSelected = {
                                latitude,
                                longitude ->

                            selectedLatitude =
                                latitude

                            selectedLongitude =
                                longitude

                            isSelectingLocation =
                                false

                            // 追加画面へ戻る
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
                 * =====================================
                 * アカウント
                 * =====================================
                 */
                1 -> {

                    AccountScreen()
                }

                /*
                 * =====================================
                 * トイレ追加
                 * =====================================
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

                            toiletName = it
                        },

                        onCleanlinessChange = {

                            cleanliness = it
                        },

                        onCommentChange = {

                            comment = it
                        },

                        /*
                         * 「地図上で場所を選ぶ」
                         */
                        onSelectLocation = {

                            onDismissSelectedToilet()

                            isSelectingLocation =
                                true

                            // マップへ
                            selectedScreen =
                                0
                        },

                        /*
                         * 「このトイレを登録」
                         */
                        onAddToilet = {

                            val latitude =
                                selectedLatitude

                            val longitude =
                                selectedLongitude

                            if (
                                latitude != null &&
                                longitude != null
                            ) {

                                val toilet = Toilet(

                                    name =
                                        toiletName.trim(),

                                    latitude =
                                        latitude,

                                    longitude =
                                        longitude,

                                    cleanliness =
                                        cleanliness,

                                    comment =
                                        comment.trim()
                                )

                                /*
                                 * MainActivity
                                 * ↓
                                 * MapLibreMapController
                                 *
                                 * に送る
                                 */
                                onAddToilet(
                                    toilet
                                )

                                /*
                                 * 入力欄をリセット
                                 */
                                toiletName = ""

                                cleanliness = 3

                                comment = ""

                                selectedLatitude = null

                                selectedLongitude = null

                                /*
                                 * 登録後はマップへ
                                 */
                                selectedScreen = 0
                            }
                        }
                    )
                }
            }
        }
    }
}