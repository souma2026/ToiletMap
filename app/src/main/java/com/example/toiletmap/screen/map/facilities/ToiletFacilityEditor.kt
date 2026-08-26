package com.example.toiletmap.screen.map.facilities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.ToiletFacilityRepository
import com.example.toiletmap.data.repository.ToiletFacilityRewardUpdateResult
import com.example.toiletmap.model.Toilet
import kotlinx.coroutines.launch


@Composable
fun ToiletFacilityEditor(

    toilet: Toilet,

    currentUserId: String?,

    onOpenAccount: () -> Unit,

    modifier: Modifier = Modifier

) {

    /*
     * =====================================
     * 男女別対応Repository
     * =====================================
     */
    val repository =
        remember {
            ToiletFacilityRepository()
        }


    val coroutineScope =
        rememberCoroutineScope()


    /*
     * =====================================
     * 現在表示するトイレ
     * =====================================
     */
    var displayedToilet by
    remember(
        toilet.id
    ) {

        mutableStateOf(
            toilet
        )
    }


    /*
     * =====================================
     * 親側の詳細情報更新に追従
     * =====================================
     */
    LaunchedEffect(

        toilet.id,

        toilet.maleWesternToiletCount,

        toilet.maleJapaneseToiletCount,

        toilet.femaleWesternToiletCount,

        toilet.femaleJapaneseToiletCount,

        toilet.hasBabyChair,

        toilet.hasDiaperChangingTable,

        toilet.hasAccessibleStall,

        toilet.hasOstomate,

        toilet.facilityUpdatedAt

    ) {

        displayedToilet =
            toilet
    }


    /*
     * =====================================
     * 編集ダイアログ
     * =====================================
     */
    var showEditDialog by
    remember(
        toilet.id
    ) {

        mutableStateOf(
            false
        )
    }


    /*
     * 保存中
     */
    var isSaving by
    remember(
        toilet.id
    ) {

        mutableStateOf(
            false
        )
    }


    /*
     * エラー
     */
    var errorMessage by
    remember(
        toilet.id
    ) {

        mutableStateOf<String?>(
            null
        )
    }


    /*
     * =====================================
     * 更新結果
     * =====================================
     */
    var updateResult by
    remember(
        toilet.id
    ) {

        mutableStateOf<
                ToiletFacilityRewardUpdateResult?
                >(
            null
        )
    }


    Column(
        modifier =
            modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {


        /*
         * =====================================
         * 設備情報
         * =====================================
         */
        ToiletFacilitySection(
            toilet =
                displayedToilet
        )


        /*
         * =====================================
         * 編集
         * =====================================
         */
        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            onClick = {

                errorMessage =
                    null


                if (
                    currentUserId == null
                ) {

                    onOpenAccount()

                } else {

                    showEditDialog =
                        true
                }
            }
        ) {

            Text(
                text =
                    if (
                        currentUserId == null
                    ) {

                        "ログインして設備情報を編集"

                    } else {

                        "設備情報を編集"
                    },

                fontWeight =
                    FontWeight.SemiBold
            )
        }


        /*
         * =====================================
         * エラー
         * =====================================
         */
        if (
            errorMessage != null
        ) {

            Text(
                text =
                    errorMessage.orEmpty(),

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }


    /*
     * =====================================
     * 編集ダイアログ
     * =====================================
     */
    if (
        showEditDialog
    ) {

        ToiletFacilityEditDialog(

            toilet =
                displayedToilet,

            isSaving =
                isSaving,

            onDismiss = {

                if (!isSaving) {

                    showEditDialog =
                        false

                    errorMessage =
                        null
                }
            },

            onSave = {
                    values ->


                /*
                 * =====================================
                 * 未ログイン
                 * =====================================
                 */
                if (
                    currentUserId == null
                ) {

                    showEditDialog =
                        false

                    onOpenAccount()

                } else {


                    /*
                     * =====================================
                     * 保存
                     * =====================================
                     */
                    isSaving =
                        true

                    errorMessage =
                        null


                    coroutineScope.launch {

                        try {

                            val result =

                                repository
                                    .updateToiletFacilities(

                                        toiletId =
                                            displayedToilet.id,


                                        /*
                                         * 男子
                                         */
                                        maleWesternToiletCount =
                                            values
                                                .maleWesternToiletCount,

                                        maleJapaneseToiletCount =
                                            values
                                                .maleJapaneseToiletCount,


                                        /*
                                         * 女子
                                         */
                                        femaleWesternToiletCount =
                                            values
                                                .femaleWesternToiletCount,

                                        femaleJapaneseToiletCount =
                                            values
                                                .femaleJapaneseToiletCount,


                                        /*
                                         * その他
                                         */
                                        hasBabyChair =
                                            values
                                                .hasBabyChair,

                                        hasDiaperChangingTable =
                                            values
                                                .hasDiaperChangingTable,

                                        hasAccessibleStall =
                                            values
                                                .hasAccessibleStall,

                                        hasOstomate =
                                            values
                                                .hasOstomate
                                    )


                            /*
                             * =====================================
                             * 最新情報へ更新
                             * =====================================
                             */
                            displayedToilet =
                                result.toilet


                            /*
                             * 編集画面を閉じる
                             */
                            showEditDialog =
                                false


                            /*
                             * ポイント結果表示
                             */
                            updateResult =
                                result


                        } catch (
                            e: Exception
                        ) {

                            e.printStackTrace()


                            errorMessage =

                                when {

                                    e.message
                                        ?.contains(
                                            "LOGIN_REQUIRED",
                                            ignoreCase = true
                                        ) == true ->

                                        "設備情報を編集するにはログインが必要です"


                                    e.message
                                        ?.contains(
                                            "PROFILE_NOT_FOUND",
                                            ignoreCase = true
                                        ) == true ->

                                        "プロフィール情報を取得できませんでした"


                                    e.message
                                        ?.contains(
                                            "TOILET_NOT_FOUND",
                                            ignoreCase = true
                                        ) == true ->

                                        "対象のトイレが見つかりませんでした"


                                    else ->

                                        e.message
                                            ?: "設備情報の更新に失敗しました"
                                }


                        } finally {

                            isSaving =
                                false
                        }
                    }
                }
            }
        )
    }


    /*
     * =====================================
     * ポイント結果
     * =====================================
     */
    val result =
        updateResult


    if (
        result != null
    ) {

        val earnedPoints =
            result.earnedPoints


        AlertDialog(

            onDismissRequest = {

                updateResult =
                    null
            },


            title = {

                Text(
                    text =
                        if (
                            earnedPoints > 0
                        ) {

                            "情報提供ありがとうございます！"

                        } else {

                            "設備情報を更新しました"
                        },

                    fontWeight =
                        FontWeight.Bold
                )
            },


            text = {

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    if (
                        earnedPoints > 0
                    ) {

                        Text(
                            text =
                                "+${earnedPoints} pt 獲得しました！",

                            style =
                                MaterialTheme
                                    .typography
                                    .headlineSmall,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )


                        Text(
                            text =
                                "現在の報酬ポイント：${result.remainingRewardPoints} pt"
                        )

                    } else {

                        Text(
                            text =
                                "設備情報を更新しました。"
                        )


                        Text(
                            text =
                                "今回は新しい設備情報の完成がないため、ポイントの獲得はありません。"
                        )


                        Text(
                            text =
                                "現在の報酬ポイント：${result.remainingRewardPoints} pt"
                        )
                    }
                }
            },


            confirmButton = {

                TextButton(
                    onClick = {

                        updateResult =
                            null
                    }
                ) {

                    Text(
                        "OK"
                    )
                }
            }
        )
    }
}