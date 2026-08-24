package com.example.toiletmap.screen.listofuncleaned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/*
 * =====================================
 * 色
 * =====================================
 */
private val FinderDark =
    Color(0xFF12313A)

private val FinderMuted =
    Color(0xFF748186)


/*
 * =====================================
 * 距離付きトイレ
 * =====================================
 */
private data class ToiletWithDistance(

    val toilet:
    UncleanedToilet,

    val distanceMeters:
    Float
)


/*
 * =====================================
 * 未清掃トイレ一覧画面
 * =====================================
 */
@Composable
fun ListOfUncleanedScreen(

    toilets:
    List<UncleanedToilet>,

    onToiletClick:
        (UncleanedToilet) -> Unit =
        {}

) {

    /*
     * =====================================
     * 現在地
     * =====================================
     */
    val locationState =
        rememberCurrentLocationState()


    /*
     * =====================================
     * 距離順に並べる
     * =====================================
     */
    val sortedToilets =

        remember(
            toilets,
            locationState.location
        ) {

            val currentLocation =
                locationState.location


            if (
                currentLocation == null
            ) {

                emptyList()

            } else {

                toilets
                    .map { toilet ->

                        ToiletWithDistance(

                            toilet =
                                toilet,

                            distanceMeters =
                                calculateDistance(

                                    currentLocation =
                                        currentLocation,

                                    toilet =
                                        toilet
                                )
                        )
                    }
                    .sortedBy {

                        it.distanceMeters
                    }
            }
        }


    /*
     * =====================================
     * 画面
     * =====================================
     */
    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    16.dp
                )

    ) {


        /*
         * タイトル
         */
        Text(

            text =
                "清掃待ちのトイレ",

            color =
                FinderDark,

            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.Bold
        )


        Text(

            text =
                "現在地から近い順",

            color =
                FinderMuted,

            fontSize =
                13.sp
        )


        Spacer(

            modifier =
                Modifier.height(
                    16.dp
                )
        )


        /*
         * =====================================
         * 位置情報権限なし
         * =====================================
         */
        if (
            !locationState.hasPermission
        ) {

            MessageArea(

                message =
                    "現在地を取得するには\n位置情報の許可が必要です"
            )

            return@Column
        }


        /*
         * =====================================
         * 現在地なし
         * =====================================
         */
        if (
            locationState.location == null
        ) {

            MessageArea(

                message =
                    "現在地を取得できませんでした"
            )

            return@Column
        }


        /*
         * =====================================
         * 未清掃トイレなし
         * =====================================
         */
        if (
            sortedToilets.isEmpty()
        ) {

            MessageArea(

                message =
                    "清掃待ちのトイレはありません"
            )

            return@Column
        }


        /*
         * =====================================
         * トイレ一覧
         * =====================================
         */
        LazyColumn(

            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )

        ) {

            items(

                items =
                    sortedToilets,

                key = {

                    it.toilet.id
                }

            ) { item ->


                UncleanedToiletCard(

                    toilet =
                        item.toilet,

                    distanceMeters =
                        item.distanceMeters,

                    onClick = {

                        onToiletClick(
                            item.toilet
                        )
                    }
                )
            }
        }
    }
}


/*
 * =====================================
 * メッセージ表示
 * =====================================
 */
@Composable
private fun MessageArea(
    message: String
) {

    Box(

        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center

    ) {

        Text(

            text =
                message,

            color =
                FinderMuted
        )
    }
}