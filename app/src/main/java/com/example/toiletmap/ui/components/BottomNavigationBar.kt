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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// =============================================
// Navigation colors
// =============================================

private val NavGreen =
    Color(0xFF0B8377)

private val NavGray =
    Color(0xFF8B989A)

private val NavBackground =
    Color.White


/**
 * =============================================
 * Bottom navigation screen indexes
 * =============================================
 *
 * 0 = 未清掃
 * 1 = 清掃
 * 2 = マップ
 * 3 = 追加
 * 4 = アカウント
 *
 * =============================================
 */
@Composable
fun BottomNavigationBar(

    selectedScreen: Int,

    onScreenSelected: (Int) -> Unit

) {


    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    92.dp
                )

    ) {


        /*
         * =============================================
         * 白いナビゲーションバー本体
         * =============================================
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

                        elevation =
                            10.dp,

                        shape =
                            RoundedCornerShape(

                                topStart =
                                    22.dp,

                                topEnd =
                                    22.dp
                            )
                    ),

            color =
                NavBackground,

            shape =
                RoundedCornerShape(

                    topStart =
                        22.dp,

                    topEnd =
                        22.dp
                ),

            shadowElevation =
                8.dp

        ) {


            Row(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 4.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {


                /*
                 * =============================================
                 * 左側
                 *
                 * 未清掃 / 清掃
                 * =============================================
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
                     * 未清掃
                     * 0
                     */
                    BottomNavItem(

                        label =
                            "未清掃",

                        icon =
                            Icons
                                .Outlined
                                .Warning,

                        selected =
                            selectedScreen == 0,

                        onClick = {

                            onScreenSelected(
                                0
                            )
                        }
                    )


                    /*
                     * 清掃
                     * 1
                     */
                    BottomNavItem(

                        label =
                            "清掃",

                        icon =
                            Icons
                                .Outlined
                                .CleaningServices,

                        selected =
                            selectedScreen == 1,

                        onClick = {

                            onScreenSelected(
                                1
                            )
                        }
                    )
                }


                /*
                 * =============================================
                 * 中央MAPボタン用の空間
                 * =============================================
                 */
                Spacer(

                    modifier =
                        Modifier.width(
                            76.dp
                        )
                )


                /*
                 * =============================================
                 * 右側
                 *
                 * 追加 / アカウント
                 * =============================================
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
                     * 追加
                     * 3
                     */
                    BottomNavItem(

                        label =
                            "追加",

                        icon =
                            Icons
                                .Filled
                                .Add,

                        selected =
                            selectedScreen == 3,

                        onClick = {

                            onScreenSelected(
                                3
                            )
                        }
                    )


                    /*
                     * アカウント
                     * 4
                     */
                    BottomNavItem(

                        label =
                            "アカウント",

                        icon =
                            Icons
                                .Outlined
                                .Person,

                        selected =
                            selectedScreen == 4,

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
         * =============================================
         * 中央のMAPボタン
         *
         * 2 = Map
         * =============================================
         */
        Column(

            modifier =
                Modifier.align(
                    Alignment.TopCenter
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {


            /*
             * 丸いMAPボタン
             */
            Box(

                modifier =
                    Modifier
                        .size(
                            60.dp
                        )
                        .shadow(

                            elevation =
                                10.dp,

                            shape =
                                CircleShape
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            NavGreen
                        )
                        .clickable {

                            /*
                             * =====================================
                             * 中央ボタンはMAP
                             *
                             * 2 = Map
                             * =====================================
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
                            .Outlined
                            .Map,

                    contentDescription =
                        "マップ",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            30.dp
                        )
                )
            }


            /*
             * MAP文字
             */
            Text(

                text =
                    "マップ",

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    ),

                color =

                    if (
                        selectedScreen == 2
                    ) {

                        NavGreen

                    } else {

                        NavGray
                    },

                fontSize =
                    11.sp,

                fontWeight =

                    if (
                        selectedScreen == 2
                    ) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Medium
                    }
            )
        }
    }
}


/*
 * =============================================
 * 通常のナビゲーション項目
 * =============================================
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
                .clip(

                    RoundedCornerShape(
                        14.dp
                    )
                )
                .clickable(
                    onClick =
                        onClick
                )
                .padding(
                    vertical = 4.dp
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
                    top = 4.dp
                ),

            color =
                color,

            fontSize =

                if (
                    label == "アカウント"
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