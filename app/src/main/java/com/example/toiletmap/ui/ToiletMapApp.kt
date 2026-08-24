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

    mapView:
    MapView,


    /*
     * 現在選択されているトイレ
     */
    selectedToilet:
    Toilet?,


    /*
     * 清掃待ちトイレ
     */
    uncleanedToilets:
    List<UncleanedToilet>,


    /*
     * トイレ詳細を閉じる
     */
    onDismissSelectedToilet:
        () -> Unit,


    /*
     * 清掃依頼
     */
    onRequestCleaning:
        (Toilet) -> Unit,


    /*
     * 清掃完了
     */
    onMarkCleaned:
        (Toilet) -> Unit,


    /*
     * トイレ追加
     */
    onAddToilet:
        (Toilet) -> Unit

) {


    /*
     * =====================================
     * 画面番号
     * =====================================
     *
     * 0 = 未清掃
     *
     * 1 = レビュー・状態更新
     *
     * 2 = Map
     *
     * 3 = トイレ追加
     *
     * 4 = アカウント
     *
     * =====================================
     */


    /*
     * =====================================
     * 初期画面
     *
     * 2 = Map
     * =====================================
     */
    var selectedScreen by
    rememberSaveable {

        mutableIntStateOf(
            2
        )
    }


    /*
     * =====================================
     * トイレ追加画面
     * 入力データ
     * =====================================
     */


    /*
     * トイレ名
     */
    var toiletName by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    /*
     * 清潔度
     */
    var cleanliness by
    rememberSaveable {

        mutableIntStateOf(
            3
        )
    }


    /*
     * コメント
     */
    var comment by
    rememberSaveable {

        mutableStateOf(
            ""
        )
    }


    /*
     * 地図で選択した緯度
     */
    var selectedLatitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    /*
     * 地図で選択した経度
     */
    var selectedLongitude by
    rememberSaveable {

        mutableStateOf<Double?>(
            null
        )
    }


    /*
     * =====================================
     * 地図で
     * トイレ追加場所を選択中か
     * =====================================
     */
    var isSelectingLocation by
    rememberSaveable {

        mutableStateOf(
            false
        )
    }



    var searchText by
    rememberSaveable {
        mutableStateOf("")
    }

    /*
     * =====================================
     * Scaffold
     * =====================================
     */
    Scaffold(

        bottomBar = {

            BottomNavigationBar(

                selectedScreen =
                    selectedScreen,

                onScreenSelected = {
                        screen ->


                    /*
                     * =====================================
                     * Map以外へ移動する場合
                     *
                     * 2 = Map
                     * =====================================
                     */
                    if (
                        screen != 2
                    ) {


                        /*
                         * 場所選択終了
                         */
                        isSelectingLocation =
                            false


                        /*
                         * トイレ詳細を閉じる
                         */
                        onDismissSelectedToilet()
                    }


                    /*
                     * =====================================
                     * 画面変更
                     * =====================================
                     */
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


            /*
             * =====================================
             * 画面切り替え
             * =====================================
             */
            when (
                selectedScreen
            ) {


                /*
                 * =====================================
                 * 0
                 *
                 * 未清掃
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
                 * 1
                 *
                 * レビュー・状態更新
                 *
                 * 今は仮画面
                 * =====================================
                 */
                1 -> {

                    ReviewStatusPlaceholderScreen()
                }


                /*
                 * =====================================
                 * 2
                 *
                 * Map
                 * =====================================
                 */
                2 -> {

                    MapScreen(

                        mapView =
                            mapView,

                        searchText = searchText,

                        onSearchTextChange = {
                            searchText = it
                        },


                        /*
                         * 場所選択状態
                         */
                        isSelectingLocation =
                            isSelectingLocation,


                        /*
                         * 選択中トイレ
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
                         * 地図をタップして
                         * トイレ追加位置を選択
                         * =====================================
                         */
                        onLocationSelected = {
                                latitude,
                                longitude ->


                            /*
                             * 緯度保存
                             */
                            selectedLatitude =
                                latitude


                            /*
                             * 経度保存
                             */
                            selectedLongitude =
                                longitude


                            /*
                             * 場所選択終了
                             */
                            isSelectingLocation =
                                false


                            /*
                             * =====================================
                             * 追加画面へ戻る
                             *
                             * 3 = 追加
                             * =====================================
                             */
                            selectedScreen =
                                3
                        },


                        /*
                         * =====================================
                         * 場所選択キャンセル
                         * =====================================
                         */
                        onCancelLocationSelection = {


                            isSelectingLocation =
                                false


                            /*
                             * =====================================
                             * 追加画面へ戻る
                             *
                             * 3 = 追加
                             * =====================================
                             */
                            selectedScreen =
                                3
                        }
                    )
                }


                /*
                 * =====================================
                 * 3
                 *
                 * トイレ追加
                 * =====================================
                 */
                3 -> {

                    AddToiletScreen(


                        /*
                         * トイレ名
                         */
                        toiletName =
                            toiletName,


                        /*
                         * 清潔度
                         */
                        cleanliness =
                            cleanliness,


                        /*
                         * コメント
                         */
                        comment =
                            comment,


                        /*
                         * 緯度
                         */
                        latitude =
                            selectedLatitude,


                        /*
                         * 経度
                         */
                        longitude =
                            selectedLongitude,


                        /*
                         * =====================================
                         * トイレ名変更
                         * =====================================
                         */
                        onToiletNameChange = {

                            toiletName =
                                it
                        },


                        /*
                         * =====================================
                         * 清潔度変更
                         * =====================================
                         */
                        onCleanlinessChange = {

                            cleanliness =
                                it
                        },


                        /*
                         * =====================================
                         * コメント変更
                         * =====================================
                         */
                        onCommentChange = {

                            comment =
                                it
                        },


                        /*
                         * =====================================
                         * 地図上で場所を選択
                         * =====================================
                         */
                        onSelectLocation = {


                            /*
                             * トイレ詳細を閉じる
                             */
                            onDismissSelectedToilet()


                            /*
                             * 場所選択開始
                             */
                            isSelectingLocation =
                                true


                            /*
                             * =====================================
                             * Mapへ移動
                             *
                             * 2 = Map
                             * =====================================
                             */
                            selectedScreen =
                                2
                        },


                        /*
                         * =====================================
                         * トイレ登録
                         * =====================================
                         */
                        onAddToilet = {


                            val latitude =
                                selectedLatitude


                            val longitude =
                                selectedLongitude


                            /*
                             * =====================================
                             * 緯度経度が
                             * 選択されている場合
                             * =====================================
                             */
                            if (
                                latitude != null &&
                                longitude != null
                            ) {


                                /*
                                 * =====================================
                                 * Toilet作成
                                 * =====================================
                                 */
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


                                /*
                                 * =====================================
                                 * MainActivityへ渡す
                                 * =====================================
                                 *
                                 * MainActivity
                                 *
                                 * ↓
                                 *
                                 * ToiletViewModel
                                 *
                                 * ↓
                                 *
                                 * ToiletRepository
                                 * =====================================
                                 */
                                onAddToilet(
                                    toilet
                                )


                                /*
                                 * =====================================
                                 * 入力内容リセット
                                 * =====================================
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
                                 * =====================================
                                 * 登録後
                                 *
                                 * Mapへ戻る
                                 *
                                 * 2 = Map
                                 * =====================================
                                 */
                                selectedScreen =
                                    2
                            }
                        }
                    )
                }


                /*
                 * =====================================
                 * 4
                 *
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
 * レビュー・状態更新
 *
 * 仮画面
 *
 * 後で本物のScreenへ置き換える
 * =====================================
 */
@Composable
private fun ReviewStatusPlaceholderScreen() {

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