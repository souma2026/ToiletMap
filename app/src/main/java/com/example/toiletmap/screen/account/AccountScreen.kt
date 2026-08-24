package com.example.toiletmap.screen.account

import android.util.Patterns
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
import java.text.Normalizer
import java.util.Locale


/*
 * =========================================
 * メールアドレスをSupabaseへ送る前に整形
 * =========================================
 *
 * 全角文字などを半角相当に変換し、
 * コピー時に混ざることがある
 * 見えない文字も取り除く。
 */
private fun normalizeEmail(
    email: String
): String {

    return Normalizer
        .normalize(
            email,
            Normalizer.Form.NFKC
        )
        .trim()

        // ゼロ幅スペースなどを削除
        .replace("\u200B", "")
        .replace("\u200C", "")
        .replace("\u200D", "")
        .replace("\uFEFF", "")

        // メールアドレスは小文字へ統一
        .lowercase(Locale.ROOT)
}


@Composable
fun AccountScreen() {

    /*
     * Supabaseのログイン状態確認中
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
     * 最初にログイン状態を確認
     */
    LaunchedEffect(Unit) {

        isLoggedIn =
            AccountRepository.isLoggedIn()

        checkingLogin =
            false
    }


    /*
     * =========================================
     * ログイン状態確認中
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
 * ログイン / アカウント登録画面
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
     * true  = 新規登録
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
     * Supabaseと通信中か
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
         * 新規登録時のみ表示
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
         * 登録 / ログインボタン
         * =========================================
         */

        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !loading,

            onClick = {

                /*
                 * =================================
                 * メールアドレスを正規化
                 * =================================
                 */

                val normalizedEmail =
                    normalizeEmail(email)


                /*
                 * =================================
                 * 入力チェック
                 * =================================
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
                    normalizedEmail.isEmpty()
                ) {

                    message =
                        "メールアドレスを入力してください"

                    return@Button
                }


                /*
                 * Android側でも
                 * メールアドレス形式を確認
                 */
                if (
                    !Patterns.EMAIL_ADDRESS
                        .matcher(normalizedEmail)
                        .matches()
                ) {

                    message =
                        "メールアドレスの形式が正しくありません"

                    return@Button
                }


                if (
                    password.isEmpty()
                ) {

                    message =
                        "パスワードを入力してください"

                    return@Button
                }


                /*
                 * Supabaseではパスワード設定によって
                 * 最低文字数が決まる。
                 *
                 * とりあえず6文字未満を弾く。
                 */
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
                         * =========================
                         * 新規登録
                         * =========================
                         */

                        if (registerMode) {

                            AccountRepository.signUp(

                                /*
                                 * ここが重要
                                 *
                                 * 入力されたemailではなく、
                                 * 正規化したメールアドレスを送る。
                                 */
                                email =
                                    normalizedEmail,

                                password =
                                    password,

                                userName =
                                    userName.trim()
                            )


                            /*
                             * Confirm emailをOFFにしている場合、
                             * 登録後すぐログイン状態になる。
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
                             * =========================
                             * ログイン
                             * =========================
                             */

                            AccountRepository.signIn(

                                email =
                                    normalizedEmail,

                                password =
                                    password
                            )


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
                         * Supabaseから返された長い通信情報を
                         * そのまま画面に表示しない。
                         */

                        message =
                            if (registerMode) {

                                "アカウント登録に失敗しました"

                            } else {

                                "ログインに失敗しました"
                            }


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
         * ログイン / 新規登録の切り替え
         * =========================================
         */

        TextButton(
            enabled =
                !loading,

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
         * エラー等を表示
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
     * 現在ログイン中のSupabaseユーザー
     */
    val currentUser =
        AccountRepository
            .getCurrentUser()


    var message by remember {
        mutableStateOf("")
    }


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
         * プロフィール写真は
         * 次の段階でSupabase Storageに接続
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
                            "ログアウトに失敗しました"

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