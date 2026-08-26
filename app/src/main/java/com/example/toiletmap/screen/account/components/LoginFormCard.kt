package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun LoginFormCard(

    registerMode: Boolean,

    userName: String,

    onUserNameChange:
        (String) -> Unit,

    email: String,

    onEmailChange:
        (String) -> Unit,

    password: String,

    onPasswordChange:
        (String) -> Unit,

    loading: Boolean,

    onSubmit: () -> Unit,

    onToggleMode: () -> Unit,

    modifier: Modifier =
        Modifier
) {

    Surface(

        modifier =
            modifier
                .fillMaxWidth(),

        shape =
            RoundedCornerShape(
                24.dp
            ),

        tonalElevation =
            2.dp
    ) {

        Column(

            modifier =
                Modifier.padding(
                    20.dp
                )
        ) {

            /*
             * =====================================
             * 新規登録時のみユーザー名
             * =====================================
             */
            if (
                registerMode
            ) {

                OutlinedTextField(

                    value =
                        userName,

                    onValueChange =
                        onUserNameChange,

                    label = {

                        Text(
                            "ユーザー名"
                        )
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine =
                        true,

                    shape =
                        RoundedCornerShape(
                            14.dp
                        )
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )
            }


            /*
             * =====================================
             * メールアドレス
             * =====================================
             */
            OutlinedTextField(

                value =
                    email,

                onValueChange =
                    onEmailChange,

                label = {

                    Text(
                        "メールアドレス"
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
                    true,

                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )


            /*
             * =====================================
             * パスワード
             * =====================================
             */
            OutlinedTextField(

                value =
                    password,

                onValueChange =
                    onPasswordChange,

                label = {

                    Text(
                        "パスワード"
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
                    true,

                shape =
                    RoundedCornerShape(
                        14.dp
                    )
            )


            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )


            /*
             * =====================================
             * ログイン / 登録
             * =====================================
             */
            Button(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        ),

                enabled =
                    !loading,

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

                onClick =
                    onSubmit
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


            /*
             * =====================================
             * ログイン ⇔ 新規登録 切り替え
             * =====================================
             */
            TextButton(

                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onToggleMode
            ) {

                Text(

                    text =
                        if (
                            registerMode
                        ) {

                            "すでにアカウントを持っている"

                        } else {

                            "新しいアカウントを作成"
                        }
                )
            }
        }
    }
}