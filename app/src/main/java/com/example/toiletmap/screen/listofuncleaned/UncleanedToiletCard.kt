package com.example.toiletmap.screen.listofuncleaned

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

private val FinderGreen =
    Color(0xFF0B8377)

private val FinderAmber =
    Color(0xFFF2B544)


@Composable
fun UncleanedToiletCard(

    toilet: UncleanedToilet,

    distanceMeters: Float,

    onShowOnMap: () -> Unit

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    3.dp
            )

    ) {


        Column(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {


            /*
             * =====================================
             * トイレ情報
             * =====================================
             */
            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                /*
                 * 場所アイコン
                 */
                Surface(

                    color =
                        FinderGreen.copy(
                            alpha = 0.1f
                        ),

                    shape =
                        RoundedCornerShape(
                            12.dp
                        )

                ) {

                    Icon(

                        imageVector =
                            Icons
                                .Outlined
                                .LocationOn,

                        contentDescription =
                            null,

                        tint =
                            FinderGreen,

                        modifier =
                            Modifier.padding(
                                10.dp
                            )
                    )
                }


                Spacer(
                    modifier =
                        Modifier.width(
                            12.dp
                        )
                )


                /*
                 * 名前・距離・清掃時間
                 */
                Column(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            3.dp
                        )

                ) {


                    Text(

                        text =
                            toilet.name,

                        color =
                            FinderDark,

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Text(

                        text =
                            formatDistance(
                                distanceMeters
                            ),

                        color =
                            FinderMuted,

                        fontSize =
                            13.sp
                    )


                    Text(

                        text =
                            "前回清掃：${
                                formatElapsedSinceCleaning(
                                    toilet.lastCleanedAtMillis
                                )
                            }",

                        color =
                            FinderMuted,

                        fontSize =
                            12.sp
                    )
                }


                /*
                 * 清掃待ち表示
                 */
                Surface(

                    color =
                        FinderAmber.copy(
                            alpha = 0.15f
                        ),

                    shape =
                        RoundedCornerShape(
                            8.dp
                        )

                ) {

                    Text(

                        text =
                            "清掃待ち",

                        modifier =
                            Modifier.padding(
                                horizontal =
                                    9.dp,
                                vertical =
                                    5.dp
                            ),

                        color =
                            FinderAmber,

                        fontSize =
                            12.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }


            /*
             * =====================================
             * 地図で見る
             * =====================================
             */
            Button(

                onClick =
                    onShowOnMap,

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        12.dp
                    ),

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            FinderGreen
                    )

            ) {


                Icon(

                    imageVector =
                        Icons
                            .Outlined
                            .Map,

                    contentDescription =
                        null
                )


                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )


                Text(
                    text =
                        "地図で見る"
                )
            }
        }
    }
}