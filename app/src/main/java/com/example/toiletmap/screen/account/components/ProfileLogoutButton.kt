package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun ProfileLogoutButton(

    onClick: () -> Unit,

    modifier: Modifier =
        Modifier
) {

    OutlinedButton(

        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        onClick =
            onClick
    ) {

        Text(
            "ログアウト"
        )
    }
}