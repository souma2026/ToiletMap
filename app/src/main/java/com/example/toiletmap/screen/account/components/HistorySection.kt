package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.toiletmap.model.ToiletEditHistory


@Composable
fun HistorySection(
    history: List<ToiletEditHistory>,
    showHistory: Boolean,
    onToggleHistory: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier =
            modifier
    ) {


        Text(
            text =
                "トイレ編集履歴",

            style =
                MaterialTheme.typography.titleLarge
        )


        Spacer(
            modifier =
                Modifier.height(4.dp)
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
                Modifier.height(12.dp)
        )


        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(14.dp),

            onClick =
                onToggleHistory
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


        if (showHistory) {

            Spacer(
                modifier =
                    Modifier.height(14.dp)
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
                                vertical = 5.dp
                            ),

                        shape =
                            RoundedCornerShape(16.dp)
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


                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
                            )


                            Text(
                                text =
                                    item.action,

                                style =
                                    MaterialTheme.typography.bodyMedium
                            )


                            Spacer(
                                modifier =
                                    Modifier.height(4.dp)
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
                                    MaterialTheme.typography.bodySmall,

                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}