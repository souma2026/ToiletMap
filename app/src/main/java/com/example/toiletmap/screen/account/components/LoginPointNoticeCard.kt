package com.example.toiletmap.screen.account.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun LoginPointNoticeCard(

    onInfoClick: () -> Unit,

    modifier: Modifier =
        Modifier
) {

    Surface(

        modifier =
            modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        tonalElevation =
            1.dp
    ) {

        Column(

            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text =
                        "ポイント",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )


                /*
                 * 丸い i ボタン
                 */
                Surface(

                    modifier =
                        Modifier
                            .size(
                                30.dp
                            )
                            .clickable(
                                onClick =
                                    onInfoClick
                            ),

                    shape =
                        CircleShape,

                    color =
                        MaterialTheme
                            .colorScheme
                            .secondaryContainer
                ) {

                    Box(

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(

                            text =
                                "i",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(

                text =
                    "ログインしていないためポイントを獲得できません",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}