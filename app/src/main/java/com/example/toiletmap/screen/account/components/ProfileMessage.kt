package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ProfileMessage(

    message: String,

    modifier: Modifier =
        Modifier
) {

    if (
        message.isBlank()
    ) {

        return
    }


    val isError =
        "失敗" in message ||
                "できません" in message


    Text(

        text =
            message,

        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

        color =
            if (
                isError
            ) {

                MaterialTheme
                    .colorScheme
                    .error

            } else {

                MaterialTheme
                    .colorScheme
                    .primary
            }
    )
}