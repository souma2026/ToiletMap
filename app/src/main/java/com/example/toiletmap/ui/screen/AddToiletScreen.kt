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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddToiletScreen() {

    // トイレ名
    var toiletName by remember {
        mutableStateOf("")
    }

    var toiletNameError by remember {
        mutableStateOf(false)
    }

    // 場所
    var location by remember {
        mutableStateOf("")
    }

    var locationError by remember {
        mutableStateOf(false)
    }


    // 利用可能時間
    var openingHours by remember {
        mutableStateOf("")
    }

    // コメント
    var comment by remember {
        mutableStateOf("")
    }

    // きれいさ 1〜5
    var cleanliness by remember {
        mutableFloatStateOf(3f)
    }

    // 登録ボタンを押した後に表示するメッセージ
    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),

        verticalArrangement = Arrangement.Top
    ) {

        // タイトル
        Text(
            text = "トイレ情報を追加",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // トイレ名
        OutlinedTextField(
            value = toiletName,
            onValueChange = {
                toiletName = it

                // 入力されたらエラーを解除
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

        // 場所
        OutlinedTextField(
            value = location,
            onValueChange = {
                location = it

                // 入力されたらエラーを解除
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

        // 利用可能時間
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

        // きれいさ
        Text(
            text = "きれいさ：${cleanliness.toInt()} / 5"
        )

        Slider(
            value = cleanliness,

            onValueChange = {
                cleanliness = it
            },

            valueRange = 1f..5f,

            steps = 3
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // コメント
        OutlinedTextField(
            value = comment,

            onValueChange = {
                comment = it
            },

            label = {
                Text("コメント")
            },

            placeholder = {
                Text("例：駅の東口付近にあります")
            },

            modifier = Modifier.fillMaxWidth(),

            minLines = 3
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // 登録ボタン
        Button(
            onClick = {

                // 未入力かチェック
                toiletNameError = toiletName.isBlank()
                locationError = location.isBlank()

                // 両方入力されている場合だけ登録成功
                message = if (!toiletNameError && !locationError) {
                    "トイレ情報を登録しました！"
                } else {
                    ""
                }
            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("登録する")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // メッセージ表示
        if (message.isNotEmpty()) {

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = message
            )
        }
    }
}