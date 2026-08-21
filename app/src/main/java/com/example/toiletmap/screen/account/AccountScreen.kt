package com.example.toiletmap.screen.account


import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "アカウント",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(
            text = "👤",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "ユーザー名：テストユーザー"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "ポイント：120 pt"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "トイレ投稿数：5件"
        )
    }
}