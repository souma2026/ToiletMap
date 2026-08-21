package com.example.toiletmap.screen.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import coil3.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/*
 * ==========================================
 * トイレ編集履歴のデータ
 * ==========================================
 */

data class ToiletEditHistory(
    val toiletName: String,
    val action: String,
    val editedAt: String
)


/*
 * ==========================================
 * アカウント情報を保存するためのクラス
 * ==========================================
 *
 * 現在はSharedPreferencesを使用しています。
 *
 * 将来的には、この部分をFirebaseなどに
 * 置き換えることができます。
 */

object AccountStorage {

    private const val PREFS_NAME = "account_data"

    private const val KEY_USER_NAME = "user_name"
    private const val KEY_POINTS = "points"
    private const val KEY_IMAGE_URI = "image_uri"
    private const val KEY_HISTORY = "toilet_edit_history"


    /*
     * SharedPreferencesを取得
     */
    private fun getPreferences(
        context: Context
    ) = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )


    /*
     * ==========================================
     * ユーザー名
     * ==========================================
     */

    fun loadUserName(context: Context): String {

        return getPreferences(context)
            .getString(
                KEY_USER_NAME,
                "テストユーザー"
            ) ?: "テストユーザー"
    }


    fun saveUserName(
        context: Context,
        userName: String
    ) {

        getPreferences(context)
            .edit()
            .putString(
                KEY_USER_NAME,
                userName
            )
            .apply()
    }


    /*
     * ==========================================
     * ポイント
     * ==========================================
     */

    fun loadPoints(context: Context): Int {

        return getPreferences(context)
            .getInt(
                KEY_POINTS,
                120
            )
    }


    fun savePoints(
        context: Context,
        points: Int
    ) {

        getPreferences(context)
            .edit()
            .putInt(
                KEY_POINTS,
                points
            )
            .apply()
    }


    /*
     * ポイントを加算する
     *
     * 例：
     * AccountStorage.addPoints(context, 10)
     */
    fun addPoints(
        context: Context,
        amount: Int
    ): Int {

        val currentPoints =
            loadPoints(context)

        val newPoints =
            currentPoints + amount

        savePoints(
            context,
            newPoints
        )

        return newPoints
    }


    /*
     * ==========================================
     * プロフィール画像
     * ==========================================
     */

    fun loadImageUri(
        context: Context
    ): String? {

        return getPreferences(context)
            .getString(
                KEY_IMAGE_URI,
                null
            )
    }


    fun saveImageUri(
        context: Context,
        uri: String
    ) {

        getPreferences(context)
            .edit()
            .putString(
                KEY_IMAGE_URI,
                uri
            )
            .apply()
    }


    /*
     * ==========================================
     * トイレ編集履歴
     * ==========================================
     */

    fun loadHistory(
        context: Context
    ): List<ToiletEditHistory> {

        val jsonString =
            getPreferences(context)
                .getString(
                    KEY_HISTORY,
                    "[]"
                ) ?: "[]"

        return try {

            val jsonArray =
                JSONArray(jsonString)

            val historyList =
                mutableListOf<ToiletEditHistory>()

            for (i in 0 until jsonArray.length()) {

                val jsonObject =
                    jsonArray.getJSONObject(i)

                historyList.add(
                    ToiletEditHistory(
                        toiletName =
                            jsonObject.getString("toiletName"),

                        action =
                            jsonObject.getString("action"),

                        editedAt =
                            jsonObject.getString("editedAt")
                    )
                )
            }

            historyList

        } catch (e: Exception) {

            emptyList()
        }
    }


    /*
     * 履歴を保存する
     */
    private fun saveHistory(
        context: Context,
        historyList: List<ToiletEditHistory>
    ) {

        val jsonArray =
            JSONArray()

        historyList.forEach { history ->

            val jsonObject =
                JSONObject()

            jsonObject.put(
                "toiletName",
                history.toiletName
            )

            jsonObject.put(
                "action",
                history.action
            )

            jsonObject.put(
                "editedAt",
                history.editedAt
            )

            jsonArray.put(
                jsonObject
            )
        }

        getPreferences(context)
            .edit()
            .putString(
                KEY_HISTORY,
                jsonArray.toString()
            )
            .apply()
    }


    /*
     * 新しい編集履歴を追加
     */
    fun addHistory(
        context: Context,
        toiletName: String,
        action: String
    ) {

        val historyList =
            loadHistory(context)
                .toMutableList()

        val dateFormat =
            SimpleDateFormat(
                "yyyy/MM/dd HH:mm",
                Locale.JAPAN
            )

        val currentTime =
            dateFormat.format(
                Date()
            )

        val newHistory =
            ToiletEditHistory(
                toiletName = toiletName,
                action = action,
                editedAt = currentTime
            )

        // 一番新しい履歴を上に表示
        historyList.add(
            0,
            newHistory
        )

        saveHistory(
            context,
            historyList
        )
    }
}


/*
 * ==========================================
 * アカウント画面
 * ==========================================
 */

