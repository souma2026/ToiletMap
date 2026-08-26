package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toiletmap.model.PointTransaction
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun PointHistorySection(
    transactions: List<PointTransaction>,
    showHistory: Boolean,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier =
            modifier
    ) {

        Text(
            text =
                "ポイント履歴",

            style =
                MaterialTheme.typography.titleLarge
        )


        Spacer(
            modifier =
                Modifier.height(4.dp)
        )


        Text(
            text =
                "${transactions.size}件の履歴があります",

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(14.dp),

            onClick =
                onToggleHistory
        ) {

            Text(
                text =
                    if (showHistory) {

                        "ポイント履歴を閉じる"

                    } else {

                        "ポイント履歴を見る"
                    }
            )
        }


        if (showHistory) {

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )


            if (transactions.isEmpty()) {

                Text(
                    text =
                        "まだポイント履歴はありません",

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                transactions.forEach { transaction ->

                    PointTransactionCard(
                        transaction =
                            transaction
                    )
                }
            }
        }
    }
}


@Composable
private fun PointTransactionCard(
    transaction: PointTransaction
) {

    val isPositive =
        transaction.amount > 0


    val amountText =
        if (isPositive) {

            "+${transaction.amount} pt"

        } else {

            "${transaction.amount} pt"
        }


    val amountColor =
        if (isPositive) {

            MaterialTheme.colorScheme.primary

        } else {

            MaterialTheme.colorScheme.error
        }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            ),

        shape =
            RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    text =
                        pointReasonLabel(
                            transaction.reason
                        ),

                    style =
                        MaterialTheme.typography.titleMedium,

                    modifier =
                        Modifier.weight(1f)
                )


                Text(
                    text =
                        amountText,

                    color =
                        amountColor,

                    fontWeight =
                        FontWeight.Bold,

                    style =
                        MaterialTheme.typography.titleMedium
                )
            }


            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )


            Text(
                text =
                    pointTypeLabel(
                        transaction.pointType
                    ),

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(
                text =
                    formatPointTransactionDate(
                        transaction.createdAt
                    ),

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


private fun pointTypeLabel(
    pointType: String
): String {

    return when (pointType) {

        "REQUEST" ->
            "清掃依頼ポイント"

        "REWARD" ->
            "清掃報酬ポイント"

        else ->
            pointType
    }
}


private fun pointReasonLabel(
    reason: String
): String {

    return when (reason) {

        "DAILY_REQUEST_POINTS" ->
            "デイリーポイント回復"

        "CLEANING_REQUEST" ->
            "清掃依頼"

        "CLEANING_COMPLETED" ->
            "清掃完了報酬"

        "ITEM_REDEMPTION" ->
            "商品交換"

        else ->
            reason
    }
}


private fun formatPointTransactionDate(
    value: String
): String {

    return try {

        OffsetDateTime
            .parse(value)
            .atZoneSameInstant(
                ZoneId.of(
                    "Asia/Tokyo"
                )
            )
            .format(
                DateTimeFormatter.ofPattern(
                    "yyyy/MM/dd HH:mm"
                )
            )

    } catch (_: Exception) {

        value
            .replace(
                "T",
                " "
            )
            .take(16)
    }
}