package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun ProfileInfoCard(
    userName: String,
    email: String,
    editing: Boolean,
    editingName: String,
    onEditingNameChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier =
            modifier,

        shape =
            RoundedCornerShape(22.dp),

        color =
            MaterialTheme.colorScheme.surface,

        tonalElevation =
            2.dp
    ) {

        Column(
            modifier =
                Modifier.padding(20.dp)
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


            if (editing) {

                OutlinedTextField(
                    value =
                        editingName,

                    onValueChange =
                        onEditingNameChange,

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
                        Modifier.height(10.dp)
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick =
                            onSave
                    ) {

                        Text(
                            "保存"
                        )
                    }


                    TextButton(
                        onClick =
                            onCancel
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
                        Modifier.height(4.dp)
                )


                Text(
                    text =
                        userName,

                    style =
                        MaterialTheme.typography.titleMedium
                )


                TextButton(
                    onClick =
                        onStartEdit
                ) {

                    Text(
                        "ユーザー名を変更"
                    )
                }
            }


            HorizontalDivider()


            Spacer(
                modifier =
                    Modifier.height(16.dp)
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
                    Modifier.height(4.dp)
            )


            Text(
                text =
                    email,

                style =
                    MaterialTheme.typography.bodyLarge
            )
        }
    }
}