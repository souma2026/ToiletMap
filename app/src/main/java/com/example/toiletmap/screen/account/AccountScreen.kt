package com.example.toiletmap.screen.account

import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale


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


/*
 * ログイン失敗理由を
 * ユーザーに分かる形へ変換
 */
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


@Composable
fun AccountScreen() {

    var isLoggedIn by remember {
        mutableStateOf(
            AccountRepository.isLoggedIn()
        )
    }


    if (isLoggedIn) {

        ProfileScreen(
            onLogout = {

                isLoggedIn =
                    false
            }
        )

    } else {

        LoginAndRegisterScreen(
            onLoginSuccess = {

                isLoggedIn =
                    true
            }
        )
    }
}


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
            .verticalScroll(
                rememberScrollState()
            )
            .padding(24.dp)
    ) {


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
                Modifier.height(30.dp)
        )


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
                    true
            )


            Spacer(
                modifier =
                    Modifier.height(15.dp)
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
                true
        )


        Spacer(
            modifier =
                Modifier.height(15.dp)
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
                true
        )


        Spacer(
            modifier =
                Modifier.height(25.dp)
        )


        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !loading,

            onClick = {

                val normalizedEmail =
                    normalizeEmail(email)


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
                        .matcher(normalizedEmail)
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
                            AccountRepository.isLoggedIn()
                        ) {

                            onLoginSuccess()

                        } else {

                            message =
                                "ログイン状態を確認できませんでした"
                        }


                    } catch (e: Exception) {

                        /*
                         * Logcatには本当の原因を残す
                         */
                        Log.e(
                            "AccountAuth",
                            "Auth failed",
                            e
                        )


                        message =
                            friendlyAuthError(e)

                    } finally {

                        loading =
                            false
                    }
                }
            }
        ) {

            Text(
                if (loading) {

                    "処理中..."

                } else if (registerMode) {

                    "アカウント登録"

                } else {

                    "ログイン"
                }
            )
        }


        TextButton(
            onClick = {

                registerMode =
                    !registerMode

                message =
                    ""
            }
        ) {

            Text(
                if (registerMode) {

                    "すでにアカウントを持っている"

                } else {

                    "新しいアカウントを作成"
                }
            )
        }


        if (message.isNotBlank()) {

            Text(
                text =
                    message,

                color =
                    MaterialTheme.colorScheme.error
            )
        }
    }
}


