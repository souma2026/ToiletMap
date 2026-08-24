package com.example.toiletmap.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private val Teal =
    Color(0xFF008C7D)

private val Inactive =
    Color(0xFF8A9499)


@Composable
fun BottomNavigationBar(

    selectedScreen:
    Int,

    onScreenSelected:
        (Int) -> Unit

) {

    Box(

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    82.dp
                )
                .background(
                    Color.White
                )

    ) {


        // ==========================================
        // 左右
        // ==========================================

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(

                        horizontal =
                            24.dp,

                        vertical =
                            8.dp
                    ),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.Bottom

        ) {


            // ==========================================
            // マップ
            // ==========================================

            BottomItem(

                label =
                    "マップ",

                selected =
                    selectedScreen == 0,

                onClick = {

                    onScreenSelected(
                        0
                    )
                },

                icon = {
                        color ->

                    MapIcon(

                        color =
                            color,

                        modifier =
                            Modifier.size(
                                25.dp
                            )
                    )
                }
            )


            // 真ん中の追加ボタン用スペース
            Spacer(

                modifier =
                    Modifier.width(
                        72.dp
                    )
            )


            // ==========================================
            // アカウント
            // ==========================================

            BottomItem(

                label =
                    "アカウント",

                selected =
                    selectedScreen == 1,

                onClick = {

                    onScreenSelected(
                        1
                    )
                },

                icon = {
                        color ->

                    UserIcon(

                        color =
                            color,

                        modifier =
                            Modifier.size(
                                25.dp
                            )
                    )
                }
            )
        }


        // ==========================================
        // 中央の追加ボタン
        // ==========================================

        Column(

            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .offset(
                        y = (-15).dp
                    )
                    .clip(

                        RoundedCornerShape(
                            30.dp
                        )
                    )
                    .clickable {

                        onScreenSelected(
                            2
                        )
                    },

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
                                10.dp,

                            shape =
                                CircleShape
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Teal
                        ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text =
                        "+",

                    color =
                        Color.White,

                    fontSize =
                        38.sp,

                    fontWeight =
                        FontWeight.Light
                )
            }


            Text(

                text =
                    "追加",

                color =

                    if (
                        selectedScreen == 2
                    ) {

                        Teal

                    } else {

                        Inactive
                    },

                fontSize =
                    12.sp,

                fontWeight =

                    if (
                        selectedScreen == 2
                    ) {

                        FontWeight.Bold

                    } else {

                        FontWeight.Medium
                    },

                modifier =
                    Modifier.padding(
                        top = 2.dp
                    )
            )
        }
    }
}


// ==========================================
// ナビゲーション1項目
// ==========================================

@Composable
private fun BottomItem(

    label:
    String,

    selected:
    Boolean,

    onClick:
        () -> Unit,

    icon:
    @Composable (Color) -> Unit

) {

    val color =

        if (
            selected
        ) {

            Teal

        } else {

            Inactive
        }


    Column(

        modifier =
            Modifier
                .width(
                    86.dp
                )
                .clip(

                    RoundedCornerShape(
                        16.dp
                    )
                )
                .clickable(

                    onClick =
                        onClick
                )
                .padding(

                    vertical =
                        4.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )

    ) {

        icon(
            color
        )


        Text(

            text =
                label,

            color =
                color,

            fontSize =
                12.sp,

            fontWeight =

                if (
                    selected
                ) {

                    FontWeight.Bold

                } else {

                    FontWeight.Medium
                }
        )
    }
}


// ==========================================
// マップアイコン
// ==========================================

@Composable
private fun MapIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val stroke =

            size.minDimension *
                    0.075f


        val oneThird =

            size.width /
                    3f


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.08f,

                    size.height *
                            0.22f
                ),

            end =
                Offset(

                    oneThird,

                    size.height *
                            0.10f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird,

                    size.height *
                            0.10f
                ),

            end =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.22f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.22f
                ),

            end =
                Offset(

                    size.width *
                            0.92f,

                    size.height *
                            0.10f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.08f,

                    size.height *
                            0.22f
                ),

            end =
                Offset(

                    size.width *
                            0.08f,

                    size.height *
                            0.84f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.08f,

                    size.height *
                            0.84f
                ),

            end =
                Offset(

                    oneThird,

                    size.height *
                            0.72f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird,

                    size.height *
                            0.72f
                ),

            end =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.84f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.84f
                ),

            end =
                Offset(

                    size.width *
                            0.92f,

                    size.height *
                            0.72f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    size.width *
                            0.92f,

                    size.height *
                            0.72f
                ),

            end =
                Offset(

                    size.width *
                            0.92f,

                    size.height *
                            0.10f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird,

                    size.height *
                            0.10f
                ),

            end =
                Offset(

                    oneThird,

                    size.height *
                            0.72f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )


        drawLine(

            color =
                color,

            start =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.22f
                ),

            end =
                Offset(

                    oneThird *
                            2f,

                    size.height *
                            0.84f
                ),

            strokeWidth =
                stroke,

            cap =
                StrokeCap.Round
        )
    }
}


// ==========================================
// アカウントアイコン
// ==========================================

@Composable
private fun UserIcon(

    color:
    Color,

    modifier:
    Modifier =
        Modifier

) {

    Canvas(

        modifier =
            modifier

    ) {

        val stroke =

            size.minDimension *
                    0.075f


        // 頭
        drawCircle(

            color =
                color,

            radius =
                size.minDimension *
                        0.18f,

            center =
                Offset(

                    size.width /
                            2f,

                    size.height *
                            0.32f
                ),

            style =
                Stroke(

                    width =
                        stroke
                )
        )


        // 身体
        drawArc(

            color =
                color,

            startAngle =
                200f,

            sweepAngle =
                140f,

            useCenter =
                false,

            topLeft =
                Offset(

                    size.width *
                            0.18f,

                    size.height *
                            0.45f
                ),

            size =
                androidx.compose.ui.geometry.Size(

                    size.width *
                            0.64f,

                    size.height *
                            0.48f
                ),

            style =
                Stroke(

                    width =
                        stroke,

                    cap =
                        StrokeCap.Round
                )
        )
    }
}