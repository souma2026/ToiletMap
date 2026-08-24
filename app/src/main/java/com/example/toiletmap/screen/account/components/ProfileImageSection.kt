package com.example.toiletmap.screen.account.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


@Composable
fun ProfileImageSection(
    avatarModel: Any?,
    uploading: Boolean,
    onChangePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier =
            modifier,

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {


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
             * 画像が無い場合や
             * 画像ロード失敗時の初期アイコン
             */
            Text(
                text =
                    "👤",

                style =
                    MaterialTheme.typography.displayLarge
            )


            if (avatarModel != null) {

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
                            "Profile image load successful"
                        )
                    },

                    onError = {

                        Log.e(
                            "AccountPhoto",
                            "Profile image load failed: $avatarModel",
                            it.result.throwable
                        )
                    }
                )
            }


            if (uploading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme
                                .colorScheme
                                .surface
                                .copy(
                                    alpha = 0.65f
                                )
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(34.dp)
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(14.dp)
        )


        OutlinedButton(
            enabled =
                !uploading,

            shape =
                RoundedCornerShape(14.dp),

            onClick =
                onChangePhoto
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
    }
}