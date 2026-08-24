package com.example.toiletmap.screen.account

import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.toiletmap.data.repository.AccountRepository
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale


// =========================================
// メールアドレス正規化
// =========================================

private fun normalizeEmail(
    email: String
): String {

    return Normalizer
        .normalize(
            email,
            Normalizer.Form.NFKC
        )
        .trim()
        .replace("\u200B", "")
        .replace("\u200C", "")
        .replace("\u200D", "")
        .replace("\uFEFF", "")
        .lowercase(Locale.ROOT)
}


// =========================================
// 認証エラーを日本語に変換
// =========================================

private fun friendlyAuthError(
    error: Exception
): String {

    val text =
        error.message
            ?.lowercase()
            ?: ""


    return when {

        "invalid login credentials" in text -> {

            "メールアドレスまたはパスワードが正しくありません"
        }


        "email not confirmed" in text -> {

            "メールアドレスの確認が完了していません"
        }


        "user already registered" in text -> {

            "このメールアドレスはすでに登録されています"
        }


        "rate limit" in text -> {

            "短時間に操作が集中しました。少し待ってから再試行してください"
        }


        else -> {

            "処理に失敗しました"
        }
    }
}


// =========================================
// ログイン / 新規登録
// =========================================

@Composable
fun LoginAndRegisterScreen(
    onLoginSuccess: () -> Unit
) {

    val scope =
        rememberCoroutineScope()


    var registerMode by remember {
        mutableStateOf(false)
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
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp)
    ) {


        Spacer(
            modifier =
                Modifier.height(12.dp)
        )


        Text(
            text =
                if (registerMode) {

                    "アカウント登録"

                } else {

                    "ログイン"
                },

            style =
                MaterialTheme.typography.headlineMedium
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(
            text =
                if (registerMode) {

                    "新しいアカウントを作成します"

                } else {

                    "ToiletMapにログインしてください"
                },

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(24.dp),

            tonalElevation =
                2.dp
        ) {

            Column(
                modifier =
                    Modifier.padding(20.dp)
            ) {


                if (registerMode) {

                    OutlinedTextField(
                        value =
                            userName,

                        onValueChange = {

                            userName = it
                        },

                        label = {

                            Text("ユーザー名")
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        singleLine =
                            true,

                        shape =
                            RoundedCornerShape(14.dp)
                    )


                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )
                }


                OutlinedTextField(
                    value =
                        email,

                    onValueChange = {

                        email = it
                    },

                    label = {

                        Text("メールアドレス")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Email
                        ),

                    singleLine =
                        true,

                    shape =
                        RoundedCornerShape(14.dp)
                )


                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )


                OutlinedTextField(
                    value =
                        password,

                    onValueChange = {

                        password = it
                    },

                    label = {

                        Text("パスワード")
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
                        true,

                    shape =
                        RoundedCornerShape(14.dp)
                )


                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )


                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),

                    enabled =
                        !loading,

                    shape =
                        RoundedCornerShape(14.dp),

                    onClick = {

                        val normalizedEmail =
                            normalizeEmail(
                                email
                            )


                        if (
                            registerMode &&
                            userName.isBlank()
                        ) {

                            message =
                                "ユーザー名を入力してください"

                            return@Button
                        }


                        if (
                            !Patterns.EMAIL_ADDRESS
                                .matcher(
                                    normalizedEmail
                                )
                                .matches()
                        ) {

                            message =
                                "メールアドレスの形式が正しくありません"

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

                                if (registerMode) {

                                    AccountRepository
                                        .signUp(
                                            normalizedEmail,
                                            password,
                                            userName.trim()
                                        )

                                } else {

                                    AccountRepository
                                        .signIn(
                                            normalizedEmail,
                                            password
                                        )
                                }


                                if (
                                    AccountRepository
                                        .isLoggedIn()
                                ) {

                                    onLoginSuccess()

                                } else {

                                    message =
                                        "ログイン状態を確認できませんでした"
                                }


                            } catch (e: Exception) {

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


                TextButton(
                    modifier =
                        Modifier.fillMaxWidth(),

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
            }
        }


        if (message.isNotBlank()) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
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