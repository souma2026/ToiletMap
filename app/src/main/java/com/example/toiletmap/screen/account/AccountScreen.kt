package com.example.toiletmap.screen.account

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.toiletmap.data.repository.AccountRepository
import com.example.toiletmap.model.ToiletEditHistory
import com.example.toiletmap.model.UserProfile
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max


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
// ログイン失敗理由
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
// 選択された写真をJPEGへ変換
//
// 選択画像が
//
// PNG
// WebP
// HEIC
//
// などでも、Storageには
//
// profile.jpg
//
// として保存するので、
// アップロード前にJPEGへ統一する。
// =========================================

private fun createAvatarJpegBytes(
    context: Context,
    uri: Uri
): ByteArray? {

    val originalBitmap =

        context
            .contentResolver
            .openInputStream(uri)
            ?.use {

                BitmapFactory.decodeStream(it)
            }
            ?: return null


    /*
     * 巨大な写真をそのままアップロードすると
     * メモリ使用量や通信量が増えるため
     *
     * 最大1200px
     *
     * に縮小する。
     */

    val maxDimension = 1200


    val currentMaxDimension =
        max(
            originalBitmap.width,
            originalBitmap.height
        )


    val bitmapToUpload: Bitmap =

        if (currentMaxDimension > maxDimension) {

            val scale =
                maxDimension.toFloat() /
                        currentMaxDimension.toFloat()


            val newWidth =
                (
                        originalBitmap.width *
                                scale
                        )
                    .toInt()
                    .coerceAtLeast(1)


            val newHeight =
                (
                        originalBitmap.height *
                                scale
                        )
                    .toInt()
                    .coerceAtLeast(1)


            Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

        } else {

            originalBitmap
        }


    val output =
        ByteArrayOutputStream()


    val success =
        bitmapToUpload.compress(
            Bitmap.CompressFormat.JPEG,
            90,
            output
        )


    if (!success) {

        return null
    }


    return output.toByteArray()
}


// =========================================
// AccountScreen
// =========================================

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

                isLoggedIn = false
            }
        )

    } else {

        LoginAndRegisterScreen(

            onLoginSuccess = {

                isLoggedIn = true
            }
        )
    }
}


// =========================================
// ログイン / 新規登録画面
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
                MaterialTheme.typography.headlineMedium,

            color =
                MaterialTheme.colorScheme.onBackground
        )


        Spacer(
            modifier =
                Modifier.height(8.dp)
        )


        Text(

            text =
                if (registerMode) {

                    "アカウントを作成してToiletMapを利用しましょう"

                } else {

                    "アカウントにログインしてください"
                },

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        Surface(

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(24.dp),

            color =
                MaterialTheme.colorScheme.surface,

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

                            Text(
                                "ユーザー名"
                            )
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

                    shape =
                        RoundedCornerShape(14.dp),

                    enabled =
                        !loading,

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


                        loading = true
                        message = ""


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

                                loading = false
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
                        Modifier.height(6.dp)
                )


                TextButton(

                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick = {

                        registerMode =
                            !registerMode

                        message = ""
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
                    MaterialTheme.colorScheme.error,

                style =
                    MaterialTheme.typography.bodyMedium
            )
        }
    }
}


