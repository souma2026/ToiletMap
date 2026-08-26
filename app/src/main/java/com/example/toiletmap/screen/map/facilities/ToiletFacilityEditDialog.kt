package com.example.toiletmap.screen.map.facilities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.toiletmap.model.Toilet


/*
 * =====================================
 * 設備情報編集結果
 * =====================================
 */
data class ToiletFacilityEditValues(

    val maleWesternToiletCount: Int?,

    val maleJapaneseToiletCount: Int?,

    val femaleWesternToiletCount: Int?,

    val femaleJapaneseToiletCount: Int?,

    val hasBabyChair: Boolean?,

    val hasDiaperChangingTable: Boolean?,

    val hasAccessibleStall: Boolean?,

    val hasOstomate: Boolean?
)


@Composable
fun ToiletFacilityEditDialog(

    toilet: Toilet,

    isSaving: Boolean,

    onDismiss: () -> Unit,

    onSave: (ToiletFacilityEditValues) -> Unit

) {

    /*
     * =====================================
     * 男子
     * =====================================
     */
    var maleWesternText by
    remember(toilet.id) {
        mutableStateOf(
            toilet
                .maleWesternToiletCount
                ?.toString()
                .orEmpty()
        )
    }


    var maleJapaneseText by
    remember(toilet.id) {
        mutableStateOf(
            toilet
                .maleJapaneseToiletCount
                ?.toString()
                .orEmpty()
        )
    }


    /*
     * =====================================
     * 女子
     * =====================================
     */
    var femaleWesternText by
    remember(toilet.id) {
        mutableStateOf(
            toilet
                .femaleWesternToiletCount
                ?.toString()
                .orEmpty()
        )
    }


    var femaleJapaneseText by
    remember(toilet.id) {
        mutableStateOf(
            toilet
                .femaleJapaneseToiletCount
                ?.toString()
                .orEmpty()
        )
    }


    /*
     * =====================================
     * その他設備
     * =====================================
     */
    var hasBabyChair by
    remember(toilet.id) {
        mutableStateOf(
            toilet.hasBabyChair
        )
    }


    var hasDiaperChangingTable by
    remember(toilet.id) {
        mutableStateOf(
            toilet.hasDiaperChangingTable
        )
    }


    var hasAccessibleStall by
    remember(toilet.id) {
        mutableStateOf(
            toilet.hasAccessibleStall
        )
    }


    var hasOstomate by
    remember(toilet.id) {
        mutableStateOf(
            toilet.hasOstomate
        )
    }


    /*
     * =====================================
     * 入力エラー
     * =====================================
     */
    var maleWesternError by remember {
        mutableStateOf(false)
    }

    var maleJapaneseError by remember {
        mutableStateOf(false)
    }

    var femaleWesternError by remember {
        mutableStateOf(false)
    }

    var femaleJapaneseError by remember {
        mutableStateOf(false)
    }


    AlertDialog(

        onDismissRequest = {

            if (!isSaving) {
                onDismiss()
            }
        },


        title = {

            Text(
                text = "設備情報を編集",
                fontWeight = FontWeight.Bold
            )
        },


        text = {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 560.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {


                /*
                 * =====================================
                 * ポイント説明
                 * =====================================
                 */
                Text(

                    text =
                        "男子・女子は洋式と和式の両方がそろうと各1pt、その他の設備は4項目すべての情報がそろうと1pt獲得できます。",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.SemiBold
                )


                Text(

                    text =
                        "1つのトイレにつき、設備情報では最大3pt獲得できます。",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    fontWeight =
                        FontWeight.SemiBold
                )


                Text(
                    text =
                        "分からない項目は空欄のまま保存できます。",

                    style =
                        MaterialTheme.typography.bodySmall
                )


                /*
                 * =====================================
                 * 男子トイレ
                 * =====================================
                 */
                Text(
                    text = "男子トイレ",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )


                FacilityCountInput(
                    label =
                        "男子・洋式",

                    value =
                        maleWesternText,

                    isError =
                        maleWesternError,

                    enabled =
                        !isSaving,

                    onValueChange = {
                            newValue ->

                        maleWesternText =
                            newValue

                        maleWesternError =
                            false
                    }
                )


                FacilityCountInput(
                    label =
                        "男子・和式",

                    value =
                        maleJapaneseText,

                    isError =
                        maleJapaneseError,

                    enabled =
                        !isSaving,

                    onValueChange = {
                            newValue ->

                        maleJapaneseText =
                            newValue

                        maleJapaneseError =
                            false
                    }
                )


                /*
                 * =====================================
                 * 女子トイレ
                 * =====================================
                 */
                Text(
                    text = "女子トイレ",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )


                FacilityCountInput(
                    label =
                        "女子・洋式",

                    value =
                        femaleWesternText,

                    isError =
                        femaleWesternError,

                    enabled =
                        !isSaving,

                    onValueChange = {
                            newValue ->

                        femaleWesternText =
                            newValue

                        femaleWesternError =
                            false
                    }
                )


                FacilityCountInput(
                    label =
                        "女子・和式",

                    value =
                        femaleJapaneseText,

                    isError =
                        femaleJapaneseError,

                    enabled =
                        !isSaving,

                    onValueChange = {
                            newValue ->

                        femaleJapaneseText =
                            newValue

                        femaleJapaneseError =
                            false
                    }
                )


                /*
                 * =====================================
                 * その他設備
                 * =====================================
                 */
                Text(
                    text = "その他の設備",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )


                FacilityAvailabilitySelector(
                    label =
                        "ベビーチェア",

                    value =
                        hasBabyChair,

                    enabled =
                        !isSaving,

                    onValueChange = {
                        hasBabyChair = it
                    }
                )


                FacilityAvailabilitySelector(
                    label =
                        "おむつ交換台",

                    value =
                        hasDiaperChangingTable,

                    enabled =
                        !isSaving,

                    onValueChange = {
                        hasDiaperChangingTable = it
                    }
                )


                FacilityAvailabilitySelector(
                    label =
                        "車いす対応個室",

                    value =
                        hasAccessibleStall,

                    enabled =
                        !isSaving,

                    onValueChange = {
                        hasAccessibleStall = it
                    }
                )


                FacilityAvailabilitySelector(
                    label =
                        "オストメイト設備",

                    value =
                        hasOstomate,

                    enabled =
                        !isSaving,

                    onValueChange = {
                        hasOstomate = it
                    }
                )
            }
        },


        dismissButton = {

            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {

                Text(
                    "キャンセル"
                )
            }
        },


        confirmButton = {

            Button(
                enabled = !isSaving,

                onClick = {

                    /*
                     * =====================================
                     * 数値変換
                     * =====================================
                     */
                    val maleWestern =
                        parseCount(
                            maleWesternText
                        )

                    val maleJapanese =
                        parseCount(
                            maleJapaneseText
                        )

                    val femaleWestern =
                        parseCount(
                            femaleWesternText
                        )

                    val femaleJapanese =
                        parseCount(
                            femaleJapaneseText
                        )


                    /*
                     * =====================================
                     * エラー判定
                     * =====================================
                     */
                    maleWesternError =
                        maleWesternText.isNotBlank() &&
                                maleWestern == null


                    maleJapaneseError =
                        maleJapaneseText.isNotBlank() &&
                                maleJapanese == null


                    femaleWesternError =
                        femaleWesternText.isNotBlank() &&
                                femaleWestern == null


                    femaleJapaneseError =
                        femaleJapaneseText.isNotBlank() &&
                                femaleJapanese == null


                    if (
                        maleWesternError ||
                        maleJapaneseError ||
                        femaleWesternError ||
                        femaleJapaneseError
                    ) {

                        return@Button
                    }


                    /*
                     * =====================================
                     * 保存
                     * =====================================
                     */
                    onSave(
                        ToiletFacilityEditValues(

                            maleWesternToiletCount =
                                maleWestern,

                            maleJapaneseToiletCount =
                                maleJapanese,

                            femaleWesternToiletCount =
                                femaleWestern,

                            femaleJapaneseToiletCount =
                                femaleJapanese,

                            hasBabyChair =
                                hasBabyChair,

                            hasDiaperChangingTable =
                                hasDiaperChangingTable,

                            hasAccessibleStall =
                                hasAccessibleStall,

                            hasOstomate =
                                hasOstomate
                        )
                    )
                }

            ) {

                if (isSaving) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                18.dp
                            ),

                        strokeWidth =
                            2.dp
                    )


                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )


                    Text(
                        "保存中"
                    )

                } else {

                    Text(
                        "保存"
                    )
                }
            }
        }
    )
}


