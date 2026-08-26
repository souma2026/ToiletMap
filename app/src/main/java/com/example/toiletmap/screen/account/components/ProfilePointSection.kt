package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun ProfilePointSection(

    requestPoints: Int,

    rewardPoints: Int,

    onInfoClick: () -> Unit,

    onOpenPointExchange: () -> Unit,

    onOpenPointExchangeHistory: () -> Unit,

    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
    ) {

        PointCard(
            points = requestPoints,
            title = "所持ポイント",
            supportingText =
                "清掃依頼に使用するポイントです"
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        PointCard(
            points = rewardPoints,
            title = "清掃報酬ポイント",
            supportingText =
                "清掃完了で獲得し、商品交換に使用できます"
        )


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        /*
         * ポイント説明
         */
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text =
                    "ポイント機能について",
                style =
                    MaterialTheme.typography.bodyMedium
            )


            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )


            Surface(
                modifier = Modifier
                    .size(30.dp)
                    .clickable(
                        onClick =
                            onInfoClick
                    ),
                shape =
                    CircleShape,
                color =
                    MaterialTheme.colorScheme.secondaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "i",
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        /*
         * 商品交換
         */
        Button(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(14.dp),
            onClick =
                onOpenPointExchange
        ) {

            Text(
                "ポイントを交換する"
            )
        }


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        /*
         * 交換履歴
         */
        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(14.dp),
            onClick =
                onOpenPointExchangeHistory
        ) {

            Text(
                "交換履歴を見る"
            )
        }
    }
}