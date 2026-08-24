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
    mapView: MapView,
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
     * 追加画面で入力している情報
     *
     * 地図へ移動して場所を選んでも
     * 入力した内容が消えないように
     * ここで情報を持っておく
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
     * → 地図をタップしてトイレの場所を選択している状態
     *
     * false
     * → 普通に地図を見ている状態
     */
    var isSelectingLocation by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = selectedScreen,
                onScreenSelected = { screen ->

                    // 場所以外のタブに移動した場合は
                    // 場所選択モードを終了する
                    if (screen != 0) {
                        isSelectingLocation = false
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
                 * マップ画面
                 * =====================================
                 */
                0 -> {

                    MapScreen(
                        mapView = mapView,
                        isSelectingLocation = isSelectingLocation,

                        /*
                         * 地図上をタップしたとき
                         *
                         * タップされた場所の
                         * 緯度・経度を受け取る
                         */
                        onLocationSelected = { latitude, longitude ->

                            selectedLatitude = latitude
                            selectedLongitude = longitude

                            // 場所選択を終了
                            isSelectingLocation = false

                            // 追加画面へ戻る
                            selectedScreen = 2
                        },

                        /*
                         * 場所選択をキャンセル
                         */
                        onCancelLocationSelection = {

                            isSelectingLocation = false

                            // 追加画面へ戻る
                            selectedScreen = 2
                        }
                    )
                }

                /*
                 * =====================================
                 * アカウント画面
                 * =====================================
                 */
                1 -> {

                    AccountScreen()
                }

                /*
                 * =====================================
                 * トイレ追加画面
                 * =====================================
                 */
                2 -> {

                    AddToiletScreen(

                        toiletName = toiletName,

                        cleanliness = cleanliness,

                        comment = comment,

                        latitude = selectedLatitude,

                        longitude = selectedLongitude,

                        /*
                         * トイレ名が変更された
                         */
                        onToiletNameChange = {
                            toiletName = it
                        },

                        /*
                         * 清潔度が変更された
                         */
                        onCleanlinessChange = {
                            cleanliness = it
                        },

                        /*
                         * コメントが変更された
                         */
                        onCommentChange = {
                            comment = it
                        },

                        /*
                         * 「地図上で場所を選ぶ」
                         * が押された
                         */
                        onSelectLocation = {

                            isSelectingLocation = true

                            // 地図画面へ移動
                            selectedScreen = 0
                        },

                        /*
                         * 「このトイレを登録」
                         * が押された
                         */
                        onAddToilet = {

                            val latitude = selectedLatitude
                            val longitude = selectedLongitude

                            /*
                             * 場所がちゃんと選択されている場合
                             */
                            if (
                                latitude != null &&
                                longitude != null
                            ) {

                                /*
                                 * Toiletデータを作る
                                 */
                                val toilet = Toilet(

                                    name = toiletName.trim(),

                                    latitude = latitude,

                                    longitude = longitude,

                                    cleanliness = cleanliness,

                                    comment = comment.trim()
                                )

                                /*
                                 * MainActivityへ渡す
                                 *
                                 * 最終的にMapLibreMapControllerへ行き、
                                 * 地図上にピンが追加される
                                 */
                                onAddToilet(toilet)

                                /*
                                 * =====================================
                                 * 登録後に入力欄を初期化
                                 * =====================================
                                 */

                                toiletName = ""

                                cleanliness = 3

                                comment = ""

                                selectedLatitude = null

                                selectedLongitude = null

                                /*
                                 * 登録したピンを見るために
                                 * マップ画面へ戻る
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