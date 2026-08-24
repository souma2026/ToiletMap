package com.example.toiletmap.screen.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.AccountRepository
import kotlinx.coroutines.launch


@Composable
fun AccountScreen() {

    /*
     * Supabaseのログイン状態を確認中かどうか
     */
    var checkingLogin by remember {
        mutableStateOf(true)
    }

    /*
     * ログイン状態
     */
    var isLoggedIn by remember {
        mutableStateOf(false)
    }


    /*
     * 画面を最初に表示したとき、
     * Supabaseにログイン済みユーザーがいるか確認
     */
    LaunchedEffect(Unit) {

        isLoggedIn =
            AccountRepository.isLoggedIn()

        checkingLogin =
            false
    }


    /*
     * =========================================
     * ログイン確認中
     * =========================================
     */

    if (checkingLogin) {

        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(100.dp)
            )

            CircularProgressIndicator()
        }

        return
    }


    /*
     * =========================================
     * ログイン済み
     * =========================================
     */

    if (isLoggedIn) {

        LoggedInAccountScreen(

            onLogout = {

                isLoggedIn =
                    false
            }
        )

        return
    }


    /*
     * =========================================
     * 未ログイン
     * =========================================
     */

    LoginAndRegisterScreen(

        onLoginSuccess = {

            isLoggedIn =
                true
        }
    )
}


/*
 * =========================================
 * ログイン / 新規登録画面
 * =========================================
 */

@Composable
fun LoginAndRegisterScreen(
    onLoginSuccess: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    /*
     * false = ログイン
     * true = アカウント登録
     */
    var registerMode by remember {
        mutableStateOf(false)
    }


    /*
     * 入力内容
     */
    var userName by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }


    /*
     * メッセージ
     */
    var message by remember {
        mutableStateOf("")
    }


    /*
     * 通信中か
     */
    var loading by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {


        /*
         * =========================================
         * タイトル
         * =========================================
         */

        Text(
            text =
                if (registerMode) {

                    "アカウント登録"

                } else {

                    "ログイン"
                },

            style =
                MaterialTheme.typography.headlineMedium,

            modifier =
                Modifier.fillMaxWidth()
        )


        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        /*
         * =========================================
         * ユーザー名
         *
         * 新規登録時だけ表示
         * =========================================
         */

        if (registerMode) {

            OutlinedTextField(
                value =
                    userName,

                onValueChange = {
                    userName = it
                },

                label = {
                    Text(
                        text = "ユーザー名"
                    )
                },

                modifier =
                    Modifier.fillMaxWidth(),

                singleLine =
                    true
            )


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }


        /*
         * =========================================
         * メールアドレス
         * =========================================
         */

        OutlinedTextField(
            value =
                email,

            onValueChange = {
                email = it
            },

            label = {
                Text(
                    text = "メールアドレス"
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Email
                ),

            singleLine =
                true
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        /*
         * =========================================
         * パスワード
         * =========================================
         */

        OutlinedTextField(
            value =
                password,

            onValueChange = {
                password = it
            },

            label = {
                Text(
                    text = "パスワード"
                )
            },

            modifier =
                Modifier.fillMaxWidth(),

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Password
                ),

            singleLine =
                true
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * =========================================
         * ログイン / 登録ボタン
         * =========================================
         */

        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !loading,

            onClick = {

                /*
                 * 入力チェック
                 */

                if (
                    registerMode &&
                    userName.trim().isEmpty()
                ) {

                    message =
                        "ユーザー名を入力してください"

                    return@Button
                }


                if (
                    email.trim().isEmpty()
                ) {

                    message =
                        "メールアドレスを入力してください"

                    return@Button
                }


                if (
                    password.isEmpty()
                ) {

                    message =
                        "パスワードを入力してください"

                    return@Button
                }


                if (
                    password.length < 6
                ) {

                    message =
                        "パスワードは6文字以上入力してください"

                    return@Button
                }


                loading =
                    true

                message =
                    ""


                scope.launch {

                    try {

                        /*
                         * =================================
                         * 新規登録
                         * =================================
                         */

                        if (registerMode) {

                            AccountRepository.signUp(

                                email =
                                    email.trim(),

                                password =
                                    password,

                                userName =
                                    userName.trim()
                            )


                            /*
                             * Confirm emailをOFFにしていれば
                             * 登録直後にログイン状態になる
                             */

                            if (
                                AccountRepository.isLoggedIn()
                            ) {

                                onLoginSuccess()

                            } else {

                                /*
                                 * Confirm emailがONの場合
                                 */

                                message =
                                    "登録しました。確認メールを確認してからログインしてください。"

                                registerMode =
                                    false
                            }


                        } else {

                            /*
                             * =================================
                             * ログイン
                             * =================================
                             */

                            AccountRepository.signIn(

                                email =
                                    email.trim(),

                                password =
                                    password
                            )


                            /*
                             * ログイン成功
                             */

                            if (
                                AccountRepository.isLoggedIn()
                            ) {

                                onLoginSuccess()

                            } else {

                                message =
                                    "ログインできませんでした"
                            }
                        }


                    } catch (e: Exception) {

                        /*
                         * Supabaseからエラーが返ってきた場合
                         */

                        message =
                            e.message
                                ?: "処理に失敗しました"

                    } finally {

                        loading =
                            false
                    }
                }
            }
        ) {

            Text(
                text =
                    when {

                        loading -> {
                            "処理中..."
                        }

                        registerMode -> {
                            "アカウント登録"
                        }

                        else -> {
                            "ログイン"
                        }
                    }
            )
        }


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        /*
         * =========================================
         * 登録 / ログイン切り替え
         * =========================================
         */

        TextButton(
            onClick = {

                registerMode =
                    !registerMode

                message =
                    ""
            }
        ) {

            Text(
                text =
                    if (registerMode) {

                        "すでにアカウントを持っている"

                    } else {

                        "新しいアカウントを作成"
                    }
            )
        }


        /*
         * =========================================
         * メッセージ表示
         * =========================================
         */

        if (
            message.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            Text(
                text =
                    message,

                color =
                    MaterialTheme.colorScheme.error
            )
        }
    }
}


