package com.example.toiletmap.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNavigationBar(
    selectedScreen: Int,
    onScreenSelected: (Int) -> Unit
) {

    NavigationBar {

        // マップ
        NavigationBarItem(
            selected = selectedScreen == 0,
            onClick = {
                onScreenSelected(0)
            },
            icon = {
                Text("🗺")
            },
            label = {
                Text("マップ")
            }
        )


        // アカウント
        NavigationBarItem(
            selected = selectedScreen == 1,
            onClick = {
                onScreenSelected(1)
            },
            icon = {
                Text("👤")
            },
            label = {
                Text("アカウント")
            }
        )


        // 追加
        NavigationBarItem(
            selected = selectedScreen == 2,
            onClick = {
                onScreenSelected(2)
            },
            icon = {
                Text("＋")
            },
            label = {
                Text("追加")
            }
        )
    }
}