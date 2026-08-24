package com.example.toiletmap.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AddToiletScreen(

    // トイレ名
    toiletName: String,

    // 清潔度 1～5
    cleanliness: Int,

    // コメント
    comment: String,

    // 地図上で選択した緯度・経度
    // まだ選択していない場合は null
    latitude: Double?,
    longitude: Double?,

    // トイレ名が変更されたとき
    onToiletNameChange: (String) -> Unit,

    // 清潔度が変更されたとき
    onCleanlinessChange: (Int) -> Unit,

    // コメントが変更されたとき
    onCommentChange: (String) -> Unit,

    // 「地図上で場所を選ぶ」を押したとき
    onSelectLocation: () -> Unit,

    // 登録ボタンを押したとき
    onAddToilet: () -> Unit
) {

    // 登録ボタンを一度押したかどうか
    // エラー表示に使用
    var triedToSubmit by remember {
        mutableStateOf(false)
    }

    // トイレ名が空か
    val nameIsEmpty =
        toiletName.isBlank()

    // 緯度または経度が選択されていないか
    val locationIsEmpty =
        latitude == null || longitude == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),

        verticalArrangement = Arrangement.Top
    ) {

        // =====================================
        // タイトル
        // =====================================

        Text(
            text = "トイレを追加",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "トイレの情報を入力して、地図上から場所を選択してください。",
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================
        // トイレ名
        // =====================================

        OutlinedTextField(
            value = toiletName,

            onValueChange = onToiletNameChange,

            label = {
                Text("トイレ名 *")
            },

            placeholder = {
                Text("例：○○駅 公衆トイレ")
            },

            singleLine = true,

            // 登録ボタンを押していて
            // なおかつ名前が空ならエラー
            isError =
                triedToSubmit &&
                        nameIsEmpty,

            supportingText = {

                if (
                    triedToSubmit &&
                    nameIsEmpty
                ) {

                    Text(
                        text = "トイレ名を入力してください"
                    )
                }
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================
        // 清潔度
        // =====================================

        Text(
            text =
                "清潔度：" +
                        "★".repeat(cleanliness) +
                        "☆".repeat(5 - cleanliness),

            style = MaterialTheme.typography.titleMedium
        )

        Slider(
            value = cleanliness.toFloat(),

            onValueChange = { value ->

                val newCleanliness =
                    value
                        .roundToInt()
                        .coerceIn(
                            1,
                            5
                        )

                onCleanlinessChange(
                    newCleanliness
                )
            },

            valueRange = 1f..5f,

            // 1, 2, 3, 4, 5
            steps = 3,

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================
        // コメント
        // =====================================

        OutlinedTextField(
            value = comment,

            onValueChange = onCommentChange,

            label = {
                Text("コメント")
            },

            placeholder = {
                Text(
                    "例：駅の改札近く。洋式で比較的きれいです。"
                )
            },

            minLines = 3,

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // =====================================
        // 場所
        // =====================================

        Text(
            text = "場所 *",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )


        // 場所が選択されている場合
        if (
            latitude != null &&
            longitude != null
        ) {

            Text(
                text =
                    "選択済み\n" +
                            "緯度：${"%.6f".format(latitude)}\n" +
                            "経度：${"%.6f".format(longitude)}"
            )

        } else {

            // まだ選択されていない場合
            Text(
                text = "まだ場所が選択されていません"
            )
        }


        // =====================================
        // 地図から場所を選択
        // =====================================

        Button(
            onClick = onSelectLocation,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 12.dp
                )
        ) {

            if (
                latitude == null ||
                longitude == null
            ) {

                Text(
                    text = "地図上で場所を選ぶ"
                )

            } else {

                Text(
                    text = "場所を選び直す"
                )
            }
        }


        // =====================================
        // 場所のエラー
        // =====================================

        if (
            triedToSubmit &&
            locationIsEmpty
        ) {

            Text(
                text =
                    "場所が未入力です。地図上で場所を選択してください。",

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )
        }


        Spacer(
            modifier = Modifier.height(28.dp)
        )


        // =====================================
        // 登録ボタン
        // =====================================

        Button(
            onClick = {

                // 登録を試みた状態にする
                triedToSubmit = true

                // 名前と場所が入力されている場合
                if (
                    !nameIsEmpty &&
                    !locationIsEmpty
                ) {

                    // 登録処理を呼ぶ
                    onAddToilet()

                    // エラー表示状態を解除
                    triedToSubmit = false
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {

            Text(
                text = "このトイレを登録"
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}