/*
 * =========================================
 * ログイン後のアカウント画面
 * =========================================
 */

@Composable
fun LoggedInAccountScreen(
    onLogout: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    /*
     * 現在ログインしているユーザー
     */
    val currentUser =
        AccountRepository.getCurrentUser()


    /*
     * メッセージ
     */
    var message by remember {
        mutableStateOf("")
    }


    /*
     * ログアウト中か
     */
    var loggingOut by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {


        /*
         * =========================================
         * タイトル
         * =========================================
         */

        Text(
            text =
                "アカウント",

            style =
                MaterialTheme.typography.headlineMedium,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    top = 20.dp
                )
        )


        Spacer(
            modifier =
                Modifier.height(50.dp)
        )


        /*
         * =========================================
         * 仮プロフィール画像
         *
         * 写真は次の段階でSupabase Storageに接続
         * =========================================
         */

        Text(
            text =
                "👤",

            style =
                MaterialTheme.typography.displayLarge
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * =========================================
         * メールアドレス
         * =========================================
         */

        Text(
            text =
                "ログイン中のメールアドレス"
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(
            text =
                currentUser?.email
                    ?: "取得できませんでした",

            style =
                MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        /*
         * =========================================
         * UID
         *
         * 開発中の確認用
         * =========================================
         */

        Text(
            text =
                "ユーザーID"
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(
            text =
                currentUser?.id
                    ?: "取得できませんでした"
        )


        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        /*
         * =========================================
         * ログアウト
         * =========================================
         */

        OutlinedButton(
            enabled =
                !loggingOut,

            onClick = {

                loggingOut =
                    true

                message =
                    ""


                scope.launch {

                    try {

                        AccountRepository
                            .signOut()


                        onLogout()


                    } catch (e: Exception) {

                        message =
                            e.message
                                ?: "ログアウトに失敗しました"

                    } finally {

                        loggingOut =
                            false
                    }
                }
            }
        ) {

            Text(
                text =
                    if (loggingOut) {

                        "ログアウト中..."

                    } else {

                        "ログアウト"
                    }
            )
        }


        /*
         * =========================================
         * エラーメッセージ
         * =========================================
         */

        if (
            message.isNotEmpty()
        ) {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            Text(
                text =
                    message,

                color =
                    MaterialTheme.colorScheme.error
            )
        }


        Spacer(
            modifier =
                Modifier.height(50.dp)
        )
    }
}