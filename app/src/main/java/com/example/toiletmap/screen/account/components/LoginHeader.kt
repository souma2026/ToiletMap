package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun LoginHeader(

    registerMode: Boolean,

    modifier: Modifier =
        Modifier
) {

    Column(
        modifier =
            modifier
    ) {

        Text(

            text =
                if (
                    registerMode
                ) {

                    "アカウント登録"

                } else {

                    "ログイン"
                },

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )


        Spacer(
            modifier =
                Modifier.height(
                    8.dp
                )
        )


        Text(

            text =
                if (
                    registerMode
                ) {

                    "新しいアカウントを作成します"

                } else {

                    "ToiletMapにログインしてください"
                },

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