package com.example.toiletmap.screen.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.PointExchangeRepository
import com.example.toiletmap.model.RewardRedemption
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun PointExchangeHistoryScreen(
    onBack: () -> Unit
) {

    var history by remember {
        mutableStateOf<List<RewardRedemption>>(
            emptyList()
        )
    }


    var loading by remember {
        mutableStateOf(true)
    }


    var errorMessage by remember {
        mutableStateOf("")
    }


    /*
     * =========================================
     * 交換履歴取得
     * =========================================
     */
    LaunchedEffect(Unit) {

        try {

            history =
                PointExchangeRepository
                    .loadRedemptionHistory()

        } catch (e: Exception) {

            e.printStackTrace()

            errorMessage =
                "交換履歴の取得に失敗しました"

        } finally {

            loading =
                false
        }
    }


    /*
     * =========================================
     * 画面全体
     * =========================================
     *
     * weightを使わず、
     * 画面全体をスクロール可能にする。
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp)
    ) {

        /*
         * 戻る
         */
        TextButton(
            onClick =
                onBack
        ) {

            Text(
                "← アカウントに戻る"
            )
        }


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        /*
         * タイトル
         */
        Text(
            text =
                "ポイント交換履歴",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        /*
         * =====================================
         * 読み込み中
         * =====================================
         */
        if (loading) {

            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                CircularProgressIndicator()
            }


            return@Column
        }


        /*
         * =====================================
         * エラー
         * =====================================
         */
        if (
            errorMessage.isNotBlank()
        ) {

            Text(
                text =
                    errorMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )


            return@Column
        }


        /*
         * =====================================
         * 履歴なし
         * =====================================
         */
        if (
            history.isEmpty()
        ) {

            Text(
                text =
                    "まだポイント交換履歴はありません",

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )


            return@Column
        }


        /*
         * =====================================
         * 履歴一覧
         * =====================================
         */
        history.forEach { redemption ->

            RedemptionHistoryCard(
                redemption =
                    redemption
            )


            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )
    }
}


/*
 * =============================================
 * 交換履歴カード
 * =============================================
 */
@Composable
private fun RedemptionHistoryCard(
    redemption: RewardRedemption
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    18.dp
                )
        ) {

            /*
             * 商品名
             */
            Text(
                text =
                    redemption.itemName,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )


            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )


            /*
             * ポイント / 状態
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement
                        .SpaceBetween
            ) {

                Text(
                    text =
                        "${redemption.pointsUsed} pt 使用"
                )


                Text(
                    text =
                        redemptionStatusText(
                            redemption.status
                        ),

                    fontWeight =
                        FontWeight.Bold
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            /*
             * 交換日時
             */
            Text(
                text =
                    formatRedemptionDate(
                        redemption.createdAt
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


/*
 * =============================================
 * DBの状態
 * ↓
 * 日本語表示
 * =============================================
 */
private fun redemptionStatusText(
    status: String
): String {

    return when (
        status.uppercase()
    ) {

        "COMPLETED" -> {
            "交換完了"
        }


        "PENDING" -> {
            "処理中"
        }


        "CANCELLED" -> {
            "キャンセル"
        }


        "FAILED" -> {
            "失敗"
        }


        else -> {
            status
        }
    }
}


/*
 * =============================================
 * SupabaseのUTC日時
 * ↓
 * 日本時間
 * =============================================
 */
private fun formatRedemptionDate(
    value: String
): String {

    return try {

        val dateTime =
            OffsetDateTime
                .parse(value)
                .atZoneSameInstant(
                    ZoneId.of(
                        "Asia/Tokyo"
                    )
                )


        dateTime.format(
            DateTimeFormatter.ofPattern(
                "yyyy/MM/dd HH:mm"
            )
        )

    } catch (e: Exception) {

        /*
         * 日付形式が想定外だった場合の
         * フォールバック表示
         */
        value
            .replace(
                "T",
                " "
            )
            .take(16)
    }
}