@Composable
fun AccountScreen() {

    val context =
        LocalContext.current


    /*
     * ==========================================
     * ユーザー名
     * ==========================================
     */

    var userName by rememberSaveable {

        mutableStateOf(
            AccountStorage.loadUserName(context)
        )
    }

    var editingUserName by rememberSaveable {

        mutableStateOf(
            userName
        )
    }

    var isEditingUserName by rememberSaveable {

        mutableStateOf(false)
    }


    /*
     * ==========================================
     * ポイント
     * ==========================================
     */

    var points by rememberSaveable {

        mutableIntStateOf(
            AccountStorage.loadPoints(context)
        )
    }


    /*
     * ==========================================
     * プロフィール画像
     * ==========================================
     */

    var selectedImageUriString by rememberSaveable {

        mutableStateOf(
            AccountStorage.loadImageUri(context)
        )
    }

    val selectedImageUri: Uri? =
        selectedImageUriString?.let {
            Uri.parse(it)
        }


    /*
     * ==========================================
     * 編集履歴
     * ==========================================
     */

    val editHistory =
        remember {

            mutableStateListOf<ToiletEditHistory>()
                .apply {

                    addAll(
                        AccountStorage.loadHistory(context)
                    )
                }
        }

    var showHistory by rememberSaveable {

        mutableStateOf(false)
    }


    /*
     * ==========================================
     * Photo Picker
     * ==========================================
     */

    val photoPickerLauncher =
        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.PickVisualMedia()

        ) { uri ->

            if (uri != null) {

                /*
                 * 写真へのアクセス権をできるだけ保持
                 */
                try {

                    context
                        .contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                } catch (_: SecurityException) {

                    // 端末によって永続権限を取得できない場合がある
                }

                selectedImageUriString =
                    uri.toString()

                AccountStorage.saveImageUri(
                    context,
                    uri.toString()
                )
            }
        }


    /*
     * ==========================================
     * 画面
     * ==========================================
     */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        /*
         * タイトル
         */

        Text(
            text = "アカウント",
            style =
                MaterialTheme.typography.headlineMedium
        )


        Spacer(
            modifier =
                Modifier.height(40.dp)
        )


        /*
         * ==========================================
         * プロフィール画像
         * ==========================================
         */

        if (selectedImageUri != null) {

            AsyncImage(
                model =
                    selectedImageUri,

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
                text = "👤",
                style =
                    MaterialTheme.typography.displayLarge
            )
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        Button(
            onClick = {

                photoPickerLauncher.launch(

                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .ImageOnly
                    )
                )
            }
        ) {

            Text(
                text = "写真を選択"
            )
        }


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * ==========================================
         * ユーザー名
         * ==========================================
         */

        if (isEditingUserName) {

            OutlinedTextField(
                value =
                    editingUserName,

                onValueChange = {
                    editingUserName = it
                },

                label = {
                    Text(
                        text = "ユーザー名"
                    )
                },

                singleLine = true
            )


            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )


            Row {

                Button(
                    onClick = {

                        val newName =
                            editingUserName.trim()

                        if (newName.isNotEmpty()) {

                            userName =
                                newName

                            AccountStorage
                                .saveUserName(
                                    context,
                                    newName
                                )

                            isEditingUserName =
                                false
                        }
                    }
                ) {

                    Text(
                        text = "保存"
                    )
                }


                Spacer(
                    modifier =
                        Modifier.size(10.dp)
                )


                TextButton(
                    onClick = {

                        editingUserName =
                            userName

                        isEditingUserName =
                            false
                    }
                ) {

                    Text(
                        text = "キャンセル"
                    )
                }
            }

        } else {

            Text(
                text =
                    "ユーザー名：$userName"
            )


            TextButton(
                onClick = {

                    editingUserName =
                        userName

                    isEditingUserName =
                        true
                }
            ) {

                Text(
                    text = "ユーザー名を変更"
                )
            }
        }


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        /*
         * ==========================================
         * ポイント
         * ==========================================
         */

        Text(
            text =
                "ポイント：$points pt",

            style =
                MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier =
                Modifier.height(30.dp)
        )


        /*
         * ==========================================
         * 編集履歴
         * ==========================================
         */

        Text(
            text =
                "トイレ編集履歴：${editHistory.size}件",

            style =
                MaterialTheme.typography.titleMedium
        )


        Spacer(
            modifier =
                Modifier.height(10.dp)
        )


        Button(
            onClick = {

                /*
                 * 最新の履歴を読み直す
                 */
                editHistory.clear()

                editHistory.addAll(
                    AccountStorage.loadHistory(context)
                )

                showHistory =
                    !showHistory
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


        Spacer(
            modifier =
                Modifier.height(20.dp)
        )


        /*
         * ==========================================
         * 履歴一覧
         * ==========================================
         */

        if (showHistory) {

            if (editHistory.isEmpty()) {

                Text(
                    text =
                        "まだ編集履歴はありません"
                )

            } else {

                editHistory.forEach { history ->

                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                    ) {

                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .sizeIn(
                                        minHeight = 100.dp
                                    )
                                    .padding(16.dp)
                        ) {

                            Text(
                                text =
                                    history.toiletName,

                                style =
                                    MaterialTheme.typography.titleMedium
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    history.action
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )


                            Text(
                                text =
                                    history.editedAt,

                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }


                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(50.dp)
        )
    }
}