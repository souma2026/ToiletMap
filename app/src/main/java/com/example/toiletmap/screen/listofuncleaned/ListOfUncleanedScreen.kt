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


private val FinderDark =
    Color(0xFF12313A)

private val FinderMuted =
    Color(0xFF748186)


private data class ToiletWithDistance(

    val toilet:
    UncleanedToilet,

    val distanceMeters:
    Float
)


@Composable
fun ListOfUncleanedScreen(

    toilets:
    List<UncleanedToilet>,

    /*
     * =====================================
     * 「地図で見る」が押されたとき
     * =====================================
     */
    onShowOnMap:
        (UncleanedToilet) -> Unit =
        {}

) {


    val locationState =
        rememberCurrentLocationState()


    /*
     * =====================================
     * 現在地から近い順
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
                    .map {
                            toilet ->

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


    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    16.dp
                )

    ) {


        Text(

            text =
                "未清掃のトイレ",

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
         * 位置情報許可なし
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
         * 現在地取得失敗
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
         * 未清掃なし
         * =====================================
         */
        if (
            sortedToilets.isEmpty()
        ) {

            MessageArea(
                message =
                    "未清掃のトイレはありません"
            )

            return@Column
        }


        /*
         * =====================================
         * 一覧
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

            ) {
                    item ->


                UncleanedToiletCard(

                    toilet =
                        item.toilet,

                    distanceMeters =
                        item.distanceMeters,


                    /*
                     * 地図で見る
                     */
                    onShowOnMap = {

                        onShowOnMap(
                            item.toilet
                        )
                    }
                )
            }
        }
    }
}


@Composable
private fun MessageArea(

    message:
    String

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