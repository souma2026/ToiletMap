package com.example.toiletmap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
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


/*
 * =========================================
 * 色
 * =========================================
 */
private val NavGreen =
    Color(
        0xFF0B8377
    )

private val NavGray =
    Color(
        0xFF8B989A
    )


/*
 * =========================================
 * BottomNavigation
 *
 * 0 = 未清掃
 * 1 = レビュー・状態更新
 * 2 = Map
 * 3 = 追加
 * 4 = アカウント
 * =========================================
 */
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
                            horizontal =
                                4.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                /*
                 * =========================================
                 * 左側
                 *
                 * 未清掃
                 * 更新
                 * =========================================
                 */
                Row(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly,

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {


                    /*
                     * =========================================
                     * 0
                     *
                     * 未清掃
                     * =========================================
                     */
                    BottomNavItem(

                        label =
                            "未清掃",

                        icon =
                            Icons
                                .Outlined
                                .Warning,

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
                     * =========================================
                     * 1
                     *
                     * レビュー・状態更新
                     * =========================================
                     */
                    BottomNavItem(

                        label =
                            "更新",

                        icon =
                            Icons
                                .Outlined
                                .Edit,

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


                /*
                 * =========================================
                 * 中央Mapボタン用スペース
                 * =========================================
                 */
                Spacer(

                    modifier =
                        Modifier.width(
                            76.dp
                        )
                )


                /*
                 * =========================================
                 * 右側
                 *
                 * 追加
                 * アカウント
                 * =========================================
                 */
                Row(

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly,

                    verticalAlignment =
                        Alignment.CenterVertically

                ) {


                    /*
                     * =========================================
                     * 3
                     *
                     * トイレ追加
                     * =========================================
                     */
                    BottomNavItem(

                        label =
                            "追加",

                        icon =
                            Icons
                                .Filled
                                .Add,

                        selected =
                            selectedScreen ==
                                    3,

                        onClick = {

                            onScreenSelected(
                                3
                            )
                        }
                    )


                    /*
                     * =========================================
                     * 4
                     *
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
                                    4,

                        onClick = {

                            onScreenSelected(
                                4
                            )
                        }
                    )
                }
            }
        }


        /*
         * =========================================
         * 中央
         *
         * Mapボタン
         *
         * 2 = Map
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
                            .Outlined
                            .Map,

                    contentDescription =
                        "マップ",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            31.dp
                        )
                )
            }


            Text(

                text =
                    "マップ",

                modifier =
                    Modifier.padding(
                        top =
                            3.dp
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
                    width =
                        68.dp,

                    height =
                        64.dp
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
                    24.dp
                )
        )


        Text(

            text =
                label,

            modifier =
                Modifier.padding(
                    top =
                        4.dp
                ),

            color =
                color,

            fontSize =

                if (
                    label ==
                    "アカウント"
                ) {

                    9.sp

                } else {

                    10.sp
                },

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