// =========================================
// プロフィール画面
// =========================================

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

        mutableStateOf<UserProfile?>(
            null
        )
    }


    var avatarDisplayUrl by remember {

        mutableStateOf<String?>(
            null
        )
    }


    /*
     * 写真選択直後に表示する
     * Android端末側のURI
     */

    var localAvatarUri by remember {

        mutableStateOf<Uri?>(
            null
        )
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


    // =========================================
    // プロフィール再取得
    // =========================================

    suspend fun reloadProfile() {

        val loadedProfile =
            AccountRepository
                .loadProfile(
                    userId
                )


        profile =
            loadedProfile


        editingName =
            loadedProfile.username


        avatarDisplayUrl =

            try {

                AccountRepository
                    .getAvatarDisplayUrl(
                        loadedProfile.avatarUrl
                    )

            } catch (e: Exception) {

                Log.e(
                    "AccountPhoto",
                    "Avatar URL creation failed",
                    e
                )


                null
            }
    }


    // =========================================
    // 初回読み込み
    // =========================================

    LaunchedEffect(
        userId
    ) {

        try {

            reloadProfile()


            history =
                AccountRepository
                    .loadHistory(
                        userId
                    )


        } catch (e: Exception) {

            Log.e(
                "AccountProfile",
                "Profile load failed",
                e
            )


            message =
                "プロフィール取得に失敗しました"


        } finally {

            loading =
                false
        }
    }


    // =========================================
    // 写真選択
    // =========================================

    val photoPicker =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts
                    .PickVisualMedia()

        ) { uri: Uri? ->


            if (uri == null) {

                return@rememberLauncherForActivityResult
            }


            /*
             * 選択直後から画面に表示する
             */

            localAvatarUri =
                uri


            scope.launch {

                uploading =
                    true

                message =
                    ""


                try {

                    /*
                     * 選択画像をJPEGへ変換
                     */

                    val jpegBytes =
                        createAvatarJpegBytes(
                            context =
                                context,
                            uri =
                                uri
                        )


                    if (jpegBytes == null) {

                        localAvatarUri =
                            null


                        message =
                            "写真を読み込めませんでした"


                        return@launch
                    }


                    /*
                     * Supabaseへアップロード
                     *
                     * 戻り値は
                     *
                     * UUID/profile.jpg
                     *
                     * というStorageパス
                     */

                    val avatarPath =
                        AccountRepository
                            .uploadAvatar(
                                userId =
                                    userId,
                                imageBytes =
                                    jpegBytes
                            )


                    /*
                     * ローカルプロフィールも更新
                     */

                    profile =
                        profile?.copy(

                            avatarUrl =
                                avatarPath
                        )


                    /*
                     * 新しいSigned URLを生成
                     */

                    val newDisplayUrl =
                        AccountRepository
                            .getAvatarDisplayUrl(
                                avatarPath
                            )


                    avatarDisplayUrl =
                        newDisplayUrl


                    message =
                        "写真を変更しました"


                    Log.d(
                        "AccountPhoto",
                        "Avatar upload successful: $avatarPath"
                    )


                } catch (e: Exception) {

                    Log.e(
                        "AccountPhoto",
                        "Photo upload failed",
                        e
                    )


                    localAvatarUri =
                        null


                    message =
                        "写真の変更に失敗しました"


                } finally {

                    uploading =
                        false
                }
            }
        }


    // =========================================
    // UI
    // =========================================

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            )
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

            color =
                MaterialTheme.colorScheme.onBackground,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp
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


        // =====================================
        // プロフィール写真
        // =====================================

        val avatarModel: Any? =
            localAvatarUri
                ?: avatarDisplayUrl


        Box(

            modifier = Modifier
                .size(124.dp)
                .clip(
                    CircleShape
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .primary
                        .copy(
                            alpha = 0.12f
                        )
                ),

            contentAlignment =
                Alignment.Center

        ) {


            /*
             * AsyncImageが読み込み失敗しても
             * 下にこのアイコンが残る
             */

            Text(

                text =
                    "👤",

                style =
                    MaterialTheme.typography.displayLarge
            )


            if (
                avatarModel != null
            ) {

                AsyncImage(

                    model =
                        avatarModel,

                    contentDescription =
                        "プロフィール画像",

                    modifier =
                        Modifier.fillMaxSize(),

                    contentScale =
                        ContentScale.Crop,

                    onSuccess = {

                        Log.d(
                            "AccountPhoto",
                            "Avatar image load successful"
                        )
                    },

                    onError = {

                        Log.e(
                            "AccountPhoto",
                            "Avatar image load failed: $avatarModel",
                            it.result.throwable
                        )
                    }
                )
            }


            /*
             * アップロード中表示
             */

            if (uploading) {

                Box(

                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme
                                .colorScheme
                                .surface
                                .copy(
                                    alpha = 0.55f
                                )
                        ),

                    contentAlignment =
                        Alignment.Center

                ) {

                    CircularProgressIndicator(

                        modifier =
                            Modifier.size(
                                34.dp
                            )
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        OutlinedButton(

            enabled =
                !uploading,

            shape =
                RoundedCornerShape(
                    14.dp
                ),

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

                text =
                    if (uploading) {

                        "アップロード中..."

                    } else {

                        "写真を変更"
                    }
            )
        }


        Spacer(
            modifier =
                Modifier.height(28.dp)
        )


        // =====================================
        // プロフィール情報カード
        // =====================================

        Surface(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

            shape =
                RoundedCornerShape(
                    22.dp
                ),

            color =
                MaterialTheme.colorScheme.surface,

            tonalElevation =
                2.dp

        ) {

            Column(

                modifier =
                    Modifier.padding(
                        20.dp
                    )
            ) {


                Text(

                    text =
                        "プロフィール",

                    style =
                        MaterialTheme.typography.titleMedium,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )


                // =============================
                // ユーザー名
                // =============================

                if (editing) {

                    OutlinedTextField(

                        value =
                            editingName,

                        onValueChange = {

                            editingName = it
                        },

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
                                10.dp
                            )
                    )


                    Row(

                        modifier =
                            Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {


                        Button(

                            onClick = {

                                if (
                                    editingName
                                        .isBlank()
                                ) {

                                    message =
                                        "ユーザー名を入力してください"

                                    return@Button
                                }


                                scope.launch {

                                    try {

                                        AccountRepository
                                            .updateUserName(
                                                userId =
                                                    userId,

                                                userName =
                                                    editingName
                                                        .trim()
                                            )


                                        profile =
                                            profile?.copy(

                                                username =
                                                    editingName
                                                        .trim()
                                            )


                                        editing =
                                            false


                                        message =
                                            "ユーザー名を変更しました"


                                    } catch (e: Exception) {

                                        Log.e(
                                            "AccountProfile",
                                            "Username update failed",
                                            e
                                        )


                                        message =
                                            "ユーザー名変更に失敗しました"
                                    }
                                }
                            }

                        ) {

                            Text(
                                "保存"
                            )
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

                            Text(
                                "キャンセル"
                            )
                        }
                    }


                } else {

                    Text(

                        text =
                            "ユーザー名",

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )


                    Text(

                        text =
                            profile?.username
                                ?: "",

                        style =
                            MaterialTheme.typography.titleMedium
                    )


                    TextButton(

                        contentPadding =
                            PaddingValues(
                                0.dp
                            ),

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


                HorizontalDivider()


                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )


                Text(

                    text =
                        "メールアドレス",

                    style =
                        MaterialTheme.typography.bodySmall,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )


                Text(

                    text =
                        currentUser.email
                            ?: "",

                    style =
                        MaterialTheme.typography.bodyLarge
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    18.dp
                )
        )


        // =====================================
        // ポイントカード
        // =====================================

        Surface(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

            shape =
                RoundedCornerShape(
                    22.dp
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .primary
                    .copy(
                        alpha = 0.10f
                    )

        ) {

            Row(

                modifier =
                    Modifier.padding(
                        20.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {


                Column {

                    Text(

                        text =
                            "所持ポイント",

                        style =
                            MaterialTheme.typography.bodyMedium,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )


                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )


                    Text(

                        text =
                            "${profile?.points ?: 0} pt",

                        style =
                            MaterialTheme.typography.headlineSmall,

                        color =
                            MaterialTheme.colorScheme.primary,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(
                    28.dp
                )
        )


        // =====================================
        // 履歴
        // =====================================

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                )
        ) {


            Text(

                text =
                    "トイレ編集履歴",

                style =
                    MaterialTheme.typography.titleLarge
            )


            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )


            Text(

                text =
                    "${history.size}件の履歴があります",

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            OutlinedButton(

                modifier =
                    Modifier.fillMaxWidth(),

                shape =
                    RoundedCornerShape(
                        14.dp
                    ),

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

                                Log.e(
                                    "AccountHistory",
                                    "History load failed",
                                    e
                                )


                                message =
                                    "履歴取得に失敗しました"
                            }
                        }
                    }
                }

            ) {

                Text(

                    text =
                        if (showHistory) {

                            "履歴を閉じる"

                        } else {

                            "履歴を見る"
                        }
                )
            }
        }


        if (showHistory) {

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )


            if (history.isEmpty()) {

                Text(

                    text =
                        "まだ履歴はありません",

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )


            } else {

                history.forEach { item ->


                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 5.dp
                            ),

                        shape =
                            RoundedCornerShape(
                                16.dp
                            )
                    ) {

                        Column(

                            modifier =
                                Modifier.padding(
                                    16.dp
                                )
                        ) {


                            Text(

                                text =
                                    item.toiletName,

                                style =
                                    MaterialTheme.typography.titleMedium
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(
                                        4.dp
                                    )
                            )


                            Text(

                                text =
                                    item.action,

                                style =
                                    MaterialTheme.typography.bodyMedium
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(
                                        4.dp
                                    )
                            )


                            Text(

                                text =
                                    item.editedAt
                                        .replace(
                                            "T",
                                            " "
                                        )
                                        .take(
                                            16
                                        ),

                                style =
                                    MaterialTheme.typography.bodySmall,

                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }


        // =====================================
        // メッセージ
        // =====================================

        if (message.isNotBlank()) {

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )


            Text(

                text =
                    message,

                modifier =
                    Modifier.padding(
                        horizontal = 24.dp
                    ),

                style =
                    MaterialTheme.typography.bodyMedium,

                color =
                    if (
                        "失敗" in message ||
                        "できません" in message
                    ) {

                        MaterialTheme.colorScheme.error

                    } else {

                        MaterialTheme.colorScheme.primary
                    }
            )
        }


        Spacer(
            modifier =
                Modifier.height(
                    30.dp
                )
        )


        // =====================================
        // ログアウト
        // =====================================

        OutlinedButton(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),

            shape =
                RoundedCornerShape(
                    14.dp
                ),

            onClick = {

                scope.launch {

                    try {

                        AccountRepository
                            .signOut()


                        onLogout()


                    } catch (e: Exception) {

                        Log.e(
                            "AccountAuth",
                            "Logout failed",
                            e
                        )


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
                Modifier.height(
                    50.dp
                )
        )
    }
}