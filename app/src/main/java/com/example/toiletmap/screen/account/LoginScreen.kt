package com.example.toiletmap.screen.account

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.screen.account.components.LoginFormCard
import com.example.toiletmap.screen.account.components.LoginHeader
import com.example.toiletmap.screen.account.components.LoginPointNoticeCard
import com.example.toiletmap.screen.account.components.PointInfoDialog
import kotlinx.coroutines.launch


@Composable
fun LoginAndRegisterScreen(

    onLoginSuccess: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    /*
     * =========================================
     * State
     * =========================================
     */

    var registerMode by remember {

        mutableStateOf(
            false
        )
    }


    var userName by remember {

        mutableStateOf("")
    }


    var email by remember {

        mutableStateOf("")
    }


    var password by remember {

        mutableStateOf("")
    }


    var message by remember {

        mutableStateOf("")
    }


    var loading by remember {

        mutableStateOf(
            false
        )
    }


    var showPointInfo by remember {

        mutableStateOf(
            false
        )
    }


    /*
     * =========================================
     * ログイン / 新規登録実行
     * =========================================
     */
    fun submit() {

        val normalizedEmail =
            normalizeEmail(
                email
            )


        /*
         * ユーザー名チェック
         */
        if (
            registerMode &&
            userName.isBlank()
        ) {

            message =
                "ユーザー名を入力してください"

            return
        }


        /*
         * メールアドレスチェック
         */
        if (
            !isValidEmail(
                normalizedEmail
            )
        ) {

            message =
                "メールアドレスの形式が正しくありません"

            return
        }


        /*
         * パスワードチェック
         */
        if (
            password.length < 6
        ) {

            message =
                "パスワードは6文字以上入力してください"

            return
        }


        loading =
            true

        message =
            ""


        scope.launch {

            try {

                /*
                 * 新規登録
                 */
                if (
                    registerMode
                ) {

                    AccountRepository
                        .signUp(

                            normalizedEmail,

                            password,

                            userName.trim()
                        )

                } else {

                    /*
                     * ログイン
                     */
                    AccountRepository
                        .signIn(

                            normalizedEmail,

                            password
                        )
                }


                /*
                 * 認証成功確認
                 */
                if (
                    AccountRepository
                        .isLoggedIn()
                ) {

                    onLoginSuccess()

                } else {

                    message =
                        "ログイン状態を確認できませんでした"
                }


            } catch (
                e: Exception
            ) {

                Log.e(
                    "AccountAuth",
                    "Auth failed",
                    e
                )


                message =
                    friendlyAuthError(
                        e
                    )


            } finally {

                loading =
                    false
            }
        }
    }


    /*
     * =========================================
     * UI
     * =========================================
     */

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .background
                )
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    24.dp
                )
    ) {

        Spacer(
            modifier =
                Modifier.height(
                    12.dp
                )
        )


        /*
         * タイトル
         */
        LoginHeader(

            registerMode =
                registerMode
        )


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        /*
         * 未ログインポイント案内
         */
        LoginPointNoticeCard(

            onInfoClick = {

                showPointInfo =
                    true
            }
        )


        Spacer(
            modifier =
                Modifier.height(
                    20.dp
                )
        )


        /*
         * ログインフォーム
         */
        LoginFormCard(

            registerMode =
                registerMode,

            userName =
                userName,

            onUserNameChange = {

                userName =
                    it
            },

            email =
                email,

            onEmailChange = {

                email =
                    it
            },

            password =
                password,

            onPasswordChange = {

                password =
                    it
            },

            loading =
                loading,

            onSubmit =
                ::submit,

            onToggleMode = {

                registerMode =
                    !registerMode

                message =
                    ""
            }
        )


        /*
         * エラーメッセージ
         */
        if (
            message.isNotBlank()
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            Text(

                text =
                    message,

                color =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        }
    }


    /*
     * =========================================
     * ポイント説明ダイアログ
     * =========================================
     */

    PointInfoDialog(

        visible =
            showPointInfo,

        onDismiss = {

            showPointInfo =
                false
        }
    )
}