@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()


    val currentUser =
        AccountRepository
            .getCurrentUser()


    if (currentUser == null) {

        LaunchedEffect(Unit) {
            onLogout()
        }

        return
    }


    val userId =
        currentUser.id


    var profile by remember {
        mutableStateOf<UserProfile?>(null)
    }


    var history by remember {
        mutableStateOf<List<ToiletEditHistory>>(
            emptyList()
        )
    }


    var editingName by remember {
        mutableStateOf("")
    }


    var editing by remember {
        mutableStateOf(false)
    }


    var showHistory by remember {
        mutableStateOf(false)
    }


    var loading by remember {
        mutableStateOf(true)
    }


    var uploading by remember {
        mutableStateOf(false)
    }


    var message by remember {
        mutableStateOf("")
    }


    suspend fun reloadProfile() {

        profile =
            AccountRepository
                .loadProfile(
                    userId
                )

        editingName =
            profile?.username
                ?: ""
    }


    LaunchedEffect(userId) {

        try {

            reloadProfile()

            history =
                AccountRepository
                    .loadHistory(
                        userId
                    )

        } catch (e: Exception) {

            message =
                "プロフィール取得に失敗しました"

        } finally {

            loading =
                false
        }
    }


    val photoPicker =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .PickVisualMedia()

        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }


            scope.launch {

                uploading =
                    true

                try {

                    val bytes =
                        context
                            .contentResolver
                            .openInputStream(uri)
                            ?.use {
                                it.readBytes()
                            }


                    if (bytes == null) {

                        message =
                            "写真を読み込めませんでした"

                        return@launch
                    }


                    val newUrl =
                        AccountRepository
                            .uploadAvatar(
                                userId,
                                bytes
                            )


                    profile =
                        profile?.copy(
                            avatarUrl =
                                newUrl
                        )


                    message =
                        "写真を変更しました"


                } catch (e: Exception) {

                    Log.e(
                        "AccountPhoto",
                        "Photo upload failed",
                        e
                    )

                    message =
                        "写真の変更に失敗しました"

                } finally {

                    uploading =
                        false
                }
            }
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
                Modifier.height(30.dp)
        )


        if (loading) {

            CircularProgressIndicator()

            return@Column
        }


        /*
         * プロフィール写真
         */

        if (
            !profile?.avatarUrl.isNullOrBlank()
        ) {

            AsyncImage(
                model =
                    profile?.avatarUrl,

                contentDescription =
                    "プロフィール画像",

                modifier =
                    Modifier
                        .size(120.dp)
                        .clip(CircleShape),

                contentScale =
                    ContentScale.Crop
            )

        } else {

            Text(
                text =
                    "👤",

                style =
                    MaterialTheme.typography.displayLarge
            )
        }


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        Button(
            enabled =
                !uploading,

            onClick = {

                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .ImageOnly
                    )
                )
            }
        ) {

            Text(
                if (uploading) {

                    "アップロード中..."

                } else {

                    "写真を変更"
                }
            )
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * ユーザー名
         */

        if (editing) {

            OutlinedTextField(
                value =
                    editingName,

                onValueChange = {
                    editingName = it
                },

                label = {
                    Text("ユーザー名")
                },

                singleLine =
                    true
            )


            Row {

                Button(
                    onClick = {

                        if (
                            editingName.isBlank()
                        ) {

                            message =
                                "ユーザー名を入力してください"

                            return@Button
                        }


                        scope.launch {

                            try {

                                AccountRepository
                                    .updateUserName(
                                        userId,
                                        editingName.trim()
                                    )


                                profile =
                                    profile?.copy(
                                        username =
                                            editingName.trim()
                                    )


                                editing =
                                    false


                            } catch (e: Exception) {

                                message =
                                    "ユーザー名変更に失敗しました"
                            }
                        }
                    }
                ) {

                    Text("保存")
                }


                TextButton(
                    onClick = {

                        editingName =
                            profile?.username
                                ?: ""

                        editing =
                            false
                    }
                ) {

                    Text("キャンセル")
                }
            }

        } else {

            Text(
                text =
                    "ユーザー名：${profile?.username ?: ""}"
            )


            TextButton(
                onClick = {

                    editing =
                        true
                }
            ) {

                Text(
                    "ユーザー名を変更"
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(15.dp)
        )


        Text(
            text =
                currentUser.email
                    ?: ""
        )


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        /*
         * ポイント
         */

        Text(
            text =
                "ポイント：${profile?.points ?: 0} pt",

            style =
                MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * 履歴
         */

        Text(
            text =
                "トイレ編集履歴：${history.size}件",

            style =
                MaterialTheme.typography.titleMedium
        )


        Button(
            onClick = {

                showHistory =
                    !showHistory


                if (showHistory) {

                    scope.launch {

                        try {

                            history =
                                AccountRepository
                                    .loadHistory(
                                        userId
                                    )

                        } catch (e: Exception) {

                            message =
                                "履歴取得に失敗しました"
                        }
                    }
                }
            }
        ) {

            Text(
                if (showHistory) {

                    "履歴を閉じる"

                } else {

                    "履歴を見る"
                }
            )
        }


        if (showHistory) {

            Spacer(
                modifier =
                    Modifier.height(15.dp)
            )


            if (history.isEmpty()) {

                Text(
                    "まだ履歴はありません"
                )

            } else {

                history.forEach { item ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 5.dp
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                text =
                                    item.toiletName,

                                style =
                                    MaterialTheme.typography.titleMedium
                            )


                            Text(
                                text =
                                    item.action
                            )


                            Text(
                                text =
                                    item.editedAt
                                        .replace(
                                            "T",
                                            " "
                                        )
                                        .take(16),

                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }


        if (message.isNotBlank()) {

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                message
            )
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        OutlinedButton(
            onClick = {

                scope.launch {

                    try {

                        AccountRepository
                            .signOut()

                        onLogout()

                    } catch (e: Exception) {

                        message =
                            "ログアウトに失敗しました"
                    }
                }
            }
        ) {

            Text(
                "ログアウト"
            )
        }


        Spacer(
            modifier =
                Modifier.height(50.dp)
        )
    }
}