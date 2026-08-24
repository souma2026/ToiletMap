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

    /*
     * 現在入力されているトイレ名
     */
    toiletName: String,

    /*
     * 清潔度
     * 1 ～ 5
     */
    cleanliness: Int,

    /*
     * コメント
     */
    comment: String,

    /*
     * 地図上で選択した場所
     *
     * まだ選択していない場合は null
     */
    latitude: Double?,
    longitude: Double?,

    /*
     * 入力内容が変更されたときの処理
     */
    onToiletNameChange: (String) -> Unit,

    onCleanlinessChange: (Int) -> Unit,

    onCommentChange: (String) -> Unit,

    /*
     * 「地図上で場所を選ぶ」
     * が押されたとき
     */
    onSelectLocation: () -> Unit,

    /*
     * 登録ボタン
     */
    onAddToilet: () -> Unit
) {

    /*
     * =====================================
     * 登録ボタンを押したか
     * =====================================
     *
     * 未入力エラーを表示するために使う
     */
    var triedToSubmit by remember {
        mutableStateOf(false)
    }

    /*
     * トイレ名が空か
     */
    val nameIsEmpty =
        toiletName.isBlank()

    /*
     * 場所が選択されていないか
     */
    val locationIsEmpty =
        latitude == null ||
                longitude == null

    /*
     * =====================================
     * 画面全体
     * =====================================
     */
    Column(

        modifier = Modifier
            .fillMaxSize()

            /*
             * 画面が小さいスマホでも
             * 下までスクロールできるようにする
             */
            .verticalScroll(
                rememberScrollState()
            )

            .padding(24.dp),

        verticalArrangement =
            Arrangement.Top
    ) {

        /*
         * =====================================
         * タイトル
         * =====================================
         */
        Text(
            text = "トイレを追加",
            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -----------------------------
        // トイレ名
        // -----------------------------

        OutlinedTextField(
            value = toiletName,

            onValueChange = {

                toiletName = it

                // 入力されたらエラー解除
                if (it.isNotBlank()) {
                    toiletNameError = false
                }
            },

            label = {
                Text("トイレ名")
            },

            placeholder = {
                Text("例：○○駅 公衆トイレ")
            },

            isError = toiletNameError,

            supportingText = {

                if (toiletNameError) {
                    Text("トイレ名を入力してください")
                }
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------
        // 場所
        // -----------------------------

        OutlinedTextField(
            value = location,

            onValueChange = {

                location = it

                // 入力されたらエラー解除
                if (it.isNotBlank()) {
                    locationError = false
                }
            },

            label = {
                Text("場所")
            },

            placeholder = {
                Text("例：○○駅 東口")
            },

            isError = locationError,

            supportingText = {

                if (locationError) {
                    Text("場所を入力してください")
                }
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // -----------------------------
        // 利用可能時間
        // -----------------------------

        OutlinedTextField(
            value = openingHours,

            onValueChange = {
                openingHours = it
            },

            label = {
                Text("利用可能時間")
            },

            placeholder = {
                Text("例：24時間")
            },

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // -----------------------------
        // きれいさ
        // -----------------------------

        Text(
            text =
                "トイレの情報を入力して、地図上から場所を選択してください。",
            modifier =
                Modifier.padding(top = 8.dp)
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        /*
         * =====================================
         * トイレ名
         * =====================================
         */
        OutlinedTextField(

            value = toiletName,

            onValueChange =
                onToiletNameChange,

            label = {
                Text(
                    text = "トイレ名 *"
                )
            },

            singleLine = true,

            /*
             * 登録しようとしたのに
             * 名前が空なら赤くする
             */
            isError =
                triedToSubmit &&
                        nameIsEmpty,

            supportingText = {

                if (
                    triedToSubmit &&
                    nameIsEmpty
                ) {

                    Text(
                        text =
                            "ここが未入力です"
                    )
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        /*
         * =====================================
         * 清潔度
         * =====================================
         */

        /*
         * ★★★★★
         * の形式で表示
         */
        Text(

            text =
                "清潔度：" +
                        "★".repeat(cleanliness) +
                        "☆".repeat(
                            5 - cleanliness
                        ),

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        /*
         * 1～5を選択できるスライダー
         */
        Slider(

            value =
                cleanliness.toFloat(),

            onValueChange = { value ->

                onCleanlinessChange(

                    value
                        .roundToInt()
                        .coerceIn(
                            1,
                            5
                        )
                )
            },

            valueRange =
                1f..5f,

            /*
             * 1 2 3 4 5
             * の5段階
             */
            steps = 3,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        /*
         * =====================================
         * コメント
         * =====================================
         */
        OutlinedTextField(

            value = comment,

            onValueChange =
                onCommentChange,

            label = {

                Text(
                    text = "コメント"
                )
            },

            placeholder = {

                Text(
                    text =
                        "例：駅の改札近く。洋式で比較的きれいです。"
                )
            },

            minLines = 3,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        /*
         * =====================================
         * 場所
         * =====================================
         */
        Text(

            text = "場所 *",

            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )

        /*
         * 場所が選択済みの場合
         */
        if (
            latitude != null &&
            longitude != null
        ) {

            Text(

                text =
                    "選択済み\n" +
                            "緯度：${"%.6f".format(latitude)}\n" +
                            "経度：${"%.6f".format(longitude)}",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )

        } else {

            /*
             * まだ場所が選ばれていない
             */
            Text(

                text =
                    "まだ場所が選択されていません",

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    )
            )
        }

        /*
         * =====================================
         * 地図から場所を選ぶボタン
         * =====================================
         */
        Button(

            onClick =
                onSelectLocation,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 12.dp
                )
        ) {

            /*
             * まだ選んでない
             */
            if (
                latitude == null ||
                longitude == null
            ) {

                Text(
                    text =
                        "地図上で場所を選ぶ"
                )

            } else {

                /*
                 * 一度選択済みの場合
                 */
                Text(
                    text =
                        "場所を選び直す"
                )
            }
        }

        /*
         * 場所未入力エラー
         */
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
            modifier =
                Modifier.height(28.dp)
        )

        /*
         * =====================================
         * 登録ボタン
         * =====================================
         */
        Button(

            onClick = {

                /*
                 * 登録しようとした
                 */
                triedToSubmit = true

                /*
                 * 名前と場所の両方が
                 * 入力されている場合のみ登録
                 */
                if (
                    !nameIsEmpty &&
                    !locationIsEmpty
                ) {

                    onAddToilet()

                    triedToSubmit = false
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {

            Text(
                text =
                    "このトイレを登録"
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }
}