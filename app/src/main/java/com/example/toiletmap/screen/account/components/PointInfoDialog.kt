package com.example.toiletmap.screen.account.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun PointInfoDialog(

    visible: Boolean,

    onDismiss: () -> Unit
) {

    if (
        !visible
    ) {

        return
    }


    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {

            Text(
                "ポイントについて"
            )
        },

        text = {

            Text(
                "清掃依頼、清掃完了、ポイントの獲得・交換はログインしないと行うことはできません。"
            )
        },

        confirmButton = {

            TextButton(

                onClick =
                    onDismiss
            ) {

                Text(
                    "閉じる"
                )
            }
        }
    )
}