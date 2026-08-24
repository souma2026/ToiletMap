package com.example.toiletmap.screen.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun PointCard(
    points: Int,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier =
            modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(22.dp),

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
                Modifier.padding(20.dp),

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
                        Modifier.height(4.dp)
                )


                Text(
                    text =
                        "$points pt",

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
}