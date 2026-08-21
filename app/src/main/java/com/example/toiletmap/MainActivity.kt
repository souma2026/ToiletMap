package com.example.toiletmap

import com.example.toiletmap.screen.account.AccountScreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.toiletmap.ui.theme.ToiletMapTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ToiletMapTheme {
                ToiletMapApp()
            }
        }
    }
}

@Composable
fun ToiletMapApp() {

    // 現在選択している画面
    // 0 = マップ
    // 1 = アカウント
    // 2 = 追加
    var selectedScreen by rememberSaveable {
        mutableIntStateOf(0)
    }

    Scaffold(

        // 画面下のボトムバー
        bottomBar = {

            NavigationBar {

                // マップ
                NavigationBarItem(
                    selected = selectedScreen == 0,
                    onClick = {
                        selectedScreen = 0
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
                        selectedScreen = 1
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
                        selectedScreen = 2
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

    ) { innerPadding ->

        // ボトムバーより上の部分
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // 選択されている画面を表示
            when (selectedScreen) {

                0 -> MapScreen()

                1 -> AccountScreen()

                2 -> AddToiletScreen()
            }
        }
    }
}


/*
 * =========================
 * マップ画面
 * =========================
 */

@Composable
fun MapScreen() {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "マップ画面",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "ここにGoogle Mapsを表示します",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


/*
 * =========================
 * トイレ追加画面
 * =========================
 */

@Composable
fun AddToiletScreen() {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "トイレ追加画面",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "ここからトイレを登録できるようにします",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}