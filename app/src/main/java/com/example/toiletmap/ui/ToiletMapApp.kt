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

    /*
     * MapLibre
     */
    mapView: MapView,


    /*
     * 全トイレ
     *
     * 検索機能で使用する
     */
    toilets: List<Toilet>,


    /*
     * 現在選択しているトイレ
     */
    selectedToilet: Toilet?,


    /*
     * 清掃待ちトイレ
     */
    uncleanedToilets: List<UncleanedToilet>,


    /*
     * 検索結果を選択したとき
     */
    onSearchToiletSelected: (Toilet) -> Unit,


    /*
     * 詳細画面を閉じる
     */
    onDismissSelectedToilet: () -> Unit,


    /*
     * 清掃依頼
     */
    onRequestCleaning: (Toilet) -> Unit,


    /*
     * 清掃完了
     */
    onMarkCleaned: (Toilet) -> Unit,


    /*
     * トイレ追加
     */
    onAddToilet: (Toilet) -> Unit

) {


    /*
     * =====================================
     * 現在の画面
     * =====================================
     *
     * 0 = 未清掃一覧
     * 1 = 状態更新
     * 2 = Map
     * 3 = トイレ追加
     * 4 = アカウント
     */
    var selectedScreen by
    rememberSaveable {

        mutableIntStateOf(
            2
        )
    }


    /*
     * =====================================
     * トイレ追加画面用
     * =====================================
     */
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

        mutableStateOf<Double?>(
            null
        )
    }


    var selectedLongitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    /*
     * 地図から場所を選択中か
     */
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

                onScreenSelected = { screen ->


                    /*
                     * Map以外へ移動した場合
                     */
                    if (
                        screen != 2
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

    ) { innerPadding ->


        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        innerPadding
                    )

        ) {


            when (selectedScreen) {


                /*
                 * =====================================
                 * 未清掃一覧
                 * =====================================
                 */
                0 -> {

                    ListOfUncleanedScreen(

                        toilets =
                            uncleanedToilets
                    )
                }


                /*
                 * =====================================
                 * 状態更新
                 * =====================================
                 */
                1 -> {

                    StatusPlaceholderScreen()
                }


                /*
                 * =====================================
                 * Map
                 * =====================================
                 */
                2 -> {

                    MapScreen(

                        mapView =
                            mapView,


                        /*
                         * 検索対象
                         */
                        toilets =
                            toilets,


                        /*
                         * 検索結果選択
                         */
                        onSearchToiletSelected = { toilet ->

                            onSearchToiletSelected(
                                toilet
                            )
                        },


                        /*
                         * 場所選択中か
                         */
                        isSelectingLocation =
                            isSelectingLocation,


                        /*
                         * 現在選択しているトイレ
                         */
                        selectedToilet =
                            selectedToilet,


                        /*
                         * 詳細を閉じる
                         */
                        onDismissSelectedToilet =
                            onDismissSelectedToilet,


                        /*
                         * 清掃依頼
                         */
                        onRequestCleaning =
                            onRequestCleaning,


                        /*
                         * 清掃完了
                         */
                        onMarkCleaned =
                            onMarkCleaned,


                        /*
                         * =====================================
                         * トイレ追加位置を地図で選択
                         * =====================================
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


                            /*
                             * トイレ追加画面へ戻る
                             */
                            selectedScreen =
                                3
                        },


                        /*
                         * 場所選択キャンセル
                         */
                        onCancelLocationSelection = {

                            isSelectingLocation =
                                false

                            selectedScreen =
                                3
                        }
                    )
                }


                /*
                 * =====================================
                 * トイレ追加
                 * =====================================
                 */
                3 -> {

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


                        /*
                         * =====================================
                         * 地図から位置を選択
                         * =====================================
                         */
                        onSelectLocation = {

                            onDismissSelectedToilet()


                            isSelectingLocation =
                                true


                            /*
                             * Mapへ移動
                             */
                            selectedScreen =
                                2
                        },


                        /*
                         * =====================================
                         * 登録
                         * =====================================
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


                                val toilet =

                                    Toilet(

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
                                 * MainActivityへ渡す
                                 */
                                onAddToilet(
                                    toilet
                                )


                                /*
                                 * 入力内容リセット
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
                                 * Mapへ戻る
                                 */
                                selectedScreen =
                                    2
                            }
                        }
                    )
                }


                /*
                 * =====================================
                 * アカウント
                 * =====================================
                 */
                4 -> {

                    AccountScreen()
                }
            }
        }
    }
}


/*
 * =====================================
 * 仮画面
 * =====================================
 */
@Composable
private fun StatusPlaceholderScreen() {

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center

    ) {

        Text(

            text =
                "トイレのレビュー・状態更新\n\nこの画面は後で実装します",

            textAlign =
                TextAlign.Center,

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )
    }
}