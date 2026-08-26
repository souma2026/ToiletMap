package com.example.toiletmap.screen.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.PointExchangeRepository
import com.example.toiletmap.model.RewardItem
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun PointExchangeScreen(

    currentPoints: Int,

    onBack: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    /*
     * =========================================
     * State
     * =========================================
     */

    var rewardItems by remember {

        mutableStateOf<List<RewardItem>>(
            emptyList()
        )
    }


    /*
     * RPC成功後は
     * 画面上の残高もすぐ更新する
     */
    var displayedPoints by remember(
        currentPoints
    ) {

        mutableStateOf(
            currentPoints
        )
    }


    var loading by remember {

        mutableStateOf(
            true
        )
    }


    var redeeming by remember {

        mutableStateOf(
            false
        )
    }


    var errorMessage by remember {

        mutableStateOf("")
    }


    var successMessage by remember {

        mutableStateOf("")
    }


    /*
     * 交換確認中の商品
     */
    var selectedItem by remember {

        mutableStateOf<RewardItem?>(
            null
        )
    }


    /*
     * =========================================
     * 二重交換防止ID
     * =========================================
     *
     * 同じ確認ダイアログ内で再試行した場合は
     * 同じUUIDを使用する。
     */
    var pendingRequestId by remember {

        mutableStateOf<String?>(
            null
        )
    }


    /*
     * =========================================
     * 商品一覧取得
     * =========================================
     */

    LaunchedEffect(
        Unit
    ) {

        try {

            rewardItems =
                PointExchangeRepository
                    .loadRewardItems()


        } catch (
            e: Exception
        ) {

            e.printStackTrace()


            errorMessage =
                "商品一覧の取得に失敗しました"


        } finally {

            loading =
                false
        }
    }


    /*
     * =========================================
     * 画面
     * =========================================
     */

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    20.dp
                )
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
                Modifier.height(
                    8.dp
                )
        )


        /*
         * タイトル
         */
        Text(

            text =
                "ポイント交換",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium,

            fontWeight =
                FontWeight.Bold
        )


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        /*
         * =====================================
         * 現在の清掃報酬ポイント
         * =====================================
         */

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
                                .primaryContainer
                    )
        ) {

            Column(

                modifier =
                    Modifier.padding(
                        18.dp
                    )
            ) {

                Text(
                    "現在の清掃報酬ポイント"
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )


                Text(

                    text =
                        "$displayedPoints pt",

                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }


        /*
         * =====================================
         * メッセージ
         * =====================================
         */

        if (
            successMessage.isNotBlank()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            Text(

                text =
                    successMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }


        if (
            errorMessage.isNotBlank()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            Text(

                text =
                    errorMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        /*
         * =====================================
         * 商品一覧
         * =====================================
         */

        if (
            loading
        ) {

            CircularProgressIndicator(

                modifier =
                    Modifier.align(
                        Alignment.CenterHorizontally
                    )
            )

        } else if (
            rewardItems.isEmpty()
        ) {

            Text(
                "現在交換できる商品はありません"
            )

        } else {

            LazyColumn(

                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {

                items(

                    items =
                        rewardItems,

                    key = {

                        it.id
                    }

                ) { item ->

                    RewardItemCard(

                        item =
                            item,

                        currentPoints =
                            displayedPoints,

                        redeeming =
                            redeeming,

                        onRedeem = {

                            /*
                             * 確認画面を開く
                             */
                            selectedItem =
                                item


                            /*
                             * この交換操作専用UUID
                             */
                            pendingRequestId =
                                UUID
                                    .randomUUID()
                                    .toString()


                            errorMessage =
                                ""
                        }
                    )
                }
            }
        }
    }


    /*
     * =========================================
     * 交換確認ダイアログ
     * =========================================
     */

    val item =
        selectedItem


    if (
        item != null
    ) {

        AlertDialog(

            onDismissRequest = {

                if (
                    !redeeming
                ) {

                    selectedItem =
                        null

                    pendingRequestId =
                        null
                }
            },

            title = {

                Text(
                    "ポイント交換の確認"
                )
            },

            text = {

                Column {

                    Text(

                        text =
                            item.name,

                        fontWeight =
                            FontWeight.Bold
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )


                    Text(

                        text =
                            "${item.requiredPoints} pt を使用して交換しますか？"
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )


                    Text(

                        text =
                            "交換後：${displayedPoints - item.requiredPoints} pt"
                    )
                }
            },

            confirmButton = {

                Button(

                    enabled =
                        !redeeming &&
                                displayedPoints >=
                                item.requiredPoints,

                    onClick = {

                        /*
                         * =================================
                         * 実際の交換処理
                         * =================================
                         */

                        val requestId =
                            pendingRequestId
                                ?: UUID
                                    .randomUUID()
                                    .toString()
                                    .also {

                                        pendingRequestId =
                                            it
                                    }


                        redeeming =
                            true

                        errorMessage =
                            ""

                        successMessage =
                            ""


                        scope.launch {

                            try {

                                val result =
                                    PointExchangeRepository
                                        .redeemRewardItem(

                                            rewardItemId =
                                                item.id,

                                            clientRequestId =
                                                requestId
                                        )


                                /*
                                 * Supabaseから返された
                                 * 正式な残高を採用
                                 */
                                displayedPoints =
                                    result
                                        .remainingRewardPoints


                                successMessage =

                                    if (
                                        result.alreadyProcessed
                                    ) {

                                        "${result.itemName}の交換はすでに完了しています"

                                    } else {

                                        "${result.itemName}を交換しました"
                                    }


                                /*
                                 * 成功したので閉じる
                                 */
                                selectedItem =
                                    null

                                pendingRequestId =
                                    null


                            } catch (
                                e: Exception
                            ) {

                                e.printStackTrace()


                                errorMessage =
                                    friendlyRedeemError(
                                        e
                                    )


                            } finally {

                                redeeming =
                                    false
                            }
                        }
                    }
                ) {

                    Text(

                        if (
                            redeeming
                        ) {

                            "交換中..."

                        } else {

                            "交換する"
                        }
                    )
                }
            },

            dismissButton = {

                OutlinedButton(

                    enabled =
                        !redeeming,

                    onClick = {

                        selectedItem =
                            null

                        pendingRequestId =
                            null
                    }
                ) {

                    Text(
                        "キャンセル"
                    )
                }
            }
        )
    }
}


/*
 * =========================================
 * 商品カード
 * =========================================
 */
@Composable
private fun RewardItemCard(

    item: RewardItem,

    currentPoints: Int,

    redeeming: Boolean,

    onRedeem: () -> Unit
) {

    val canRedeem =
        currentPoints >=
                item.requiredPoints


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

            Text(

                text =
                    item.name,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )


            if (
                item.description.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(
                            6.dp
                        )
                )


                Text(

                    text =
                        item.description,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


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
                        "${item.requiredPoints} pt",

                    fontWeight =
                        FontWeight.Bold
                )


                Button(

                    enabled =
                        canRedeem &&
                                !redeeming,

                    onClick =
                        onRedeem
                ) {

                    Text(

                        if (
                            canRedeem
                        ) {

                            "交換する"

                        } else {

                            "ポイント不足"
                        }
                    )
                }
            }
        }
    }
}


/*
 * =========================================
 * RPCエラーをユーザー向け表示へ変換
 * =========================================
 */
private fun friendlyRedeemError(
    error: Exception
): String {

    val message =
        error.message
            ?.uppercase()
            ?: ""


    return when {

        "INSUFFICIENT_REWARD_POINTS" in message -> {

            "清掃報酬ポイントが不足しています"
        }


        "LOGIN_REQUIRED" in message -> {

            "ログインが必要です"
        }


        "REWARD_ITEM_NOT_FOUND" in message -> {

            "この商品は現在交換できません"
        }


        "PROFILE_NOT_FOUND" in message -> {

            "アカウント情報を取得できませんでした"
        }


        else -> {

            "ポイント交換に失敗しました"
        }
    }
}