/*
 * =====================================
 * 台数入力
 * =====================================
 */
@Composable
private fun FacilityCountInput(

    label: String,

    value: String,

    isError: Boolean,

    enabled: Boolean,

    onValueChange: (String) -> Unit

) {

    OutlinedTextField(
        value = value,

        onValueChange = {
                newValue ->

            /*
             * 空欄または数字だけ許可
             */
            if (
                newValue.isEmpty() ||
                newValue.all {
                        character ->

                    character in '0'..'9'
                }
            ) {

                onValueChange(
                    newValue
                )
            }
        },

        modifier =
            Modifier.fillMaxWidth(),

        label = {

            Text(
                label
            )
        },

        suffix = {

            Text(
                "台"
            )
        },

        placeholder = {

            Text(
                "例：2"
            )
        },

        supportingText = {

            if (isError) {

                Text(
                    "正しい台数を入力してください"
                )

            } else {

                Text(
                    "空欄 = 情報なし"
                )
            }
        },

        isError =
            isError,

        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Number
            ),

        singleLine =
            true,

        enabled =
            enabled
    )
}


/*
 * =====================================
 * 空欄
 * ↓
 * null
 * =====================================
 */
private fun parseCount(
    text: String
): Int? {

    if (text.isBlank()) {
        return null
    }

    return text.toIntOrNull()
}


/*
 * =====================================
 * 情報なし / あり / なし
 * =====================================
 */
@Composable
private fun FacilityAvailabilitySelector(

    label: String,

    value: Boolean?,

    enabled: Boolean,

    onValueChange: (Boolean?) -> Unit

) {

    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {

        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge,
            fontWeight =
                FontWeight.SemiBold
        )


        FacilityRadioOption(
            text =
                "情報なし",

            selected =
                value == null,

            enabled =
                enabled,

            onClick = {
                onValueChange(null)
            }
        )


        FacilityRadioOption(
            text =
                "あり",

            selected =
                value == true,

            enabled =
                enabled,

            onClick = {
                onValueChange(true)
            }
        )


        FacilityRadioOption(
            text =
                "なし",

            selected =
                value == false,

            enabled =
                enabled,

            onClick = {
                onValueChange(false)
            }
        )
    }
}


@Composable
private fun FacilityRadioOption(

    text: String,

    selected: Boolean,

    enabled: Boolean,

    onClick: () -> Unit

) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(
                    vertical = 2.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        RadioButton(
            selected =
                selected,

            onClick =
                onClick,

            enabled =
                enabled
        )


        Spacer(
            modifier =
                Modifier.width(
                    6.dp
                )
        )


        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium
        )
    }
}