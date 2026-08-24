package com.example.toiletmap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val NavGreen =
    Color(
        0xFF0B8377
    )

private val NavGray =
    Color(
        0xFF8B989A
    )


@Composable
fun BottomNavigationBar(

    selectedScreen: Int,

    onScreenSelected:
        (Int) -> Unit

) {

    /*
     * =========================================
     * 全体
     * =========================================
     */

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    88.dp
                )

    ) {


        /*
         * =========================================
         * 白いナビゲーションバー
         * =========================================
         */

        Surface(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        72.dp
                    )
                    .align(
                        Alignment.BottomCenter
                    )
                    .shadow(
                        10.dp
                    ),

            color =
                Color.White,

            shadowElevation =
                8.dp

        ) {


            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp
                        ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                /*
                 * =========================================
                 * マップ
                 * =========================================
                 */

                BottomNavItem(

                    label =
                        "マップ",

                    icon =
                        Icons
                            .Outlined
                            .Map,

                    selected =
                        selectedScreen ==
                                0,

                    onClick = {

                        onScreenSelected(
                            0
                        )
                    }
                )


                /*
                 * 真ん中の追加ボタン用スペース
                 */
                Box(

                    modifier =
                        Modifier.size(
                            86.dp
                        )
                )


                /*
                 * =========================================
                 * アカウント
                 * =========================================
                 */

                BottomNavItem(

                    label =
                        "アカウント",

                    icon =
                        Icons
                            .Outlined
                            .Person,

                    selected =
                        selectedScreen ==
                                1,

                    onClick = {

                        onScreenSelected(
                            1
                        )
                    }
                )
            }
        }


        /*
         * =========================================
         * 真ん中の追加ボタン
         * =========================================
         */

        Column(

            modifier =
                Modifier.align(
                    Alignment.TopCenter
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {


            Box(

                modifier =
                    Modifier
                        .size(
                            58.dp
                        )
                        .shadow(

                            elevation =
                                8.dp,

                            shape =
                                CircleShape
                        )
                        .background(

                            color =
                                NavGreen,

                            shape =
                                CircleShape
                        )
                        .clickable {

                            /*
                             * ToiletMapAppでは
                             * 2 = 追加画面
                             */
                            onScreenSelected(
                                2
                            )
                        },

                contentAlignment =
                    Alignment.Center

            ) {

                Icon(

                    imageVector =
                        Icons
                            .Filled
                            .Add,

                    contentDescription =
                        "追加",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            32.dp
                        )
                )
            }


            Text(

                text =
                    "追加",

                modifier =
                    Modifier.padding(
                        top = 3.dp
                    ),

                color =

                    if (
                        selectedScreen ==
                        2
                    ) {

                        NavGreen

                    } else {

                        NavGray
                    },

                fontSize =
                    11.sp,

                fontWeight =

                    if (
                        selectedScreen ==
                        2
                    ) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Normal
                    }
            )
        }
    }
}


/*
 * =========================================
 * 左右のナビゲーション項目
 * =========================================
 */

@Composable
private fun BottomNavItem(

    label: String,

    icon: ImageVector,

    selected: Boolean,

    onClick: () -> Unit

) {


    val color =

        if (
            selected
        ) {

            NavGreen

        } else {

            NavGray
        }


    Column(

        modifier =
            Modifier
                .size(
                    width = 92.dp,
                    height = 64.dp
                )
                .clickable(

                    onClick =
                        onClick
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center

    ) {


        Icon(

            imageVector =
                icon,

            contentDescription =
                label,

            tint =
                color,

            modifier =
                Modifier.size(
                    25.dp
                )
        )


        Text(

            text =
                label,

            modifier =
                Modifier.padding(
                    top = 4.dp
                ),

            color =
                color,

            fontSize =
                11.sp,

            fontWeight =

                if (
                    selected
                ) {

                    FontWeight.Bold

                } else {

                    FontWeight.Normal
                }
        )
    }
}