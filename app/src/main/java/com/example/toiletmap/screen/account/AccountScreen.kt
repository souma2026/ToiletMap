package com.example.toiletmap.screen.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.toiletmap.data.repository.AccountRepository


@Composable
fun AccountScreen() {

    /*
     * null  = Supabaseのログイン状態を確認中
     * true  = ログイン済み
     * false = 未ログイン
     */
    var isLoggedIn by remember {
        mutableStateOf<Boolean?>(
            null
        )
    }


    // =========================================
    // アプリ起動後・画面生成時
    //
    // Supabaseが端末に保存している
    // セッションの読み込み完了を待つ
    // =========================================

    LaunchedEffect(Unit) {

        isLoggedIn =
            AccountRepository
                .restoreLoginState()
    }


    // =========================================
    // 状態によって画面を切り替える
    // =========================================

    when (isLoggedIn) {


        // -------------------------------------
        // Supabase初期化中
        // -------------------------------------

        null -> {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                CircularProgressIndicator()
            }
        }


        // -------------------------------------
        // ログイン済み
        // -------------------------------------

        true -> {

            ProfileScreen(

                onLogout = {

                    isLoggedIn =
                        false
                }
            )
        }


        // -------------------------------------
        // 未ログイン
        // -------------------------------------

        false -> {

            LoginAndRegisterScreen(

                onLoginSuccess = {

                    isLoggedIn =
                        true
                }
            )
        }
    }
}